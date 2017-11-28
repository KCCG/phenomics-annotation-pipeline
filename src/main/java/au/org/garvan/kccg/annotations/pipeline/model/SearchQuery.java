package au.org.garvan.kccg.annotations.pipeline.model;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by ahmed on 28/11/17.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel(description = "All attributes are optional; when more than one is provided, then search result will satisfy all conditions (Operation AND)")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQuery {


    @JsonProperty
    @ApiModelProperty(notes = "Gene list to search the articles; if more than one than it will be handles as AND condition")

    List<String> genes;

    @JsonProperty
    Author author;

    @JsonProperty
    Publication publication;

    @JsonProperty
    DateRange dateRange;

    public class DateRange{
        @JsonProperty
        String startDate;
        @JsonProperty
        String endDate;

    }
}
