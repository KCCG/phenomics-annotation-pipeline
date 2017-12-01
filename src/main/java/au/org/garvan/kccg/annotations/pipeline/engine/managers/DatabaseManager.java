package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.DynamoDBHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.GraphDBHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.S3Handler;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.model.SearchQuery;
import au.org.garvan.kccg.annotations.pipeline.model.SearchResult;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ahmed on 22/11/17.
 */

@Component
public class DatabaseManager {

    private final Logger slf4jLogger = LoggerFactory.getLogger(DatabaseManager.class);

    @Autowired
    private DynamoDBHandler dynamoDBHandler;

    @Autowired
    private GraphDBHandler graphDBHandler;

    @Autowired
    private S3Handler s3Handler;


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

    public Map<Article, JSONObject> searchArticles(Map<SearchQueryParams, Object> params){

        Map<Article, JSONObject> searchedArticles = new HashMap<>();
        Set<String> articleIDs =  graphDBHandler.fetchArticles(params);

        for (String id: articleIDs)
        {
            JSONObject jsonAnnotations = null;
            Article article;
            JSONObject jsonArticle =  fetchArticle(id);
            if(!jsonArticle.isEmpty())
            {
                article = new Article(new DynamoDBObject(jsonArticle, EntityType.Article), false);
                jsonAnnotations = dynamoDBHandler.getAnnotations(Integer.parseInt(id), AnnotationType.GENE);
                searchedArticles.put(article,jsonAnnotations);

            }

        }

        return  searchedArticles;
    }

    public JSONObject fetchArticle(String id){
        return dynamoDBHandler.getArticle(Integer.parseInt(id));
    }




}
