package au.org.garvan.kccg.annotations.pipeline.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.preprocessors.LongFormMarker;
import au.org.garvan.kccg.annotations.pipeline.preprocessors.ShortFormExtractor;
import au.org.garvan.kccg.annotations.pipeline.processors.CoreNLPHanlder;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 6/7/17.
 */
public class APDocument extends LinguisticEntity {


    @Getter
    @Setter
    private List<APSentence> sentences = new ArrayList<>();


    @Getter
    @Setter
    private String cleanedText;


    public APDocument(int id, String text) {
        super(id, text);
    }


    public APDocument(String text) {
        super(text);
    }


    public List<APToken> getTokens() {
        List<APToken> tmpList = this.getSentences().stream().flatMap(y -> y.getTokens().stream()).collect(Collectors.toList());
        tmpList.sort(Comparator.comparing(a -> a.beginPosition()));
        return tmpList;


    }

    //This function is created to help graph db absorb all entities from an APdoc
    public Map<APSentence, APToken> getTokensWithEntities() {
        Map<APSentence, APToken> returnData = new HashMap<>();
        for (APSentence sent : this.getSentences()) {
            List<APToken> selectTokens = sent.getTokens().stream().filter(t -> t.getLexicalEntityList().size() > 0).collect(Collectors.toList());
            if (selectTokens.size() > 0) {
                selectTokens.stream().forEach(t -> returnData.put(sent, t));
            }

        }
        return returnData;
    }


    public void hatch() {
        DocumentPreprocessor.preprocessDocument(this);
    }


    public APSentence getSentenceWithID(int id) {
        List<APSentence> lstSent = sentences.stream().filter(s -> s.getId() == id).collect(Collectors.toList());
        if (lstSent.size() > 0) {
            return lstSent.get(0);
        } else {
            return null;
        }
    }
}

