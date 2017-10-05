package au.org.garvan.kccg.annotations.pipeline.lexicons;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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