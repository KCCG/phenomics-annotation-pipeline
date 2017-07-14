package au.org.garvan.kccg.annotations.pipeline.linguisticentites;

import au.org.garvan.kccg.annotations.pipeline.preprocessors.CoreNLPHanlder;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ahmed on 12/7/17.
 */
public class APDocumentTest {

    APDocument testDoc;

    @After
    public void tearDown() throws Exception {

    }

    @Before
    public void init() throws Exception{
        testDoc = new APDocument(1, " This is a test document. It will be used for different operations for document class.");
        CoreNLPHanlder.init();
    }


    @Test
    public void hatch() throws Exception {

        testDoc.hatch();
        int x = testDoc.getTokens().size();

        for (APSentence s : testDoc.getSentences())
        {
            s.generateParseTree();

        }
        Assert.assertNotEquals(testDoc.getSentences().size(),0);



    }

}