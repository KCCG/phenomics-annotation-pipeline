package au.org.garvan.kccg.annotations.pipeline.entities.linguistic;

import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ahmed on 13/7/17.
 */
public class APParseTreeRow extends LinguisticEntity {


    @Getter
    @Setter
    public int parentID; // required


    @Getter
    @Setter
    public boolean isLeafNode; // required


    @Getter
    @Setter
    public int offsetBegin; // required

    public APParseTreeRow(
            int ID,
            int parentID,
            String text,
            boolean isLeafNode,
            int offsetBegin)
    {
        super(ID, text);
        this.parentID = parentID;
        this.isLeafNode = isLeafNode;
        this.offsetBegin = offsetBegin;
    }

}
