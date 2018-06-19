package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Drug;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 4/10/17.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class APDrug extends LexicalEntity {

    private String drugBankUrl;
    private String drugBankID;
    private String definition;
    private String label;
    private List<String> synonyms;

    public void checkEmptyLists(){
        if(synonyms ==null)
            synonyms = new ArrayList<>();


    }




    public JSONObject getAnnotationJsonForAnnotator(){
        JSONObject jsonDiseases = new JSONObject();

        jsonDiseases.put("drugBankID", drugBankID);
        jsonDiseases.put("definition", definition);
        jsonDiseases.put("label", label);

        if(synonyms !=null && synonyms.size()>0)
            jsonDiseases.put("synonyms", synonyms);


        return jsonDiseases;

    }


    public JSONObject getAnnotationJsonForAffinity(){
        JSONObject jsonDrug = new JSONObject();

        jsonDrug.put("queryId", this.getDrugBankID());
        jsonDrug.put("queryType", AnnotationType.DRUG.toString());

        String queryString = String.format("\"%s\"", this.label.trim());
        if(synonyms.size()>0)
        {
            queryString = queryString + " OR " + synonyms.stream().map(s-> String.format("\"%s\"", s.trim())).collect(Collectors.joining(" OR "));

        }
        jsonDrug.put("queryString", queryString);
        return jsonDrug;

    }



    public List<String> stringList() {
        List<String> lstData = new ArrayList<>();

        lstData.add(String.format("========%s========", drugBankID));
        lstData.add(String.format("%s: %s", "Complete URI", drugBankUrl));
        lstData.add(String.format("%s: %s", "Preferred Label", label));
        lstData.add(String.format("%s: %s", "Other Labels",  String.join("\n",synonyms)));

        return lstData;
    }


}
