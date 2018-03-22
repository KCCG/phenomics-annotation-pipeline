package au.org.garvan.kccg.annotations.pipeline.engine.connectors;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.AnnotationHit;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ahmed on 20/3/18.
 */
public class AffinityConnector {
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build();
    //    private static String affinityURL = "http://52.65.79.178:8983/solr/Articles";
    private static String affinityURL = "http://localhost:9020";
    private static String selectQuery = "/annotation";
    protected final Logger log = LoggerFactory.getLogger(AffinityConnector.class);

    public List<AnnotationHit> annotateAbstract(String text, int id, String lang) {
        AnnotationHit[] annotationHits = null;

        HttpUrl.Builder httpBuilder = HttpUrl.parse(affinityURL + selectQuery).newBuilder();
        JSONObject jsonArticle = new JSONObject();
        jsonArticle.put("articleID", String.valueOf(id));
        jsonArticle.put("articleAbstract", text);
        jsonArticle.put("language", lang);

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

                ObjectMapper mapper = new ObjectMapper();
                annotationHits = mapper.readValue(response.body().bytes(), AnnotationHit[].class);

            }
            if (annotationHits != null) {
                return Arrays.asList(annotationHits);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;

    }

}
