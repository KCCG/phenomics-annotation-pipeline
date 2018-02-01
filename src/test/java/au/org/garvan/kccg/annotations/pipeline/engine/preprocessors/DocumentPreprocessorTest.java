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
            Assert.assertEquals(4, phrases.size());

        }

    }

//    @Test
//    public void testPhrasesForLongerSentences(){
//        APDocument testDoc = new APDocument("A report is given on the recent discovery of outstanding immunological properties in BA 1 [N-(2-cyanoethylene)-urea] having a (low) molecular mass M = 111.104. Experiments in 214 DS carcinosarcoma bearing Wistar rats have shown that BA 1, at a dosage of only about 12 percent LD50 (150 mg kg) and negligible lethality (1.7 percent), results in a recovery rate of 40 percent without hyperglycemia and, in one test, of 80 percent with hyperglycemia. Under otherwise unchanged conditions the reference substance ifosfamide (IF) -- a further development of cyclophosphamide -- applied without hyperglycemia in its most efficient dosage of 47 percent LD50 (150 mg kg) brought about a recovery rate of 25 percent at a lethality of 18 percent. (Contrary to BA 1, 250-min hyperglycemia caused no further improvement of the recovery rate.) However this comparison is characterized by the fact that both substances exhibit two quite different (complementary) mechanisms of action. Leucocyte counts made after application of the said cancerostatics and dosages have shown a pronounced stimulation with BA 1 and with ifosfamide, the known suppression in the post-therapeutic interval usually found with standard cancerostatics. In combination with the cited plaque test for BA 1, blood pictures then allow conclusions on the immunity status. Since IF can be taken as one of the most efficient cancerostatics--there is no other chemotherapeutic known up to now that has a more significant effect on the DS carcinosarcoma in rats -- these findings are of special importance. Finally, the total amount of leucocytes and lymphocytes as well as their time behaviour was determined from the blood picture of tumour-free rats after i.v. application of BA 1. The thus obtained numerical values clearly show that further research work on the prophylactic use of this substance seems to be necessary and very promising.\n");
//        DocumentPreprocessor.preprocessDocument(testDoc);
//        List<APSentence> apSentenceLIst = testDoc.getSentences();
//
//        for(APSentence sent: testDoc.getSentences())
//        {
//            System.out.println(String.format("%d:%s" , sent.getId() ,sent.getOriginalText()));
//            List<APPhrase> phrases= sent.getPhrases(PhraseType.NOUN, true);
//            for(APPhrase apPhrase:phrases){
//                System.out.println(apPhrase.toString());
//            }
//
//        }
//



//    }

    @Test
    public void preprocessPhrase() {
        String text = "new face recognition";
        DocumentPreprocessor.preprocessPhrase(text);

    }
}