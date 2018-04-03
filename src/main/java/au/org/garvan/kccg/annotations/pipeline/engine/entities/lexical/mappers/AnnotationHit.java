package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationHit {
    @JsonProperty
    String annotationID;
    @JsonProperty
    List<AnnotationTerm> hits = new ArrayList<>();


}
