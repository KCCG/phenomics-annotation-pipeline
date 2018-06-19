package au.org.garvan.kccg.annotations.pipeline.engine.annotators.disease;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.drug.DrugHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease.APDisease;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.APMultiWordAnnotationMapper;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.AnnotationHit;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.AnnotationTerm;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.AnnotationTermComparitor;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Common;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static au.org.garvan.kccg.annotations.pipeline.engine.annotators.Utilities.getAlphaPattern;


@AllArgsConstructor
public class DiseaseHandler {
    private static String STANDARD;
    private static String VERSION;
    private final Logger slf4jLogger = LoggerFactory.getLogger(DiseaseHandler.class);
    private Map<String, APDisease> mondoDiseases;
    private Map<String, String> diseaseLabelToMondo;

    /***
     * Init for index reading
     */
    public DiseaseHandler() {
        mondoDiseases = new HashMap<>();
        diseaseLabelToMondo = new HashMap<>();
        readDIndexFile("mondoAnnotationIndex.txt");

    }

    public static List<List<AnnotationTerm>> chainAnnotationTerms(List<AnnotationTerm> terms) {
        List<List<AnnotationTerm>> chains = new ArrayList<>();
        terms.sort(new AnnotationTermComparitor());
        AnnotationTerm first = terms.get(0);
        List<AnnotationTerm> smallChain = new ArrayList<>();
        smallChain.add(first);
        if (terms.size() > 1) {
            for (int x = 1; x < terms.size(); x++) {
                AnnotationTerm second = terms.get(x);
                if (second.getTokenStartIndex() - 1 == first.getTokenStartIndex()) {
                    smallChain.add(second);
                } else {
                    chains.add(smallChain);
                    smallChain = new ArrayList<>();
                    smallChain.add(second);
                }
                first = second;

                if (x == terms.size() - 1) {
                    chains.add(smallChain);
                }
            }

        } else {
            chains.add(smallChain);
        }
        return chains;
    }

    /***
     * Annotation function called from document preprocessor.
     * @param apDocument
     * @param diseaseHits
     */

    //TODO: Split in two functions, one for calling affinity and other to fetch results.
    public void processAndUpdateDocument(APDocument apDocument, List<AnnotationHit> diseaseHits) {


        //TODO: Add call and fetch result
        for (AnnotationHit dHit : diseaseHits) {
            APDisease selectedDisease = getDisease(dHit.getAnnotationID());
            List<List<AnnotationTerm>> chainedTerms = chainAnnotationTerms(dHit.getHits());
            for (List<AnnotationTerm> term : chainedTerms) {
                Pair<Integer, Integer> offsetRange = getOffsetRange(term);
                Pair<APSentence, List<APToken>> documentMap = apDocument.getSentenceAndTokenWithTokenOffsets(offsetRange.getFirst(), offsetRange.getSecond());

                if (documentMap != null) {
                    boolean validMatch = true;
                    if (documentMap.getSecond().size() == 1) {
                        validMatch = Common.STOPPING_POS.contains(documentMap.getSecond().get(0).getPartOfSpeech()) ? false : true;
                    }

                    if (validMatch) {
                        String abstractCut = apDocument.getCleanedText().substring(offsetRange.getFirst(), offsetRange.getSecond());
                        validMatch = isValidCaseMatch(selectedDisease, abstractCut);
                    }


                    if (validMatch) {
                        Annotation annotation = new Annotation();
                        annotation.setStandard(STANDARD);
                        annotation.setVersion(VERSION);
                        annotation.setType(AnnotationType.DISEASE);
                        annotation.setEntity(selectedDisease);
                        annotation.setTokenIDs(documentMap.getSecond().stream().map(t -> t.getId()).collect(Collectors.toList()));
                        annotation.setTokenOffsets(documentMap.getSecond().stream().map(t -> t.getSentOffset()).collect(Collectors.toList()));
                        annotation.setNegated(false);
                        documentMap.getFirst().getAnnotations().add(annotation);
                    }
                }


            }

        }

        //POINT: Longest Match
        sustainLongestMatch(apDocument);

    }

