package au.org.garvan.kccg.annotations.pipeline.model.feedback.outward;

import au.org.garvan.kccg.annotations.pipeline.model.feedback.inward.FeedbackAnnotationItem;
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
public class FeedbackAnnotationItemDetailed {

    @JsonProperty
    @ApiModelProperty
    String id;

    @JsonProperty
    @ApiModelProperty
    String text;

    @JsonProperty
    @ApiModelProperty
    String type;

    @ApiModelProperty
    @JsonProperty
    int startIndex;

    @ApiModelProperty
    @JsonProperty
    int endIndex;

    public FeedbackAnnotationItemDetailed(FeedbackAnnotationItem feedbackAnnotationItem)
    {
        id = feedbackAnnotationItem.getId();
        startIndex = feedbackAnnotationItem.getStartIndex();
        endIndex = feedbackAnnotationItem.getEndIndex();
    }

}
