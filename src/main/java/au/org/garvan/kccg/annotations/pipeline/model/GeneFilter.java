package au.org.garvan.kccg.annotations.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.util.ArrayList;
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
public class GeneFilter {

    String symbol;
    Integer count;
}
