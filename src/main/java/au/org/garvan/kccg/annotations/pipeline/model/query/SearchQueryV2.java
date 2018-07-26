package au.org.garvan.kccg.annotations.pipeline.model.query;

import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.List;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQueryV2 {


    @ApiModelProperty
    private String queryId;

    @JsonProperty
    @ApiModelProperty
    private List<String> searchItems;

    @JsonProperty
    @ApiModelProperty
    private List<String> filterItems;

    @JsonProperty
    @ApiModelProperty
    private Boolean searchAll;


    public SearchQueryV2 clone(){
       SearchQueryV2 searchQueryV2 =   new SearchQueryV2();
       searchQueryV2.setQueryId(this.queryId);
       searchQueryV2.setSearchItems(this.searchItems.stream().collect(Collectors.toList()));
       searchQueryV2.setFilterItems(this.filterItems.stream().collect(Collectors.toList()));
       searchQueryV2.setSearchAll(this.searchAll);
       return searchQueryV2;
    }


    public JSONObject constructJson(){
        ObjectMapper mapper = new ObjectMapper();
        String jsonString="{}";
        try {
            jsonString= mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        JSONObject returnObject = (JSONObject) JSONValue.parse(jsonString);
        return returnObject;
    }


}
