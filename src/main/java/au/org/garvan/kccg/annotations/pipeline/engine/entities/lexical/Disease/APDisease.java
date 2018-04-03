package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 4/10/17.
 */

@AllArgsConstructor
@Data
public class APDisease extends LexicalEntity {

    private String oboURI;
    private String mondoID;
    private String definition;
    private String label;
    private List<APDiseaseSynonym> synonyms;
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
                        APDiseaseSynonym tempSyn = new APDiseaseSynonym((LinkedHashMap) obj);
                        synonyms.add(tempSyn);
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


    public String getStringForFile(){
        String disease = String.format("%s:::\"%s\"", mondoID, label.replace("/", " " )
//                .trim()
//                .replace("(", "")
//                .replace(")", "")
//                .replace(".", "")
//                .replace(";", "")
//                .replace(":", "")
//                .replace(",", "")
//                .replace("-", "")
                .trim());
        for (APDiseaseSynonym s : synonyms){


            if(!Strings.isNullOrEmpty(s.getVal()) && !s.getVal().toUpperCase().equals(s.getVal()) ){
                disease = disease + " OR " +  String.format("\"%s\"", s.getVal().replace("/", " " )
//                        .trim()
//                        .replace("(", "")
//                        .replace(")", "")
//                        .replace(".", "")
//                        .replace(";", "")
//                        .replace(":", "")
//                        .replace(",", "")
//                        .replace("-", "")
                        .trim());
            }

        }
        return disease;

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
        lstData.add(String.format("%s: %s", "Other Labels",  String.join("\n",synonyms.stream().map(s->s.getVal()).collect(Collectors.toList()))));

        return lstData;
    }


}
