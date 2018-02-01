package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.LabelObject;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.dictionary.CRResources;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LabelSetProcessorThread implements Runnable {


        private static final String LABEL_SET = "LABEL_SET";

        private static final String LABEL_HASH = "LABEL_HASH";

        private DB labelDB;

        // CLEAN LABEL -> CONCEPT HASH
        private Map<String, Integer> labelMap;

        private Map<String, Boolean> labelProcessStatus;

        // CLEAN LABEL HASH -> TA LABEL OBJECT
        private Map<Integer, LabelObject> labelSet;

        public LabelSetProcessorThread(Map<String, Integer> labelMap,
                                       Map<String, Boolean> labelProcessStatus,
                                       CRResources taResources) {
            this.labelMap = labelMap;
            this.labelProcessStatus = labelProcessStatus;
            this.labelDB = DBMaker.memoryDB().make();
            this.labelSet = this.labelDB.treeMapCreate(LABEL_SET).make();
        }

        @Override
        public void run() {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            for (String label : labelMap.keySet()) {
                executor.execute(new LabelProcessThread(label, labelProcessStatus.get(label),  PseudoTA.getCrResources(), labelSet));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
        }

        public Map<Integer, LabelObject> getLabelSet() {
            return labelSet;
        }

        public void close() {
            this.labelDB.delete(LABEL_SET);
            this.labelDB.delete(LABEL_HASH);
            this.labelDB.close();
        }


}
