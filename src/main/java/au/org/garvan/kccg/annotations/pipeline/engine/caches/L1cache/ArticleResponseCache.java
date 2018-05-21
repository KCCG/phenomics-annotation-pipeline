package au.org.garvan.kccg.annotations.pipeline.engine.caches.L1cache;

import au.org.garvan.kccg.annotations.pipeline.engine.caches.CacheKeyGenerator;
import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchQueryV2;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchResultV1;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchResultV2;
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
public class ArticleResponseCache {

    private static LoadingCache<String, List<SearchResultV2>> queryCache;
    private static final Logger slf4jLogger = LoggerFactory.getLogger(ArticleResponseCache.class);

    static {
        slf4jLogger.info("Initializing Article Response Cache L1.");
        queryCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build(
                        new CacheLoader<String, List<SearchResultV2>>(){
                            @Override
                            public List<SearchResultV2> load(String key) throws Exception {
                                return new ArrayList<>();
                            }
                        }
                );

    }

    private static void put(String key, List<SearchResultV2> articles){
        queryCache.put(key, articles);
        slf4jLogger.info(String.format("L1 Cache - Approximate size of articles cache:%d", queryCache.size()));

    }

    private static List<SearchResultV2> get(String key){
        List<SearchResultV2> cacheHit = null;
        try {
            slf4jLogger.info(String.format("L1Cache call for articles list with key:%s", key));
            cacheHit = queryCache.get(key);
              if(cacheHit.size()==0){
                  slf4jLogger.info(String.format("L1Cache miss for articles list with key:%s", key));
                  return null;
              }
        } catch (Exception e) {
            e.printStackTrace();
        }
        slf4jLogger.info(String.format("L1Cache hit for articles list with key:%s", key));
        return cacheHit;
    }


    public static List<SearchResultV2> getArticles(SearchQueryV2 query, PaginationRequestParams params){
        String key = CacheKeyGenerator.getL1CacheKeyForPaginatedArticles(query, params);
        return get(key);
    }

    public static void putArticles(SearchQueryV2 query, PaginationRequestParams params, List<SearchResultV2> lstArticles){
        String key = CacheKeyGenerator.getL1CacheKeyForPaginatedArticles(query, params);
        slf4jLogger.info(String.format("L1Cache putting articles list with key:%s", key));
        put(key, lstArticles);
    }


    public static void clearCache(){
        slf4jLogger.info("L1 Cache - Flushing all article entries from cache.");
        queryCache.invalidateAll();
    }


}
