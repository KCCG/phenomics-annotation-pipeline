package au.org.garvan.kccg.annotations.pipeline.connectors;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import au.org.garvan.kccg.annotations.pipeline.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.enums.CommonParams;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APDocument;
import jdk.nashorn.internal.parser.JSONParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ahmed on 7/7/17.
 */
public class SolrConnector implements BaseConnector {
    protected final Logger log = LoggerFactory.getLogger(SolrConnector.class);

    private static String documentPoolURL = "http://52.65.79.178:8983/solr/Articles";
    private static String selectQuery = "/select";


    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build();

    @Override
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
        System.out.println ("Calling solr to fetch articles for date:" + date.toEpochDay() );
        Response response = CLIENT.newCall(request).execute();

        if (response.code() == 200) {

            JSONObject jsonObject = (JSONObject) JSONValue.parse( response.body().string().trim()) ;
            JSONArray jsonDocumentsArray =  (JSONArray)((JSONObject)jsonObject.get("response")).get("docs");
            for (Object obj : jsonDocumentsArray) {
                JSONObject jsonDoc = (JSONObject) obj;
                if ( jsonDoc.containsKey("articleAbstract") ) {
                    int ID = Integer.parseInt(((JSONArray)jsonDoc.get("PMID")).get(0).toString());
                    String articleAbstract = ((JSONArray)jsonDoc.get("articleAbstract")).get(0).toString();
                    docs.add(new APDocument(ID, articleAbstract));
                }
            }

        }
        return docs;

    }

    @Override
    public List<APDocument> getDocuments(String queryText, CommonParams type) throws IOException {
        List<APDocument> docs = new ArrayList<>();

        HttpUrl.Builder httpBuider = HttpUrl.parse(documentPoolURL + selectQuery).newBuilder();
        httpBuider.addQueryParameter("wt", "json");
        httpBuider.addQueryParameter("q", queryText);
        httpBuider.addQueryParameter("rows", Integer.toString(50000));


        Request request = new Request.Builder()
                .get()
                .url(httpBuider.build().url())
                .build();
        System.out.println ("Calling solr to fetch articles for query:" + queryText);
        Response response = null;
            response = CLIENT.newCall(request).execute();


        if (response.code() == 200) {
            JSONObject jsonObject = (JSONObject) JSONValue.parse( response.body().string().trim()) ;
            JSONArray jsonDocumentsArray =  (JSONArray)((JSONObject)jsonObject.get("response")).get("docs");
            for (Object obj : jsonDocumentsArray) {
                JSONObject jsonDoc = (JSONObject) obj;
                if ( jsonDoc.containsKey("articleAbstract") ) {
                    int ID = Integer.parseInt(((JSONArray)jsonDoc.get("PMID")).get(0).toString());
                    String articleAbstract = ((JSONArray)jsonDoc.get("articleAbstract")).get(0).toString();
                    docs.add(new APDocument(ID, articleAbstract));
                }
            }

        }
        return docs;

    }


    @Override
    public APDocument getDocument(String PMID) throws IOException {
        HttpUrl.Builder httpBuider = HttpUrl.parse(documentPoolURL + selectQuery).newBuilder();
        httpBuider.addQueryParameter("wt", "json");
        httpBuider.addQueryParameter("q", "dateCreatedEpoch:*");
        httpBuider.addQueryParameter("rows", "1");


        httpBuider.setQueryParameter("q", "PMID:" + PMID);
        Request request = new Request.Builder()
                .get()
                .url(httpBuider.build().url())
                .build();

        Response response = CLIENT.newCall(request).execute();

        if (response.code() == 200) {
            APDocument collectedDoc;
            JSONObject jsonObject = (JSONObject) JSONValue.parse( response.body().string().trim()) ;
            JSONArray jsonDocumentsArray =  (JSONArray)((JSONObject)jsonObject.get("response")).get("docs");
            JSONObject jsonDoc = (JSONObject)jsonDocumentsArray.get(0);

            if ( jsonDoc.containsKey("PMID")  &&  jsonDoc.get("PMID").toString().equals(PMID)) {
                    int ID = Integer.parseInt(((JSONArray)jsonDoc.get("PMID")).get(0).toString());
                    String articleAbstract = ((JSONArray)jsonDoc.get("articleAbstract")).get(0).toString();
                    collectedDoc = new APDocument(ID, articleAbstract);
                    return  collectedDoc;
            }
             else{
                log.info(String.format("Article with PMID:%s is not availavle.",PMID));
            }

        }

        return new APDocument(0,"");

    }

    @Override
    public List<Article> getArticles(String param, CommonParams type) {
        return null;
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
            JSONObject jsonObject = (JSONObject) JSONValue.parse( response.body().string().trim()) ;
            count = Integer.parseInt(((JSONObject) jsonObject.get("response")) .get("numFound").toString());

        }

        return count;
    }


}
