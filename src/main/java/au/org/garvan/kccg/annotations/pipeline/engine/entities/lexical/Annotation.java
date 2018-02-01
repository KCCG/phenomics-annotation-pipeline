package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical;

import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Annotation {

    private List<Integer> tokenIDs;
    private List<Integer> tokensOffsetBegin;
    private LexicalEntity entity;
    private AnnotationType type;
    private String standard;
    private String version;

}
