package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.L2CacheArticle;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.L2CacheObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 22/11/17.
 */
@Component
public class S3Handler {
    private static String abstractsBucketName;
    private static String l2CacheFiltersBucketName;
    private static String l2CacheArticlesBucketName;
    private static String region = "ap-southeast-2";
    private static AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(region).build();
    private final Logger slf4jLogger = LoggerFactory.getLogger(S3Handler.class);

    @Autowired
    public S3Handler(@Value("${spring.dbhandlers.S3.abstractbucket}") String s3AbstractsBucket,
                     @Value("${spring.dbhandlers.S3.l2cachefiltersbucket}") String phenomicsL2CacheFilters,
                     @Value("${spring.dbhandlers.S3.l2cachearticlesbucket}") String phenomicsL2CacheArticles) {

        abstractsBucketName = s3AbstractsBucket;
        l2CacheFiltersBucketName = phenomicsL2CacheFilters;
        l2CacheArticlesBucketName = phenomicsL2CacheArticles;

        slf4jLogger.info(String.format("S3Handler wired with buckets:%s - %s - %s", abstractsBucketName, l2CacheFiltersBucketName, l2CacheArticlesBucketName));
    }

    /////////////////////////////////////////////////                 //////////////////////////////////////////////////////////////////////////

    public void storeAbstract(Article article) {
        String keyName = String.format("%d.json", article.getPubMedID());
        byte[] bytesToWrite = article.getArticleAbstract().constructJson().toString().getBytes();
        ObjectMetadata omd = new ObjectMetadata();
        omd.setContentLength(bytesToWrite.length);
        PutObjectResult result = s3client.putObject(new PutObjectRequest(abstractsBucketName, keyName, new ByteArrayInputStream(bytesToWrite), omd));

    }

