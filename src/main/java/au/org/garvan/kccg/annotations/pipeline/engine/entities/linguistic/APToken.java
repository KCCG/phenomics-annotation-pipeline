package au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Common;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
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
    private Point sentOffset;

    @Setter
    @Getter
    private boolean shortForm;

    @Setter
    @Getter
    private boolean punctuation;

    @Setter
    private String normalizedText;

    @Setter
    @Getter
    private List<LexicalEntity> lexicalEntityList;


    //CR: Added for CR support
    //Shape would be set on demand
    private String shape = null;


    //CR: Added for CR Support
    @Getter
    @Setter
    private boolean isTail = false;

    /***
     * Lazy loading as only Concept Recognizer would need it.
     * @return
     */
    public String getShape()
    {
        if(shape==null)
            shape = TAConstants.shape(getNormalizedText());
        return shape;
    }

    /***
     * CR: Support
     * Changed to support Concept Recognizer to use lowercase
     * @return
     */
    public String getNormalizedText(){
        if (Strings.isNullOrEmpty(normalizedText)){
            normalizedText = getOriginalText().toLowerCase();
        }
        return normalizedText;
    }


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


    @Override
    public String toString(){ {
            return String.format("[%d:%d] %s [%s] [%s] ", sentOffset.x, sentOffset.y, getOriginalText(), getNormalizedText(), getPartOfSpeech());
        }

    }



    public APToken(DynamoDBObject dbObject){
        super(Integer.parseInt(dbObject.getJsonObject().get("id").toString()), dbObject.getJsonObject().get("originalText").toString());
        if(dbObject.getEntityType().equals(EntityType.APToken))
        {
            partOfSpeech = dbObject.getJsonObject().get("partOfSpeech").toString();
            lemma = dbObject.getJsonObject().get("lemma").toString();
            sentOffset = new Point(Integer.parseInt(((JSONObject)dbObject.getJsonObject().get("sentOffset")).get("x").toString()),
                    Integer.parseInt(((JSONObject)dbObject.getJsonObject().get("sentOffset")).get("y").toString()));
            shortForm = Boolean.parseBoolean(dbObject.getJsonObject().get("shortForm").toString());
            punctuation = Boolean.parseBoolean(dbObject.getJsonObject().get("punctuation").toString());
            normalizedText =  dbObject.getJsonObject().containsKey("normalizedText")? dbObject.getJsonObject().get("normalizedText").toString() : "";
            lexicalEntityList = new ArrayList<>();

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

        JSONObject jsonPoint = new JSONObject();
        jsonPoint.put("x",sentOffset.getX());
        jsonPoint.put("y",sentOffset.getY());
        returnObject.put("sentOffset",jsonPoint);
        returnObject.put("shortForm", shortForm);
        returnObject.put("punctuation", punctuation);

        if(!normalizedText.isEmpty())
            returnObject.put("normalizedText", normalizedText);

//        if(lexicalEntityList.size()>0)
//        {
//            JSONArray jsonLexicalEntityList = new JSONArray();
//            lexicalEntityList.forEach(le-> jsonLexicalEntityList.add(le.constructJson()));
//            returnObject.put("lexicalEntityList",jsonLexicalEntityList);
//
//        }

        return returnObject;
    }

}
