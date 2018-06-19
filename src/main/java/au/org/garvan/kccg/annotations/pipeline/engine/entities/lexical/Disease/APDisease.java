package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 4/10/17.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class APDisease extends LexicalEntity {

    private String oboURI;
    private String mondoID;
    private String definition;
    private String label;
    private List<String> synonyms;
    private boolean deprecated;

    public APDisease(LinkedHashMap linkedHashMap) {
        synonyms = new ArrayList<>();
        if (linkedHashMap.containsKey("id") && linkedHashMap.get("type").equals("CLASS")) {
            oboURI = linkedHashMap.get("id").toString();
            mondoID = getMondoID(oboURI);

            if (linkedHashMap.containsKey("meta")) {

                LinkedHashMap linkedMeta = (LinkedHashMap) linkedHashMap.get("meta");
                if (linkedMeta.containsKey("deprecated") && linkedMeta.get("deprecated").toString().equals("true"))
                    deprecated = true;
                if (linkedMeta.containsKey("definition")) {
                    definition = ((LinkedHashMap) linkedMeta.get("definition")).get("val").toString();
                }

                if (linkedMeta.containsKey("synonyms")) {

                    ArrayList lstSynonyms = (ArrayList) linkedMeta.get("synonyms");
                    for (Object obj : lstSynonyms) {
                        LinkedHashMap synonymMap =   ((LinkedHashMap)obj);
                        if(synonymMap.get("pred").toString().equals("hasExactSynonym"))
                            synonyms.add(synonymMap.get("val").toString());

                    }
                }
            }

            if (linkedHashMap.containsKey("lbl")) {
                label = linkedHashMap.get("lbl").toString();
            }

        }
        else
            deprecated = true;



    }

    public void checkEmptyLists(){
        if(synonyms ==null)
            synonyms = new ArrayList<>();
    }




    public JSONObject getAnnotationJsonForAnnotator(){
        JSONObject jsonDiseases = new JSONObject();

        jsonDiseases.put("oboURI", oboURI);
        jsonDiseases.put("mondoID", mondoID);
        jsonDiseases.put("definition", definition);
        jsonDiseases.put("label", label);
        jsonDiseases.put("synonyms", synonyms);
        jsonDiseases.put("deprecated", deprecated);

        return jsonDiseases;

    }


    public JSONObject getAnnotationJsonForAffinity(){
        JSONObject jsonDiseases = new JSONObject();

        jsonDiseases.put("queryId", this.getMondoID());
        jsonDiseases.put("queryType", AnnotationType.DISEASE.toString());

        String queryString = String.format("\"%s\"", this.label.trim());
        if(synonyms.size()>0)
        {
            queryString = queryString + " OR " + synonyms.stream().map(s-> String.format("\"%s\"", s.trim())).collect(Collectors.joining(" OR "));

        }
        jsonDiseases.put("queryString", queryString);
        return jsonDiseases;

    }

    String getMondoID(String uri) {
        try {
            int index = uri.lastIndexOf('/');
            String id_part = uri.substring(index + 1, uri.length());
            String[] parts = id_part.split("_");
            return parts[0] + ":" + parts[1];
        }
        catch(Exception e){
            System.out.println("Invalid URI");
        }

        return "";
    }

    public List<String> stringList() {
        List<String> lstData = new ArrayList<>();

        lstData.add(String.format("========%s========", mondoID ));
        lstData.add(String.format("%s: %s", "Complete URI", oboURI));
        lstData.add(String.format("%s: %s", "Preferred Label", label));
        lstData.add(String.format("%s: %s", "Other Labels",  String.join("\n",synonyms)));

        return lstData;
    }


}
