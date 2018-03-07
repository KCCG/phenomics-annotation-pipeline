package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.CandidatePhrase;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.ProcessedInput;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.generate.SequenceBasedGeneratorThread;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.generate.SequenceGeneratorCache;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns.ConjunctionPatternMatcher;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns.ConjunctionPatternProcessor;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenericCandidateGenerator implements ICandidateGenerator {

	private static final Logger logger = LoggerFactory.getLogger(GenericCandidateGenerator.class);

	private APSentence sentence;
	private ConjunctionPatternMatcher conjunctionPatterMatcher;
	private SequenceGeneratorCache sequenceGeneratorCache;

	private Map<String, String> vocabulary;
	private Map<String, ConceptCandidate> candidates;
	
	private int maxThreads;
	
	public GenericCandidateGenerator(APSentence sentence, SequenceGeneratorCache sequenceGeneratorCache,
			ConjunctionPatternProcessor patternProcessor, int maxThreads,
			ProcessedInput processedInput) {
		this.maxThreads = maxThreads;
		this.sentence = sentence;
		//CR:DONE Hatching sentence
		this.sentence.conceptRecognizerHatch();
		this.sequenceGeneratorCache = sequenceGeneratorCache;
		this.conjunctionPatterMatcher = new ConjunctionPatternMatcher(patternProcessor);
		
		this.vocabulary = processedInput.getVocabulary();
		this.candidates = processedInput.getConceptCandidates();
	}

	@Override
	public void generate() {

		logger.debug("SENTENCE: " + sentence.getOriginalText());
		for (int idx : sentence.getIndexedTokens().keySet()) {
			logger.debug(" -- [" + idx + "]: " + sentence.getTokens().get(idx));
		}

		Map<CandidatePhrase, Boolean> phrases = processBrackets();
		
		logger.debug(" ==> STAGE 1 (PROCESS BRACKETS): ");
		for (CandidatePhrase phrase : phrases.keySet()) {
			logger.debug(" --- " + phrase.toString() + " == " + phrases.get(phrase));
		}

		phrases = splitVerbs(phrases);
		logger.debug(" ==> STAGE 2 (SPLIT VERBS): ");
		for (CandidatePhrase phrase : phrases.keySet()) {
			logger.debug(" --- " + phrase.toString() + " == " + phrases.get(phrase));
		}

		phrases = splitPunctuation(phrases);
		logger.debug(" ==> STAGE 3 (SPLIT PUNCTUATION): ");
		for (CandidatePhrase phrase : phrases.keySet()) {
			logger.debug(" --- " + phrase.toString() + " == " + phrases.get(phrase));
		}

		phrases = splitConjunctions(phrases);
		logger.debug(" ==> STAGE 4 (SPLIT CONJUNCTIONS): ");
		for (CandidatePhrase phrase : phrases.keySet()) {
			logger.debug(" --- " + phrase.toString() + " == " + phrases.get(phrase));
		}

		phrases = splitOther(phrases);
		logger.debug(" ==> STAGE 5 (SPLIT OTHER): ");
		for (CandidatePhrase phrase : phrases.keySet()) {
			logger.debug(" --- " + phrase.toString() + " == " + phrases.get(phrase));
		}

		logger.debug(" ==> STAGE 6 (SUBSENTENCES): ");
		Map<CandidatePhrase, Boolean> subSentencePhrases = new LinkedHashMap<>();
		for (int i : sentence.getSubSentences().keySet()) {
			List<Integer> list = sentence.getSubSentences().get(i);
			logger.debug(" -- " + list);
            subSentencePhrases.put(createCandidateFromSubsentence(list), true);
		}
        subSentencePhrases = splitConjunctions(subSentencePhrases);
		for (CandidatePhrase phrase : subSentencePhrases.keySet()) {
			logger.debug(" --- " + phrase.toString() + " == " + subSentencePhrases.get(phrase));
		}
		phrases.putAll(subSentencePhrases);

		generateCandidates(phrases);

		for (String id : candidates.keySet()) {
			logger.debug(" - C[" + id + "] = " + candidates.get(id));
		}
	}

	private Map<CandidatePhrase, Boolean> processBrackets() {
		Map<CandidatePhrase, Boolean> map = new LinkedHashMap<CandidatePhrase, Boolean>();

		List<Integer> startB = sentence.getStartBracketPositions();
		List<Integer> endB = sentence.getEndBracketPositions();
		
		int prevEnd = 0;
		for (int i = 0 ; i < startB.size(); i++) {
			int startIndex = startB.get(i);
			
			if (i < endB.size()) {
				int endIndex = endB.get(i);
				
				if (prevEnd < startIndex) {
					map.put(createCandidate(prevEnd, startIndex), true);
				}
				map.put(createCandidate(startIndex + 1, endIndex), true);
				prevEnd = endIndex + 1;
			}
		}
		
		if (prevEnd < sentence.getTokens().size()) {
			map.put(createCandidate(prevEnd, sentence.getTokens().size()), true);
		}
		
		return map;
	}

	private Map<CandidatePhrase, Boolean> splitVerbs(Map<CandidatePhrase, Boolean> candidates) {
		Map<CandidatePhrase, Boolean> map = new LinkedHashMap<CandidatePhrase, Boolean>();
		
		for (CandidatePhrase phrase : candidates.keySet()) {
			if (phrase.hasVerbs()) {
				for (CandidatePhrase p : phrase.splitVerbs().values()) {
					map.put(p, true);
				}
			} else {
				map.put(phrase, true);
			}
		}

		return map;
	}
	
	private Map<CandidatePhrase, Boolean> splitPunctuation(Map<CandidatePhrase, Boolean> phrases) {
		Map<CandidatePhrase, Boolean> map = new LinkedHashMap<CandidatePhrase, Boolean>();
		
		for (CandidatePhrase phrase : phrases.keySet()) {
			if (phrase.hasPunctuation()) {
				Map<CandidatePhrase, Boolean> m = phrase.splitPunctuation();
				for (CandidatePhrase p : m.keySet()) {
					if (!p.isEmpty()) {
						map.put(p, m.get(p));
					}
				}
			} else {
				map.put(phrase, true);
			}
		}

		return map;
	}

	private Map<CandidatePhrase, Boolean> splitConjunctions(Map<CandidatePhrase, Boolean> phrases) {
		Map<CandidatePhrase, Boolean> map = new LinkedHashMap<CandidatePhrase, Boolean>();
		
		for (CandidatePhrase phrase : phrases.keySet()) {
			boolean currentFlag = phrases.get(phrase);
			
			if (phrase.hasConjunctions()) {
				map.put(phrase, false);
				conjunctionPatterMatcher.process(phrase, sentence);
				for (CandidatePhrase p : conjunctionPatterMatcher.getGenerated().keySet()) {
					map.put(p, true);
				}
			} else {
				map.put(phrase, currentFlag);
			}
		}

		return map;
	}

	private Map<CandidatePhrase, Boolean> splitOther(Map<CandidatePhrase, Boolean> phrases) {
		Map<CandidatePhrase, Boolean> map = new LinkedHashMap<CandidatePhrase, Boolean>();
		
		for (CandidatePhrase phrase : phrases.keySet()) {
			boolean currentFlag = phrases.get(phrase);
			if (currentFlag) {
				Map<CandidatePhrase, Boolean> m = phrase.splitOther(sentence, currentFlag);
				for (CandidatePhrase p : m.keySet()) {
					map.put(p, m.get(p));
				}
			} else {
				map.put(phrase, true);
			}
		}

		return map;
	}

    private CandidatePhrase createCandidateFromSubsentence(List<Integer> positions) {
        CandidatePhrase candidate = new CandidatePhrase();
        for (int position : positions) {
            candidate.addToken(position, sentence.getVerbPositions().containsKey(position),
                    sentence.getPunctuation().containsKey(position),
                    sentence.getConjunctions().containsKey(position));
        }
        return candidate;
    }

    private CandidatePhrase createCandidate(int startIndex, int endIndex) {
		CandidatePhrase candidate = new CandidatePhrase();
		for (int i = startIndex; i < endIndex; i++) {
			candidate.addToken(i, sentence.getVerbPositions().containsKey(i), 
					sentence.getPunctuation().containsKey(i),
					sentence.getConjunctions().containsKey(i));
		}
		return candidate;
	}

	private void generateCandidates(Map<CandidatePhrase, Boolean> phrases) {
		for (CandidatePhrase phrase : phrases.keySet()) {
			boolean flag = phrases.get(phrase);
			
			ConceptCandidate cand = new ConceptCandidateCreator(phrase, sentence, vocabulary).getConceptCandidate();
			if (cand != null) {
				candidates.put(cand.getId(), cand);

				if (flag) {
					logger.debug(" ==> " + cand.getTokens());
					SortedMap<Integer, List<int[]>> sequenceMap = sequenceGeneratorCache.getSequences(phrase.getTokenPositions().size());
	
					ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
					for (List<int[]> sequence : sequenceMap.values()) {
						executor.execute(new SequenceBasedGeneratorThread(phrase, sequence, vocabulary, sentence, candidates));
					}
	
					executor.shutdown();
					while (!executor.isTerminated()) {
					}
				}
			}
		}
	}
}
