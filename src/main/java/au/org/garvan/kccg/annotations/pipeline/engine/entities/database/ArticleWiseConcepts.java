package au.org.garvan.kccg.annotations.pipeline.engine.entities.database;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

// A class to collect article wise concepts and counts
// This is used for ranking and collecting query wide filters.
@Data
@AllArgsConstructor
public class ArticleWiseConcepts{
    String PMID;
    String identifier;
    String type;
    String text;
    BigDecimal count;

}
