package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
