package au.org.garvan.kccg.annotations.pipeline.engine.connectors;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.AnnotationHit;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.EngineEnvironment;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.config.ConfigLoader;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.config.EngineConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ahmed on 20/3/18.
 */

public class AffinityConnector {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(AffinityConnector.class);

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build();

    private String affinityURL;

    private String portNumber = ":9020";
    private static String selectQuery = "/annotation";
    private static String selectQueryAsync = "/annotationAsync";
    private static String resultQuery = "/annotationResults";



    public AffinityConnector(){

        String workerId = EngineEnvironment.getWorkerID();
        slf4jLogger.info(String.format("Affinity connector is looking for hook. Worker id:%s", workerId));
        portNumber = String.format(":%s", String.valueOf(Integer.parseInt(workerId) + 9020));
        affinityURL = EngineConfig.getAffinityEndpoint() + portNumber;
        slf4jLogger.info(String.format("Affinity connector wired with endpoint:%s", affinityURL));

    }

    public List<AnnotationHit> annotateAbstract(String text, int id, String lang) {
        AnnotationHit[] annotationHits = null;

        HttpUrl.Builder httpBuilder = HttpUrl.parse(affinityURL + selectQuery).newBuilder();
        JSONObject jsonArticle = new JSONObject();
        jsonArticle.put("articleID", String.valueOf(id));
        jsonArticle.put("articleAbstract", text);
        jsonArticle.put("language", lang);


        Integer retryCounter = 0;
        Boolean goodResponse = false;

        while (!goodResponse && retryCounter<3) {
            try {
                String jsonString = jsonArticle.toString();

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
                Request request = new Request.Builder()
                        .post(body)
                        .url(httpBuilder.build().url())
                        .build();
                Response response = null;

                response = CLIENT.newCall(request).execute();

                if (response.code() == 200) {
                    goodResponse = true;
                    ObjectMapper mapper = new ObjectMapper();
                    annotationHits = mapper.readValue(response.body().bytes(), AnnotationHit[].class);
                    response.body().close();

                } else {
                    retryCounter++;
                    slf4jLogger.error(String.format("Bad response from Affinity engine, sleeping for 2 seconds. status code:%d", response.code()));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                if (goodResponse) {
                    return Arrays.asList(annotationHits);
                }
            } catch (IOException e) {
                retryCounter++;
                slf4jLogger.error(String.format("Bad connection for Affinity engine, sleeping for 2 seconds."));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if(retryCounter>=3){
            slf4jLogger.error(String.format("Bad responses from Affinity, Tried 3 times. Terminating the application"));
            System.exit(0);
        }


        return null;

    }

    @Async
    public void annotateAbstractAsync(String text, int id, String lang) {
        AnnotationHit[] annotationHits = null;

        HttpUrl.Builder httpBuilder = HttpUrl.parse(affinityURL + selectQueryAsync).newBuilder();
        JSONObject jsonArticle = new JSONObject();
        jsonArticle.put("articleID", String.valueOf(id));
        jsonArticle.put("articleAbstract", text);
        jsonArticle.put("language", lang);


        Integer retryCounter = 0;
        Boolean goodResponse = false;

        while (!goodResponse && retryCounter<3) {
            try {
                String jsonString = jsonArticle.toString();

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
                Request request = new Request.Builder()
                        .post(body)
                        .url(httpBuilder.build().url())
                        .build();
                Response response = null;

                response = CLIENT.newCall(request).execute();

                if (response.code() == 200) {
                    goodResponse = true;
                    response.body().close();


                } else {
                    retryCounter++;
                    slf4jLogger.error(String.format("Bad response from Affinity engine, sleeping for 2 seconds. status code:%d", response.code()));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                if (goodResponse) {
                    return;
                }
            } catch (IOException e) {
                retryCounter++;
                slf4jLogger.error(String.format("Bad connection for Affinity engine, sleeping for 2 seconds."));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if(retryCounter>=3){
            slf4jLogger.error(String.format("Bad responses from Affinity, Tried 3 times. Terminating the application"));
            System.exit(0);
        }


        return;

    }


    public List<AnnotationHit> annotateAbstractResult(String text, int id, String lang) {
        AnnotationHit[] annotationHits = null;

        HttpUrl.Builder httpBuilder = HttpUrl.parse(affinityURL + resultQuery).newBuilder();
        JSONObject jsonArticle = new JSONObject();
        jsonArticle.put("articleID", String.valueOf(id));
        jsonArticle.put("articleAbstract", text);
        jsonArticle.put("language", lang);


        Integer retryCounter = 0;
        Boolean goodResponse = false;

        while (!goodResponse && retryCounter<3) {
            try {
                String jsonString = jsonArticle.toString();

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
                Request request = new Request.Builder()
                        .post(body)
                        .url(httpBuilder.build().url())
                        .build();
                Response response = null;

                response = CLIENT.newCall(request).execute();

                if (response.code() == 200) {
                    goodResponse = true;
                    ObjectMapper mapper = new ObjectMapper();
                    annotationHits = mapper.readValue(response.body().bytes(), AnnotationHit[].class);
                    response.body().close();

                } else {
                    retryCounter++;
                    slf4jLogger.error(String.format("Bad response from Affinity engine, sleeping for 2 seconds. status code:%d", response.code()));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                if (goodResponse) {
                    return Arrays.asList(annotationHits);
                }
            } catch (IOException e) {
                retryCounter++;
                slf4jLogger.error(String.format("Bad connection for Affinity engine, sleeping for 2 seconds."));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if(retryCounter>=3){
            slf4jLogger.error(String.format("Bad responses from Affinity, Tried 3 times. Terminating the application"));
            System.exit(0);
        }


        return null;

    }


}
