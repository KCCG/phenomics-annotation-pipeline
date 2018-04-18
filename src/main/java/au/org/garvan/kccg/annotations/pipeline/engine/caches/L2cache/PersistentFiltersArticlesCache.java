package au.org.garvan.kccg.annotations.pipeline.engine.caches.L2cache;

import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.DynamoDBHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.L2CacheObject;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PersistentFiltersArticlesCache {

    @Autowired
    DynamoDBHandler dynamoDBHandler;


    public List<ConceptFilter> getL2CachedFilters(String key){
        List<ConceptFilter> conceptFilters = new ArrayList<>();
        JSONObject metaData =  dynamoDBHandler.getCachedMetaData(key);
        if (metaData !=null) {
            //TODO: Call S3 for filter fetch and return
            return conceptFilters;
        }
        else
            return null;


    }


    public L2CacheObject getL2CachedData(String key, boolean fetchArticles){
        L2CacheObject l2CacheObject = new L2CacheObject();

        JSONObject metaData =  dynamoDBHandler.getCachedMetaData(key);
        if (metaData ==null)
            return null;
        else
        {
            l2CacheObject.setArticlesCount(Integer.parseInt(metaData.get("articlesCount").toString()));
            l2CacheObject.setCachedArticleTopCount(Integer.parseInt(metaData.get("cachedArticleTopCount").toString()));
            l2CacheObject.setCachedArticleBottomCount(Integer.parseInt(metaData.get("cachedArticleBottomCount").toString()));
            l2CacheObject.setFiltersCount(Integer.parseInt(metaData.get("filtersCount").toString()));

            //TODO: Call S3 for filter reading and return it

            if(fetchArticles) {

                //TODO: Call S3 for articles reading if required
            }

        }

        return l2CacheObject;
    }


    public void putL2CachedData(String key, L2CacheObject cacheObject){
        //TODO: Write logic to put data in dynamo db and s3

    }

}
