package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.CandidatePhrase;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TAPositionedToken;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TASentence;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TAToken;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.process.TokenCleaner;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.process.TokenCleaner;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.CandidatePhrase;

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
				//CR: Changing clean form to original text for time being
				this.vocabulary.put(token.getOriginalText(), token.getOriginalText());
				conceptCandidate.addToken(index, token);
			}
			/*
			TokenCleaner tokenCleaner = new TokenCleaner(token.getOriginalForm(), token.getPosTag(), false, false);
			if (tokenCleaner.isValid()) {
				this.vocabulary.put(token.getOriginalForm(), tokenCleaner.getSingleToken().getCleanForm());
				token.setCleanForm(tokenCleaner.getSingleToken().getCleanForm());
				token.setPosTag(tokenCleaner.getSingleToken().getPosTag());

				conceptCandidate.addToken(index, token);
			}
			*/
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
