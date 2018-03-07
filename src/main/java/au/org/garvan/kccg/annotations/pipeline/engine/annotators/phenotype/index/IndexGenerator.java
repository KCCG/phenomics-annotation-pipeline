package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process.PseudoTA;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.CRConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class IndexGenerator {
    private static final Logger logger = LoggerFactory.getLogger(IndexGenerator.class);


    public static void main(String[] args) {
        //CR: Clean this mess.

        String path = "";

        try {
            File folder = new ClassPathResource(CRConstants.RESOURCES_PATH).getFile();
            logger.info(folder.getAbsolutePath());
            path = folder.getAbsolutePath()+"/";
        } catch (IOException e) {
            e.printStackTrace();
        }

        PseudoTA.init(path);
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
