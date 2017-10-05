package au.org.garvan.kccg.annotations.pipeline.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.entities.lexical.LexicalEntity;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 7/7/17.
 */
public class APToken extends LinguisticEntity {


    @Setter
    @Getter
    private String partOfSpeech;

    @Setter
    @Getter
    private String lemma;

    @Setter
    @Getter
    private String modifiedText;

    @Setter
    @Getter
    private Point sentOffset;

    @Setter
    @Getter
    private boolean shortForm;

    @Setter
    @Getter
    private boolean punctuation;

    @Setter
    @Getter
    private List<LexicalEntity> lexicalEntityList;




    public APToken(int id, String text, String POS, String lemmaText) {
        super(id, text);
        partOfSpeech = POS;
        lemma = lemmaText;
        lexicalEntityList = new ArrayList<>();

    }


    public APToken(String text, String POS, String lemmaText) {
        super(text);
        partOfSpeech = POS;
        lemma = lemmaText;

    }


    public APToken() {

    }


    public int beginPosition() {
        return sentOffset.x;
    }

}
