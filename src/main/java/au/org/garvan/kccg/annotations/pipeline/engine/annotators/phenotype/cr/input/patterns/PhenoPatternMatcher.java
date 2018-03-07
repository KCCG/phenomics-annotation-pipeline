package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;

public class PhenoPatternMatcher {

	private static final Logger logger = LoggerFactory.getLogger(PhenoPatternMatcher.class);

	private Map<String, ConceptCandidate> candidates;
	private Map<String, List<PhenoPattern>> patterns;
	private Map<String, String> vocabulary;

	public PhenoPatternMatcher(Map<String, List<PhenoPattern>> patterns,
			Map<String, String> vocabulary, Map<String, ConceptCandidate> conceptCandidates) {
		this.patterns = patterns;
		this.vocabulary = vocabulary;
		this.candidates = conceptCandidates;
	}

	public void match(APSentence sentence) {
		Map<Integer, APToken> verbs = sentence.getVerbPositions();
		Map<Integer, String> tokenPos = createTokenPosMap(sentence.getIndexedTokens());
		
		logger.debug("Compiled token POS map: " + tokenPos);
		
		Map<Integer, String> verbPositions = new LinkedHashMap<Integer, String>();
		List<Integer> verbPositionsIndex = new ArrayList<Integer>();

		for (int position : verbs.keySet()) {
			APToken verbToken = verbs.get(position);
			//CR:DONE Changed clean to normalized form
			if (TAConstants.VERB_MAP.containsKey(verbToken.getNormalizedText())) {
				String baseVerb = TAConstants.VERB_MAP.get(verbToken.getNormalizedText());
				if (patterns.containsKey(baseVerb)) {
					verbPositions.put(position, baseVerb);
					verbPositionsIndex.add(position);
				}
			}
		}

		if (verbPositionsIndex.isEmpty()) {
			return;
		}

		Collections.sort(verbPositionsIndex);
		logger.debug("Verb positions: " + verbPositions);
		logger.debug("Verb POS Index: " + verbPositionsIndex);
		
		if (verbPositionsIndex.size() > 1) {
			int start = 0;
			for (int i = 0; i < verbPositionsIndex.size(); i++) {
				int end;
				if (i == verbPositionsIndex.size() - 1) {
					end = sentence.getTokens().size() - 1;
				} else {
					end = verbPositionsIndex.get(i + 1) - 1;
				}
				
				String verb = verbPositions.get(verbPositionsIndex.get(i));
				logger.debug(" START: " + start + " - END: " + end + " - VERB: " + verb);
				
				List<PhenoPattern> patternList = patterns.containsKey(verb) ? patterns.get(verb) : new ArrayList<PhenoPattern>();
				
				PhenoPattern longestPattern = null;
				int maxLength = 0;
				List<Integer> candidate = new ArrayList<Integer>();
				int verbPosition = -1;
				
				for (PhenoPattern phenoPattern : patternList) {
					List<Integer> matches = match(start, end, verbPositionsIndex.get(i), phenoPattern, tokenPos);
					
					logger.debug("RESULT: " + matches);
					
					if (matches.get(0).intValue() != -1) {
						int length = matches.get(1) - matches.get(0);
						if (length == phenoPattern.getPatternShape().size() && length > maxLength) {
							maxLength = length;
							longestPattern = phenoPattern;
							verbPosition = verbPositionsIndex.get(i);
							candidate = matches;
						}
					}
				}
				
				if (longestPattern != null) {
					logger.debug("Longest pattern found: " + longestPattern + " => " + candidate);
					addCandidates(longestPattern, candidate, verbPosition, sentence);
				}
				
				start = verbPositionsIndex.get(i) + 1;
			}
		} else {
			String verb = verbPositions.get(verbPositionsIndex.get(0));
			logger.debug(" START: 0" + " - INDEX: " + (sentence.getTokens().size() - 1) + " - VERB: " + verb);

			List<PhenoPattern> patternList = patterns.containsKey(verb) ? patterns.get(verb) : new ArrayList<PhenoPattern>();
			
			PhenoPattern longestPattern = null;
			int maxLength = 0;
			List<Integer> candidate = new ArrayList<Integer>();
			int verbPosition = -1;
			
			for (PhenoPattern phenoPattern : patternList) {
				List<Integer> matches = match(0, sentence.getTokens().size() - 1, verbPositionsIndex.get(0), phenoPattern, tokenPos);

				logger.debug("RESULT: " + matches);

				if (matches.get(0).intValue() != -1) {
					int length = matches.get(1) - matches.get(0);
					if (length == phenoPattern.getPatternShape().size() && length > maxLength) {
						maxLength = length;
						longestPattern = phenoPattern;
						verbPosition = verbPositionsIndex.get(0);
						candidate = matches;
					}
				}
			}
			
			if (longestPattern != null) {
				logger.debug("Longest pattern found: " + longestPattern + " => " + candidate + " => " + verbPosition);
				addCandidates(longestPattern, candidate, verbPosition, sentence);
			}
		}
	}

