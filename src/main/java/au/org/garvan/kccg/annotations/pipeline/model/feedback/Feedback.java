package au.org.garvan.kccg.annotations.pipeline.model.feedback;

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
public class Feedback {

    @ApiModelProperty
    private String pmid;

    @JsonProperty
    @ApiModelProperty
    private String feedback;

    @JsonProperty
    @ApiModelProperty
    private FeedbackAnnotationItem annotation;



    @Override
    public String toString(){
        return String.format("Article ID:%s | FeedBack:%s | AnnotationID:%s",
                pmid,feedback,annotation.getId());
    }
}
