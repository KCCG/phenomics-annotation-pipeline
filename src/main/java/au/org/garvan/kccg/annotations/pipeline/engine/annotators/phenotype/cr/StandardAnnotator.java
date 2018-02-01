//package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.beans.ConceptAnnotation;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.InputProcessor;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.ProcessedInput;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.negation.NegationDetection;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.relations.FilterPhenotypes;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.relations.RelationDetection;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.spellcheck.CRSpellCheck;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.spellcheck.SpellCheckThread;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.search.IndexSearch;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.DataSourceMetadata;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.ConceptType;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.StatsUtil;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;
//
//public class StandardAnnotator {
//
//	private static final Logger logger = LoggerFactory.getLogger(StandardAnnotator.class);
//
//	private int maxThreads;
//	private InputProcessor inputProcessor;
//	private IndexSearch indexSearch;
//	private CRSpellCheck spellCheck;
//
//	private Map<String, Double> stats;
//
//	public StandardAnnotator(int maxThreads, InputProcessor inputProcessor, IndexSearch indexSearch, CRSpellCheck spellCheck) {
//		this.maxThreads = maxThreads;
//		this.inputProcessor = inputProcessor;
//		this.indexSearch = indexSearch;
//		this.spellCheck = spellCheck;
//		this.stats = new LinkedHashMap<String, Double>();
//	}
//
//	public Map<DataSourceMetadata, List<ConceptAnnotation>> annotate(String text, boolean longestMatch, boolean spellCheck, boolean detectNegation, boolean addRelations) {
//		ProcessedInput pInput = inputProcessor.process(text);
//		this.stats.putAll(inputProcessor.getStats());
//
//		double sTime = System.currentTimeMillis();
//		Map<String, List<String>> vocabulary = doSpellCheck(pInput, spellCheck);
//		double eTime = System.currentTimeMillis();
//		this.stats.put(StatsUtil.VOCAB_PROC_TIME, (eTime - sTime));
//
//		Map<DataSourceMetadata, Map<ConceptCandidate, DS_ConceptInfo>> dsConceptCandidates = indexSearch.search(vocabulary, inputProcessor.getDictionary(TAConstants.DICT_LEXVAR), pInput.getConceptCandidates());
//		this.stats.putAll(indexSearch.getStats());
//
//		sTime = System.currentTimeMillis();
//		Map<DataSourceMetadata, List<ConceptAnnotation>> conceptAnnotationResults = new HashMap<DataSourceMetadata, List<ConceptAnnotation>>();
//		for (DataSourceMetadata ds : dsConceptCandidates.keySet()) {
//			List<ConceptAnnotation> results = process(dsConceptCandidates.get(ds), longestMatch, spellCheck, detectNegation, addRelations);
//			conceptAnnotationResults.put(ds, results);
//		}
//		eTime = System.currentTimeMillis();
//		this.stats.put(StatsUtil.POST_PROC_TIME, (eTime - sTime));
//		return conceptAnnotationResults;
//	}
//
//	private List<ConceptAnnotation> process(Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates, boolean longestMatch, boolean spellCheck,
//			boolean detectNegation, boolean addRelations) {
//		if (detectNegation) {
//			conceptCandidates = new NegationDetection(conceptCandidates).getCandidates();
//		}
//		List<ConceptAnnotation> annotations = new ArrayList<ConceptAnnotation>();
//		if (addRelations) {
//			RelationDetection relDetect = new RelationDetection(conceptCandidates);
//			annotations = relDetect.getMapping().isEmpty() ?
//					transform(relDetect.getCandidates()) :
//					transformRelations(relDetect.getCandidates(), relDetect.getDsCandidates(), relDetect.getMapping());
//		} else {
//			annotations = transform(new FilterPhenotypes(conceptCandidates).getCandidates());
//		}
//		return longestMatch ? new LongestMatchSort(annotations).getAnnotations() : new LongestSameMatchSort(annotations).getAnnotations();
//	}
//
//	private List<ConceptAnnotation> transformRelations(
//			Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates,
//			Map<ConceptCandidate, DS_ConceptInfo> dsCandidates,
//			Map<ConceptCandidate, ConceptCandidate> mapping) {
//		List<ConceptAnnotation> list = new ArrayList<ConceptAnnotation>();
//		for (ConceptCandidate candidate : conceptCandidates.keySet()) {
//			DS_ConceptInfo conceptInfo = conceptCandidates.get(candidate);
//
//			int startIndex = candidate.getStartOffset() - candidate.getSentence().getDocOffset().x;
//			int endIndex = candidate.getEndOffset() - candidate.getSentence().getDocOffset().y;
//			String target = candidate.getSentence().getOriginalText().substring(startIndex, endIndex);
//
//			ConceptAnnotation annotation = new ConceptAnnotation(candidate.getStartOffset(), candidate.getEndOffset(), target);
//			annotation.setConcept(conceptInfo);
//			annotation.setNegated(candidate.isNegated());
//			annotation.setLength(endIndex - startIndex);
//
//			if (mapping.containsKey(candidate)) {
//				annotation.addRelation(ConceptType.DEGREE_OF_SEVERITY, mapping.get(candidate), dsCandidates.get(mapping.get(candidate)));
//			}
//
//			list.add(annotation);
//		}
//		return list;
//	}
//
//	private List<ConceptAnnotation> transform(Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates) {
//		List<ConceptAnnotation> list = new ArrayList<ConceptAnnotation>();
//		for (ConceptCandidate candidate : conceptCandidates.keySet()) {
//			DS_ConceptInfo conceptInfo = conceptCandidates.get(candidate);
//
//			int startIndex = candidate.getStartOffset() - candidate.getSentence().getDocOffset().x;
//			int endIndex = candidate.getEndOffset() - candidate.getSentence().getDocOffset().y;
//			String target = candidate.getSentence().getOriginalText().substring(startIndex, endIndex);
//
//			ConceptAnnotation annotation = new ConceptAnnotation(candidate.getStartOffset(), candidate.getEndOffset(), target);
//			annotation.setConcept(conceptInfo);
//			annotation.setNegated(candidate.isNegated());
//			annotation.setLength(endIndex - startIndex);
//			list.add(annotation);
//		}
//		return list;
//	}
//
//	private Map<String, List<String>> doSpellCheck(ProcessedInput pInput, boolean doSpellCheck) {
//		Map<String, List<String>> spellCheckedMap = Collections.synchronizedMap(new HashMap<String, List<String>>());
//		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
//		for (String key : pInput.getVocabulary().keySet()) {
//			String value = pInput.getVocabulary().get(key);
//			executor.execute(new SpellCheckThread(key, value, spellCheck, spellCheckedMap, doSpellCheck));
//		}
//		executor.shutdown();
//	    while (!executor.isTerminated()) {
//	    }
//
//	    logger.debug("Spell checked map: " + spellCheckedMap);
//
//	    return spellCheckedMap;
//	}
//
//	public Map<String, Double> getStats() {
//		return stats;
//	}
//}
