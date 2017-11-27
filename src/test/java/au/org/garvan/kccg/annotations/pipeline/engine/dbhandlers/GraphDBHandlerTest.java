package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.BaseConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.connectors.JsonConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.CommonParams;
import org.junit.Before;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by ahmed on 30/10/17.
 */
public class GraphDBHandlerTest {
    private final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBHandlerTest.class);
    private GraphDBHandler gdb;
    private BaseConnector testConnector;
    private List<Article> articles;

    @Before
    public void init() {

        gdb = new GraphDBHandler();
        testConnector = new JsonConnector();
        articles =  testConnector.getArticles("test1000.json", CommonParams.FILENAME);

    }
//    @Test
//    public void createArticleQuery() throws Exception {
//
//        int index = 0;
//        for (Article a: articles)
//        {
//            slf4jLogger.info(String.format("Processing Article index:%d and ID:%d", index, a.getPubMedID()));
//            a.getArticleAbstract().hatch();
//            JSONObject temp = a.constructJson();
//            gdb.createArticleQuery(a);
//            index ++;
//        }
//
//    }

}