	public void addCandidates(PhenoPattern longestPattern, List<Integer> candidate, int verbPosition, APSentence sentence) {
		logger.debug("Creating candidates: " + longestPattern + " => " + candidate + " => " + verbPosition + " => " + longestPattern.getPatternTail());
		
		List<Integer> tokenIndex = new ArrayList<Integer>();
		for (int i = candidate.get(0); i < candidate.get(1) + 1; i++) {
			tokenIndex.add(i);
		}
		logger.debug("Token index: " + tokenIndex);
		
		for (List<Integer> phraseIndex : longestPattern.getPatternTail()) {
			ConceptCandidate conceptCandidate = new ConceptCandidate();
			conceptCandidate.setSentence(sentence);
			conceptCandidate.setNC(true);

			for (int idx : phraseIndex) {
				APToken token  = sentence.getTokens().get(tokenIndex.get(idx));

				if (token.getPartOfSpeech() != null) {
					if (TAConstants.POS_EXCLUDE.containsKey(token.getPartOfSpeech())) {
						continue;
					}
				}
				
				boolean valid = true;
				String shape = token.getShape();
				if (shape == null) {
					shape = TAConstants.shape(token.getOriginalText());
				}
				if (shape.contains("x")) {
					if (shape.replaceAll("x", "").length() < 2) {
						valid = false;
					}
				}
				
				if (valid) {
					this.vocabulary.put(token.getOriginalText(), token.getNormalizedText());
					if (tokenIndex.get(idx) > verbPosition) {
						token.setTail(true);
					}
					conceptCandidate.addToken(idx, token);
				}
			}
			
			if (conceptCandidate.getTokens().size() > 0) {
				logger.debug(" ===> ADDING: " + conceptCandidate.toString());
				candidates.put(conceptCandidate.getId(), conceptCandidate);
			}
		}
	}
	
	private Map<Integer, String> createTokenPosMap(Map<Integer, APToken> tokens) {
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		for (int idx : tokens.keySet()) {
			String posTag = tokens.get(idx).getPartOfSpeech();
			char ch = posTag.charAt(0);
			if (Character.isLetter(ch)) {
				map.put(idx, posTag.substring(0, 2));
			} else {
				map.put(idx, posTag);
			}
			
		}
		return map;
	}

	private List<Integer> match(int start, int end, int verbIndex, PhenoPattern phenoPattern, Map<Integer, String> tokenPos) {
		logger.debug(" -> START: " + start + " - END: " + end + " - VB INDEX: " + verbIndex + " - PATTERN: " + phenoPattern);
		List<Integer> candidate = new ArrayList<Integer>();
		
		int count = -1;
		int entityStart = -1;
		for (int i = verbIndex - 1; i >= start; i--) {
			String posTag = tokenPos.get(i);
			String patternPosTag = phenoPattern.getPatternShape().containsKey(count) ? phenoPattern.getPatternShape().get(count) : "";
			
			logger.debug("START IDX: " + i + " -> " +  posTag + " == " + patternPosTag);
			
			if (posTag.equalsIgnoreCase(patternPosTag)){
				entityStart = i;
			} else {
				break;
			}
			count--;
		}

		candidate.add(entityStart);
		if (entityStart == -1) {
			return candidate;
		}
		
		count = 1;
		int entityEnd = -1;
		for (int i = verbIndex + 1; i <= end; i++) {
			String posTag = tokenPos.get(i);
			String patternPosTag = phenoPattern.getPatternShape().containsKey(count) ? phenoPattern.getPatternShape().get(count) : "";

			logger.debug("END IDX: " + i + " -> " +  posTag + " == " + patternPosTag);

			if (posTag.equalsIgnoreCase(patternPosTag)){
				entityEnd = i;
			} else {
				break;
			}
			count++;
		}
		
		if (entityEnd == -1) {
			candidate.clear();
			candidate.add(new Integer(-1));
			return candidate;
		}
		
		candidate.add(entityEnd);
		return candidate;
	}
}
