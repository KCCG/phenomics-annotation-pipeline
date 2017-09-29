package au.org.garvan.kccg.annotations.pipeline.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APDocument;
import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APToken;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ahmed on 24/8/17.
 */
public class ShortFormExtractorTest {
    @Test
    public void markShortForms() throws Exception {
        List<APToken> lstTokens = Arrays.asList(new APToken("(DIET)","NN","(DIET)"));
        ShortFormExtractor.markShortForms(lstTokens);

    }

}