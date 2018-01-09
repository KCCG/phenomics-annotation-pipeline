package au.org.garvan.kccg.annotations.pipeline.model;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Created by ahmed on 9/1/18.
 */
@Data
@AllArgsConstructor
public class RankedArticle {
    
    String PMID;
    BigDecimal count;
    Integer rank;
    Article article;
    JSONObject annotations;
}