    private void sustainLongestMatch(APDocument apDocument) {
        List<APSentence> apSentences = apDocument.getSentences().stream().filter(s -> s.getAnnotations().stream().filter(a -> a.getType().equals(AnnotationType.DISEASE)).collect(Collectors.toList()).size() > 1).collect(Collectors.toList());
        for (APSentence candidateSentence : apSentences) {

            List<Annotation> annotations = candidateSentence.getAnnotations().stream().filter(a -> a.getType().equals(AnnotationType.DISEASE)).collect(Collectors.toList());
            List<Integer> removals = new ArrayList<>();
            for (Integer x = 0; x < annotations.size(); x++) {
                for (Integer y = x + 1; y < annotations.size(); y++) {
                    if (annotations.get(x).getTokenIDs().size() != annotations.get(y).getTokenIDs().size()) {
                        Integer largerAnnotationIndex = annotations.get(x).getTokenIDs().size() > annotations.get(y).getTokenIDs().size() ? x : y;
                        Integer smallAnnotationIndex = annotations.get(x).getTokenIDs().size() > annotations.get(y).getTokenIDs().size() ? y : x;

                        if (annotations.get(largerAnnotationIndex).getTokenIDs().containsAll(annotations.get(smallAnnotationIndex).getTokenIDs())) {
                            slf4jLogger.debug(String.format("Dissolving smaller annotation. Document id:%d |  LargeTokens:%s  | SmallTokens:%s ", apDocument.getId(), annotations.get(largerAnnotationIndex).getTokenIDs().toString(), annotations.get(smallAnnotationIndex).getTokenIDs().toString()));
                            candidateSentence.getAnnotations().remove(annotations.get(smallAnnotationIndex));

                        }

                    }

                }

            }


        }


    }

    public Boolean isValidCaseMatch(APDisease disease, String abstractCut) {

        String tokenStringOriginal = getAlphaPattern(abstractCut);
        String tokenStringLowerCase = tokenStringOriginal.toLowerCase();


        List<String> potentialMatches = new ArrayList<>();


        if (getAlphaPattern(disease.getLabel().toLowerCase()).equals(tokenStringLowerCase)) {
            potentialMatches.add(getAlphaPattern(disease.getLabel().toLowerCase()));
        }
        potentialMatches.addAll(
                disease.getSynonyms().stream()
                        .filter(s -> getAlphaPattern(s.toLowerCase()).equals(tokenStringLowerCase))
                        .map(s -> getAlphaPattern(s)).collect(Collectors.toList()));


        for (String pMatch : potentialMatches) {
            if (pMatch.toUpperCase().equals(pMatch)) {
                if (pMatch.equals(tokenStringOriginal))
                    return true;
            } else {
                return true;
            }
        }

        return false;

    }

    public Pair<Integer, Integer> getOffsetRange(List<AnnotationTerm> annotationTerms) {
        Integer begin = 100000;
        Integer end = -1;
        for (AnnotationTerm annotationTerm : annotationTerms) {
            if (annotationTerm.getStartOffset() <= begin)
                begin = annotationTerm.getStartOffset();
            if (annotationTerm.getEndOffset() >= end)
                end = annotationTerm.getEndOffset();
        }
        return new Pair<>(begin, end);
    }

    /***
     * Helping function for autocomplete.
     * @param infix
     * @return
     */
    public List<APMultiWordAnnotationMapper> searchDisease(String infix) {
        return diseaseLabelToMondo.entrySet().stream()
                .filter(x -> x.getKey().toLowerCase().contains(infix.toLowerCase()) && !x.getKey().toLowerCase().contains("("))
                .map(p -> new APMultiWordAnnotationMapper(p.getValue(), p.getKey())).collect(Collectors.toList());
    }

    public APDisease getDisease(String key) {
        if (mondoDiseases.containsKey(key))
            return mondoDiseases.get(key);
        else
            return null;
    }


