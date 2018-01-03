package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.BaseConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.connectors.JsonConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.CommonParams;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import edu.stanford.nlp.util.ArrayMap;
import org.json.simple.JSONObject;
import org.junit.Before;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by ahmed on 30/10/17.
 */
public class GraphDBHandlerTest {


    private final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBHandlerTest.class);
    @Autowired
    private GraphDBHandler gdb;
    private BaseConnector testConnector;
    private List<Article> articles;

//    @Before
//    public void init() {
//
//        testConnector = new JsonConnector();
//        articles =  testConnector.getArticles("test1000.json", CommonParams.FILENAME);
//        gdb = new GraphDBHandler("http://localhost:7474/","", "");
//    }
//
//
//    @Test
//    public void fetchArticlesWithGeneOne() throws Exception {
//        Map<SearchQueryParams, Object> params = new HashMap<>();
//        params.put(SearchQueryParams.GENES, new Pair<>("OR" ,Arrays.asList("TNF")));
//        gdb.fetchArticles(params);
//    }

//    @Test
//    public void fetchArticlesWithGeneTwo() throws Exception {
//        Map<SearchQueryParams,Object> params = new HashMap<>();
//        params.put(SearchQueryParams.GENES,Arrays.asList("PMS2", "MSH6"));
//        gdb.fetchArticles(params);
//
//    }
//    @Test
//    public void fetchArticlesWithGeneThree() throws Exception {
//        Map<SearchQueryParams,Object> params = new HashMap<>();
//        params.put(SearchQueryParams.GENES,Arrays.asList("PMS2", "MSH6","MSH2"));
//        gdb.fetchArticles(params);
//
//    }
//
//    @Test
//    public void fetchArticlesWithPublication() throws Exception {
//        Map<SearchQueryParams,Object> params = new HashMap<>();
//        Publication pub = new Publication();
//        pub.setIssnNumber("1932-6203");
//        params.put(SearchQueryParams.PUBLICATION, pub);
//        gdb.fetchArticles(params);
//
//    }
//    @Test
//    public void fetchArticlesWithPublicationAndGene() throws Exception {
//        Map<SearchQueryParams,Object> params = new HashMap<>();
//        Publication pub = new Publication();
//        pub.setIssnNumber("1932-6203");
//        params.put(SearchQueryParams.PUBLICATION, pub);
//        params.put(SearchQueryParams.GENES, Arrays.asList("T"));
//        gdb.fetchArticles(params);
//
//    }
//    @Test
//    public void createArticleQuery() throws Exception {
//
//        int index = 0;
//        for (Article a: articles.subList(0,300))
//        {
//            slf4jLogger.info(String.format("Processing Article index:%d and ID:%d", index, a.getPubMedID()));
//            a.getArticleAbstract().hatch();
//            if(a.getAbstractEntities().containsKey("annotations"))
//                gdb.createArticleQuery(a);
//            index ++;
//        }
//
//    }




}