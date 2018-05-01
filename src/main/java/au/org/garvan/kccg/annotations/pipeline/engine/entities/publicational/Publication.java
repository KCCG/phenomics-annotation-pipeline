package au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Common;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.crypto.ec.ECElGamalDecryptor;
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
    private String isoAbbreviation;
    @Getter
    @Setter
    private String issnType;
    @Getter
    @Setter
    private String issnNumber;


    public Publication(JSONObject jsonPublication) {
        title = jsonPublication.get("title").toString();
        isoAbbreviation = jsonPublication.get("isoAbbreviation").toString();
        issnType = jsonPublication.get("issnType").toString();
        issnNumber = jsonPublication.get("issnNumber").toString();

        if(Strings.isNullOrEmpty(isoAbbreviation))
            isoAbbreviation = "N/A";
         if(Strings.isNullOrEmpty(issnType))
             issnType = "N/A";


    }


    public Publication(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.Publication))
        {
            try {
                title = dbObject.getJsonObject().get("title").toString();
            }
            catch (Exception e){
                title = "N/A";
            }

            try {
                isoAbbreviation = dbObject.getJsonObject().get("isoAbbreviation").toString();
            }
            catch (Exception e){
                isoAbbreviation = "N/A";
            }

            try {
                issnType = Common.emptyStringToNA(dbObject.getJsonObject().get("issnType").toString());
            }
            catch(Exception e)
            {
                issnType = "N/A";
            }
            try {
                issnNumber = Common.emptyStringToNA(dbObject.getJsonObject().get("issnNumber").toString());
            }
            catch (Exception e)
            {
                issnNumber = "N/A";
            }

        }
        else{

        }


    }

    public boolean isValidKey(){
        if (Strings.isNullOrEmpty(issnNumber))
            return false;
        if (Strings.isNullOrEmpty(issnType))
            return false;
        if (Strings.isNullOrEmpty(title))
            return false;
        if (Strings.isNullOrEmpty(isoAbbreviation))
            return false;

        return true;
    }

    public JSONObject constructJson(){
        JSONObject returnObject = new JSONObject();
        returnObject.put("title", title);
        returnObject.put("isoAbbreviation", isoAbbreviation);
        returnObject.put("issnType", issnType);
        returnObject.put("issnNumber", issnNumber);
        return returnObject;
    }

    public Publication(){}

}
