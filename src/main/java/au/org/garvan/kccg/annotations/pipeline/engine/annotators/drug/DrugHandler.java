package au.org.garvan.kccg.annotations.pipeline.engine.annotators.drug;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Drug.APDrug;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@AllArgsConstructor
public class DrugHandler {
    private final Logger slf4jLogger = LoggerFactory.getLogger(DrugHandler.class);

    private Map<String, APDrug> drugBankDrugs;
    private Map<String, String> drugLabelToDrugBank;

    private static String STANDARD;
    private static String VERSION;

    /***
     * Init for index reading
     */
    public DrugHandler(){
        drugBankDrugs = new HashMap<>();
        drugLabelToDrugBank = new HashMap<>();
        readDIndexFile("drugBankAnnotationIndex.txt");
    }



    //TODO: Split in two functions, one for calling affinity and other to fetch results.
    public void processAndUpdateDocument(APDocument apDocument, List<AnnotationHit> drugHits) {


        //TODO: Add call and fetch result
        for(AnnotationHit dHit: drugHits){
            APDrug selectedDrug = getDrug(dHit.getAnnotationID());
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
                        annotation.setType(AnnotationType.DRUG);
                        annotation.setEntity(selectedDrug);
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
        List<APSentence> apSentences = apDocument.getSentences().stream().filter(s->s.getAnnotations().stream().filter(a->a.getType().equals(AnnotationType.DRUG)).collect(Collectors.toList()).size()>1).collect(Collectors.toList());
        for (APSentence candidateSentence : apSentences){

            List<Annotation> annotations = candidateSentence.getAnnotations().stream().filter(a->a.getType().equals(AnnotationType.DRUG)).collect(Collectors.toList());
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
        return drugLabelToDrugBank.entrySet().stream().filter(x->x.getKey().toLowerCase().contains(infix.toLowerCase())).map(p-> new APMultiWordAnnotationMapper(p.getValue(),p.getKey())).collect(Collectors.toList());
    }

    public APDrug getDrug(String key) {
        if (drugBankDrugs.containsKey(key))
            return drugBankDrugs.get(key);
        else
            return null;
    }


    public String getDrugBankLabelWithId(String id){
        if(drugBankDrugs.containsKey(id)){
            return drugBankDrugs.get(id).getLabel();
        }
        else return "";
    }

    public String getDrugBankUrlWithId(String id){
        if(drugBankDrugs.containsKey(id)){
            return drugBankDrugs.get(id).getDrugBankUrl();
        }
        else return "";
    }

    public String getDrugBankDefinitionWithId(String id){
        if(drugBankDrugs.containsKey(id)){
            return drugBankDrugs.get(id).getDefinition();
        }
        else return "";
    }


    /***
     * Index is generated and given with artifact
     * @param fileName
     */
    @Deprecated
    private void readFile(String fileName) {

        slf4jLogger.info(String.format("Reading index file. Filename:%s", fileName));
        String path =  "lexicons/"+ fileName;
        InputStream input = getClass().getResourceAsStream("resources/" + path);
        if (input == null) {
            // this is how we load file within editor (eg eclipse)
            input = DrugHandler.class.getClassLoader().getResourceAsStream(path);
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
            if(jsonObject.containsKey("drugs")){
                JSONArray jsonDrugs = (JSONArray) jsonObject.get("drugs");
                for(Object obj: jsonDrugs){
                    JSONObject jsonDrug = (JSONObject) obj;
                    ObjectMapper mapper = new ObjectMapper();
                    APDrug apDrug =  mapper.readValue(jsonDrug.toString(), APDrug.class);
                    apDrug.checkEmptyLists();
                    drugBankDrugs.put(apDrug.getDrugBankID(), apDrug);
                    drugLabelToDrugBank.put(apDrug.getLabel(), apDrug.getDrugBankID());
                    apDrug.getSynonyms().stream().forEach(s-> drugLabelToDrugBank.put(s, apDrug.getDrugBankID()));

                }
            }



            reader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void readDIndexFile(String fileName) {
        slf4jLogger.info(String.format("Reading index file. Filename:%s", fileName));
        String path =  "lexicons/"+ fileName;
        InputStream input = getClass().getResourceAsStream("resources/" + path);
        if (input == null) {
            // this is how we load file within editor (eg eclipse)
            input = DrugHandler.class.getClassLoader().getResourceAsStream(path);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        List<String> drugLines = new ArrayList<>();

        try {
            while ((line = reader.readLine()) != null) {
                if(!Strings.isNullOrEmpty(line))
                    drugLines.add(line);
            }
            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        if(drugLines.size()>3 && drugLines.get(0).contains("DRUG")){

            STANDARD = drugLines.get(1).split("=")[1];
            VERSION = drugLines.get(2).split("=")[1];

            APDrug tempDrug = new APDrug();
            boolean synonym=false;
            for(Integer index= 3; index<drugLines.size(); index++ ){

                String entry = drugLines.get(index);
                switch(entry){
                    case "[ENTITY]":
                        synonym=false;
                        tempDrug = new APDrug();
                        tempDrug.checkEmptyLists();
                        break;
                    case "[ID]":
                        synonym=false;
                        index ++;
                        tempDrug.setDrugBankID(drugLines.get(index));
                        break;
                    case "[LBL]":
                        synonym=false;
                        index ++;
                        tempDrug.setLabel(drugLines.get(index));
                        break;
                    case "[DEF]":
                        index ++;
                        tempDrug.setDefinition(drugLines.get(index));
                        synonym=false;
                        break;
                    case "[URL]":
                        index ++;
                        tempDrug.setDrugBankUrl(drugLines.get(index));
                        synonym=false;
                        break;
                    case "[SYN]":
                        synonym=true;
                        break;
                    case "[~ENTITY]":
                        synonym=false;
                        final String id = tempDrug.getDrugBankID();
                        drugBankDrugs.put(id, tempDrug);
                        drugLabelToDrugBank.put(tempDrug.getLabel(), id);
                        tempDrug.getSynonyms().stream().forEach(s-> drugLabelToDrugBank.put(s, id));
                        break;
                    default:
                        if(synonym)
                            tempDrug.getSynonyms().add(drugLines.get(index));


                }





            }


        }


    }





}