    public String getMondoLabelWithId(String id) {
        if (mondoDiseases.containsKey(id)) {
            return mondoDiseases.get(id).getLabel();
        } else return "";
    }

    public String getMondoDefinitionWithId(String id) {
        if (mondoDiseases.containsKey(id)) {
            return mondoDiseases.get(id).getDefinition();
        } else return "";
    }


    /***
     * Index is generated and given with artifact
     * @param fileName
     */
    private void readFile(String fileName) {

        slf4jLogger.info(String.format("Reading index file. Filename:%s", fileName));
        String path = "lexicons/" + fileName;
        InputStream input = getClass().getResourceAsStream("resources/" + path);
        if (input == null) {
            // this is how we load file within editor (eg eclipse)
            input = DiseaseHandler.class.getClassLoader().getResourceAsStream(path);
        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONObject jsonObject = (JSONObject) JSONValue.parse(stringBuilder.toString());
            VERSION = jsonObject.get("version").toString();
            STANDARD = jsonObject.get("standard").toString();
            if (jsonObject.containsKey("diseases")) {
                JSONArray jsonDiseases = (JSONArray) jsonObject.get("diseases");

                for (Object obj : jsonDiseases) {
                    JSONObject jsonDisease = (JSONObject) obj;

                    ObjectMapper mapper = new ObjectMapper();
                    APDisease apDisease = mapper.readValue(jsonDisease.toString(), APDisease.class);
                    mondoDiseases.put(apDisease.getMondoID(), apDisease);
                    diseaseLabelToMondo.put(apDisease.getLabel(), apDisease.getMondoID());
                    apDisease.getSynonyms().stream().forEach(s -> diseaseLabelToMondo.put(s, apDisease.getMondoID()));

                }
            }


            reader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readDIndexFile(String fileName) {
        slf4jLogger.info(String.format("Reading index file. Filename:%s", fileName));
        String path = "lexicons/" + fileName;
        InputStream input = getClass().getResourceAsStream("resources/" + path);
        if (input == null) {
            // this is how we load file within editor (eg eclipse)
            input = DrugHandler.class.getClassLoader().getResourceAsStream(path);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        List<String> diseaseLines = new ArrayList<>();

        try {
            while ((line = reader.readLine()) != null) {
                if (!Strings.isNullOrEmpty(line))
                    diseaseLines.add(line);
            }
            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        if (diseaseLines.size() > 3 && diseaseLines.get(0).contains("DISEASE")) {

            STANDARD = diseaseLines.get(1).split("=")[1];
            VERSION = diseaseLines.get(2).split("=")[1];

            APDisease tempDisease = new APDisease();
            boolean synonym = false;
            for (Integer index = 3; index < diseaseLines.size(); index++) {

                String entry = diseaseLines.get(index);
                switch (entry) {
                    case "[ENTITY]":
                        synonym = false;
                        tempDisease = new APDisease();
                        tempDisease.checkEmptyLists();
                        break;
                    case "[ID]":
                        synonym = false;
                        index++;
                        tempDisease.setMondoID(diseaseLines.get(index));
                        break;
                    case "[LBL]":
                        synonym = false;
                        index++;
                        tempDisease.setLabel(diseaseLines.get(index));
                        break;
                    case "[DEF]":
                        index++;
                        tempDisease.setDefinition(diseaseLines.get(index));
                        synonym = false;
                        break;
                    case "[URL]":
                        index++;
                        tempDisease.setOboURI(diseaseLines.get(index));
                        synonym = false;
                        break;
                    case "[SYN]":
                        synonym = true;
                        break;
                    case "[~ENTITY]":
                        synonym = false;
                        final String id = tempDisease.getMondoID();
                        mondoDiseases.put(id, tempDisease);
                        diseaseLabelToMondo.put(tempDisease.getLabel(), id);
                        tempDisease.getSynonyms().stream().forEach(s -> diseaseLabelToMondo.put(s, id));
                        break;
                    default:
                        if (synonym)
                            tempDisease.getSynonyms().add(diseaseLines.get(index));


                }


            }


        }
    }


}
