package au.org.garvan.kccg.annotations.pipeline.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by ahmed on 12/1/18.
 */



@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ConceptFilter {

    @JsonProperty
    String id;
    @JsonProperty
    String type;
    @JsonProperty
    String text;
    @JsonProperty
    Integer rank;
    @JsonProperty
    Integer articleCount;
    @JsonProperty
    Integer filteredArticleCount;

    public void incrementArticleCount(Integer count){
        articleCount  = articleCount+count;
    }

    public void incrementFilteredArticleCount(Integer count){
        filteredArticleCount  = filteredArticleCount+count;
    }

    @Override
    public ConceptFilter clone(){
        ConceptFilter cloneFilter = new ConceptFilter(
                this.id, this.type,this.text,this.rank,this.articleCount,this.filteredArticleCount
        );
        return  cloneFilter;
    }
}
