package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process.LabelSetProcessorThread;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process.PseudoTA;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.DataSourceMetadata;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.GeneralUtil;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.IndexConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TimeUtil;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexEntryCreator {

	private static final Logger logger = LoggerFactory.getLogger(IndexEntryCreator.class);

	// CLEAN LABEL -> CONCEPT HASH
	private Map<String, Integer> labelMap;

	private Map<String, Boolean> labelProcessStatus;

	private IndexProcessor indexProcessor;
	
//	private TAPipeline taPipeline;
	
	private DB labelDB;

	public IndexEntryCreator(IndexProcessor indexProcessor) {
		this.indexProcessor = indexProcessor;
//		this.taPipeline = taPipeline;

		this.labelDB = DBMaker.memoryDB().transactionDisable().make();
		this.labelMap = labelDB.treeMapCreate(IndexConstants.LABEL_MAP).make();
		this.labelProcessStatus = labelDB.treeMapCreate(IndexConstants.LABEL_STATUS_MAP).make();
	}

	public void index(IndexEntry indexEntry) {
		logger.debug("Generated document for [" + indexEntry.getUri() + "]");
		DS_ConceptInfo conceptInfo = new DS_ConceptInfo(indexEntry.getUri(), indexEntry.getLabel(), indexEntry.getType());
		cleanAndAddLabel(indexEntry.getLabel(),
                conceptInfo.getUri(),
                conceptInfo.getUri().hashCode(),
                indexEntry.isProcessExact(),
                indexEntry.getStopWords(),
                indexEntry.getExpansionWords());
		
		for (String originalLabel : indexEntry.getSynonyms()) {
			conceptInfo.addAlternativeLabel(originalLabel);
			cleanAndAddLabel(originalLabel,
                    conceptInfo.getUri(),
                    conceptInfo.getUri().hashCode(),
                    indexEntry.isProcessExact(),
                    indexEntry.getStopWords(),
                    indexEntry.getExpansionWords());
		}
		indexProcessor.addConcept(conceptInfo);
	}

	private void cleanAndAddLabel(String label, String uri, int conceptHash, boolean exact, List<String> stopWords, Map<String, List<String>> expansionWords) {
		String cleanLabel = exact ? label.toLowerCase() : GeneralUtil.removeBrackets(label.toLowerCase()).toLowerCase();
		if (!labelMap.containsKey(cleanLabel)) {
			logger.debug("URI[" + uri + "]: " + cleanLabel);
			labelMap.put(cleanLabel, uri.hashCode());
			labelProcessStatus.put(cleanLabel, exact);
			
			List<String> alternatives = new LabelGenerator(cleanLabel, stopWords, expansionWords).getAlternatives();
			logger.debug("URI[" + uri + "]: ALTERNATIVES: " + alternatives);
			for (String alternative : alternatives) {
				alternative = alternative.trim().toLowerCase();
				if (!labelMap.containsKey(alternative)) {
					labelMap.put(alternative, conceptHash);
					labelProcessStatus.put(alternative, exact);
				}					
			}
		} else {
			logger.warn("Label [" + cleanLabel + "] already exists !");
		}
	}
	
	public void setMetadata(DataSourceMetadata metadata) {
		indexProcessor.setMetadata(metadata);
	}

	public void process() {
		double sTime = TimeUtil.start();
		logger.info("Compacting  concept info...");
		indexProcessor.commit();
		logger.info("TA pipeline done [" + TimeUtil.end(sTime) + "] ...");

		// STEP 1: Run TA
		sTime = TimeUtil.start();
		logger.info("Running TA pipeline ...");
		ExecutorService executor = Executors.newFixedThreadPool(1);
		LabelSetProcessorThread labelSetProcThread = new LabelSetProcessorThread(labelMap, labelProcessStatus, PseudoTA.getCrResources());
		executor.execute(labelSetProcThread);
		executor.shutdown();
	    while (!executor.isTerminated()) {
	    }
		logger.info("TA pipeline done [" + TimeUtil.end(sTime) + "] ...");


		// STEP 2: Build vocabulary
		logger.info("Building vocabulary ...");
		sTime = TimeUtil.start();

		indexProcessor.createTokenSet(labelSetProcThread.getLabelSet());
	    indexProcessor.consolidate();
		logger.info("Vocabulary built [" + TimeUtil.end(sTime) + "] ...");

		// RECREATE label to symbol map

		logger.info("Building label map ... ");
		sTime = TimeUtil.start();
		indexProcessor.buildLabelMap(labelMap, labelSetProcThread.getLabelSet());
		logger.info("Label map built [" + TimeUtil.end(sTime) + "] ...");

	    labelSetProcThread.close();
	}

	public void close() {
		this.labelDB.delete(IndexConstants.LABEL_MAP);
		this.labelDB.delete(IndexConstants.LABEL_STATUS_MAP);
		this.labelDB.close();
	}

}
