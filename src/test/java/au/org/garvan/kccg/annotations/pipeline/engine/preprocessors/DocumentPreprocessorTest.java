package au.org.garvan.kccg.annotations.pipeline.engine.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.*;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.PhraseType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by ahmed on 1/8/17.
 */
public class DocumentPreprocessorTest {

    APDocument doc ;

    @Before
    public void setUp() throws Exception {
        doc = new APDocument(0, "The roving eye which he had cultivated for so many years.");
    }

    @Test
    public void preprocessDocument() throws Exception {

        DocumentPreprocessor.preprocessDocument(doc);
        List<APSentence> apSentenceLIst = doc.getSentences();
        Assert.assertEquals(1, apSentenceLIst.size());

        for (APSentence sent : apSentenceLIst){

            List<APPhrase> vphrases= sent.getPhrases(PhraseType.VERB, true);
            List<APPhrase> phrases= sent.getPhrases(PhraseType.NOUN, false);
            Assert.assertEquals(2, vphrases.size());
            Assert.assertEquals(3, phrases.size());

        }

    }

    @Test
    public void testPhrasesForLongerSentences(){
        APDocument testDoc = new APDocument("This article describes a 3D microfluidic paper-based analytical device that can be used to conduct an enzyme-linked immunosorbent assay (ELISA). The device comprises two parts: a sliding strip (which contains the active sensing area) and a structure surrounding the sliding strip (which holds stored reagents-buffers, antibodies, and enzymatic substrate-and distributes fluid). Running an ELISA involves adding sample (e.g. blood) and water, moving the sliding strip at scheduled times, and analyzing the resulting color in the sensing area visually or using a flatbed scanner. We demonstrate that this device can be used to detect C-reactive protein (CRP)-a biomarker for neonatal sepsis, pelvic inflammatory disease, and inflammatory bowel diseases-at a concentration range of 1-100ng/mL in 1000-fold diluted blood (1-100µg/mL in undiluted blood). The accuracy of the device (as characterized by the area under the receiver operator characteristics curve) is 89% and 83% for cut-offs of 10ng/mL (for neonatal sepsis and pelvic inflammatory disease) and 30ng/mL (for inflammatory bowel diseases) CRP in 1000-fold diluted blood respectively. In resource-limited settings, the device can be used as a part of a kit (containing the device, a fixed-volume capillary, a pre-filled tube, a syringe, and a dropper); this kit would cost ~ $0.50 when produced in large scale (>100,000 devices/week). This kit has the technical characteristics to be employed as a pre-screening tool, when combined with other data such as patient history and clinical signs.");
        DocumentPreprocessor.preprocessDocument(testDoc);
        List<APSentence> apSentenceLIst = testDoc.getSentences();

        for(APSentence sent: testDoc.getSentences())
        {
            System.out.println(String.format("%d:%s" , sent.getId() ,sent.getOriginalText()));
            List<APPhrase> phrases= sent.getPhrases(PhraseType.NOUN, true);
            for(APPhrase apPhrase:phrases){
                System.out.println(apPhrase.toString());
            }

        }




    }

    @Test
    public void preprocessPhrase() {
        String text = "new face recognition";
        DocumentPreprocessor.preprocessPhrase(text);

    }
}