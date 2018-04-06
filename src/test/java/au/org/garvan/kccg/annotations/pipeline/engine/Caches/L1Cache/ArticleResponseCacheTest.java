package au.org.garvan.kccg.annotations.pipeline.engine.Caches.L1Cache;

import au.org.garvan.kccg.annotations.pipeline.model.query.SearchQueryV1;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchResultV1;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ArticleResponseCacheTest {


    @Test
    public void testCache() {
        String key1 = "S-hp:001;F-1;PS-10;PN:1";
        String key2 = "S-hp:001;F-1,2000;PS-10;PN:1";

        SearchResultV1 anArticle = new SearchResultV1();
        anArticle.setPmid(1);
        ArticleResponseCache.put(key1, Arrays.asList(anArticle));
        List<SearchResultV1> cached = ArticleResponseCache.get(key1);
        Assert.assertEquals(1, cached.size());
        List<SearchResultV1> cached2 = ArticleResponseCache.get(key2);
        Assert.assertNull(cached2);



    }

}