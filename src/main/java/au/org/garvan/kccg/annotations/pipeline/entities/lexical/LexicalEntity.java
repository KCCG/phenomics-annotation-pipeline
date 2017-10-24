package au.org.garvan.kccg.annotations.pipeline.entities.lexical;

import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;

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

}
