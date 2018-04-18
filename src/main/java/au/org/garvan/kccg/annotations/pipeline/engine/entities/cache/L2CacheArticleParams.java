package au.org.garvan.kccg.annotations.pipeline.engine.entities.cache;

import lombok.Data;

@Data
public class L2CacheArticleParams {

    Integer totalArticlesCount = -1;
    Integer skip = -1;
    Integer limit = -1;

    @Override
    public  String toString(){
        return String.format("ArticleCount:%d - Skip:%d - Limit:%d", totalArticlesCount, skip, limit);
    }

}
