package au.org.garvan.kccg.annotations.pipeline.engine.entities.database;

import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ahmed on 12/1/18.
 */
@Data
public class DBManagerResultSet {
    List<RankedArticle> rankedArticles = new ArrayList<>();
    List<ConceptFilter> conceptCounts = new ArrayList<>();
}
