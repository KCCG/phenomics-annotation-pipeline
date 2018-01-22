package au.org.garvan.kccg.annotations.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ahmed on 19/1/18.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankedAutoCompleteEntity {

    @JsonProperty
    String id;

    @JsonProperty
    String text;

    @JsonProperty
    String type;

    @JsonProperty
    Integer rank;

}
