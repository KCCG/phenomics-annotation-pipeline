package au.org.garvan.kccg.annotations.pipeline.engine.entities.cache;

import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class L2CacheObject {

    Integer articlesCount = - 1;
    Integer filtersCount = -1;
    Integer cachedArticleTopCount = -1;
    Integer cachedArticleBottomCount =-1;
    Integer bottomBatchSkip = -1;

    List<ConceptFilter> finalFilters;
    List<RankedArticle> topRankedArticles;
    List<RankedArticle> bottomRankedArticles;


}
