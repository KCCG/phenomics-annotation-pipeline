package au.org.garvan.kccg.annotations.pipeline.entities.publicational;

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


}
