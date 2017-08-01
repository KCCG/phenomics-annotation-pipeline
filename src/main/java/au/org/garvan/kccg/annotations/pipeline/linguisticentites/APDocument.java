package au.org.garvan.kccg.annotations.pipeline.linguisticentites;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 6/7/17.
 */
public class APDocument extends LinguisticEntity {

    @Property
    @Getter
    @Setter
    private List<APSentence> sentences = new ArrayList<>();


    public APDocument(int id, String text) {
        super(id, text);
    }


    public List<APToken> getTokens() {
        List<APToken> tmpList = this.getSentences().stream().flatMap(y -> y.getTokens().stream()).collect(Collectors.toList());
        tmpList.sort(Comparator.comparing(a -> a.beginPosition()));
        return tmpList;


    }


    public void hatch() {

        Annotation annotation = CoreNLPHanlder.annotateText(this.getOriginalText());
        List<CoreMap> sentencesMap = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentencesMap) {
            APSentence sent = new APSentence(sentence.toString());
            sent.setDocOffset(new Point(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)));

            sent.setAnnotatedTree(sentence.get(TreeCoreAnnotations.TreeAnnotation.class));
            sent.setSemanticGraph(sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class));

            int id = 1;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                APToken tok = new APToken(id, token.originalText(), token.get(CoreAnnotations.PartOfSpeechAnnotation.class).toString(), token.lemma());
                tok.setSentOffset(new Point(token.beginPosition(), token.endPosition()));
                tok.setOriginalText(token.originalText());
                sent.getTokens().add(tok);
                id++;

            }

            this.sentences.add(sent);
        }
    }

    public APSentence getSentenceWithID(int id) {
        List<APSentence> lstSent= sentences.stream().filter(s->s.getId()==id).collect(Collectors.toList());
        if (lstSent.size()>0)
        {
            return lstSent.get(0);
        }
        else
        {
            return null;
        }
    }
}

