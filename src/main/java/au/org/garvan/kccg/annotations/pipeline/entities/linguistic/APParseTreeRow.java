package au.org.garvan.kccg.annotations.pipeline.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

/**
 * Created by ahmed on 13/7/17.
 */
public class APParseTreeRow extends LinguisticEntity {


    @Getter
    @Setter
    public int parentID; // required


    @Getter
    @Setter
    public boolean isLeafNode; // required


    @Getter
    @Setter
    public int offsetBegin; // required

    public APParseTreeRow(
            int ID,
            int parentID,
            String text,
            boolean isLeafNode,
            int offsetBegin)
    {
        super(ID, text);
        this.parentID = parentID;
        this.isLeafNode = isLeafNode;
        this.offsetBegin = offsetBegin;
    }

    public APParseTreeRow(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.APParseTreeRow))
        {

        }
        else{

        }

    }

    @Override
    public JSONObject constructJson(){
        JSONObject returnObject = new JSONObject();
        returnObject.put("parentID",parentID);
        returnObject.put("isLeafNode", isLeafNode);
        returnObject.put("offsetBegin", offsetBegin);
        return returnObject;
    }



}
