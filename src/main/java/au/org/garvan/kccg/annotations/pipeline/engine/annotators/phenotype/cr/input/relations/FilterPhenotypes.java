package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.relations;

import java.util.LinkedHashMap;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.ConceptType;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;

public class FilterPhenotypes {

	private Map<ConceptCandidate, DS_ConceptInfo> newCandidates;
	
	public FilterPhenotypes(Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates) {
		newCandidates = new LinkedHashMap<ConceptCandidate, DS_ConceptInfo>();
		
		for (ConceptCandidate candidate : conceptCandidates.keySet()) {
			DS_ConceptInfo conceptInfo = conceptCandidates.get(candidate);
			if (conceptInfo.getType().equalsIgnoreCase(ConceptType.PHENOTYPE)) {
				newCandidates.put(candidate, conceptInfo);
			}
		}
	}

	public Map<ConceptCandidate, DS_ConceptInfo> getCandidates() {
		return newCandidates;
	}

}
