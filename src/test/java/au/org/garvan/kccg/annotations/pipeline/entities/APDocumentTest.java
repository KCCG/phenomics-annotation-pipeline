package au.org.garvan.kccg.annotations.pipeline.entities;

import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.processors.CoreNLPHanlder;
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
        testDoc = new APDocument(1, "MDMA has been shown to induce feelings of sociability, a positive emotional bias and enhanced empathy. While previous research has used only visual emotional stimuli, communication entails more than that single dimension and it is known that auditory information is also crucial in this process. In addition, it is, however, unclear what the neurobiological mechanism underlying these MDMA effects on social behaviour is. Previously, studies have shown that MDMA-induced emotional excitability and positive mood are linked to the action on the serotonin (5-HT) 2A receptor.The present study aimed at investigating the effect of MDMA on processing of sounds (Processing of Affective Sounds Task (PAST)) and cognitive biases (Approach-Avoidance Task (AAT)) towards emotional and social stimuli and the role of 5-HT2A receptor in these effects.Twenty healthy recreational users entered a 2 × 2, placebo-controlled, within-subject study with ketanserin (40 mg) as pre-treatment and MDMA (75 mg) as treatment. Behavioural (PAST, AAT) measures were conducted 90 min after treatment with MDMA, respectively, 120 min after ketanserin. Self-report mood measures and oxytocin concentrations were taken at baseline and before and after behavioural tests.Findings showed that MDMA reduced arousal elicited by negative sounds. This effect was counteracted by ketanserin pre-treatment, indicating involvement of the 5-HT2 receptor in this process. MDMA did not seem to induce a bias towards emotional and social stimuli. It increased positive and negative mood ratings and elevated oxytocin plasma concentrations. The reduction in arousal levels when listening to negative sounds was not related to the elevated subjective arousal.It is suggested that this decrease in arousal to negative stimuli reflects potentially a lowering of defences, a process that might play a role in the therapeutic process.");
        CoreNLPHanlder.init();
    }


    @Test
    public void testHatch() throws Exception {

        testDoc.hatch();
        int x = testDoc.getTokens().size();

        for (APSentence s : testDoc.getSentences())
        {
            s.generateParseTree();

        }
        Assert.assertNotEquals(testDoc.getSentences().size(),0);



    }   @Test
    public void testTokenization() throws Exception {

        testDoc.hatch();
        int x = testDoc.getTokens().size();

        for (APSentence s : testDoc.getSentences())
        {   System.out.println(s.getOriginalText());
            s.getTokens().forEach(t-> System.out.println(t.getOriginalText()+":"+t.getPartOfSpeech()));

        }
        Assert.assertNotEquals(testDoc.getSentences().size(),0);



    }



}