package au.org.garvan.kccg.annotations.pipeline.entities.publicational;

import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import org.json.simple.JSONObject;


/**
 * Created by ahmed on 30/10/17.
 */
public class Author {

    @Getter
    @Property
    private String foreName;
    @Getter
    @Property
    private String lastName;
    @Getter
    @Property
    private String initials;


    public Author(JSONObject jsonAuthor) {


        initials = jsonAuthor.containsKey("initials") ? (String) jsonAuthor.get("initials") : "";
        foreName = jsonAuthor.containsKey("foreName") ? (String) jsonAuthor.get("foreName") : "";
        lastName = jsonAuthor.containsKey("lastName") ? (String) jsonAuthor.get("lastName") : "";
    }


}
