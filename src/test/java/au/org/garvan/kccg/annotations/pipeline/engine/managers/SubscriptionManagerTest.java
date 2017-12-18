package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.DynamoDBHandler;
import au.org.garvan.kccg.annotations.pipeline.model.SearchQuery;
import au.org.garvan.kccg.annotations.pipeline.model.SubscriptionQuery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by ahmed on 14/12/17.
 */
public class SubscriptionManagerTest {

    @Autowired
    SubscriptionManager subscriptionManager;

//
//    @Before
//    public void init(){
//        subscriptionManager = new SubscriptionManager();
//        subscriptionManager.dbManager = new DatabaseManager(new DynamoDBHandler("","", "test-phenomics-subscriptions"), null,null );
//
//    }
//    @Test
//    public void processSubscriptionUpdate() throws Exception {
//        SubscriptionQuery query = new SubscriptionQuery();
//        query.setSubscriptionId(UUID.randomUUID().toString());
//        query.setEmailId("am@am.com");
//        query.setDigestFrequencyInDays(1);
//        query.setQuery(new SearchQuery());
//        query.setSearchName("testSearch");
//
//        subscriptionManager.processSubscription(query);
//    }
//    @Test
//    public void processSubscriptionNew() throws Exception {
//        SubscriptionQuery query = new SubscriptionQuery();
//        query.setEmailId("am@am.com");
//        query.setDigestFrequencyInDays(1);
//        query.setQuery(new SearchQuery());
//        query.setSearchName("testSearch");
//
//        subscriptionManager.processSubscription(query);
//    }

}