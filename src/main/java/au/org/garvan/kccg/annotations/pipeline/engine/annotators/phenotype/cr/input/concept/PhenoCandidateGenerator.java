package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept;

import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns.PhenoPattern;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns.PhenoPatternMatcher;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TASentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;

public class PhenoCandidateGenerator implements ICandidateGenerator {

	private APSentence taSentence;
	private PhenoPatternMatcher patternMatcher;

	public PhenoCandidateGenerator(APSentence taSentence, Map<String, List<PhenoPattern>> patterns,
			Map<String, String> vocabulary, Map<String, ConceptCandidate> conceptCandidates) {
		this.taSentence = taSentence;
		this.patternMatcher = new PhenoPatternMatcher(patterns, vocabulary, conceptCandidates);
	}

	@Override
	public void generate() {
		patternMatcher.match(taSentence);
	}
}
