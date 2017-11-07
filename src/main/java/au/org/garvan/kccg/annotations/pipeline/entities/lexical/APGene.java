package au.org.garvan.kccg.annotations.pipeline.entities.lexical;

import au.org.garvan.kccg.annotations.pipeline.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 4/10/17.
 */

@AllArgsConstructor
public class APGene extends LexicalEntity {


    @Getter
    @Setter
    private int HGNCID;


    @Getter
    @Setter
    private String approvedSymbol;


    @Getter
    @Setter
    private String approvedName;


    @Getter
    @Setter
    private String status;


    @Getter
    @Setter
    private List<String> previousSymbols;


    @Getter
    @Setter
    private List<String> synonyms;


    @Getter
    @Setter
    private List<String> chromosome;


    @Getter
    @Setter
    private List<String> accessionNumbers;


    @Getter
    @Setter
    private List<String> refSeqIds;


    @Getter
    @Setter
    private List<String> geneFamilyTag;


    @Getter
    @Setter
    private List<String> geneFamilyDescription;


    @Getter
    @Setter
    private List<Integer> geneFamilyID;



    public APGene(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.APGene))
        {

        }
        else{

        }

    }

    public List<String> stringList() {
        List<String> lstData = new ArrayList<>();
        lstData.add(String.format("%s: %d", "HGNCID", HGNCID));
        lstData.add(String.format("%s: %s", "Approved Symbol", approvedSymbol));
        lstData.add(String.format("%s: %s", "Approved Name", approvedName));
        lstData.add(String.format("%s: %s", "Status", status));
        lstData.add(String.format("%s: %s", "Previous Symbols", String.join(",", previousSymbols)));
        lstData.add(String.format("%s: %s", "Synonyms", String.join(",", synonyms)));
        lstData.add(String.format("%s: %s", "Chromosome", String.join(",", chromosome)));
        lstData.add(String.format("%s: %s", "Accession Numbers", String.join(",", accessionNumbers)));
        lstData.add(String.format("%s: %s", "Ref Seq IDs", String.join(",", refSeqIds)));
        lstData.add(String.format("%s: %s", "Gene Family Tag(s)", String.join(",", geneFamilyTag)));
        lstData.add(String.format("%s: %s", "Game Family Des(s)", String.join(",", geneFamilyDescription)));
        lstData.add(String.format("%s: %s", "Gene Family Id(s)", String.join(",", geneFamilyID.stream().map(e -> e.toString()).collect(Collectors.toList()))));
        return lstData;
    }


    @Override
    public JSONObject constructJson(){
        JSONObject returnObject = super.constructJson();
        returnObject.put("HGNCID",HGNCID);
        returnObject.put("approvedSymbol",approvedSymbol);
        returnObject.put("approvedName",approvedName);
        returnObject.put("status",status);
        returnObject.put("previousSymbols", new JSONArray().addAll(previousSymbols));
        returnObject.put("synonyms", new JSONArray().addAll(synonyms));
        returnObject.put("chromosome",new JSONArray().addAll(chromosome));
        returnObject.put("accessionNumbers",new JSONArray().addAll(accessionNumbers));
        returnObject.put("refSeqIds",new JSONArray().addAll(refSeqIds));
        returnObject.put("geneFamilyTag",new JSONArray().addAll(geneFamilyTag));
        returnObject.put("geneFamilyDescription",new JSONArray().addAll(geneFamilyDescription));
        returnObject.put("geneFamilyID",new JSONArray().addAll(geneFamilyID));

        return returnObject;
    }






}
