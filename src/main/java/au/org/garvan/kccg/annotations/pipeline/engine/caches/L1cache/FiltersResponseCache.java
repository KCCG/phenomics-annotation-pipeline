package au.org.garvan.kccg.annotations.pipeline.engine.caches.L1cache;

import au.org.garvan.kccg.annotations.pipeline.engine.caches.CacheKeyGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB.GraphDBCachedNatives;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.FiltersCacheObject;
import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchQueryV2;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class FiltersResponseCache {

    private static LoadingCache<String, FiltersCacheObject> queryCache;
    private static final Logger slf4jLogger = LoggerFactory.getLogger(FiltersResponseCache.class);


    static {
        slf4jLogger.info("Initializing Filters Response Cache L1.");
        queryCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build(
                        new CacheLoader<String, FiltersCacheObject>(){
                            @Override
                            public FiltersCacheObject load(String key) throws Exception {
                                return new FiltersCacheObject();
                            }
                        }
                );

    }
    private static void put(String key, FiltersCacheObject filters){
        queryCache.put(key, filters);
        slf4jLogger.info("L1 Cache - Approximate size of filters cache:%d", queryCache.size());
    }

    private static FiltersCacheObject get(String key){
        FiltersCacheObject cacheHit = null;
        try {
            slf4jLogger.info(String.format("L1Cache call for filters with key:%s", key));
            cacheHit = queryCache.get(key);
              if(cacheHit.getArticlesCount()<0){
                  slf4jLogger.info(String.format("L1Cache miss for filters with key:%s", key));
                  return null;
              }
        } catch (Exception e) {
            e.printStackTrace();
        }
        slf4jLogger.info(String.format("L1Cache hit for filters with key:%s", key));
        return cacheHit;
    }

    public static FiltersCacheObject getFilters(SearchQueryV2 query, boolean includeHistorical){
        String key = CacheKeyGenerator.getL1CacheKeyForFilters(query,includeHistorical);
        return get(key);

    }

    public static void putFilters(SearchQueryV2 query, boolean includeHistorical, FiltersCacheObject filters){
        String key = CacheKeyGenerator.getL1CacheKeyForFilters(query,includeHistorical);
        slf4jLogger.info(String.format("L1Cache putting filters with key:%s", key));
        put(key, filters);
    }

    public static void clearCache(){
        slf4jLogger.info("L1 Cache - Flushing all filters entries from cache.");
        queryCache.invalidateAll();
    }

}
