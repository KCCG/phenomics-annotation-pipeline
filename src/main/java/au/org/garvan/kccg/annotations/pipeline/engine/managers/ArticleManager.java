package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.lambda.WorkerLambdaConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.profiles.ProcessingProfile;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Common;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.EngineEnvironment;
import au.org.garvan.kccg.annotations.pipeline.model.annotation.RawArticle;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by ahmed on 22/11/17.
 */

@Service
public class ArticleManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(ArticleManager.class);

    @Autowired
    DatabaseManager dbManager;


    public void init(){

        slf4jLogger.info(String.format("Article Manager init() called."));
        DocumentPreprocessor.init();


    }

    @Async
    public void processArticles(List<RawArticle> articleList, String batchId)
    {
        slf4jLogger.info(String.format("Received articles batch for processing. Batch Id:%s Batch Size: %d", batchId,  articleList.size()));
        for (RawArticle input: articleList){

            Article article = constructArticle(input);
            try {
                slf4jLogger.info(String.format("Processing started for article ID: %d", article.getPubMedID()));

//                if (true)
                if (!isDuplicate(article))
                {
                    slf4jLogger.info(String.format("Article is identified as unique. ID: %d", article.getPubMedID()));
                    article.getArticleAbstract().hatch(article.getPubMedID());

                    slf4jLogger.info(String.format("Article processed successfully, ID: %d", article.getPubMedID()));
                    dbManager.persistArticle(article);

                } else {
                    slf4jLogger.info(String.format("Article is identified as duplicate. Processing is aborted. ID: %d", article.getPubMedID()));
                }
            }
            catch (Exception e){
                slf4jLogger.error(String.format("Error in processing article with ID: %d. Exception:%s", article.getPubMedID(),e.getMessage()));
            }


        }//Article Loop
        slf4jLogger.info(String.format("Finished articles batch for processing. Batch Id:%s ", batchId));

        if(EngineEnvironment.getSelfIngestionEnabled()){
            slf4jLogger.info(String.format("Self Ingestion is Enabled. Invoking Lambda. "));
            WorkerLambdaConnector.invokeWorkerLambda(EngineEnvironment.getWorkerID(), false);
        }
    }

    private boolean isDuplicate(Article article){
       JSONObject jsonArticle =  dbManager.fetchArticle(Integer.toString(article.getPubMedID()));
       if (jsonArticle.isEmpty())
           return false;
       else
           return true;

    }

    private Article constructArticle(RawArticle rawArticle){
        Article article = new Article(rawArticle.getPubMedID(),
                LocalDate.now().toEpochDay(),
                LocalDate.parse(rawArticle.getDatePublished()),
                LocalDate.parse(rawArticle.getDateCreated()),
                LocalDate.parse(rawArticle.getDateRevised()),
                rawArticle.getArticleTitle(),
                new APDocument(rawArticle.getArticleAbstract()),
                rawArticle.getLanguage(),
                rawArticle.getAuthors(),
                rawArticle.getMeshHeadingList(),
                rawArticle.getPublication());

        article.getArticleAbstract().setProcessingProfile(Common.getStandardProfile());
        return article;
    }
}
