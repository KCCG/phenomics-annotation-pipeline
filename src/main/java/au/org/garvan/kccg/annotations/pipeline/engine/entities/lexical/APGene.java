package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
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


    public List<String> stringList() {
        List<String> lstData = new ArrayList<>();
        lstData.add(String.format("========HGNC:%s========", HGNCID ));
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
        //TODO: Fix Arrays loading

        returnObject.put("previousSymbols", previousSymbols);
        returnObject.put("synonyms", synonyms);
        returnObject.put("chromosome",chromosome);
        returnObject.put("accessionNumbers",accessionNumbers);
        returnObject.put("refSeqIds",refSeqIds);
        returnObject.put("geneFamilyTag",geneFamilyTag);
        returnObject.put("geneFamilyDescription",geneFamilyDescription);
        returnObject.put("geneFamilyID",geneFamilyID);

        return returnObject;
    }






}
