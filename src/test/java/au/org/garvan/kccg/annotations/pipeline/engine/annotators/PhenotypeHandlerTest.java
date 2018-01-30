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
        APDocument testDoc = new APDocument("To study the relation between CD266 rs763361 gene polymorphism and CD226 serum level and to evaluate their role in susceptibility and disease activity of RA in a cohort of Egyptian individuals. The serum level of CD226 was measured using a suitable ELISA kit and the CD226 rs763361 gene polymorphism was typed by PCR-RFLP for 112 RA patients and 100 healthy controls. Significant association with RA was found with CD226 T allele (OR (95%CI) = 1.6 (1.04-2.4), P = 0.032), and higher CD226 serum level (P = 0.001). Higher CD226 levels were associated with higher ESR values (P = 0.035), positive CRP (0.048), increased number of tender joints (P = 0.045), and higher DAS score (P = 0.035). Serum CD226 is an independent risk factor for the prediction of RA (P = 0.001). No correlations were found between the serum level of CD226 and different CD226 genotypes and also between them and RA activity grades. The CD226 T allele may be susceptibility risk factors for the development of RA and the higher serum level of CD226 may be involved in the pathogenesis of RA in Egyptian patients. The serum level of CD226 and not CD226 genotypes could be considered as an independent risk factor for the prediction of RA within healthy individuals and also for RA disease activity.");
        DocumentPreprocessor.preprocessDocument(testDoc);
        List<APSentence> apSentenceLIst = testDoc.getSentences();


        PhenotypeHandler phenotypeHandler = new PhenotypeHandler("/Users/ahmed/code/CR/hpo_cr/resources/", 1, true);

        Map<DataSourceMetadata, List<ConceptAnnotation>> result = phenotypeHandler.annotate(apSentenceLIst,true,true,true,true);

        int x = 0;





    }
}