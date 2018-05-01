package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.Feedback;
import au.org.garvan.kccg.annotations.pipeline.model.subscription.SubscriptionQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by ahmed on 22/11/17.
 */

@Service
public class FeedbackManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(FeedbackManager.class);

    @Autowired
    DatabaseManager dbManager;

    public void init(){
        slf4jLogger.info(String.format("Feedback Manager init() called."));
    }

    public boolean processFeedback(Feedback feedback)
    {
        slf4jLogger.info(String.format("Feedback received:%s.",feedback.toString()));
        return true;

    }

}
