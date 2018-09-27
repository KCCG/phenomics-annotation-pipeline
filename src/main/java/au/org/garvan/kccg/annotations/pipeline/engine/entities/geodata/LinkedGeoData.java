package au.org.garvan.kccg.annotations.pipeline.engine.entities.geodata;


import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by ahmed on 21/9/18.
 */
@AllArgsConstructor
@Data
public class LinkedGeoData {

    @Property
    private String uid;
    @Property
    private String title;
    @Property
    private String link;
    @Property
    private String entryType;
    @Property
    private Integer samplesCount;

    @JsonIgnore
    @Property
    private List<String> pubmedIds;



}
