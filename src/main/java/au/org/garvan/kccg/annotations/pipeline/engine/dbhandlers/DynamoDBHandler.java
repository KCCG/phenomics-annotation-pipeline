package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.L2CacheObject;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.util.concurrent.RateLimiter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ahmed on 7/11/17.
 */
@Component
public class DynamoDBHandler {

    private final Logger slf4jLogger = LoggerFactory.getLogger(DynamoDBHandler.class);

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion("ap-southeast-2").build();
    DynamoDB dynamoDB = new DynamoDB(client);
    String articleTableName;
    String annotationTableName;
    String subscriptionTableName;
    String L2CacheTableName;


    @Autowired
    public DynamoDBHandler(@Value("${spring.dbhandlers.dynamodb.articletablename}") String configArticleTableName,
                           @Value("${spring.dbhandlers.dynamodb.annotationtablename}") String configAnnotationTableName,
                           @Value("${spring.dbhandlers.dynamodb.subscriptiontablename}") String configSubscriptionTableName,
                           @Value("${spring.dbhandlers.dynamodb.l2cachetablename}") String configL2CacheTableName) {
        articleTableName = configArticleTableName;
        annotationTableName = configAnnotationTableName;
        subscriptionTableName = configSubscriptionTableName;
        L2CacheTableName  = configL2CacheTableName;

        slf4jLogger.info(String.format("DynamoDBHandler wired with Articles Table: %s | Annotations Table:%s | Subscriptions Table:%s | L2Cache Table: %s", articleTableName, annotationTableName,subscriptionTableName, L2CacheTableName));

    }

    public void insertItem(JSONObject jsonArticle, JSONArray jsonAnnotations) {

        Table articleTable = dynamoDB.getTable(articleTableName);
        Item article = Item.fromJSON(jsonArticle.toString());
        articleTable.putItem(article);

        for(Object obj: jsonAnnotations){
            JSONObject jsonObject = (JSONObject) obj;
            Table annotationTable = dynamoDB.getTable(annotationTableName);
            Item annotations = Item.fromJSON(jsonObject.toString());
            annotationTable.putItem(annotations);
        }


    }

    public JSONObject getArticle(int pubMedId) {
        Table table = dynamoDB.getTable(articleTableName);
        Item item = table.getItem("pubMedID", Integer.toString(pubMedId));
        if (item == null) {
            slf4jLogger.info(String.format("Article with ID:%d was missing in db.", pubMedId));
            return new JSONObject();
        } else {
            slf4jLogger.debug(String.format("Article with ID:%d was found in db.", pubMedId));
            return (JSONObject) JSONValue.parse(item.toJSON());
        }

    }

    public JSONObject getAnnotations(int pubMedId, AnnotationType aType) {
        Table table = dynamoDB.getTable(annotationTableName);
        Item item = table.getItem("pubMedID", Integer.toString(pubMedId), "annotationType", aType.toString());
        if (item == null)
            return new JSONObject();
        else
            return (JSONObject) JSONValue.parse(item.toJSON());

    }

    /***
     * This should be an idempotent method, and insert or updated the subscription request.
     * @param jsonSubscription
     * @return
     */
    public boolean insertSubscription(JSONObject jsonSubscription) {
        Table table = dynamoDB.getTable(subscriptionTableName);
        Item subscription = Item.fromJSON(jsonSubscription.toString());
        PutItemOutcome outcome = table.putItem(subscription);
        //TODO: Update result based on information
        return true;
    }

    public JSONArray getSubscriptions(){
        JSONArray results = new JSONArray();
        RateLimiter limiter = RateLimiter.create(1.0);
        Map<String, AttributeValue> lastKeyEvaluated = null;
        do {
            limiter.acquire();
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(subscriptionTableName)
                    .withLimit(50)
                    .withExclusiveStartKey(lastKeyEvaluated)
                    .withAttributesToGet(Arrays.asList("subscriptionId"));

            ScanResult result = client.scan(scanRequest);

            for (Map<String, AttributeValue> item : result.getItems()){
                results.add(collectScanResult(item));
            }
            lastKeyEvaluated = result.getLastEvaluatedKey();
        } while (lastKeyEvaluated != null);
        return results;
    }


