package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;

public class ProcessedInput {

	private Map<String, String> vocabulary;
	
	private Map<String, ConceptCandidate> conceptCandidates;
	
	public ProcessedInput() {
		vocabulary = Collections.synchronizedMap(new HashMap<String, String>());
		conceptCandidates = Collections.synchronizedMap(new HashMap<String, ConceptCandidate>());
	}
	
	public Map<String, String> getVocabulary() {
		return vocabulary;
	}
	
	public Map<String, ConceptCandidate> getConceptCandidates() {
		return conceptCandidates;
	}
}
