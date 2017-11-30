package au.org.garvan.kccg.annotations.pipeline.engine.profiles;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by ahmed on 22/11/17.
 */
@AllArgsConstructor
public class ProcessingProfile {

    @Getter
    boolean textNormalization = false;
    @Getter
    boolean SfLfExtraction = false;
    @Getter
    boolean geneAnnotation = false;
    @Getter
    boolean persistGraph = false;
    @Getter
    boolean persistEntities = false;
    @Getter
    boolean persistLinguisticStructure = false;

    public ProcessingProfile(){}

}
