package au.org.garvan.kccg.annotations.pipeline.engine.Caches.L1Cache;

import au.org.garvan.kccg.annotations.pipeline.model.query.SearchResultV1;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class ArticleResponseCache {

    private static LoadingCache<String,List<SearchResultV1>> queryCache;


    static {
        queryCache = CacheBuilder.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(300, TimeUnit.HOURS)
                .build(
                        new CacheLoader<String, List<SearchResultV1>>(){
                            @Override
                            public List<SearchResultV1> load(String key) throws Exception {
                                return new ArrayList<>();
                            }
                        }
                );

    }

    public static void put(String key, List<SearchResultV1> articles){
        queryCache.put(key, articles);
    }

    public static List<SearchResultV1> get(String key){
        List<SearchResultV1> cacheHit = null;
        try {
              cacheHit = queryCache.get(key);
              if(cacheHit.size()==0)
                  return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cacheHit;
    }


}
