package au.org.garvan.kccg.annotations.pipeline.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
    private String normalizedText;

    @Setter
    @Getter
    private List<LexicalEntity> lexicalEntityList;




    public APToken(int id, String text, String POS, String lemmaText) {
        super(id, text);
        normalizedText = "";
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
    public APToken(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.APToken))
        {

        }
        else{

        }

    }


    public int beginPosition() {
        return sentOffset.x;
    }


    @Override
    public JSONObject constructJson(){
        JSONObject returnObject = super.constructJson();
        returnObject.put("partOfSpeech", partOfSpeech);
        returnObject.put("lemma", lemma);
        returnObject.put("partOfSpeech", partOfSpeech);

        JSONObject jsonPoint = new JSONObject();
        jsonPoint.put("x",sentOffset.getX());
        jsonPoint.put("y",sentOffset.getY());
        returnObject.put("sentOffset",jsonPoint);
        returnObject.put("shortForm", shortForm);
        returnObject.put("punctuation", punctuation);
        returnObject.put("normalizedText", normalizedText);

        if(lexicalEntityList.size()>0)
        {
            JSONArray jsonLexicalEntityList = new JSONArray();
            lexicalEntityList.forEach(le-> jsonLexicalEntityList.add(le.constructJson()));
            returnObject.put("lexicalEntityList",jsonLexicalEntityList);

        }

        return returnObject;
    }

}
