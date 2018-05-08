package au.org.garvan.kccg.annotations.pipeline.engine.annotators.disease;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.AffinityConnector;
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import edu.stanford.nlp.util.ArrayMap;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.neo4j.cypher.internal.compiler.v2_3.commands.expressions.Collect$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DiseaseHandler{
    private final Logger slf4jLogger = LoggerFactory.getLogger(DiseaseHandler.class);

    @Autowired
    private static AffinityConnector affinityConnector;
    private Map<String, APDisease> mondoDiseases;
    private Map<String, String> diseaseLabelToMondo;

    private static String STANDARD;
    private static String VERSION;

    public DiseaseHandler(){

        //affinityConnector = new AffinityConnector();

    }

    /***
     * Init for index reading
     */
    public void init(){
        mondoDiseases = new HashMap<>();
        diseaseLabelToMondo = new HashMap<>();
        readFile("mondoAnnotationIndex.json");

    }


    /***
     * Annotation function called from document preprocessor.
     * @param apDocument
     * @param articleId
     */

    //TODO: Split in two functions, one for calling affinity and other to fetch results.
    public void processAndUpdateDocument(APDocument apDocument, int articleId) {

        //TODO: Call
        List<AnnotationHit> diseaseHits =   affinityConnector.annotateAbstract(apDocument.getCleanedText(), articleId, "en");


        //TODO: Add call and fetch result
        for(AnnotationHit dHit: diseaseHits){
            APDisease selectedDisease = getDisease(dHit.getAnnotationID());
            List<List<AnnotationTerm>> chainedTerms = chainAnnotationTerms(dHit.getHits());
            for(List<AnnotationTerm> term : chainedTerms){
                Pair<Integer,Integer> offsetRange = getOffsetRange(term);
                Pair<APSentence, List<APToken>> documentMap = apDocument.getSentenceAndTokenWithTokenOffsets(offsetRange.getFirst(), offsetRange.getSecond());

                if(documentMap != null){
                    boolean validToken = true;
                    if(documentMap.getSecond().size()==1 ){
                        validToken= Common.STOPPING_POS.contains(documentMap.getSecond().get(0).getPartOfSpeech()) ? false : true;
                    }

                    if(validToken) {
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

    private void sustainLongestMatch(APDocument apDocument){
        List<APSentence> apSentences = apDocument.getSentences().stream().filter(s->s.getAnnotations().stream().filter(a->a.getType().equals(AnnotationType.DISEASE)).collect(Collectors.toList()).size()>1).collect(Collectors.toList());
        for (APSentence candidateSentence : apSentences){

            List<Annotation> annotations = candidateSentence.getAnnotations().stream().filter(a->a.getType().equals(AnnotationType.DISEASE)).collect(Collectors.toList());
            List<Integer> removals = new ArrayList<>();
            for(Integer x = 0; x< annotations.size(); x++) {
                for(Integer y = x+1; y<annotations.size(); y++)
                {
                    if(annotations.get(x).getTokenIDs().size() != annotations.get(y).getTokenIDs().size()) {
                        Integer largerAnnotationIndex = annotations.get(x).getTokenIDs().size() > annotations.get(y).getTokenIDs().size()? x :y;
                        Integer smallAnnotationIndex =  annotations.get(x).getTokenIDs().size() > annotations.get(y).getTokenIDs().size()? y :x;

                        if(annotations.get(largerAnnotationIndex).getTokenIDs().containsAll(annotations.get(smallAnnotationIndex).getTokenIDs())){
                            slf4jLogger.debug(String.format("Dissolving smaller annotation. Document id:%d |  LargeTokens:%s  | SmallTokens:%s ", apDocument.getId(), annotations.get(largerAnnotationIndex).getTokenIDs().toString(), annotations.get(smallAnnotationIndex).getTokenIDs().toString()));
                            candidateSentence.getAnnotations().remove(annotations.get(smallAnnotationIndex));

                        }

                    }

                }

            }


        }


    }

    public Pair<Integer, Integer> getOffsetRange(List<AnnotationTerm> annotationTerms){
        Integer begin = 100000;
        Integer end = -1;
        for(AnnotationTerm annotationTerm: annotationTerms){
            if(annotationTerm.getStartOffset()<=begin)
                begin= annotationTerm.getStartOffset();
            if(annotationTerm.getEndOffset()>=end)
                end = annotationTerm.getEndOffset();
        }
        return new Pair<>(begin, end);
    }

    public static List<List<AnnotationTerm>> chainAnnotationTerms(List<AnnotationTerm> terms){
        List<List<AnnotationTerm>> chains = new ArrayList<>();
        terms.sort(new AnnotationTermComparitor());
        AnnotationTerm first = terms.get(0);
        List<AnnotationTerm> smallChain = new ArrayList<>();
        smallChain.add(first);
        if(terms.size()>1)
        {
            for(int x = 1; x<terms.size(); x++){
                AnnotationTerm second = terms.get(x);
                if(second.getTokenStartIndex()-1 == first.getTokenStartIndex()){
                    smallChain.add(second);
                }
                else
                {
                    chains.add(smallChain);
                    smallChain = new ArrayList<>();
                    smallChain.add(second);
                }
                first = second;

                if(x==terms.size()-1){
                    chains.add(smallChain);
                }
            }

        }
        else
        {
            chains.add(smallChain);
        }
        return chains;
    }


    /***
     * Helping function for autocomplete.
     * @param infix
     * @return
     */
    public List<APMultiWordAnnotationMapper> searchDisease(String infix){
        return diseaseLabelToMondo.entrySet().stream().filter(x->x.getKey().contains(infix.toLowerCase())).map(p-> new APMultiWordAnnotationMapper(p.getValue(),p.getKey())).collect(Collectors.toList());
    }

    public APDisease getDisease(String key) {
        if (mondoDiseases.containsKey(key))
            return mondoDiseases.get(key);
        else
            return null;
    }


    /***
     * Index is generated and given with artifact
     * @param fileName
     */
    private void readFile(String fileName) {

        slf4jLogger.info(String.format("Reading index file. Filename:%s", fileName));
        String path =  "lexicons/"+ fileName;
        InputStream input = getClass().getResourceAsStream("resources/" + path);
        if (input == null) {
            // this is how we load file within editor (eg eclipse)
            input = DiseaseHandler.class.getClassLoader().getResourceAsStream(path);
        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONObject jsonObject = (JSONObject) JSONValue.parse(stringBuilder.toString());
            VERSION = jsonObject.get("version").toString();
            STANDARD = jsonObject.get("standard").toString();
            if(jsonObject.containsKey("diseases")){
                JSONArray jsonDiseases = (JSONArray) jsonObject.get("diseases");

                for(Object obj: jsonDiseases){
                    JSONObject jsonDisease = (JSONObject) obj;

                    ObjectMapper mapper = new ObjectMapper();
                    APDisease apDisease =  mapper.readValue(jsonDisease.toString(), APDisease.class);
                    mondoDiseases.put(apDisease.getMondoID(), apDisease);
                    diseaseLabelToMondo.put(apDisease.getLabel(), apDisease.getMondoID());
                    apDisease.getSynonyms().stream().forEach(s-> diseaseLabelToMondo.put(s, apDisease.getMondoID()));

                }
            }



            reader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
