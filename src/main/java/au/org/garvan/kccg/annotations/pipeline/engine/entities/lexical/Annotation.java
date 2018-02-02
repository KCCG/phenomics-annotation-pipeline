package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical;

import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Annotation {

    private List<Integer> tokenIDs;
    private List<Point> tokenOffsets;
    private LexicalEntity entity;
    private AnnotationType type;
    private String standard;
    private String version;

    public Point getOffet(){
        Point offset = new Point(1000, 0);
        for(Point point: tokenOffsets){
            if(point.x<offset.x)
                offset.x = point.x;
            if(point.y>offset.y)
                offset.y = point.y;
        }
        return offset;

    }
}
