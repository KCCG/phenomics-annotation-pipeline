package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class APPhenotype extends LexicalEntity{

    @Getter
    DS_ConceptInfo phenotype;

    @Getter
    String hpoID;

    public  APPhenotype(DS_ConceptInfo conceptInfo){
        hpoID = conceptInfo.getUri();
        phenotype = conceptInfo;
    }


    public List<String> stringList() {
        List<String> lstData = new ArrayList<>();
        lstData.add(String.format("%s: %s", "HPO", hpoID));
        lstData.add(String.format("%s: %s", "Complete URI", phenotype.getCompleteURI()));
        lstData.add(String.format("%s: %s", "Preferred Label", phenotype.getPreferredLabel()));
        lstData.add(String.format("%s: %s", "Other Labels",  String.join("\n",phenotype.getAlternativeLabels())));
        return lstData;
    }
}
