package au.org.garvan.kccg.annotations.pipeline.engine.caches.L2cache;

import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.DynamoDBHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.S3Handler;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.FiltersCacheObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.L2CacheObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class PersistentFiltersArticlesCache {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(PersistentFiltersArticlesCache.class);
    private static LoadingCache<String, L2CacheObject> l3Cache;

    @Autowired
    DynamoDBHandler dynamoDBHandler;

    @Autowired
    S3Handler s3Handler;



    ////////////////////////////////////////////// L3 Cache Setup //////////////////////////////////
    static {
        slf4jLogger.info("Initializing Persistent Filters Article Cache L2.");
        l3Cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .build(
                        new CacheLoader<String, L2CacheObject>(){
                            @Override
                            public L2CacheObject load(String key) throws Exception {
                                return new L2CacheObject();
                            }
                        }
                );


    }

    private static void put(String key, L2CacheObject l2CacheObject){
        l3Cache.put(key, l2CacheObject);
        slf4jLogger.info(String.format("L2Cache | L3Cache putting l2CacheObject with key:%s", key));
    }

    private static L2CacheObject get(String key){
        L2CacheObject cacheHit = null;
        try {
            slf4jLogger.info(String.format("L2Cache | L3Cache call for l2CacheObject with key:%s", key));
            cacheHit = l3Cache.get(key);
            if(cacheHit.getArticlesCount()<0){
                slf4jLogger.info(String.format("L2Cache | L3Cache miss for l2CacheObject with key:%s", key));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        slf4jLogger.info(String.format("L2Cache | L3Cache hit for filters with key:%s", key));
        return cacheHit;
    }


    ////////////////////////////////////////////// L3 Cache Setup End //////////////////////////////////

    public L2CacheObject getL2CachedData(String key, boolean fetchArticles){
        L2CacheObject l2CacheObject = get(key);
        if(l2CacheObject !=null) {
            if((fetchArticles && (l2CacheObject.getTopRankedArticles() !=null && l2CacheObject.getTopRankedArticles().size()>-1)) || !fetchArticles)
                return l2CacheObject;

        }

        l2CacheObject = dynamoDBHandler.getCachedMetaData(key);
        if (l2CacheObject ==null)
            return null;
        else
        {
            //Update l2Cache Object with filters. In case false is returned then return a null.
            if(!s3Handler.getL2CacheFilters(l2CacheObject)) {
                return null;
            }
            if(fetchArticles) {
                //Update l2Cache Object with articles. In case false is returned then return a null.
                if(!s3Handler.getL2CacheArticles(l2CacheObject))
                    return null;
            }
        }
        put(key ,l2CacheObject);
        return l2CacheObject;
    }


    public void putL2CachedData(L2CacheObject cacheObject){

        put(cacheObject.getCacheKey(), cacheObject);

        //Get data key using the cache key. As S3 would not allow special chars as file name.
        String dataKey = DigestUtils.md5DigestAsHex(cacheObject.getCacheKey().getBytes());
        cacheObject.setDataKey(dataKey);
        slf4jLogger.info(String.format("L2Cache Putting cache metadata to dynamodb table for cache key:%s", cacheObject.getCacheKey()));

        dynamoDBHandler.putCachedMetaData(cacheObject);
        if(cacheObject.getFiltersCount()>0){
            slf4jLogger.info(String.format("L2Cache Putting filters to S3 for cache key:%s | data key:%s", cacheObject.getCacheKey(), dataKey));
            s3Handler.putL2CacheFilters(cacheObject);
        }
        if(cacheObject.getArticlesCount()>0)
        {
            slf4jLogger.info(String.format("L2Cache Putting articles to S3 for cache key:%s | data key:%s", cacheObject.getCacheKey(), dataKey));
            s3Handler.putL2CacheArticles(cacheObject);
        }

    }

    public List<String> checkIfL2CachedDataIsThereForSpeedQuery(String key, Integer docLimit) {
        List<String> articleIds = new ArrayList<>();
        L2CacheObject l2CacheObject = dynamoDBHandler.getCachedMetaData(key);
        if (l2CacheObject != null) {
            if (l2CacheObject.getArticlesCount() <= docLimit) {
                if (s3Handler.getL2CacheArticles(l2CacheObject)) {
                    articleIds = l2CacheObject.getTopRankedArticles().stream().map(d -> d.getPMID()).collect(Collectors.toList());
                }
            }
        }
        return articleIds;
    }

}
