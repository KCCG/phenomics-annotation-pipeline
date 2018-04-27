package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.disease.DiseaseHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DiseaseHandlerTest {

    DiseaseHandler diseaseHandler = new DiseaseHandler();
    @Test
    public void readFile() {
            diseaseHandler.readFile("mondo20180415.json");

    }


    @Test
    public void processAndUpdateDocument() {
        APDocument testDoc = new APDocument("Angiosarcoma is a rare vascular soft tissue tumor of endothelial origin most commonly seen in the elderly as a primary cutaneous head and neck malignancy. Furthermore, a peritoneal angiosarcoma is an exceedingly rare entity. This is the second case of primary peritoneal angiosarcoma reported in literature that is not associated with prior radiotherapy. Herein, we describe a case of primary peritoneal angiosarcoma metastatic to both the liver and bone in a male patient with metachronous renal cell carcinoma and parathyroid adenoma.");
        testDoc.getProcessingProfile().getAnnotationRequests().add(AnnotationType.DISEASE);
        testDoc.hatch(1);
    }
}
