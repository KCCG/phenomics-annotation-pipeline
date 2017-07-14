package au.org.garvan.kccg.annotations.pipeline.connectors;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APDocument;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by ahmed on 7/7/17.
 */
public class SolrConnector {

    private static String documentPoolURL = "http://localhost:8983/solr/Articles";
    private static String selectQuery = "/select";


    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build();

    public List<APDocument> getDocuments(LocalDate date) throws IOException {
        List<APDocument> docs = new ArrayList<>();

        int totalDocuments = getTotalDocuments(date);

        HttpUrl.Builder httpBuider = HttpUrl.parse(documentPoolURL + selectQuery).newBuilder();
        httpBuider.addQueryParameter("wt", "json");
        httpBuider.addQueryParameter("q", "dateCreatedEpoch:*");
        httpBuider.addQueryParameter("rows", Integer.toString(totalDocuments));


        httpBuider.setQueryParameter("q", "dateCreatedEpoch:" + date.toEpochDay());
        Request request = new Request.Builder()
                .get()
                .url(httpBuider.build().url())
                .build();

        Response response = CLIENT.newCall(request).execute();

        if (response.code() == 200) {
            JSONObject jsonObject = new JSONObject(response.body().string().trim());
            JSONArray jsonDocumentsArray = jsonObject.getJSONObject("response").getJSONArray("docs");
            for (Object jsonDoc : jsonDocumentsArray) {
                if ( ((JSONObject) jsonDoc).has("articleAbstract") ) {
                    int ID = Integer.parseInt(((JSONObject) jsonDoc).getJSONArray("PMID").get(0).toString());
                    String articleAbstarct = ((JSONObject) jsonDoc).getJSONArray("articleAbstract").get(0).toString();
                    docs.add(new APDocument(ID, articleAbstarct));
                }
            }

        }
        return docs;

    }


    public int getTotalDocuments(LocalDate date) throws IOException {
        int count = 0;

        HttpUrl.Builder httpBuider = HttpUrl.parse(documentPoolURL + selectQuery).newBuilder();
        httpBuider.addQueryParameter("wt", "json");
        httpBuider.addQueryParameter("q", "dateCreatedEpoch:*");
        httpBuider.addQueryParameter("fl", "dateCreatedEpoch");
        httpBuider.addQueryParameter("rows", "1");

        httpBuider.setQueryParameter("q", "dateCreatedEpoch:" + date.toEpochDay());
        Request request = new Request.Builder()
                .get()
                .url(httpBuider.build().url())
                .build();

        Response response = CLIENT.newCall(request).execute();


        if (response.code() == 200) {
            JSONObject jsonObject = new JSONObject(response.body().string().trim());
            count = (int) jsonObject.getJSONObject("response").get("numFound");

        }

        return count;
    }


}
