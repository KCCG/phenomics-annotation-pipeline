package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import org.apache.commons.lang3.StringUtils;

public class Utilities {

    public static AnnotationType getAnnotationTypeBasedOnId(String id){

        if (id.contains("HP")) {
            return AnnotationType.PHENOTYPE;

        }
        else if (StringUtils.isNumeric(id)){
            return AnnotationType.GENE;

        }
        else if (id.contains("MONDO")){
            return AnnotationType.DISEASE;
        }
        return
                AnnotationType.ENTITY;

    }
}
