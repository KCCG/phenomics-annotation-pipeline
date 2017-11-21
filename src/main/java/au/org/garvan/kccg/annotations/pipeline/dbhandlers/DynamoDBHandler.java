package au.org.garvan.kccg.annotations.pipeline.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.enums.AnnotationType;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Created by ahmed on 7/11/17.
 */
public class DynamoDBHandler {

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion("ap-southeast-2").build() ;
    DynamoDB dynamoDB = new DynamoDB(client);
    String articleTableName = "test-phenomics-articles";
    String annotationsTableName = "test-phenomics-annotations";


    public void insertItem(JSONObject jsonArticle, JSONObject jsonAnnotations)
    {

        Table articleTable =  dynamoDB.getTable(articleTableName);
        Item article = Item.fromJSON(jsonArticle.toString());
        articleTable.putItem(article);

        if(jsonAnnotations.containsKey("pubMedID")) {
            Table annotationTable = dynamoDB.getTable(annotationsTableName);
            Item annotations = Item.fromJSON(jsonAnnotations.toString());
            annotationTable.putItem(annotations);
        }


    }

    public JSONObject getArticle(int pubMedId){
        Table table =  dynamoDB.getTable(articleTableName);
        Item item =  table.getItem("pubMedID", Integer.toString(pubMedId));
        return  (JSONObject) JSONValue.parse(item.toJSON());

    }

   public JSONObject getAnnotations(int pubMedId, AnnotationType aType){
        Table table =  dynamoDB.getTable(annotationsTableName);
        Item item =  table.getItem("pubMedID", Integer.toString(pubMedId),"annotationType",aType.toString());
        if (item == null)
            return new JSONObject();
        else
            return  (JSONObject) JSONValue.parse(item.toJSON());

    }







}
