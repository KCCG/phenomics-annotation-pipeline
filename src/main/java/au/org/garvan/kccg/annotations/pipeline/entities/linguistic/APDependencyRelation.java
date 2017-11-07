package au.org.garvan.kccg.annotations.pipeline.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import jdk.nashorn.internal.objects.annotations.Constructor;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

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



    public APDependencyRelation(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.APDependencyRelation))
        {

        }
        else{

        }

    }
    public APDependencyRelation(){

    }
}
