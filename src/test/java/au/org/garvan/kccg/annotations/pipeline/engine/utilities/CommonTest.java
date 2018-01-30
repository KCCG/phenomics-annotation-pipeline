package au.org.garvan.kccg.annotations.pipeline.engine.utilities;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommonTest {

    @Test
    public void getPunctuationTrimmedText() {

        APToken token = new APToken("(", "-LRB-","-LRB-");
        String trimmed= Common.getPunctuationTrimmedText(token);
    }
}