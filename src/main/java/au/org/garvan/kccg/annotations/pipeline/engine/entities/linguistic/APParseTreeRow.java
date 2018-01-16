package au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

/**
 * Created by ahmed on 13/7/17.
 */
public class APParseTreeRow extends LinguisticEntity {


    @Getter
    @Setter
    private int parentID; // required


    @Getter
    @Setter
    private boolean isLeafNode; // required


    @Getter
    @Setter
    private int offsetBegin; // required

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
        super(Integer.parseInt(dbObject.getJsonObject().get("id").toString()), dbObject.getJsonObject().get("originalText").toString());
        if(dbObject.getEntityType().equals(EntityType.APParseTreeRow))
        {
            parentID = Integer.parseInt(dbObject.getJsonObject().get("parentID").toString());
            isLeafNode = Boolean.parseBoolean(dbObject.getJsonObject().get("parentID").toString());
            offsetBegin = Integer.parseInt(dbObject.getJsonObject().get("offsetBegin").toString());

        }
        else{

        }

    }

    @Override
    public JSONObject constructJson(){
        JSONObject returnObject = super.constructJson();
        returnObject.put("parentID",parentID);
        returnObject.put("isLeafNode", isLeafNode);
        returnObject.put("offsetBegin", offsetBegin);
        return returnObject;
    }



}
