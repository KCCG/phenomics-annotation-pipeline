package au.org.garvan.kccg.annotations.pipeline.connectors;

import au.org.garvan.kccg.annotations.pipeline.Enums.CommonParams;
import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by ahmed on 25/7/17.
 */
public interface BaseConnector {

    List<APDocument> getDocuments(String param, CommonParams type);
    List<APDocument> getDocuments(LocalDate date) throws IOException;
    APDocument getDocument(String PMID) throws IOException;



}
