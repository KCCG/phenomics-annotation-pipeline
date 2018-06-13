package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Drug;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
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

    private String dbankUrl;
    private String dbankID;
    private String definition;
    private String label;
    private List<String> synonyms;

//    @JsonProperty
//    private List<Mixture> mixtures;

//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    static class Mixture{
//        @JsonProperty
//        String label;
//        @JsonProperty
//        List<String> ingredients;
//
//        public JSONObject getJson(){
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("label", label);
//            jsonObject.put("ingredients", ingredients);
//            return jsonObject;
//
//        }
//    }

//    public void addMixture(String name, List<String> ingredients){
//        if(mixtures == null)
//            mixtures = new ArrayList<>();
//
//            Mixture mixture = new Mixture(name, ingredients);
//            mixtures.add(mixture);
//
//    }

    public void checkEmptyLists(){
        if(synonyms ==null)
            synonyms = new ArrayList<>();
//        if(mixtures ==null)
//            mixtures = new ArrayList<>();

    }




    public JSONObject getAnnotationJsonForAnnotator(){
        JSONObject jsonDiseases = new JSONObject();

        jsonDiseases.put("dbankID", dbankID);
        jsonDiseases.put("definition", definition);
        jsonDiseases.put("label", label);

        if(synonyms !=null && synonyms.size()>0)
            jsonDiseases.put("synonyms", synonyms);
//
//        if(mixtures!=null && mixtures.size()>0){
//            JSONArray jsonMixtures = new JSONArray();
//            for (Mixture m: mixtures)
//                jsonMixtures.add(m.getJson());
//            jsonDiseases.put("mixtures", jsonMixtures);
//
//        }


        return jsonDiseases;

    }


    public JSONObject getAnnotationJsonForAffinity(){
        JSONObject jsonDrug = new JSONObject();

        jsonDrug.put("queryId", this.getDbankID());
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

        lstData.add(String.format("========%s========", dbankID ));
        lstData.add(String.format("%s: %s", "Complete URI", dbankUrl));
        lstData.add(String.format("%s: %s", "Preferred Label", label));
        lstData.add(String.format("%s: %s", "Other Labels",  String.join("\n",synonyms)));

        return lstData;
    }


}
