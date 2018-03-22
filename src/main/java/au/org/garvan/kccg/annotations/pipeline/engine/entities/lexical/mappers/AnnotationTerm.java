package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnotationTerm {

    @JsonProperty
    Integer tokenStartIndex;
    @JsonProperty
    Integer tokenEndIndex;
    @JsonProperty
    Integer startOffset;
    @JsonProperty
    Integer endOffset;
}
