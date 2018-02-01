package au.org.garvan.kccg.annotations.pipeline.engine.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.CoreNLPManager;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.LongFormMarker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by ahmed on 12/9/17.
 */
//TODO: Update the functionality of this class with new tokenization.
// Brackets and punctuations are separated into tokens now.
@Deprecated
public class LongFormMarkerTest {

//    APDocument doc ;
//
//    @Before
//    public void setUp() throws Exception {
//        CoreNLPManager.init();
//
//    }
//
//    @Test
//    public void getSubtextsFromText() throws Exception {
//        List<String> result =  LongFormMarker.getSubtextsFromText("Hyphon-split-", '-');
//        Assert.assertEquals(result.size(),2);
//
//    }
//
//
//    @Test
//    public void markLongFormsPrepositionTest()  {
//        doc = new APDocument(0, " This is a text doc.  in for reprocessing (PP)");
//        doc.hatch();
//        APSentence apSentence = doc.getSentences().get(1);
//        LongFormMarker.markLongForms(apSentence);
//
//    }
//
//    @Test
//    public void markLongFormsPluralTest()  {
//        doc = new APDocument(0, " This is a text doc. This will test abbreviation plasma concentrations (PCs)");
//        doc.hatch();
//        APSentence apSentence = doc.getSentences().get(1);
//        LongFormMarker.markLongForms(apSentence);
//
//    }
//
//    @Test
//    public void markLongFormsHyphenAndPreposition()  {
//        doc = new APDocument(0, " This is a text doc. This will test abbreviation high-density lipoportein (HDL)");
//        doc.hatch();
//        APSentence apSentence = doc.getSentences().get(1);
//        LongFormMarker.markLongForms(apSentence);
//        Assert.assertFalse(apSentence.getSfLfLink().isEmpty());
//    }
//
//    @Test
//    public void markLongFormsHyphenAndPrepositionWithThreeSubtexts()  {
//        doc = new APDocument(0, " This is a text doc. This will test abbreviation charcot-marie-tooth (CMT)");
//        doc.hatch();
//        APSentence apSentence = doc.getSentences().get(1);
//        LongFormMarker.markLongForms(apSentence);
//        Assert.assertFalse(apSentence.getSfLfLink().isEmpty());
//    }
//
//
//    @Test
//    public void markLongFormsOneWordInOrder()  {
//        doc = new APDocument(0, " This is a text doc. This will test abbreviation Concurrent Chemoradiotherapy (CCRT)");
//        doc.hatch();
//        APSentence apSentence = doc.getSentences().get(1);
//        LongFormMarker.markLongForms(apSentence);
//        Assert.assertFalse(apSentence.getSfLfLink().isEmpty());
//    }
//
//
//    @Test
//    public void markLongFormsOneWordInOrderCase2()  {
//        doc = new APDocument(0, " This is a text doc. This will test abbreviation frontotemporal labor degenration (FTLD)");
//        doc.hatch();
//        APSentence apSentence = doc.getSentences().get(1);
//        LongFormMarker.markLongForms(apSentence);
//        Assert.assertFalse(apSentence.getSfLfLink().isEmpty());
//    }
//




}