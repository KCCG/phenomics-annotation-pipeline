package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.dictionary;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//CR:DONE TAResources is migrated here
public class CRResources {

        private static final Logger logger = LoggerFactory.getLogger(CRResources.class);

        private TAMultipleDictionary synonymDictionary;

        private Map<String, TASimpleDictionary> simpleDictionaries;

        public CRResources(Properties properties) {
            simpleDictionaries = new HashMap<String, TASimpleDictionary>();
            loadResources(properties);
        }

        private void loadResources(Properties properties) {
            for (Object key : properties.keySet()) {
                String dict = (String) key;
                String dictFile = properties.getProperty(dict);
                if (dict.equalsIgnoreCase(TAConstants.DICT_SYNONYMS)) {
                    loadSynonymDictionary(dictFile);
                } else {
                    if (TAConstants.DICT_LIST.contains(dict)) {
                        loadDictionary(dict, dictFile);
                    }
                }
            }
        }

        private void loadSynonymDictionary(String dictFile) {
            synonymDictionary = new TAMultipleDictionary();
            try {
                logger.info("Found dictionary at: " + dictFile);
                if (synonymDictionary.load(dictFile, TAConstants.DICT_SYNONYMS)) {
                    logger.info("Dictionary successfully loaded.");
                } else {
                    logger.error("Unable to load dictionary: " + dictFile);
                }
            } catch (Exception e) {
                logger.error("Unable to load dictionary [" + dictFile + "]: " + e.getMessage(), e);
            }
        }

        private void loadDictionary(String dict, String dictFile) {
            TASimpleDictionary dictionary = new TASimpleDictionary();
            try {
                logger.info("Found dictionary [" + dict + "] at: " + dictFile);
                if (dictionary.load(dictFile, dict)) {
                    logger.info("Dictionary [" + dict + "] successfully loaded.");
                    simpleDictionaries.put(dict, dictionary);
                } else {
                    logger.error("Unable to load dictionary: " + dictFile);
                }
            } catch (Exception e) {
                logger.error("Unable to load dictionary [" + dictFile + "]: " + e.getMessage(), e);
            }
        }

        public TASimpleDictionary getSimpleDictionary(String dict) {
            return simpleDictionaries.get(dict);
        }

        public List<String> getSynonym(String entry) {
            return synonymDictionary.getListValue(entry);
        }

        public void close() {
            synonymDictionary.close();
            for (TASimpleDictionary dictionary : simpleDictionaries.values()) {
                dictionary.close();
            }
        }


}
