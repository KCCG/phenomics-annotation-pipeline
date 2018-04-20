package au.org.garvan.kccg.annotations.pipeline.engine.entities.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;


@AllArgsConstructor
@Data
public class L2CacheArticle {
    //Stored by GraphDBHandler
    String PMID;
    BigDecimal totalConceptHits;
    Integer totalSearchedHits;
    Integer totalFilteredHits;
    Integer rank;
}
