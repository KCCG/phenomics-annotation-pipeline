package au.org.garvan.kccg.annotations.pipeline.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.connectors.BaseConnector;
import au.org.garvan.kccg.annotations.pipeline.connectors.JsonConnector;
import au.org.garvan.kccg.annotations.pipeline.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.enums.CommonParams;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 30/10/17.
 */
public class GraphDBHandlerTest {
    private GraphDBHandler gdb;
    private BaseConnector testConnector;
    private List<Article> articles;

    @Before
    public void init() {

        gdb = new GraphDBHandler();
        testConnector = new JsonConnector();
        articles =  testConnector.getArticles("1500949368578.json", CommonParams.FILENAME);

    }
        @Test
    public void createArticleQuery() throws Exception {

        for (Article a: articles)
        {
            a.getArticleAbstract().hatch();
            gdb.createArticleQuery(a);
        }

    }

}