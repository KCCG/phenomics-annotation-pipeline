package au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic;

import lombok.Getter;


import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ahmed on 7/7/17.
 */

public class LinguisticEntity {
    private static final AtomicInteger count = new AtomicInteger(0);

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String originalText;


    public LinguisticEntity()
    {
        this.id = count.incrementAndGet();
    }

    public LinguisticEntity(int id, String text) {
        this.id = id;
        this.originalText = text;
    }

    public LinguisticEntity(String text) {
        this.id = count.incrementAndGet();
        this.originalText = text;
    }


    public JSONObject constructJson(){
        JSONObject returnObject = new JSONObject();
        returnObject.put("id",id);
        returnObject.put("originalText", originalText);
        return returnObject;
    }
}
