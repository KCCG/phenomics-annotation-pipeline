package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.dictionary.CRResources;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class PseudoTA {
    private static final Logger logger = LoggerFactory.getLogger(PseudoTA.class);

    @Getter
    public static CRResources crResources;
    private static Properties properties;

    public static void init(String resourcesFolder){
        properties = createProperties(resourcesFolder);
        crResources = new CRResources(properties);
    }


    private static Properties createProperties(String resourcesFolder) {
        if (resourcesFolder == null) {
            logger.error("Unable to initialize TAPipeline. Resources folder is invalid.");
            return null;
        }

        if (!resourcesFolder.endsWith("/")) {
            resourcesFolder += "/";
        }
        File file = new File(resourcesFolder);
        if (!file.exists() && !file.isDirectory()) {
            logger.error("Unable to initialize TAPipeline. Resources folder does not exist.");
            return null;
        }

        Properties properties = new Properties();
//        file = new File(resourcesFolder + TAConstants.TA_MODEL_SENTENCE);
//        if (!file.exists()) {
//            logger.error("Unable to initialize TAPipeline. Sentence splitter model inexistent.");
//            return null;
//        }
//        file = new File(resourcesFolder + TAConstants.TA_MODEL_TOKEN);
//        if (!file.exists()) {
//            logger.error("Unable to initialize TAPipeline. Tokenizer model inexistent.");
//            return null;
//        }
//        file = new File(resourcesFolder + TAConstants.TA_MODEL_POS);
//        if (!file.exists()) {
//            logger.error("Unable to initialize TAPipeline. POS Tager model inexistent.");
//            return null;
//        }
//        file = new File(resourcesFolder + TAConstants.TA_MODEL_PARSER);
//        if (!file.exists()) {
//            logger.error("Unable to initialize TAPipeline. Parser model inexistent.");
//            return null;
//        }

//        properties.put(TAConstants.TA_MODEL_POS, resourcesFolder + TAConstants.TA_MODEL_POS);
//        properties.put(TAConstants.TA_MODEL_SENTENCE, resourcesFolder + TAConstants.TA_MODEL_SENTENCE);
//        properties.put(TAConstants.TA_MODEL_TOKEN, resourcesFolder + TAConstants.TA_MODEL_TOKEN);
//        properties.put(TAConstants.TA_MODEL_PARSER, resourcesFolder + TAConstants.TA_MODEL_PARSER);
        for (String fileName : TAConstants.DICT_LIST_FILES) {
            file = new File(resourcesFolder + fileName);
            if (file.exists()) {
                properties.put(TAConstants.dictForFile(fileName), resourcesFolder + fileName);
            } else {
                logger.error("Unable to initialize TAPipeline. Dictionary [" + fileName + "] does not exist.");
                return null;
            }
        }
        file = new File(resourcesFolder + TAConstants.DICT_SYNONYMS_FILE);
        if (file.exists()) {
            properties.put(TAConstants.DICT_SYNONYMS, resourcesFolder + TAConstants.DICT_SYNONYMS_FILE);
        } else {
            logger.error("Unable to initialize TAPipeline. Dictionary [" + TAConstants.DICT_SYNONYMS_FILE + "] does not exist.");
            return null;
        }

        return properties;
    }
}
