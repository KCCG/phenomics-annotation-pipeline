package au.org.garvan.kccg.annotations.pipeline.model.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnDemandText {

    @JsonProperty(required = true)
    private final String text;


    @JsonProperty(required = false)
    private List<String> annotationProfile;

}
