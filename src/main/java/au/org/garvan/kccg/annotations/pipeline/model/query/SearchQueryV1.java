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

/**
 * Created by ahmed on 28/11/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel(description = "All attributes are optional; when more than one is provided, then search result will satisfy all conditions (Operation AND)")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQueryV1 {


    @ApiModelProperty
    private String queryId;

    @JsonProperty
    @ApiModelProperty
    private List<InputItemDto> searchItems;

    @JsonProperty
    @ApiModelProperty
    private List<InputItemDto> filterItems;


    @Data
    public static class InputItemDto {

        @JsonProperty
        String id;
        @JsonProperty
        String type;
    }




    public List<String> getGeneIDs(){
        return searchItems.stream()
                .filter(x -> x.getType().equals(AnnotationType.GENE.toString()))
                .collect(Collectors.toList())
                .stream()
                .map(g -> g.getId())
                .collect(Collectors.toList());

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
