package au.org.garvan.kccg.annotations.pipeline.processors;

import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APSentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.PropertiesUtils;
import java.util.Properties;

/**
 * Created by ahmed on 12/7/17.
 */
public final class CoreNLPHanlder {

    private static StanfordCoreNLP documentPipeline;
    private static StanfordCoreNLP sentencePipeline;


    private CoreNLPHanlder(){}


    public static void init()
    {

        Properties docProps = PropertiesUtils.asProperties("annotators", "tokenize, ssplit");
        documentPipeline = new StanfordCoreNLP(docProps);

        Properties sentProps = PropertiesUtils.asProperties(
                "annotators", "tokenize,ssplit,pos,lemma,parse,natlog",
                "tokenize.language", "en");
        sentProps.setProperty("tokenize.whitespace", "true");
        sentencePipeline = new StanfordCoreNLP(sentProps);

    }

    public static Annotation annotateDocText(String text){
        Annotation annotation = new Annotation(text);
        documentPipeline.annotate(annotation);
        return annotation;
    }
    public static Annotation annotateSentText(String text)
    {
        Annotation annotation = new Annotation(text);
        sentencePipeline.annotate(annotation);
        return annotation;

    }



}