package au.org.garvan.kccg.annotations.pipeline.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APDocument;
import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APSentence;
import au.org.garvan.kccg.annotations.pipeline.processors.CoreNLPHanlder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ahmed on 12/9/17.
 */
public class LongFormMarkerTest {

    APDocument doc ;

    @Before
    public void setUp() throws Exception {
        CoreNLPHanlder.init();

    }

    @Test
    public void getSubtextsFromText() throws Exception {
        List<String> result =  LongFormMarker.getSubtextsFromText("Hyphon-split-", '-');
        Assert.assertEquals(result.size(),3);

    }


    @Test
    public void markLongFormsPrepositionTest()  {
        doc = new APDocument(0, " This is a text doc.  in for reprocessing (PP)");
        doc.hatch();
        APSentence apSentence = doc.getSentences().get(1);
        LongFormMarker.markLongForms(apSentence);

    }

    @Test
    public void markLongFormsPluralTest()  {
        doc = new APDocument(0, " This is a text doc. This will test abbreviation plasma concentrations (PCs)");
        doc.hatch();
        APSentence apSentence = doc.getSentences().get(1);
        LongFormMarker.markLongForms(apSentence);

    }

    @Test
    public void markLongFormsHyphenAndPreposition()  {
        doc = new APDocument(0, " This is a text doc. This will test abbreviation high-density lipoportein (HDL)");
        doc.hatch();
        APSentence apSentence = doc.getSentences().get(1);
        LongFormMarker.markLongForms(apSentence);
        Assert.assertFalse(apSentence.getSfLfLink().isEmpty());
    }

    @Test
    public void markLongFormsHyphenAndPrepositionWithThreeSubtexts()  {
        doc = new APDocument(0, " This is a text doc. This will test abbreviation charcot-marie-tooth (CMT)");
        doc.hatch();
        APSentence apSentence = doc.getSentences().get(1);
        LongFormMarker.markLongForms(apSentence);
        Assert.assertFalse(apSentence.getSfLfLink().isEmpty());
    }


    @Test
    public void markLongFormsOneWordInOrder()  {
        doc = new APDocument(0, " This is a text doc. This will test abbreviation Concurrent Chemoradiotherapy (CCRT)");
        doc.hatch();
        APSentence apSentence = doc.getSentences().get(1);
        LongFormMarker.markLongForms(apSentence);
        Assert.assertFalse(apSentence.getSfLfLink().isEmpty());
    }


    @Test
    public void markLongFormsOneWordInOrderCase2()  {
        doc = new APDocument(0, " This is a text doc. This will test abbreviation frontotemporal labor degenration (FTLD)");
        doc.hatch();
        APSentence apSentence = doc.getSentences().get(1);
        LongFormMarker.markLongForms(apSentence);
        Assert.assertFalse(apSentence.getSfLfLink().isEmpty());
    }





}