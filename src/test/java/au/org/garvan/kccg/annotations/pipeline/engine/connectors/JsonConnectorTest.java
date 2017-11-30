package au.org.garvan.kccg.annotations.pipeline.engine.connectors;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.BaseConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.connectors.JsonConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.CommonParams;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ahmed on 25/7/17.
 */
public class JsonConnectorTest {


    BaseConnector testConnector = new JsonConnector();

    @Before
    public void init(){


    }

    @Test
    public void getDocuments() throws Exception {
        testConnector.getDocuments("1500949368578.json", CommonParams.FILENAME);
    }

    @Test
    public void getArticles() throws Exception {
        testConnector.getArticles("1500949368578.json", CommonParams.FILENAME);

    }




}