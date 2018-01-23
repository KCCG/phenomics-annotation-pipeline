package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.DynamoDBHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.GraphDBHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.S3Handler;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchResult;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 22/11/17.
 */
@NoArgsConstructor
@AllArgsConstructor
@Component
public class DatabaseManager {

    private final Logger slf4jLogger = LoggerFactory.getLogger(DatabaseManager.class);

    @Autowired
    private DynamoDBHandler dynamoDBHandler;

    @Autowired
    private GraphDBHandler graphDBHandler;

    @Autowired
    private S3Handler s3Handler;


    public boolean persistArticle(Article article) {

        try {
            slf4jLogger.info(String.format("Persistence initialized. Article ID: %d", article.getPubMedID()));

            dynamoDBHandler.insertItem(article.constructJson(), article.getAbstractEntities());
            slf4jLogger.info(String.format("Persistence locked - Dynomodb. Article ID: %d", article.getPubMedID()));

            s3Handler.storeAbstract(article);
            slf4jLogger.info(String.format("Persistence locked - S3. Article ID: %d", article.getPubMedID()));

            graphDBHandler.createArticleQuery(article);
            slf4jLogger.info(String.format("Persistence locked - GraphDB. Article ID: %d", article.getPubMedID()));

            slf4jLogger.info(String.format("Persistence finalized. Article ID: %d", article.getPubMedID()));

            return true;
        } catch (Exception e) {
            slf4jLogger.error(String.format("Error in persisting article with ID:%d", article.getPubMedID()));
            return false;
        }

    }

    public DBManagerResultSet searchArticles(Map<SearchQueryParams, Object> params, PaginationRequestParams qParams) {
        DBManagerResultSet resultSet = graphDBHandler.fetchArticles(params, qParams);

        List<RankedArticle> rankedArticles = resultSet.getRankedArticles();

        for (RankedArticle anArticle : rankedArticles) {
            JSONObject jsonArticle = fetchArticle(anArticle.getPMID());
            if (!jsonArticle.isEmpty()) {
                anArticle.setArticle(new Article(new DynamoDBObject(jsonArticle, EntityType.Article), false));
                anArticle.setJsonAnnotations(dynamoDBHandler.getAnnotations(Integer.parseInt(anArticle.getPMID()), AnnotationType.GENE));

            }
        }
        resultSet.setRankedArticles(rankedArticles);
        return resultSet;

    }

    public JSONObject fetchArticle(String id) {
        return dynamoDBHandler.getArticle(Integer.parseInt(id));
    }


//    Subscription Methods
//    *****************************

    public boolean persistSubscription(Map<String, Object> subscriptionRequest) {

        JSONObject jsonSubscriptionRequest = new JSONObject();
        for (Map.Entry<String, Object> entry : subscriptionRequest.entrySet()) {
            jsonSubscriptionRequest.put(entry.getKey(), entry.getValue());
        }
        return dynamoDBHandler.insertSubscription(jsonSubscriptionRequest);
    }

    public boolean checkIfSubscriptionExists(String qId) {

        return dynamoDBHandler.checkSubscription(qId);
    }

    public JSONObject getSubscription(String qId) {
        return dynamoDBHandler.getSubscription(qId);
    }

    public JSONArray getSubscriptions() {
        return dynamoDBHandler.getSubscriptions();
    }

    public boolean deleteSubscription(String qId) {
        return dynamoDBHandler.deleteSubscription(qId);
    }

    public boolean updateSubscriptionTime(String qId, Long runDate, String timeStamp) {
        return dynamoDBHandler.updateSubscriptionTime(qId, runDate, timeStamp);
    }


}
