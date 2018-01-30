package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.beans.ConceptAnnotation;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config.DataSourceMetadata;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PhenotypeHandlerTest {

    @Test
    public void process() {
        APDocument testDoc = new APDocument("(Contrary to BA 1, 250-min hyperglycemia caused no further improvement of the recovery rate.) ");
        DocumentPreprocessor.preprocessDocument(testDoc);
        List<APSentence> apSentenceLIst = testDoc.getSentences();


        PhenotypeHandler phenotypeHandler = new PhenotypeHandler("/Users/ahmed/code/CR/hpo_cr/resources/", 1, true);

        Map<DataSourceMetadata, List<ConceptAnnotation>> result = phenotypeHandler.annotate(apSentenceLIst,true,true,true,true);

        int x = 0;





    }

    @Test
    public void process2() {
        APDocument testDoc = new APDocument("(Contrary to BA 1, 250-min hyperglycemia caused no further improvement of the recovery rate.) However this comparison is characterized by the fact that both substances exhibit two quite different (complementary) mechanisms of action. ");
        DocumentPreprocessor.preprocessDocument(testDoc);
        List<APSentence> apSentenceLIst = testDoc.getSentences();


        PhenotypeHandler phenotypeHandler = new PhenotypeHandler("/Users/ahmed/code/CR/hpo_cr/resources/", 1, true);

        Map<DataSourceMetadata, List<ConceptAnnotation>> result = phenotypeHandler.annotate(apSentenceLIst,true,true,true,true);

        int x = 0;





    }
}