    ///////////////////////////////////////////////// L2 Cache Calls //////////////////////////////////////////////////////////////////////////
    public boolean putL2CacheArticles(L2CacheObject l2CacheObject) {
        String keyName = String.format("%s.json", l2CacheObject.getDataKey());
        ObjectMapper mapper = new ObjectMapper();

        JSONObject jsonFileArticles = new JSONObject();
        try {
            if (l2CacheObject.getTopArticlesCount() > 0) {
                List<L2CacheArticle> cacheArticles = getCacheArticles(l2CacheObject.getTopRankedArticles());
                String topArticleString = mapper.writeValueAsString(cacheArticles);
                JSONArray jsonTopArticlesArray = (JSONArray) JSONValue.parse(topArticleString);
                jsonFileArticles.put("topRankedArticles", jsonTopArticlesArray);
            }
            if (l2CacheObject.getBottomArticlesCount() > 0) {
                List<L2CacheArticle> cacheArticles = getCacheArticles(l2CacheObject.getBottomRankedArticles());
                String bottomArticleString = mapper.writeValueAsString(cacheArticles);
                JSONArray jsonBottomArticlesArray = (JSONArray) JSONValue.parse(bottomArticleString);
                jsonFileArticles.put("bottomRankedArticles", jsonBottomArticlesArray);

            }
            putJsonIntoS3(l2CacheArticlesBucketName, keyName, jsonFileArticles);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }


    public boolean putL2CacheFilters(L2CacheObject l2CacheObject) {
        String keyName = String.format("%s.json", l2CacheObject.getDataKey());

        ObjectMapper mapper = new ObjectMapper();
        String filterString;
        try {
            filterString = mapper.writeValueAsString(l2CacheObject.getFinalFilters());
            JSONArray jsonFilters = (JSONArray) JSONValue.parse(filterString);

            JSONObject jsonFilterFile = new JSONObject();
            jsonFilterFile.put("filters", jsonFilters);
            putJsonIntoS3(l2CacheFiltersBucketName, keyName, jsonFilterFile);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public boolean getL2CacheFilters(L2CacheObject l2CacheObject) {
        String keyName = String.format("%s.json", l2CacheObject.getDataKey());
        JSONObject jsonObject = getJSonFromS3(l2CacheFiltersBucketName, keyName);

        if (jsonObject != null) {
            slf4jLogger.info(String.format("L2Cache Filters cached file is found for CacheKey:%s | DataKey:%s", l2CacheObject.getCacheKey(), l2CacheObject.getDataKey()));
            if (jsonObject.containsKey("filters")) {
                JSONArray jsonArray = (JSONArray) jsonObject.get("filters");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    ConceptFilter[] conceptFiltersArray = mapper.readValue(jsonArray.toString().getBytes(), ConceptFilter[].class);
                    l2CacheObject.setFinalFilters(Arrays.asList(conceptFiltersArray));
                    slf4jLogger.debug(String.format("L2Cache Filters have been collected for CacheKey:%s | DataKey:%s", l2CacheObject.getCacheKey(), l2CacheObject.getDataKey()));
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                slf4jLogger.error(String.format("L2Cache Filters cached file is there but missing filters for CacheKey:%s | DataKey:%s", l2CacheObject.getCacheKey(), l2CacheObject.getDataKey()));
                return false;
            }

        } else {
            slf4jLogger.error(String.format("L2Cache Filters cached file is missing for CacheKey:%s | DataKey:%s", l2CacheObject.getCacheKey(), l2CacheObject.getDataKey()));
            return false;

        }
        return false;

    }


    public boolean getL2CacheArticles(L2CacheObject l2CacheObject) {
        String keyName = String.format("%s.json", l2CacheObject.getDataKey());
        JSONObject jsonObject = getJSonFromS3(l2CacheArticlesBucketName, keyName);
        if (jsonObject != null) {
            slf4jLogger.info(String.format("L2Cache Articles cached file is found for CacheKey:%s | DataKey:%s", l2CacheObject.getCacheKey(), l2CacheObject.getDataKey()));

            if (l2CacheObject.getTopArticlesCount() > 0 && jsonObject.containsKey("topRankedArticles")) {
                JSONArray jsonArray = (JSONArray) jsonObject.get("topRankedArticles");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    L2CacheArticle [] cachedArticles = mapper.readValue(jsonArray.toString().getBytes(), L2CacheArticle[].class);
                    l2CacheObject.setTopRankedArticles(getRankedArticles(Arrays.asList(cachedArticles)));
                    slf4jLogger.debug(String.format("L2Cache Top Ranked Articles have been collected CacheKey:%s | DataKey:%s", l2CacheObject.getCacheKey(), l2CacheObject.getDataKey()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            if (l2CacheObject.getBottomArticlesCount() > 0 && jsonObject.containsKey("bottomRankedArticles")) {
                JSONArray jsonArray = (JSONArray) jsonObject.get("bottomRankedArticles");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    L2CacheArticle [] cachedArticles = mapper.readValue(jsonArray.toString().getBytes(), L2CacheArticle[].class);
                    l2CacheObject.setBottomRankedArticles(getRankedArticles(Arrays.asList(cachedArticles)));
                    slf4jLogger.debug(String.format("L2Cache Bottom Ranked Articles have been collected CacheKey:%s | DataKey:%s", l2CacheObject.getCacheKey(), l2CacheObject.getDataKey()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }


        } else {
            slf4jLogger.error(String.format("L2Cache Articles cached file is missing for CacheKey:%s | DataKey:%s", l2CacheObject.getCacheKey(), l2CacheObject.getDataKey()));
            return false;

        }
        return true;

    }
    ///////////////////////////////////////////////// L2 Cache Calls End  /////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////// Generic Functions  /////////////////////////////////////////////////////////////////////////

    private boolean putJsonIntoS3(String bucketName, String key, JSONObject inputJson) {
        byte[] bytesToWrite = inputJson.toString().getBytes();
        ObjectMetadata omd = new ObjectMetadata();
        omd.setContentLength(bytesToWrite.length);
        PutObjectResult result = s3client.putObject(new PutObjectRequest(bucketName, key, new ByteArrayInputStream(bytesToWrite), omd));
        return true;
    }

    private JSONObject getJSonFromS3(String bucketName, String key) {
        try {
            S3Object filtersS3Object = s3client.getObject(bucketName, key);
        if (filtersS3Object != null) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(filtersS3Object.getObjectContent()));
            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONObject jsonObject = (JSONObject) JSONValue.parse(stringBuilder.toString());
            return jsonObject;
        } else {

            return null;
        }
        }
        catch (AmazonS3Exception s3e)
        {
          slf4jLogger.error(String.format("L2Cache Error in file reading from s3. message:%s", s3e.getMessage()));
          return null;
        } catch (IOException e) {
            slf4jLogger.error(String.format("L2Cache Error in file reading from s3. message:%s", e.getMessage()));
            return null;
        }

    }

    private List<L2CacheArticle> getCacheArticles(List<RankedArticle> rankedArticles){
        return rankedArticles.stream().map(r -> new L2CacheArticle(
                r.getPMID(),
                r.getTotalConceptHits(),
                r.getTotalSearchedHits(),
                r.getTotalFilteredHits(),
                r.getRank()
        )).collect(Collectors.toList());
    }

    private List<RankedArticle> getRankedArticles(List<L2CacheArticle> cacheArticles){
        return cacheArticles.stream().map(r -> new RankedArticle(
                r.getPMID(),
                r.getTotalConceptHits(),
                r.getTotalSearchedHits(),
                r.getTotalFilteredHits(),
                r.getRank(),
                null,
                null
        )).collect(Collectors.toList());
    }


}
