package au.org.garvan.kccg.annotations.pipeline.model.feedback.outward;

import au.org.garvan.kccg.annotations.pipeline.model.feedback.inward.Feedback;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.inward.FeedbackAnnotationItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedbackRequest {


    @ApiModelProperty
    private String pmid;

    @JsonProperty
    @ApiModelProperty
    private String feedback;

    @JsonProperty
    @ApiModelProperty
    private FeedbackAnnotationItemDetailed annotation;

    @JsonProperty
    @ApiModelProperty
    private String sendersAddress;


    @JsonProperty
    @ApiModelProperty
    private String timeStamp;


    @Override
    public String toString(){
        return String.format("From:%s | Article ID:%s | FeedBack:%s | AnnotationID:%s",
                sendersAddress, pmid,feedback,annotation.getId());
    }

    //Facilitation
    public FeedbackRequest(Feedback input){
        String [] splits = input.getFeedbackId().split(";");
        if(splits.length==3)
        {
            pmid = splits[0];
            annotation = new FeedbackAnnotationItemDetailed( new FeedbackAnnotationItem(splits[1], splits[2]));
            feedback =  input.getFeedback();
        }


    }
}
