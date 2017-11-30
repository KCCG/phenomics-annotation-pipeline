package au.org.garvan.kccg.annotations.pipeline.engine.lexicons;

import au.org.garvan.kccg.annotations.pipeline.engine.lexicons.GenesHandler;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ahmed on 5/10/17.
 */
public class GenesHandlerTest {
    @Before
    public void setUp() throws Exception {
    }



    @Test
    public void loadGenes() throws Exception {
        GenesHandler aGeneHandler = new GenesHandler("genes.txt");
        aGeneHandler.loadGenes();


    }

}