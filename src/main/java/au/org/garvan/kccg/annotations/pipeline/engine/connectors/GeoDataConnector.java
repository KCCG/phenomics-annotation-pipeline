package au.org.garvan.kccg.annotations.pipeline.engine.connectors;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.geodata.LinkedGeoData;
import edu.stanford.nlp.util.StringUtils;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ahmed on 20/9/18.
 */

public class GeoDataConnector {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(GeoDataConnector.class);

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build();

    private String eLinkUrl;
    private String eSummaryUrl;


    public GeoDataConnector() {

        eLinkUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi";
        eSummaryUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi";
        slf4jLogger.info(String.format("GeoDataConnector wired with endpoint:%s", eLinkUrl));

    }

    public List<Long> getLinkedIds(List<String> ids) {
        //dbfrom=pubmed&db=gds&id=25643280&retmode=json
        HttpUrl.Builder httpBuilder = HttpUrl.parse(eLinkUrl).newBuilder();
        httpBuilder.addQueryParameter("dbfrom", "pubmed");
        httpBuilder.addQueryParameter("db", "gds");
        httpBuilder.addQueryParameter("retmode", "json");
        httpBuilder.addQueryParameter("id", StringUtils.join(ids, ","));


        try {

            Request request = new Request.Builder()
                    .get()
                    .url(httpBuilder.build().url())
                    .build();
            Response response = null;

            response = CLIENT.newCall(request).execute();

            if (response.code() == 200) {
                List<Long> returnList = new ArrayList<>();

                JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string().trim());
                if (jsonObject.containsKey("linksets")) {
                    JSONArray linkSets = (JSONArray) jsonObject.get("linksets");
                    if (linkSets.size() > 0) {
                        JSONObject jsonLinkSet = (JSONObject) linkSets.get(0);

                        if (jsonLinkSet.containsKey("linksetdbs")) {
                            JSONArray linkSetDbs = (JSONArray) jsonLinkSet.get("linksetdbs");
                            if (linkSetDbs.size() > 0) {
                                JSONObject jsonLinkSetDbs = (JSONObject) linkSetDbs.get(0);
                                if (jsonLinkSetDbs.containsKey("links")) {
                                    returnList = (List<Long>) jsonLinkSetDbs.get("links");
                                    return returnList;
                                }

                            }


                        }

                    }


                }
                response.body().close();

            } else {
                slf4jLogger.error(String.format("Bad response from pubmed. status code:%d", response.code()));
            }


        } catch (IOException e) {
            slf4jLogger.error(String.format("Bad connection for pubmed."));
        }


        return null;
    }

    public List<LinkedGeoData> getLinkedData(List<Long> ids) {
        //db=gds&retmode=json&id=5000
        HttpUrl.Builder httpBuilder = HttpUrl.parse(eSummaryUrl).newBuilder();
        httpBuilder.addQueryParameter("db", "gds");
        httpBuilder.addQueryParameter("retmode", "json");
        httpBuilder.addQueryParameter("id", StringUtils.join(ids, ","));


        try {

            Request request = new Request.Builder()
                    .get()
                    .url(httpBuilder.build().url())
                    .build();
            Response response = null;

            response = CLIENT.newCall(request).execute();

            if (response.code() == 200) {
                List<LinkedGeoData> returnList = new ArrayList<>();

                JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string().trim());
                JSONObject jsonResult = (JSONObject) jsonObject.get("result");

                for (long id : ids) {
                    String strId = String.valueOf(id);
                    if (jsonResult.containsKey(strId)) {
                        JSONObject tempData = (JSONObject) jsonResult.get(strId);
                        if (tempData.containsKey("accession")) {
                            try {
                                String accession = tempData.get("accession").toString();
                                //https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=GSE18160
                                String webLink = String.format("https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=%s",accession);
                                    String title = tempData.get("title").toString();
                                    String entryType = tempData.get("entrytype").toString();
                                    JSONArray samples = (JSONArray) tempData.get("samples");
                                    List<String> pmids = (List<String>) tempData.get("pubmedids");

                                    returnList.add(new LinkedGeoData(accession, title,webLink, entryType, samples.size(),  pmids));


                            } catch (Exception e) {
                                slf4jLogger.error("Issue in result from pubmed geodata.");
                            }

                        } else {
                            slf4jLogger.debug("No record found for the id. ");

                        }


                    }
                }
                response.body().close();
                return returnList;

            } else {
                slf4jLogger.error(String.format("Bad response from pubmed. status code:%d", response.code()));
            }


        } catch (IOException e) {
            slf4jLogger.error(String.format("Bad connection for pubmed."));
        }


        return null;
    }


}
