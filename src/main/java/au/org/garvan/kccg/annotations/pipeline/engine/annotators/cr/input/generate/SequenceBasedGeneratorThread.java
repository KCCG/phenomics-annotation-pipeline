package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.CandidatePhrase;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept.ConceptCandidateCreator;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.CandidatePhrase;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TASentence;

import java.util.List;
import java.util.Map;

public class SequenceBasedGeneratorThread implements Runnable {

	private CandidatePhrase phrase;
	private List<int[]> sequenceList;
	private Map<String, String> vocabulary;
	private Map<String, ConceptCandidate> candidates;
	private APSentence sentence;
	
	public SequenceBasedGeneratorThread(CandidatePhrase phrase, List<int[]> sequenceList, Map<String, String> vocabulary, 
			APSentence sentence, Map<String, ConceptCandidate> candidates) {
		this.phrase = phrase;
		this.sequenceList = sequenceList;
		this.vocabulary = vocabulary;
		this.sentence = sentence;
		this.candidates = candidates;
	}
	
	public void run() {
		for (int[] sequence : sequenceList) {
//			System.out.println(" - SEQUENCE: " + Arrays.asList(ArrayUtils.toObject(sequence)));

			CandidatePhrase newPhrase = phrase.generate(sequence);
//			System.out.println(" - NEW PHRASE: " + newPhrase);
			ConceptCandidate cand = new ConceptCandidateCreator(newPhrase, sentence, vocabulary).getConceptCandidate();
//			System.out.println(" - CAND: " + cand);
			if (cand != null) {
				candidates.put(cand.getId(), cand);
			}
		}
	}

}
