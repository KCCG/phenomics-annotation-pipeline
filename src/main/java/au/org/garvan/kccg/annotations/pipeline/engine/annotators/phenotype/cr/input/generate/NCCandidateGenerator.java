package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.generate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ICandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.PhenoCandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns.PhenoPattern;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TASentence;

public class NCCandidateGenerator {

	private Map<String, List<PhenoPattern>> patterns;

	public NCCandidateGenerator(Map<Integer, String> ncPatterns) {
		patterns = new LinkedHashMap<String, List<PhenoPattern>>();
		for (String pattern : ncPatterns.values()) {
			addPattern(pattern);
		}
	}

	private void addPattern(String pattern) {
		PhenoPattern phenoPattern = new PhenoPattern(pattern);
		if (phenoPattern.getVerb() != null) {
			List<PhenoPattern> list = patterns.containsKey(phenoPattern.getVerb()) ? patterns.get(phenoPattern.getVerb()) : new ArrayList<PhenoPattern>();
			list.add(phenoPattern);
			patterns.put(phenoPattern.getVerb(), list);
		}
	}

	public ICandidateGenerator generate(APSentence taSentence, Map<String, String> vocabulary,
										Map<String, ConceptCandidate> conceptCandidates) {
		return new PhenoCandidateGenerator(taSentence, patterns, vocabulary, conceptCandidates);
	}
}
