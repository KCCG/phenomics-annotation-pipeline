package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.ArticleManager;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.omg.CORBA.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * Created by ahmed on 7/11/17.
 */
@Component
public class DynamoDBHandler {

    private final Logger slf4jLogger = LoggerFactory.getLogger(DynamoDBHandler.class);

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion("ap-southeast-2").build() ;
    DynamoDB dynamoDB = new DynamoDB(client);
    String articleTableName ;
    String annotationTableName;
    String subscriptionTableName;

    @Autowired
    public DynamoDBHandler(@Value("${spring.dbhandlers.dynamodb.articletablename}") String configArticleTableName,
                           @Value("${spring.dbhandlers.dynamodb.annotationtablename}") String configAnnotationTableName,
                           @Value("${spring.dbhandlers.dynamodb.subscriptiontablename}") String configSubscriptionTableName ){
        articleTableName = configArticleTableName;
        annotationTableName = configAnnotationTableName;
        subscriptionTableName = configSubscriptionTableName;

        slf4jLogger.info(String.format("DynamoDBHandler wired with Articles Table: %s and Annotations Table:%s.", articleTableName, annotationTableName));

    }

    public void insertItem(JSONObject jsonArticle, JSONObject jsonAnnotations)
    {

        Table articleTable =  dynamoDB.getTable(articleTableName);
        Item article = Item.fromJSON(jsonArticle.toString());
        articleTable.putItem(article);

        if(jsonAnnotations.containsKey("pubMedID")) {
            Table annotationTable = dynamoDB.getTable(annotationTableName);
            Item annotations = Item.fromJSON(jsonAnnotations.toString());
            annotationTable.putItem(annotations);
        }


    }

    public JSONObject getArticle(int pubMedId){
        Table table =  dynamoDB.getTable(articleTableName);
        Item item =  table.getItem("pubMedID", Integer.toString(pubMedId));
        if (item == null)
            return new JSONObject();
        else
            return  (JSONObject) JSONValue.parse(item.toJSON());

    }

   public JSONObject getAnnotations(int pubMedId, AnnotationType aType){
        Table table =  dynamoDB.getTable(annotationTableName);
        Item item =  table.getItem("pubMedID", Integer.toString(pubMedId),"annotationType",aType.toString());
        if (item == null)
            return new JSONObject();
        else
            return  (JSONObject) JSONValue.parse(item.toJSON());

    }

    /***
     * This should be an idempotent method, and insert or updated the subscription request.
     * @param jsonSubscription
     * @return
     */
    public boolean insertSubscription(JSONObject jsonSubscription){
        Table table =  dynamoDB.getTable(subscriptionTableName);
        Item subscription = Item.fromJSON(jsonSubscription.toString());
        PutItemOutcome outcome = table.putItem(subscription);
//        slf4jLogger.info( outcome.getPutItemResult().toString());
        //TODO: Update result based on information
        return true;
    }

//    public boolean getSubscription(){
//        Table table =  dynamoDB.getTable(subscriptionTableName);
//        Index index = table.getIndex("nextRunDate-index");
//
//        QuerySpec spec = new QuerySpec()
//                .withKeyConditionExpression("#d = :v_date ")
//                .withNameMap(new NameMap()
//                        .with("#d", "nextRunDate"))
//                .withValueMap(new ValueMap()
//                        .withNumber(":v_date", 0));
//
//        ItemCollection<QueryOutcome> items = index.query(spec);
//        Iterator<Item> iter = items.iterator();
//        while (iter.hasNext()) {
//            System.out.println(iter.next().toJSONPretty());
//        }

//        slf4jLogger.info( outcome.getPutItemResult().toString());
//        return true;
//    }

    // TODO: If used by more than one functions then make it generic
    public boolean checkSubscription(String id){
        Table table =  dynamoDB.getTable(subscriptionTableName);
        Item item =  table.getItem("subscriptionId", id);
        if (item == null)
            return false;
        else
            return true;

    }







}
