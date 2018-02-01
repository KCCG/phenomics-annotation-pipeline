package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process.TokenCleaner;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.CandidatePhrase;

public class ConceptCandidateCreator {

	private ConceptCandidate conceptCandidate;
	private Map<String, String> vocabulary;
	
	public ConceptCandidateCreator(CandidatePhrase phrase, APSentence sentence, Map<String, String> vocabulary) {
		this.conceptCandidate = new ConceptCandidate();
		this.conceptCandidate.setSentence(sentence);
		this.vocabulary = vocabulary;

		for (int index : phrase.getTokenPositions()) {
			APToken token  = sentence.getTokens().get(index);
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
				//CR:DONE got normalized text
				this.vocabulary.put(token.getOriginalText(), token.getNormalizedText());
				conceptCandidate.addToken(index, token);
			}

//			TokenCleaner tokenCleaner = new TokenCleaner(token.getOriginalText(), token.getPartOfSpeech(), false, false);
//			if (tokenCleaner.isValid()) {
//				this.vocabulary.put(token.getOriginalText(), tokenCleaner.getSingleToken().getNormalizedText());
//				token.setNormalizedText(tokenCleaner.getSingleToken().getNormalizedText());
//				token.setPartOfSpeech(tokenCleaner.getSingleToken().getPartOfSpeech());
//				conceptCandidate.addToken(index, token);
//			}

		}
	}

	public ConceptCandidate getConceptCandidate() {
		/*
		if (!conceptCandidate.getTokens().isEmpty()) {
			this.generateAlternative();
		}
		*/
		return conceptCandidate.getTokens().isEmpty() ? null : conceptCandidate;
	}

	//Point: Not used anywhere
	private void generateAlternative() {
		List<APToken> splitMap = new ArrayList<APToken>();
		for (APToken token : conceptCandidate.getTokens().values()) {
//			TokenCleaner tokenCleaner = new TokenCleaner(token.getCleanForm(), token.getPosTag(), true, false);
			TokenCleaner tokenCleaner = new TokenCleaner(token.getNormalizedText(), token.getPartOfSpeech(), true, false);

			if (tokenCleaner.isSplit()) {
				List<APToken> tokenList = tokenCleaner.getTokens();
				for (APToken taToken : tokenList) {
					splitMap.add(taToken);
				}
			} else {
				splitMap.add(tokenCleaner.getSingleToken());
			}
		}

		if (!splitMap.isEmpty()) {
			if (splitMap.size() != conceptCandidate.getTokens().size()) {
				conceptCandidate.setAlternative(splitMap);
			}
		}
		
	}
}
