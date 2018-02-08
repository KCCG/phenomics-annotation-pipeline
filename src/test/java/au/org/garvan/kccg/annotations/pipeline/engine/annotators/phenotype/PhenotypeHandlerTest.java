package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype;


import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import org.junit.Test;

public class PhenotypeHandlerTest {

    @Test
    public void process() {
        APDocument testDoc = new APDocument("Degloving injuries are surgical conditions in which an extensive portion of skin and subcutaneous tissue is detached from the underlying fasciae, muscles, or bone surface. Frequently, there is an association of fracture underlying the degloved area. We aimed to compare the short-term outcomes of degloving injuries with and without underlying fracture. A prospective cohort study was conducted. We recruited patients with degloving injuries, and followed them up for 30 days to assess the outcomes. We collected data on socio-demography, cause and mechanism of injury, presence of underlying fracture, presence of shock at admission, injury severity score, location and size of degloving injuries, their management, and short-term outcomes. There were two comparison groups of degloving injuries based on the presence or absence of underlying fracture. We analyzed the differences between the two groups by using Fisher exact test for categorical variables and Student's t test for continuous variables; p values < 0.05 were considered to be significant. Risk ratio was calculated for the short-term outcomes. There were 1.56% (n = 51) of degloving injuries among 3279 admitted trauma patients during the study period of 5 months; 1% (n = 33) with and 0.56% (n = 18) without underlying fracture. For the overall degloving injuries, male-female ratio was 2 and mean age was 28.8 years; they were caused by road traffic crashes in 84%, and resulted in shock at admission in 29%. In the group with underlying fracture, lower limbs were frequently affected in 45% (p = 0.0018); serial debridement and excision of the avulsed flap were the most performed surgical procedures in 22% (p = 0.0373) and 14% (p = 0.0425), respectively; this same group had 3.9 times increased risk of developing poor outcomes (mainly infections) after 30 days and longer hospital stay (26.52 ± 31.31 days, p = 0.0472). Degloving injuries with underlying fracture are frequent in the lower limbs, and have increased risk of poor short-term outcomes and longer hospital stay. We recommend an early plastic surgery review at admission of patients with degloving injuries with underlying fracture to improve the flap viability and reduce the infection risk.");
        DocumentPreprocessor.preprocessDocument(testDoc, testDoc.getId());
        PhenotypeHandler phenotypeHandler = new PhenotypeHandler();
        phenotypeHandler.processAndUpdateDocument(testDoc);
        int x = 0;

    }

    @Test
    public void process2() {
        APDocument testDoc = new APDocument("To examine the effect and mechanism of Roux-en-Y gastric bypass (RYGB) on the improvement of diabetes according to the length of anastomosis and the gastric pouch volume in an animal model.Glucose intolerance was induced with a high-fat diet for 3 months in Sprague-Dawley rats. The animals were subjected to conventional RYGB (cRYGB; 5% gastric pouch with 15-cm Roux limb, 40-cm biliopancreatic limb; n = 9), short-limb RYGB (sRYGB; 5%, 8, 4 cm; n = 9), fundus-sparing RYGB (fRYGB; 30%, 8, 4 cm; n = 9), or sham operation (n = 9). After 6 weeks, oral glucose tolerance tests (OGTTs) were performed, and gut hormones including insulin, total GLP-1, GIP, and ghrelin were analyzed.The cRYGB group showed significantly decreased food intake, body weight, and random glucose (p < 0.05). sRYGB resulted in a similar change of body weight loss to that of cRYGB, but with no improvement of hyperglycemia. The fRYGB group showed similar changes of body weight and random glucose to those of the sham group. In cRYGB and sRYGB, the level of insulin steeply increased until 30 min during OGTT. GLP-1 was higher at 30 min in cRYGB than in other groups, without significance. The fRYGB group showed a slowly increasing pattern in OGTT and GLP-1, and the lowest peak point in insulin and GIP.cRYGB with 95% gastric resection was needed to achieve not only weight loss but also diabetes improvement, which could be related to the increase in GLP-1.");
        DocumentPreprocessor.preprocessDocument(testDoc, testDoc.getId());
        PhenotypeHandler phenotypeHandler = new PhenotypeHandler();
        phenotypeHandler.processAndUpdateDocument(testDoc);
        int x = 0;

    }

    @Test
    public void process3() {
        APDocument testDoc = new APDocument("Klotho is a transmembrane protein and acts as an upstream modulator of insulin-like growth factor-1 receptor (IGF-1R) signaling, which was indicated to be involved in the pathogenesis of solid cancer and hematological malignancies, including T‑cell lymphoma. Although Klotho was recently identified as a tumor suppressor in several types of human malignancies, the potential role of Klotho in T‑cell lymphoma has not been reported. In the present study, we investigated the expression level and the molecular events of Klotho in T‑cell lymphoma. Significantly lower expression of Klotho was observed in T‑cell lymphoma patient samples compared to normal lymph nodes. Functional analysis after Klotho overexpression revealed significantly inhibited tumor cell viability in T‑cell lymphoma. Moreover, apoptosis of T‑cell lymphoma cells were induced after transfected with Klotho-overexpressing vectors. Forced expression of Klotho resulted in decline of activation of IGF-1R signaling, accompanied by decreased phosphorylation of its downstream targets, including AKT and ERK1/2. These data indicated that Klotho acts as a tumor suppressor via inhibiting IGF-1R signaling, thus suppressing the viability and promoting apoptosis in T‑cell lymphoma. Taken together, Klotho may serve as a potential target for the therapeutic intervention of T‑cell lymphoma.");
        DocumentPreprocessor.preprocessDocument(testDoc, testDoc.getId());
        PhenotypeHandler phenotypeHandler = new PhenotypeHandler();
        phenotypeHandler.processAndUpdateDocument(testDoc);
        int x = 0;

    }
}