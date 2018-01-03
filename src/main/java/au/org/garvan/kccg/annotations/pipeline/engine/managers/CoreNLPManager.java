package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.PropertiesUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by ahmed on 12/7/17.
 */
public final class CoreNLPManager {
    private static final Logger slf4jLogger = LoggerFactory.getLogger(CoreNLPManager.class);

    private static boolean lock = false;
    private static StanfordCoreNLP documentPipeline;
    private static StanfordCoreNLP sentencePipeline;


    @Setter
    @Getter
    private static boolean isInitialized = false;


    private CoreNLPManager(){}


    public static void init()
    {
        slf4jLogger.info(String.format("Initializing CoreNLP pipelines."));
        Properties docProps = PropertiesUtils.asProperties("annotators", "tokenize, ssplit");
        documentPipeline = new StanfordCoreNLP(docProps);


        Properties sentProps = PropertiesUtils.asProperties(
                "annotators", "tokenize,ssplit,pos,lemma,parse,natlog",
                "tokenize.language", "en");
        sentProps.setProperty("tokenize.whitespace", "true");
        sentencePipeline = new StanfordCoreNLP(sentProps);
        isInitialized = true;
        slf4jLogger.info(String.format("CoreNLP pipelines initialized successfully."));

    }
    public static void clearMemory(){
        // Just to make sure we are not polluting pipelines.
        runMonitor();
        // This will lock the CoreNLP manager
        lock= true;
        documentPipeline.clearAnnotatorPool();
        sentencePipeline.clearAnnotatorPool();
        Runtime.getRuntime().gc();
        isInitialized = false;
        init();
        lock=false;

    }



    public static Annotation annotateDocText(String text){
        runMonitor();
        Annotation annotation = new Annotation(text);
        documentPipeline.annotate(annotation);
        return annotation;
    }
    public static Annotation annotateSentText(String text)
    {
        runMonitor();
        Annotation annotation = new Annotation(text);
        sentencePipeline.annotate(annotation);
        return annotation;

    }


    private static void runMonitor(){
        while(lock) {
            try {
                slf4jLogger.info(String.format("Waiting for pipeline init....Tic...Toc..."));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }



}
