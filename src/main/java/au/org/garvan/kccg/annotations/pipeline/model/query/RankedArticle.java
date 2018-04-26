package au.org.garvan.kccg.annotations.pipeline.model.query;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by ahmed on 9/1/18.
 */
@Data
@AllArgsConstructor
public class RankedArticle {

    //Stored by GraphDBHandler
    String PMID;
    BigDecimal totalConceptHits;
    Integer totalSearchedHits;
    Integer totalFilteredHits;
    Integer rank;
    //Filled by DBManager
    @JsonIgnore
    Article article;
    @JsonIgnore
    List<JSONObject> jsonAnnotations;

}
