package au.org.garvan.kccg.annotations.pipeline.model;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.UUID;

/**
 * Created by ahmed on 14/12/17.
 */

@Data
//@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel(description = "")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionQuery {

    @JsonProperty
    @ApiModelProperty()
    private String subscriptionId;

    @JsonProperty(required = true)
    @ApiModelProperty(notes = "Search query to perform periodically.",required = true)
    private  SearchQuery query;

    @JsonProperty(required = true)
    @ApiModelProperty(required = true)
    private String emailId;

    @JsonProperty(required = true)
    @ApiModelProperty (required = true)
    private String searchName;

    @JsonProperty(required = true)
    @ApiModelProperty(required = true)
    private int digestFrequencyInDays;

    @JsonProperty
    @ApiModelProperty(hidden = true)
    private String nextRunTime;


}
