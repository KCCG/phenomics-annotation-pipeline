package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate;

import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.ProcessedInput;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept.GenericCandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.patterns.ConjunctionPatternProcessor;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TASentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;

public class MatrixBasedCandidateGenerator {

	private SequenceGeneratorCache sequenceGeneratorCache;
	private ConjunctionPatternProcessor patternProcessor;
	
	private int maxThreads;

	public MatrixBasedCandidateGenerator(int maxThreads, Map<Integer, String> generalPatterns) {
		this.maxThreads = maxThreads;
		sequenceGeneratorCache = new SequenceGeneratorCache();
		sequenceGeneratorCache.initialize();
		patternProcessor = new ConjunctionPatternProcessor(generalPatterns);
	}

	public GenericCandidateGenerator generate(APSentence sentence, ProcessedInput processedInput) {
		return new GenericCandidateGenerator(sentence, sequenceGeneratorCache, patternProcessor, maxThreads, processedInput);
	}
}
