package au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational;


import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import com.google.common.base.Strings;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * Created by ahmed on 7/3/18.
 */
@AllArgsConstructor
public class MeshHeading {

    @Getter
    @Property
    private String UI;
    @Getter
    @Property
    private String text;
    @Getter
    @Property
    private Boolean isMajor;

    public MeshHeading(JSONObject jsonMeshHeading){
        UI = jsonMeshHeading.containsKey ("UI")?(String) jsonMeshHeading.get("UI"):"";
        text = jsonMeshHeading.containsKey("text")?(String) jsonMeshHeading.get("text"):"";
        isMajor = jsonMeshHeading.containsKey("MajorTopicYN")?  ((Boolean) jsonMeshHeading.get("MajorTopicYN")): false;
    }

    public MeshHeading(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.MeshHeading))
        {
            UI = dbObject.getJsonObject().get("UI").toString();
            text = dbObject.getJsonObject().get("text").toString();
            isMajor = (Boolean)dbObject.getJsonObject().get("isMajor");

        }
        else{

        }

    }
    public JSONObject constructJson()
    {
        JSONObject meshHeading = new JSONObject();
        meshHeading.put("UI", UI);
        meshHeading.put("text", text);
        meshHeading.put("isMajor", isMajor);
        return meshHeading;
    }

    public boolean isValid() {

        return  ! (Strings.isNullOrEmpty(UI) || Strings.isNullOrEmpty(text));
    }
}