    public JSONObject getSubscription(String id) {
        Table table = dynamoDB.getTable(subscriptionTableName);
        Item item = table.getItem("subscriptionId", id);

        if (item == null)
            return new JSONObject();
        else
            return (JSONObject) JSONValue.parse(item.toJSON());

    }

    public boolean updateSubscriptionTime(String id, long rundate,  String time) {

        Map<String,AttributeValue> key = new HashMap<>();
        key.put("subscriptionId",new AttributeValue().withS(id));

        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                .withTableName(subscriptionTableName)
                .withKey(key)
                .addAttributeUpdatesEntry("nextRunTime",
                        new AttributeValueUpdate().withValue(new AttributeValue().withS(time)))
                .addAttributeUpdatesEntry("lastRunDate",
                        new AttributeValueUpdate().withValue(new AttributeValue().withN(String.valueOf(rundate))));

        UpdateItemResult updateItemResult = client.updateItem(updateItemRequest);
        return true;

    }

    // TODO: If used by more than one functions then make it generic
    public boolean checkSubscription(String id) {
        Table table = dynamoDB.getTable(subscriptionTableName);
        Item item = table.getItem("subscriptionId", id);
        if (item == null)
            return false;
        else
            return true;

    }

    public boolean deleteSubscription(String id) {
        Table table = dynamoDB.getTable(subscriptionTableName);
        DeleteItemOutcome deleteItemOutcome = table.deleteItem("subscriptionId", id);
        deleteItemOutcome.getDeleteItemResult().getAttributes();
        return true;
    }

    private JSONObject collectScanResult(Map<String, AttributeValue> item)
    {
        String subscriptionKey = item.get("subscriptionId").getS();
        return getSubscription(subscriptionKey);
    }





    //////////////////////////////////////////////////////    Cache DB Functions       ///////////////////////////////////////////////////////////////////////////////////

    public L2CacheObject getCachedMetaData(String key){
        Table table = dynamoDB.getTable(L2CacheTableName);
        Item item = table.getItem("cacheKey", key);
        if (item == null) {
            slf4jLogger.info(String.format("L2 Cache miss with key:%s", key));
            return null;
        } else {

            L2CacheObject l2CacheObject = new L2CacheObject();
            l2CacheObject.setCacheKey(item.getString("cacheKey"));
            l2CacheObject.setArticlesCount(item.getInt(("articlesCount")));
            l2CacheObject.setTopArticlesCount(item.getInt("topArticlesCount"));
            l2CacheObject.setBottomArticlesCount(item.getInt("bottomArticlesCount"));
            l2CacheObject.setFiltersCount(item.getInt("filtersCount"));
            l2CacheObject.setBottomBatchSkip(item.getInt("bottomBatchSkip"));
            l2CacheObject.setDataKey(item.getString("dataKey"));
            slf4jLogger.debug(String.format("L2 Cache hit in dynamo with key:%s", key));
            return l2CacheObject;
        }


    }

    public void putCachedMetaData(L2CacheObject l2CacheObject){
        Table table = dynamoDB.getTable(L2CacheTableName);
        JSONObject jsonMetaData = new JSONObject();
        jsonMetaData.put("cacheKey", l2CacheObject.getCacheKey() );
        jsonMetaData.put("articlesCount", l2CacheObject.getArticlesCount() );
        jsonMetaData.put("topArticlesCount", l2CacheObject.getTopArticlesCount() );
        jsonMetaData.put("bottomArticlesCount", l2CacheObject.getBottomArticlesCount() );
        jsonMetaData.put("filtersCount", l2CacheObject.getFiltersCount() );
        jsonMetaData.put("bottomBatchSkip", l2CacheObject.getBottomBatchSkip());
        //MD5 hash key for data json files.
        jsonMetaData.put("dataKey", l2CacheObject.getDataKey());
        Item cachedMetaData = Item.fromJSON(jsonMetaData.toString());
        PutItemOutcome outcome = table.putItem(cachedMetaData);

    }

}
