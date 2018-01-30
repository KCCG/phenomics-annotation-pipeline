package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.process.PseudoTA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class IndexGenerator {
    private static final Logger logger = LoggerFactory.getLogger(IndexGenerator.class);


    public static void main(String[] args) {
        //CR: CLean this mess.
        // Point

        PseudoTA.init("/Users/ahmed/code/CR/hpo_cr/resources/");
        String propFile = "/Users/ahmed/code/CR/hpo_cr/index.properties";// args[0];
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(new File(propFile)));

            IndexManager indexManager = IndexManager.getInstance(properties);
            if (indexManager.isValid()) {
                if (indexManager.initialize()) {
                    indexManager.index();
                    indexManager.close();
                } else {
                    logger.error("Unable to initialize the Index Manager. Aborting indexing process ...");
                }
            } else {
                logger.error("Unable to instantiate the Index Manager. Aborting indexing process ...");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


}
