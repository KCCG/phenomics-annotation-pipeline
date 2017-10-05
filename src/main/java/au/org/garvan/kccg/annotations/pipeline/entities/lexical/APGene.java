package au.org.garvan.kccg.annotations.pipeline.entities.lexical;

import jdk.nashorn.internal.objects.annotations.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private List<String> Synonyms;



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


}
