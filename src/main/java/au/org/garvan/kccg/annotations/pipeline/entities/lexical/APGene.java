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
public class APGene {


    @Property
    @Getter
    @Setter
    private int HGNCID;

    @Property
    @Getter
    @Setter
    private String approvedSymbol;

    @Property
    @Getter
    @Setter
    private String approvedName;

    @Property
    @Getter
    @Setter
    private String status;

    @Property
    @Getter
    @Setter
    private List<String> previousSymbols;


    @Property
    @Getter
    @Setter
    private List<String> Synonyms;


    @Property
    @Getter
    @Setter
    private List<String> chromosome;


    @Property
    @Getter
    @Setter
    private List<String> accessionNumbers;


    @Property
    @Getter
    @Setter
    private List<String> refSeqIds;


    @Property
    @Getter
    @Setter
    private String geneFamilyTag;

    @Property
    @Getter
    @Setter
    private String geneFamilyDescription;


    @Property
    @Getter
    @Setter
    private int geneFamilyID;


}
