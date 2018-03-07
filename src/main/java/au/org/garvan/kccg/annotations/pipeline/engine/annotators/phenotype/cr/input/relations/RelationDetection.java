package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.relations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.ConceptType;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;

public class RelationDetection {

	private Map<ConceptCandidate, DS_ConceptInfo> newCandidates;
	private Map<ConceptCandidate, DS_ConceptInfo> dsCandidates;
	private Map<ConceptCandidate, ConceptCandidate> mapping;

	private Map<Integer, ConceptCandidate> startPositions;
	private Map<Integer, ConceptCandidate> dsStartPositions;

	public RelationDetection(Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates) {
		startPositions = new LinkedHashMap<Integer, ConceptCandidate>();
		dsStartPositions = new LinkedHashMap<Integer, ConceptCandidate>();
		newCandidates = new LinkedHashMap<ConceptCandidate, DS_ConceptInfo>();
		dsCandidates = new LinkedHashMap<ConceptCandidate, DS_ConceptInfo>();
		mapping = new HashMap<ConceptCandidate, ConceptCandidate>();

		for (ConceptCandidate candidate : conceptCandidates.keySet()) {
			DS_ConceptInfo conceptInfo = conceptCandidates.get(candidate);
			if (conceptInfo.getType().equalsIgnoreCase(ConceptType.PHENOTYPE)) {
				newCandidates.put(candidate, conceptInfo);
				List<Integer> sortedPositions = sort(candidate.getTokens());
				startPositions.put(sortedPositions.get(0), candidate);
			} else {
				if (conceptInfo.getType().equalsIgnoreCase(ConceptType.DEGREE_OF_SEVERITY)) {
					dsCandidates.put(candidate, conceptInfo);
					List<Integer> sortedPositions = sort(candidate.getTokens());
					dsStartPositions.put(sortedPositions.get(0), candidate);
				}
			}
		}
		
		for (int dsPosition : dsStartPositions.keySet()) {
			int next = dsPosition + 1;
			if (startPositions.containsKey(next)) {
				ConceptCandidate candidate = startPositions.get(next);
				mapping.put(candidate, dsStartPositions.get(dsPosition));
			}
		}
	}

	private List<Integer> sort(Map<Integer, APToken> tokens) {
		List<Integer> list = new ArrayList<Integer>();
		for (int key : tokens.keySet()) {
			list.add(key);
		}
		Collections.sort(list);
		return list;
	}

	public Map<ConceptCandidate, DS_ConceptInfo> getCandidates() {
		return newCandidates;
	}

	public Map<ConceptCandidate, ConceptCandidate> getMapping() {
		return mapping;
	}

	public Map<ConceptCandidate, DS_ConceptInfo> getDsCandidates() {
		return dsCandidates;
	}
}
