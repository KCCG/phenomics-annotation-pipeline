package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.util.LinkedHashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APDiseaseSynonym {

    private String pred;
    private String val;

    public APDiseaseSynonym(LinkedHashMap linkedObject){
        pred = val = "N/A";
        if(linkedObject.containsKey("pred")){
            pred = linkedObject.get("pred").toString();
        }
        if(linkedObject.containsKey("val")){
            val = linkedObject.get("val").toString();
        }


    }

}
