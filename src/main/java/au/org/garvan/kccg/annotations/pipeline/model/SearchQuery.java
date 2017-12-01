package au.org.garvan.kccg.annotations.pipeline.model;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by ahmed on 28/11/17.
 */
@Data
//@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel(description = "All attributes are optional; when more than one is provided, then search result will satisfy all conditions (Operation AND)")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQuery {


    @ApiModelProperty(hidden = true)
    private String queryId = UUID.randomUUID().toString();

    @JsonProperty
    @ApiModelProperty(notes = "Gene list to search the articles(1-3 Allowed so far); if more than one than it will be handles as AND condition")
    private List<String> genes;

    @JsonProperty
    private Author author;

    @JsonProperty
    private Publication publication;

//    @JsonProperty
//    DateRange dateRange;
//
//    public class DateRange{
//        @JsonProperty
//        String startDate;
//        @JsonProperty
//        String endDate;
//
//    }


}
