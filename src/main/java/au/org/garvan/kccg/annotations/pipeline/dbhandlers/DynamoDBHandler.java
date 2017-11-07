package au.org.garvan.kccg.annotations.pipeline.dbhandlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

/**
 * Created by ahmed on 7/11/17.
 */
public class DynamoDBHandler {

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();






}
