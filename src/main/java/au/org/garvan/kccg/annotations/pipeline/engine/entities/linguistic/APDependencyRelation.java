package au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 13/7/17.
 */

public class APDependencyRelation {


    @Getter
    @Setter
    private String relation; // required


    @Getter
    @Setter
    private APToken governor; // required


    @Getter
    @Setter
    private APToken dependent; // required


    @Override
    public String toString()
    {
        return String.format("%s(%s-%s, %s-%s)", relation, governor.getOriginalText(),governor.getId(), dependent.getOriginalText(),dependent.getId());
    }

    public JSONObject constructJson(){
        JSONObject returnObject = new JSONObject();
        returnObject.put("relation",relation);
        returnObject.put("governorID", governor.getId());
        returnObject.put("dependentID", dependent.getId());
        return returnObject;
    }



    public APDependencyRelation(DynamoDBObject dbObject, List<APToken> tokens){
        if(dbObject.getEntityType().equals(EntityType.APDependencyRelation))
        {
            relation = dbObject.getJsonObject().get("relation").toString();
            int depId =  Integer.parseInt(dbObject.getJsonObject().get("dependentID").toString());
            int govId =  Integer.parseInt(dbObject.getJsonObject().get("governorID").toString());

            dependent = tokens.stream().filter(t->t.getId() == depId).collect(Collectors.toList()).get(0);
            if (govId>0)
                governor = tokens.stream().filter(t->t.getId() == govId).collect(Collectors.toList()).get(0);
            else
                governor = new APToken(0, "ROOT", "", "");

        }
        else{

        }

    }
    public APDependencyRelation(){

    }
}
