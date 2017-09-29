package au.org.garvan.kccg.annotations.pipeline.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APDocument;
import au.org.garvan.kccg.annotations.pipeline.processors.DocumentProcessor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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