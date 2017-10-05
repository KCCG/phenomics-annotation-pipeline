package au.org.garvan.kccg.annotations.pipeline.entities.linguistic;

import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ahmed on 13/7/17.
 */
public class APDependencyRelation {


    @Getter
    @Setter
    private String relation; // required


    @Getter
    @Setter
    private APToken governor; // required


    @Getter
    @Setter
    private APToken dependent; // required


    @Override
    public String toString()
    {
        return String.format("%s(%s-%s, %s-%s)", relation, governor.getOriginalText(),governor.getId(), dependent.getOriginalText(),dependent.getId());
    }
}
