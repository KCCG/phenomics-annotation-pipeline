package au.org.garvan.kccg.annotations.pipeline.engine.profiles;

import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 22/11/17.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessingProfile {


    //Text processing
    private boolean processTextNormalization = false;
    private boolean processSfLfExtraction = false;
    private boolean processParseTree = false;
    private boolean processDependencies = false;

    //Annotations
    private List<AnnotationType> annotationRequests = new ArrayList<>();


}
