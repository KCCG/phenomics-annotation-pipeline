//package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input;
//
//import java.io.File;
//import java.util.Collections;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.dictionary.TASimpleDictionary;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate.MatrixBasedCandidateGenerator;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate.NCCandidateGenerator;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.patterns.InputProcessorPatternsReader;
//
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.StatsUtil;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;
//
//public class InputProcessor {
//
//	private static final Logger logger = LoggerFactory.getLogger(InputProcessor.class);
//
//	private boolean valid = false;
//
//	private int maxThreads;
//	private boolean processNC;
//	private TAPipeline taPipeline;
//
//	private InputProcessorPatternsReader patternsReader;
//	private MatrixBasedCandidateGenerator matrixBasedGenerator;
//	private NCCandidateGenerator phenoCandidateGenerator;
//
//	private Map<String, Double> stats;
//
//	public InputProcessor(String resourcesFolder, int maxThreads, boolean processNC, TAPipeline taPipeline) {
//		this.maxThreads = maxThreads;
//		this.processNC = processNC;
//		this.taPipeline = taPipeline;
//		this.stats = new LinkedHashMap<String, Double>();
//
//		this.valid = initComponents(resourcesFolder);
//	}
//
//	private boolean initComponents(String resourcesFolder) {
//		File file = new File(resourcesFolder + TAConstants.INPUTPROC_PATTERN_FILE);
//		if (!file.exists()) {
//			logger.error("Failed to initialize Input Processor: Pattern file [" + resourcesFolder + TAConstants.INPUTPROC_PATTERN_FILE + "] not found.");
//			return false;
//		}
//		patternsReader = new InputProcessorPatternsReader(resourcesFolder + TAConstants.INPUTPROC_PATTERN_FILE);
//		if (patternsReader.isValid()) {
//			matrixBasedGenerator = new MatrixBasedCandidateGenerator(maxThreads, patternsReader.getGeneralPatterns());
//			phenoCandidateGenerator = new NCCandidateGenerator(patternsReader.getNcPatterns());
//			logger.info("Input Processor initialized. Using " + maxThreads + " threads.");
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//	public void close() {
//		patternsReader.close();
//	}
//
//	public TASimpleDictionary getDictionary(String dictionary) {
//		return TAPipeline.taResources.getSimpleDictionary(dictionary);
//	}
//
//	public ProcessedInput process(String text) {
//		Map<Integer, TASentence> sentenceMap = Collections.synchronizedMap(new LinkedHashMap<Integer, TASentence>());
//		ProcessedInput processedInput = new ProcessedInput();
//		double sTime = System.currentTimeMillis();
//		TATextProcessor textProcessor = taPipeline.textProcessor(text, sentenceMap);
//		textProcessor.run();
//	    double eTime = System.currentTimeMillis();
//	    this.stats.put(StatsUtil.SENTENCE_PROC_TIME, (eTime - sTime));
//	    this.stats.put(StatsUtil.NO_SENTENCES, (double) sentenceMap.size());
//
//		sTime = System.currentTimeMillis();
//		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
//		for (int sentIndex : sentenceMap.keySet()) {
//			executor.execute(new InputProcessorThread(sentenceMap.get(sentIndex), processedInput, matrixBasedGenerator, processNC, phenoCandidateGenerator));
//		}
//		executor.shutdown();
//	    while (!executor.isTerminated()) {
//	    }
//	    eTime = System.currentTimeMillis();
//	    this.stats.put(StatsUtil.INPUT_PROC_TIME, (eTime - sTime));
//
//	    return processedInput;
//	}
//
//	public Map<String, Double> getStats() {
//		return stats;
//	}
//
//	public boolean isValid() {
//		return valid;
//	}
//}
