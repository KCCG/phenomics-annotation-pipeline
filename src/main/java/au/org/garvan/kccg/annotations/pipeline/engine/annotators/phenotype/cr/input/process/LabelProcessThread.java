package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.LabelObject;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.dictionary.CRResources;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APPhrase;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LabelProcessThread implements Runnable {
        private static final Logger logger = LoggerFactory.getLogger(LabelProcessThread.class);

        private String label;

        private boolean processExact;


        // CLEAN LABEL HASH -> TA LABEL OBJECT
        private Map<Integer, LabelObject> labelSet;

        public LabelProcessThread(String label, boolean processExact,  CRResources crResources,
                                      Map<Integer, LabelObject> labelSet) {
            this.label = label;
            this.labelSet = labelSet;
            this.processExact = processExact;
        }

        @Override
        public void run() {

            //CR: This was a label process thread. This is being changed to use the corenlp pipeline.
            if (!Strings.isNullOrEmpty(label)) {
                APPhrase labelPhrase = DocumentPreprocessor.preprocessPhrase(label);
                List<APToken> tokens = labelPhrase.getTokens();
                List<APToken> resultList = new ArrayList<APToken>();

                for (APToken token : tokens) {
                    String pos = token.getPartOfSpeech();
                    String dictPos = PseudoTA.getCrResources().getSimpleDictionary(TAConstants.DICT_POS).getValue(token.getOriginalText().toLowerCase());
                    if (!pos.equalsIgnoreCase(dictPos)) {
                        pos = dictPos;
                    }
                    if (TAConstants.POS_EXCLUDE.containsKey(pos)) {
                        continue;
                    }

                    TokenCleaner tokenCleaner = new TokenCleaner(token.getOriginalText(), pos, true, processExact);
                    if (tokenCleaner.isValid()) {
                        if (tokenCleaner.isSplit()) {
                            List<APToken> tokenList = tokenCleaner.getTokens();
                            for (APToken t : tokenList) {
                                resultList.add(t);
                            }
                        } else {
                            resultList.add(tokenCleaner.getSingleToken());
                        }
                    }

                }
                labelSet.put(label.hashCode(), new LabelObject(label, resultList));
                logger.debug(" - AFTER [" + label + "] => " + resultList);
            }
            else{
                logger.debug("Empty Label");


            }

    }


}
