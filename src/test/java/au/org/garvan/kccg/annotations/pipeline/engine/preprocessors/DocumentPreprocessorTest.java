package au.org.garvan.kccg.annotations.pipeline.engine.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ahmed on 1/8/17.
 */
public class DocumentPreprocessorTest {

    APDocument doc ;

    @Before
    public void setUp() throws Exception {
        doc = new APDocument(0, " This is a text doc.And written for preprocessing");
    }

    @Test
    public void preprocessDocument() throws Exception {

        DocumentPreprocessor.preprocessDocument(doc);
    }

}