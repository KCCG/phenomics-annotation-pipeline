package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.model.SubscriptionQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.time.format.DateTimeFormatter;
/**
 * Created by ahmed on 22/11/17.
 */

@Service
public class SubscriptionManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(SubscriptionManager.class);

    @Autowired
    DatabaseManager dbManager;

    @Value("${spring.subscriptionservice.subscriptiontime}")
    private int subscriptionTime=2;

    public void init(){

        slf4jLogger.info(String.format("Subscription Manager init() called."));
    }


    public Pair<Boolean,Object> processSubscription(SubscriptionQuery subRequest)
    {

        //Check if it is an update query, if yes, verify ID.
        if(Strings.isNullOrEmpty(subRequest.getSubscriptionId()))
        {
            subRequest.setSubscriptionId(UUID.randomUUID().toString());
            slf4jLogger.info(String.format("Received a new subscription request. subscription Id:%s", subRequest.getSubscriptionId()));
        }
        else
        {
            slf4jLogger.info(String.format("Received subscription update request. subscription Id:%s", subRequest.getSubscriptionId()));
            if(dbManager.checkIfSubscriptionExists(subRequest.getSubscriptionId())){
                slf4jLogger.info(String.format("Subscription Id:%s is valid.", subRequest.getSubscriptionId()));
            }
            else
            {
                slf4jLogger.info(String.format("Subscription Id:%s is not valid.", subRequest.getSubscriptionId()));
                return new Pair<>(false, String.format("Invalid Subscription Id:%s",subRequest.getSubscriptionId()));
            }
        }


        // Frequency should be between 1-7
        int frequencyInDays =  Math.min(30, Math.max(1, subRequest.getDigestFrequencyInDays()));
        LocalDate nextRunDate = getNextRunDate(frequencyInDays);


        //Check email Id validity .
        StringBuilder message = new StringBuilder();
        if(!emailValidator(subRequest.getEmailId(), message))
        {
            slf4jLogger.info(String.format("Subscription id:%s %s", subRequest.getSubscriptionId(), message.toString()));
            return new Pair<>(false, message.toString());
        }

        //Check search query validity.
        JSONObject jsonQuery = subRequest.getQuery().constructJson();
        if(!jsonQuery.containsKey("queryId")){
            return new Pair<>(false, "Search query is invalid.");
        }

        Map<String, Object> argumentMap = new HashMap<>();
        argumentMap.put("lastRunDate", LocalDate.now().minusDays(1).toEpochDay());
        argumentMap.put("subscriptionId", subRequest.getSubscriptionId());
        argumentMap.put("emailId", subRequest.getEmailId());
        argumentMap.put("query", jsonQuery);
        argumentMap.put("frequencyInDays", frequencyInDays);
        argumentMap.put("searchName", subRequest.getSearchName());
        dbManager.persistSubscription(argumentMap);
        slf4jLogger.info(String.format("Finished subscription request for processing. Subscription Id:%s ", subRequest.getSubscriptionId()));


        LocalDateTime processTime = LocalDateTime.of(nextRunDate, LocalTime.of(subscriptionTime,0,0));
        String text = processTime.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE_TIME);
        return new Pair<>(true, text);
    }




    public  Pair<Boolean,Object> getSubscription(String id){
        JSONObject jsonSubscription = dbManager.getSubscription(id);
        if(jsonSubscription.containsKey("subscriptionId")){
            ObjectMapper objectMapper = new ObjectMapper();
            SubscriptionQuery returnObject = new SubscriptionQuery();
            try {
                returnObject= objectMapper.readValue(jsonSubscription.toJSONString(), SubscriptionQuery.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Pair<>(true,returnObject);
        }
        else
            return new Pair<>(false, "Subscription not found. ");


    }

    private LocalDate getNextRunDate(int frequencyInDays){
        int hours = LocalDateTime.now().getHour();
        if (hours<subscriptionTime){
          frequencyInDays--;
        }
        return LocalDate.now().plusDays(frequencyInDays);

    }

    private boolean emailValidator(String email, StringBuilder message) {
        boolean isValid = false;
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            isValid = true;
        } catch (NullPointerException n) {
            message.append("Email Id is missing.");
        } catch (AddressException e) {
            message.append(String.format("Invalid email Id. %s", email));
        }
        return isValid;
    }




}
