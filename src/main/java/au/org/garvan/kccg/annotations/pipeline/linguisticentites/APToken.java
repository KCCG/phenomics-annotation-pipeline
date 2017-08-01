package au.org.garvan.kccg.annotations.pipeline.linguisticentites;

import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

/**
 * Created by ahmed on 7/7/17.
 */
public class APToken extends LinguisticEntity{


    @Property
    @Setter
    @Getter
    private String partOfSpeech;


    @Property
    @Setter
    @Getter
    private String lemma;

    @Property
    @Setter
    @Getter
    private String modifiedText;

    @Property
    @Setter
    @Getter
    private Point sentOffset;

    public APToken(int id, String text, String POS, String lemmaText)
    {
        super(id, text);
        partOfSpeech = POS;
        lemma = lemmaText;

    }


    public APToken(String text, String POS, String lemmaText)
    {
        super(text);
        partOfSpeech = POS;
        lemma = lemmaText;

    }


    public APToken()
    {

    }


    public int beginPosition()
    {
        return sentOffset.x;
    }

}
