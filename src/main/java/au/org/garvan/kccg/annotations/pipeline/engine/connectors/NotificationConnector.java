package au.org.garvan.kccg.annotations.pipeline.engine.connectors;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.AnnotationHit;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.inward.Feedback;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.outward.FeedbackRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ahmed on 02/5/18.
 */
@Component
public class NotificationConnector {
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build();
    private static String notificationURL = ""; //http://localhost:9090";
    private static String feedbackEndpoint = "/feedback";
    protected final Logger log = LoggerFactory.getLogger(NotificationConnector.class);

    @Autowired
    public NotificationConnector(@Value("${spring.notificationconnector.endpoint}") String endpoint){
        log.info(String.format("Notification connector is initialized with endpoint:%s", endpoint));
        notificationURL = endpoint;
    }

    @Async
    public void sendFeedback(FeedbackRequest feedbackRequest) {
        log.info(String.format("Dispatching feedback to notification service. | Details:%s.",feedbackRequest.toString()));
        HttpUrl.Builder httpBuilder = HttpUrl.parse(notificationURL + feedbackEndpoint).newBuilder();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(feedbackRequest);

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
            Request request = new Request.Builder()
                    .post(body)
                    .url(httpBuilder.build().url())
                    .build();
            Response response = null;
            response = CLIENT.newCall(request).execute();

            if (response.code() == 200) {
                log.info("Feedback successfully dispatched.");

            }
            else
            {
                log.error("Feedback dispatch failed.");
            }


        } catch (IOException e) {
            log.error("Exception in dispatch failed.");

        }


    }

}
