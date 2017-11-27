package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.DynamoDBHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.GraphDBHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.S3Handler;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ahmed on 22/11/17.
 */
public class DatabaseManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(DatabaseManager.class);

    private DynamoDBHandler dynamoDBHandler;
    private GraphDBHandler graphDBHandler;
    private S3Handler s3Handler;

    public void init(){

        dynamoDBHandler = new DynamoDBHandler();
        graphDBHandler = new GraphDBHandler();
        s3Handler = new S3Handler();

    }

    public boolean persistArticle(Article article){

        try{
            slf4jLogger.info(String.format("Persistence initialized. Article ID: %d", article.getPubMedID()));

            dynamoDBHandler.insertItem(article.constructJson(), article.getAbstractEntities());
            slf4jLogger.info(String.format("Persistence locked - Dynomodb. Article ID: %d", article.getPubMedID()));

            s3Handler.storeAbstract(article);
            slf4jLogger.info(String.format("Persistence locked - S3. Article ID: %d", article.getPubMedID()));

            graphDBHandler.createArticleQuery(article);
            slf4jLogger.info(String.format("Persistence locked - GraphDB. Article ID: %d", article.getPubMedID()));

            slf4jLogger.info(String.format("Persistence finalized. Article ID: %d", article.getPubMedID()));

            return true;
        }
        catch (Exception e){
            slf4jLogger.error(String.format("Error in persisting article with ID:%d", article.getPubMedID()));
            return false;
        }

    }

    public DatabaseManager(){
        init();
    }



}
