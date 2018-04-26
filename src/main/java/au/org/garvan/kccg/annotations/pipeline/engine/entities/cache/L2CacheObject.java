package au.org.garvan.kccg.annotations.pipeline.engine.entities.cache;

import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class L2CacheObject {

    @JsonProperty
    private String cacheKey = "";
    @JsonProperty
    private Integer articlesCount = - 1;
    @JsonProperty
    private Integer filtersCount = -1;
    @JsonProperty
    private Integer topArticlesCount = -1;
    @JsonProperty
    private Integer bottomArticlesCount =-1;
    @JsonProperty
    private Integer bottomBatchSkip = -1;

    @JsonProperty
    private String dataKey = "";
    @JsonProperty
    private List<ConceptFilter> finalFilters;
    @JsonProperty
    private List<RankedArticle> topRankedArticles;
    @JsonProperty
    private List<RankedArticle> bottomRankedArticles;


}
