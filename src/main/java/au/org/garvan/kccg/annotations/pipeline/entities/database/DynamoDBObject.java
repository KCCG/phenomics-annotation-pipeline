package au.org.garvan.kccg.annotations.pipeline.entities.database;

import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * Created by ahmed on 7/11/17.
 */
public class DynamoDBObject {

    @Getter
    JSONObject jsonObject;
    @Getter
    EntityType entityType;

    public DynamoDBObject(JSONObject jObj, EntityType eT){
        jsonObject = jObj;
        entityType = eT;

    }
}
