package au.org.garvan.kccg.annotations.pipeline.engine.connectors;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.AnnotationHit;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.AnnotationTerm;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.MeshHeading;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import com.amazonaws.services.dynamodbv2.document.api.ListTablesApi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AffinityConnectorTest {

    AffinityConnector affinityConnector = new AffinityConnector();
    APDocument apDocument;
    @Before
    public void init(){
        apDocument= new APDocument("Angiosarcoma is a rare vascular soft tissue tumor of endothelial origin most commonly seen in the elderly as a primary cutaneous head and neck malignancy. Furthermore, a peritoneal angiosarcoma is an exceedingly rare entity. This is the second case of primary peritoneal angiosarcoma reported in literature that is not associated with prior radiotherapy. Herein, we describe a case of primary peritoneal angiosarcoma metastatic to both the liver and bone in a male patient with metachronous renal cell carcinoma and parathyroid adenoma.");
//        apDocument.hatch(1);
        apDocument.setCleanedText(apDocument.getOriginalText());
    }
    @Test
    public void annotateAbstract() {
            List<AnnotationHit> hits = affinityConnector.annotateAbstract(apDocument.getCleanedText(), 1, "end");

    }
}