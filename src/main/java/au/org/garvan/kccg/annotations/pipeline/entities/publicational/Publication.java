package au.org.garvan.kccg.annotations.pipeline.entities.publicational;

import au.org.garvan.kccg.annotations.pipeline.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

/**
 * Created by ahmed on 30/10/17.
 */

@AllArgsConstructor
public class Publication {

    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String ISOAbbreviation;
    @Getter
    @Setter
    private String ISSNType;
    @Getter
    @Setter
    private String ISSNNumber;


    public Publication(JSONObject jsonPublication) {
        title = jsonPublication.get("title").toString();
        ISOAbbreviation = jsonPublication.get("isoAbbreviation").toString();
        ISSNType = jsonPublication.get("issnType").toString();
        ISSNNumber = jsonPublication.get("issnNumber").toString();

    }


    public Publication(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.Publication))
        {

        }
        else{

        }


    }

    public JSONObject constructJson(){
        JSONObject returnObject = new JSONObject();
        returnObject.put("title", title);
        returnObject.put("isoAbbreviation", ISOAbbreviation);
        returnObject.put("issnType", ISSNType);
        returnObject.put("issnNumber", ISSNNumber);
        return returnObject;
    }

}
