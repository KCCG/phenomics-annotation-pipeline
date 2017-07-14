package au.org.garvan.kccg.annotations.pipeline.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.connectors.SolrConnector;
import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;



/**
 * Created by ahmed on 7/7/17.
 */
public class DocumentProcessor {


    private static SolrConnector instSolrConnector = new SolrConnector();


    public static void main(String[] args) throws IOException {

        CoreNLPHanlder.init();

        LocalDate fetchDate = LocalDate.of(2017,07,03);
        List<APDocument> collectedDocs = instSolrConnector.getDocuments(fetchDate);
        APDocument doc = collectedDocs.get(0);

        collectedDocs.get(0).hatch();

    }


}
