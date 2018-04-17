package au.org.garvan.kccg.annotations.pipeline.engine.entities.cache;

import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FiltersCacheObject {

    Integer articlesCount = - 1;
    List<ConceptFilter> finalFilters;

}
