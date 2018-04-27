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
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class DiseaseHandler{
    private final Logger slf4jLogger = LoggerFactory.getLogger(DiseaseHandler.class);


    private static AffinityConnector affinityConnector;
    private Map<String, APDisease> mondoDiseases;
    private Map<String, String> diseaseLabelToMondo;

    private static final String STANDARD = "MONDO";
    private static final String VERSION = "2018";

    public DiseaseHandler(){

        affinityConnector = new AffinityConnector();

    }


    public void readFile(String fName) {}

    public void processAndUpdateDocument(APDocument apDocument, int articleId) {

        List<AnnotationHit> diseaseHits =   affinityConnector.annotateAbstract(apDocument.getCleanedText(), articleId, "en");

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


    public List<APMultiWordAnnotationMapper> searchDisease(String infix){
        return diseaseLabelToMondo.entrySet().stream().filter(x->x.getKey().contains(infix.toLowerCase())).map(p-> new APMultiWordAnnotationMapper(p.getValue(),p.getKey())).collect(Collectors.toList());
    }

    public APDisease getDisease(String key) {
        if (mondoDiseases.containsKey(key))
            return mondoDiseases.get(key);
        else
            return null;
    }




}
