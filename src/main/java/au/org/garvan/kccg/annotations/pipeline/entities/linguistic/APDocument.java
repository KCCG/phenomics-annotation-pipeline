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

import java.util.ArrayList;
import java.util.Arrays;
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

    @Property
    @Getter
    @Setter
    private String cleanedText;



    public APDocument(int id, String text) {
        super(id, text);
    }


    public List<APToken> getTokens() {
        List<APToken> tmpList = this.getSentences().stream().flatMap(y -> y.getTokens().stream()).collect(Collectors.toList());
        tmpList.sort(Comparator.comparing(a -> a.beginPosition()));
        return tmpList;


    }


    public void hatch() {
        DocumentPreprocessor.preprocessDocument(this);

        //Using cleaned text after prepossessing is done
        Annotation docAnnotation = CoreNLPHanlder.annotateDocText(cleanedText);

        List<CoreMap> sentencesMap = docAnnotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentencesMap) {
            APSentence sent = new APSentence(sentence.toString());
            sent.setDocOffset(new Point(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)));
            sentences.add(sent);
        }

        for (APSentence sent : sentences)
        {
            List<Character> punctuations = Arrays.asList(',','.',':',';','\'');
            Annotation sentAnnotation = CoreNLPHanlder.annotateSentText(sent.getOriginalText());
            List<CoreMap> localSentenceMap = sentAnnotation.get(CoreAnnotations.SentencesAnnotation.class);

            if (localSentenceMap.size()>1)
                System.out.println(String.format("More than one sentence splits. Sent entityId:%d",sent.getId()));
            CoreMap aSent = localSentenceMap.get(0);

            sent.setAnnotatedTree(aSent.get(TreeCoreAnnotations.TreeAnnotation.class));
            sent.setSemanticGraph(aSent.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class));

            int id = 1;
            for (CoreLabel token : aSent.get(CoreAnnotations.TokensAnnotation.class)) {
                String text = token.originalText();
                APToken tok = new APToken(id, text, token.get(CoreAnnotations.PartOfSpeechAnnotation.class).toString(), token.lemma());
                tok.setSentOffset(new Point(token.beginPosition(), token.endPosition()));
                tok.setPunctuation(punctuations.contains(text.charAt(text.length()-1)));
                sent.getTokens().add(tok);
                id++;

            }
            ShortFormExtractor.markShortForms(sent.getTokens());
            LongFormMarker.markLongForms(sent);

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

