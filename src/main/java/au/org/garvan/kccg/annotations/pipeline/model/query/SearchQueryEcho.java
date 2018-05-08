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
//@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQueryEcho {


    @ApiModelProperty
    private String queryId;

    @JsonProperty
    @ApiModelProperty
    private List<InputItemDto> searchItems;

    @JsonProperty
    @ApiModelProperty
    private List<InputItemDto> filterItems;


    @Data
    @AllArgsConstructor
    public static class InputItemDto {

        @JsonProperty
        String id;
        @JsonProperty
        String type;
        @JsonProperty
        String text;
    }

    public SearchQueryEcho(String qId, List<ConceptFilter> sItems, List<ConceptFilter> fItems ){
        queryId = qId;
        searchItems = sItems.stream().map(s-> new InputItemDto(s.getId(), s.getType(),s.getText())).collect(Collectors.toList());
        filterItems = fItems.stream().map(s-> new InputItemDto(s.getId(), s.getType(),s.getText())).collect(Collectors.toList());
    }

}
