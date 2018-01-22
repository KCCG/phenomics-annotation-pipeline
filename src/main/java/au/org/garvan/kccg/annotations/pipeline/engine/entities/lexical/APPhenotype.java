package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 19/01/18.
 */

@AllArgsConstructor
public class APPhenotype extends LexicalEntity {


    @Getter
    @Setter
    private String id;


    @Getter
    @Setter
    private String text;

}
