package au.org.garvan.kccg.annotations.pipeline.model.feedback.inward;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedbackAnnotationItem {

    @JsonProperty
    @ApiModelProperty
    String id;

    @ApiModelProperty
    @JsonProperty
    int startIndex;

    @ApiModelProperty
    @JsonProperty
    int endIndex;

}
