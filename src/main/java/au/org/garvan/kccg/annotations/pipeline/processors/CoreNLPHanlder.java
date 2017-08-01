package au.org.garvan.kccg.annotations.pipeline.processors;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.Properties;

/**
 * Created by ahmed on 12/7/17.
 */
public final class CoreNLPHanlder {

    private static StanfordCoreNLP pipeline;
    private CoreNLPHanlder(){}


    public static void init()
    {
        Properties props = PropertiesUtils.asProperties(
                "annotators", "tokenize,ssplit,pos,lemma,parse,natlog",
                "tokenize.language", "en");

//        props.setProperty("annotators","tokenize, ssplit, pos, lemma, parse");
        pipeline = new StanfordCoreNLP(props);
    }

    public static Annotation annotateText(String text)
    {
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        return annotation;

    }

}
