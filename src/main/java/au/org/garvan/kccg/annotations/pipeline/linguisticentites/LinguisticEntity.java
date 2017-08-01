package au.org.garvan.kccg.annotations.pipeline.linguisticentites;

import lombok.Getter;


import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ahmed on 7/7/17.
 */

public class LinguisticEntity {
    private static final AtomicInteger count = new AtomicInteger(0);

    @Getter
    @Setter
    @Property
    private int id;

    @Getter
    @Setter
    @Property
    private String originalText;


    public LinguisticEntity()
    {

    }

    public LinguisticEntity(int id, String text) {
        this.id = id;
        this.originalText = text;
    }

    public LinguisticEntity(String text) {
        this.id = count.incrementAndGet();
        this.originalText = text;
    }
}
