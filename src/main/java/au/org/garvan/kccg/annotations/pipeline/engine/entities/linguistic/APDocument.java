package au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import au.org.garvan.kccg.annotations.pipeline.engine.profiles.ProcessingProfile;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 6/7/17.
 */
public class APDocument extends LinguisticEntity {


    @Getter
    @Setter
    private List<APSentence> sentences = new ArrayList<>();


    @Getter
    @Setter
    private String cleanedText;

    @Getter
    @Setter
    private ProcessingProfile processingProfile;


    public APDocument(int id, String text) {
        super(id, text);
        processingProfile = new ProcessingProfile();
    }


    public APDocument(String text) {
        super(text);
        processingProfile = new ProcessingProfile();

    }

    public APDocument(DynamoDBObject dbObject){
        super(Integer.parseInt(dbObject.getJsonObject().get("id").toString()), dbObject.getJsonObject().get("originalText").toString());
        if(dbObject.getEntityType().equals(EntityType.APDocument))
        {
            cleanedText = dbObject.getJsonObject().get("cleanedText").toString();
            JSONArray jsonSentences =  (JSONArray) dbObject.getJsonObject().get("sentences");
            for (Object jsonSentence : jsonSentences) {
                sentences.add(new APSentence(new DynamoDBObject((JSONObject)jsonSentence,EntityType.APSentence)));
            }
        }
        else{

        }

    }

    public List<APToken> getTokens() {
        List<APToken> tmpList = this.getSentences().stream().flatMap(y -> y.getTokens().stream()).collect(Collectors.toList());
        tmpList.sort(Comparator.comparing(a -> a.beginPosition()));
        return tmpList;


    }

    //This function is created to help graph db absorb all entities from an APdoc
    public Map<APSentence, List<APToken>> getTokensWithEntities() {
        Map<APSentence, List<APToken>> returnData = new HashMap<>();
        for (APSentence sent : this.getSentences()) {
            List<APToken> selectTokens = sent.getTokens().stream().filter(t -> t.getLexicalEntityList().size() > 0).collect(Collectors.toList());
            if(selectTokens.size()>0)
                returnData.put(sent,selectTokens);

        }
        return returnData;
    }


    public void hatch(Integer articleID) {
        DocumentPreprocessor.preprocessDocument(this, articleID);
    }


    public APSentence getSentenceWithID(int id) {
        List<APSentence> lstSent = sentences.stream().filter(s -> s.getId() == id).collect(Collectors.toList());
        if (lstSent.size() > 0) {
            return lstSent.get(0);
        } else {
            return null;
        }
    }


    @Override
    public JSONObject constructJson(){
        JSONObject returnObject = super.constructJson();

        JSONArray jsonSentences = new JSONArray();
        sentences.forEach(s-> jsonSentences.add(s.constructJson()));
        returnObject.put("sentences",jsonSentences);
        returnObject.put("cleanedText",cleanedText);
        return returnObject;
    }


}

