package au.org.garvan.kccg.annotations.pipeline.connectors;

import au.org.garvan.kccg.annotations.pipeline.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.enums.CommonParams;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by ahmed on 25/7/17.
 */
public interface BaseConnector {

    List<APDocument> getDocuments(String param, CommonParams type)throws IOException;
    List<APDocument> getDocuments(LocalDate date) throws IOException;
    APDocument getDocument(String PMID) throws IOException;


    List<Article> getArticles(String param, CommonParams type);




}
