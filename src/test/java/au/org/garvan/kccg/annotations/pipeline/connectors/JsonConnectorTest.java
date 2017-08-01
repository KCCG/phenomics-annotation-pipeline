package au.org.garvan.kccg.annotations.pipeline.connectors;

import au.org.garvan.kccg.annotations.pipeline.Enums.CommonParams;
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


}