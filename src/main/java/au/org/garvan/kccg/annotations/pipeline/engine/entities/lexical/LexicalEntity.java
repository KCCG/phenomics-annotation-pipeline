package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical;

import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ahmed on 4/10/17.
 */

public class LexicalEntity {
    private static final AtomicInteger count = new AtomicInteger(0);

    @Getter
    @Setter
    private int entityId;

    public LexicalEntity()
    {
        this.entityId = count.incrementAndGet();

    }


    public JSONObject constructJson(){
        JSONObject returnObject = new JSONObject();
        returnObject.put("id",entityId);
        return returnObject;
    }

}
