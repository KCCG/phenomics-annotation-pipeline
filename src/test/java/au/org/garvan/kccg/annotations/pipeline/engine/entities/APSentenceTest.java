package au.org.garvan.kccg.annotations.pipeline.engine.entities;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.CoreNLPManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by ahmed on 13/7/17.
 */
public class APSentenceTest {


    List<APSentence> testSents;

    @Before
    public void init() throws Exception {
        CoreNLPManager.init();
        APDocument testDoc;
        testDoc = new APDocument(1, "The Scottish school leavers cohort provides population-wide prospective follow-up of local authority secondary school leavers in Scotland through linkage of comprehensive education data with hospital and mortality records. It considers educational attainment as a proxy for socioeconomic position in young adulthood and enables the study of associations and causal relationships between educational attainment and health outcomes in young adulthood.Education data for 284 621 individuals who left a local authority secondary school during 2006/2007-2010/2011 were linked with birth, death and hospital records, including general/acute and mental health inpatient and day case records. Individuals were followed up from date of school leaving until September 2012. Age range during follow-up was 15 years to 24 years.Education data included all formal school qualifications attained by date of school leaving; sociodemographic information; indicators of student needs, educational or non-educational support received and special school unit attendance; attendance, absence and exclusions over time and school leaver destination. Area-based measures of school and home deprivation were provided. Health data included dates of admission/discharge from hospital; principal/secondary diagnoses; maternal-related, birth-related and baby-related variables and, where relevant, date and cause of death. This paper presents crude rates for all-cause and cause-specific deaths and general/acute and psychiatric hospital admissions as well as birth outcomes for children of female cohort members.This study is the first in Scotland to link education and health data for the population of local authority secondary school leavers and provides access to a large, representative cohort with the ability to study rare health outcomes. There is the potential to study health outcomes over the life course through linkage with future hospital and death records for cohort members. The cohort may also be expanded by adding data from future school leavers. There is scope for linkage to the Prescribing Information System and the Scottish Primary Care Information Resource.");
        testDoc.hatch();
        testSents = testDoc.getSentences();

    }

    @Test
    public void generateParseTree() throws Exception {

        APSentence aSentence = testSents.get(0);
        aSentence.generateParseTree();
        Assert.assertNotEquals(aSentence.getParseTree().size(), 0);
    }

    @Test
    public void generateDependencies() throws Exception {

        APSentence aSentence = testSents.get(0);
        aSentence.generateDependencies();
        Assert.assertNotEquals(aSentence.getDependencyRelations().size(), 0);
    }

}