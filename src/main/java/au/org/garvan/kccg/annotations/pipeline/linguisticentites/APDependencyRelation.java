package au.org.garvan.kccg.annotations.pipeline.linguisticentites;

import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ahmed on 13/7/17.
 */
public class APDependencyRelation {

    @Property
    @Getter
    @Setter
    private String relation; // required

    @Property
    @Getter
    @Setter
    private APToken governor; // required

    @Property
    @Getter
    @Setter
    private APToken dependent; // required


    @Override
    public String toString()
    {
        return String.format("%s(%s-%s, %s-%s)", relation, governor.getText(),governor.getId(), dependent.getText(),dependent.getId());
    }
}
