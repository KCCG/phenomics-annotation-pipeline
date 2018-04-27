package au.org.garvan.kccg.annotations.pipeline.engine.annotators.disease;

import org.junit.Test;

import static org.junit.Assert.*;

public class IndexGeneratorTest {

    IndexGenerator diseaseIndexGenerator = new IndexGenerator();

    @Test
    public void generateIndex() {

        diseaseIndexGenerator.generateIndex("mondo20180415.json");
    }
}