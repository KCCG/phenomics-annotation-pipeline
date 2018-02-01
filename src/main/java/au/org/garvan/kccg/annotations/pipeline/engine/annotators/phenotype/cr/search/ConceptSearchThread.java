package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TAPositionedToken;

public class ConceptSearchThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ConceptSearchThread.class);

	private ConceptCandidate conceptCandidate;
	private Map<Integer, DS_ConceptInfo> conceptInfoMap;
	private NavigableSet<Object[]> documentMap;
	private Map<String, String> symbols;
	private NavigableSet<Object[]> documentSizeMap;
	private Map<ConceptCandidate, DS_ConceptInfo> annotatedConcepts;
	
	public  ConceptSearchThread(ConceptCandidate conceptCandidate, Map<String, String> symbols,
			Map<ConceptCandidate, DS_ConceptInfo> annotatedConcepts,
			Map<Integer, DS_ConceptInfo> conceptInfoMap,
			NavigableSet<Object[]> documentMap, NavigableSet<Object[]> documentSizeMap) {
		this.conceptCandidate = conceptCandidate;
		this.conceptInfoMap = conceptInfoMap;
		this.documentMap = documentMap;
		this.documentSizeMap = documentSizeMap;
		this.symbols = symbols;
		this.annotatedConcepts = annotatedConcepts;
	}
	
	@Override
	public void run() {
		logger.debug("Looking for: " + conceptCandidate.getTokens());
		int size = conceptCandidate.getTokens().size();
		Map<Integer, Integer> conceptMap = new HashMap<Integer, Integer>();
		int cc = 0;

		Map<String, Integer> tokenCount = new HashMap<String, Integer>();
		for (APToken token : conceptCandidate.getTokens().values()) {
			int count = tokenCount.containsKey(token.getNormalizedText()) ? tokenCount.get(token.getNormalizedText()) : 0;
			count++;
			tokenCount.put(token.getNormalizedText(), count);
		}
		
		for (APToken token : conceptCandidate.getTokens().values()) {
			int tCount = tokenCount.get(token.getNormalizedText());
			logger.debug(" - TCOUNT [" + token + "]: " + tCount);
			
			if (token.getNormalizedText() != null) {
				String symbol = symbols.get(token.getOriginalText());
				logger.debug(" - Looking [" + token + " - " + token.getNormalizedText() + "]: " + symbol);
				
				if (symbol == null) {
					conceptMap = new HashMap<Integer, Integer>();
					break;
				}
				
				Map<Integer, Map<Integer, Integer>> sizeMap = findSize(symbol);
				Map<Integer, Map<Integer, Integer>> map = find(symbol);
				
//				logger.debug(" - Found [" + symbol + "]: " + map);
//				logger.debug(" - Found size [" + symbol + "]: " + sizeMap);
				
				if (!map.containsKey(tCount) || !sizeMap.containsKey(size)) {
					conceptMap = new HashMap<Integer, Integer>();
					break;
				}

				Map<Integer, Integer> cMap = map.get(tCount);
				Map<Integer, Integer> sMap = sizeMap.get(size);
				Map<Integer, Integer> intersect = intersect(cMap, sMap);
//				logger.debug(" - CMAP: " + cMap);
//				logger.debug(" - SMAP: " + sMap);
//				logger.debug(" - INTERSECT: " + intersect);
				
				if (cc == 0) {
					conceptMap.putAll(intersect);
				} else {
					Map<Integer, Integer> tempMap = new HashMap<Integer, Integer>();
					for (int labelHash : intersect.keySet()) {
						if (conceptMap.containsKey(labelHash)) {
							tempMap.put(labelHash, conceptMap.get(labelHash));
						}
					}
					if (tempMap.isEmpty()) {
						conceptMap = new HashMap<Integer, Integer>();
						break;
					}
					
					conceptMap = new HashMap<Integer, Integer>();
					conceptMap.putAll(tempMap);
				}
			} else {
				break;
			}
			
			cc++;
		}
		
		logger.debug(" - FINAL CONCEPT MAP: " + conceptMap);
		int finalConceptHash = -1;
		for (int labelHash : conceptMap.keySet()) {
			finalConceptHash = conceptMap.get(labelHash);
		}

		if (finalConceptHash != -1) {
			DS_ConceptInfo conceptInfo = conceptInfoMap.get(finalConceptHash);
			logger.debug("CONCEPT [" + finalConceptHash + "]: " + conceptInfo);
			annotatedConcepts.put(conceptCandidate, conceptInfo);
		}
	}

	private Map<Integer, Integer> intersect(Map<Integer, Integer> cMap,
			Map<Integer, Integer> sMap) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (int labelHash : sMap.keySet()) {
			if (cMap.containsKey(labelHash)) {
				result.put(labelHash, sMap.get(labelHash));
			}
		}
		return result;
	}

	private Map<Integer, Map<Integer, Integer>> findSize(String symbol) {
		Map<Integer, Map<Integer, Integer>> map = new HashMap<Integer, Map<Integer,Integer>>();
		Iterator<Object[]> iter = Fun.filter(documentSizeMap, symbol).iterator();
		while (iter.hasNext()) {
			Object[] obj = iter.next();
			
			int conceptHash = ((Integer) obj[1]).intValue();
			int labelHash = ((Integer) obj[2]).intValue();
			int size = ((Integer) obj[3]).intValue();
			
			Map<Integer, Integer> countMap = map.containsKey(size) ? map.get(size) : new HashMap<Integer, Integer>();
			countMap.put(labelHash, conceptHash);
			map.put(size, countMap);
		}
		return map;
	}
	
	private Map<Integer, Map<Integer, Integer>> find(String symbol) {
		Map<Integer, Map<Integer, Integer>> map = new HashMap<Integer, Map<Integer,Integer>>();
		Iterator<Object[]> iter = Fun.filter(documentMap, symbol).iterator();
		while (iter.hasNext()) {
			Object[] obj = iter.next();
			
			int conceptHash = ((Integer) obj[1]).intValue();
			int labelHash = ((Integer) obj[2]).intValue();
			int count = ((Integer) obj[3]).intValue();
			
			Map<Integer, Integer> countMap = map.containsKey(count) ? map.get(count) : new HashMap<Integer, Integer>();
			countMap.put(labelHash, conceptHash);
			map.put(count, countMap);
		}
		return map;
	}
}
