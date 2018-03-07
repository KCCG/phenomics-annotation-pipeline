package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.dictionary.TASimpleDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TimeUtil;

public class IndexSearchExecutor {

	private static final Logger logger = LoggerFactory.getLogger(IndexSearchExecutor.class);

	private int maxThreads;
	private Map<String, List<String>> vocabulary;
	private Map<String, ConceptCandidate> conceptCandidates;
	private Map<Integer, String> symbolMap;
	private Map<Integer, DS_ConceptInfo> conceptInfoMap;
	private NavigableSet<Object[]> documentMap;
	private NavigableSet<Object[]> documentSizeMap;
	private TASimpleDictionary lexVarDictionary;
	
	private Map<ConceptCandidate, DS_ConceptInfo> annotatedConcepts;
	
	public IndexSearchExecutor(int maxThreads, Map<String, List<String>> vocabulary, 
			TASimpleDictionary lexVarDictionary, Map<String, ConceptCandidate> conceptCandidates, 
			Map<Integer, String> symbolMap, 
			Map<Integer, DS_ConceptInfo> conceptInfoMap, 
			NavigableSet<Object[]> documentMap, 
			NavigableSet<Object[]> documentSizeMap) {
		this.maxThreads = maxThreads;
		this.vocabulary = vocabulary;
		this.conceptCandidates = conceptCandidates;
		this.symbolMap = symbolMap;
		this.conceptInfoMap = conceptInfoMap;
		this.documentMap = documentMap;
		this.documentSizeMap = documentSizeMap;
		this.lexVarDictionary = lexVarDictionary;
		
		annotatedConcepts = Collections.synchronizedMap(new HashMap<ConceptCandidate, DS_ConceptInfo>());
		search();
	}
	
	private void search() {
		double sTime = TimeUtil.start();
		Map<String, String> symbols = lookupSymbols(vocabulary);
		
		logger.debug("Symbol map: " + symbols);
		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
		for (String s : conceptCandidates.keySet()) {
			executor.execute(new ConceptSearchThread(conceptCandidates.get(s), symbols, annotatedConcepts, conceptInfoMap, documentMap, documentSizeMap));
		}
		executor.shutdown();
	    while (!executor.isTerminated()) {
	    }
		logger.debug("Search run [" + TimeUtil.end(sTime) + "]");
	}
	
	private Map<String, String> lookupSymbols(Map<String, List<String>> vocabulary) {
		Map<String, String> symbols = Collections.synchronizedMap(new HashMap<String, String>());
		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
		for (String token : vocabulary.keySet()) {
			executor.execute(new SymbolLookupThread(token, vocabulary, symbolMap, lexVarDictionary, symbols));
		}
		executor.shutdown();
	    while (!executor.isTerminated()) {
	    }
		return symbols;
	}

	public Map<ConceptCandidate, DS_ConceptInfo> getConceptCandidates() {
		return annotatedConcepts;
	}
	
}
