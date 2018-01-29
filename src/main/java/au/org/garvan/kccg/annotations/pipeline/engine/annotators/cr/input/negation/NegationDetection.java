package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.negation;

import java.util.LinkedHashMap;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.DS_ConceptInfo;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TAPositionedToken;

public class NegationDetection {

	private static final Logger logger = LoggerFactory.getLogger(NegationDetection.class);

	private Map<ConceptCandidate, DS_ConceptInfo> newConceptCandidates;
	
	public NegationDetection(Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates) {
		newConceptCandidates = new LinkedHashMap<ConceptCandidate, DS_ConceptInfo>();
		for (ConceptCandidate candidate : conceptCandidates.keySet()) {
			DS_ConceptInfo concept = conceptCandidates.get(candidate);
			process(candidate, concept);
		}
	}

	private void process(ConceptCandidate candidate, DS_ConceptInfo concept) {
		GenNegEx g = new GenNegEx(true);
		String sentence = candidate.getSentence().getOriginalText();
		
		try {
			APToken tailToken = null;
			for (int tokenIdx : candidate.getTokens().keySet()) {
				APToken token = candidate.getTokens().get(tokenIdx);
				if (token.isTail()) {
					tailToken = token;
					break;
				}
			}

			boolean negated = false;
			if (tailToken == null) {
				logger.debug("Candidate [" + candidate.getStartOffset() + "::" + candidate.getEndOffset() + "] => Sentence [" + candidate.getSentence().getDocOffset().x + "::" + candidate.getSentence().getDocOffset().y + "]: " + candidate.getSentence().getOriginalText());
				int startIndex = candidate.getStartOffset() - candidate.getSentence().getDocOffset().x;
				int endIndex = candidate.getEndOffset() - candidate.getSentence().getDocOffset().y;
				String target = sentence.substring(startIndex, endIndex);
				logger.debug("Target [" + startIndex + "::" + endIndex + "]: " + target);
				String context = findContext(target, sentence);
				String cleanSent = CallKit.cleans(context);
				String scope = g.negScope(cleanSent);
				if (!scope.equalsIgnoreCase("-1") && !scope.equalsIgnoreCase("-2")) {
					negated = CallKit.contains(scope, cleanSent, CallKit.cleans(target));
				}
			} else {
				String context = findContext(tailToken.getOriginalText(), sentence);
				logger.debug("Tail token: " + tailToken + " => Context: " + context);
				String cleanSent = CallKit.cleans(context);
				String scope = g.negScope(cleanSent);
				if (!scope.equalsIgnoreCase("-1") && !scope.equalsIgnoreCase("-2")) {
					negated = CallKit.contains(scope, cleanSent, CallKit.cleans(tailToken.getOriginalText()));
				}
			}
			
			logger.debug("Negated: " + negated);
			candidate.setNegated(negated);
			newConceptCandidates.put(candidate, concept);
		} catch (Exception e) {
			logger.error("Negation detection exception: " + e.getMessage(), e);
			newConceptCandidates.put(candidate, concept);
		}
	}

	private String findContext(String target, String text) {
		int index = text.indexOf(target);
		String context = text.substring(0, index + target.length());
		index = context.lastIndexOf(",");
		if (index != -1) {
			context = context.substring(index + 1);
			return context;
		}
		return text;
	}

	public Map<ConceptCandidate, DS_ConceptInfo> getCandidates() {
		return newConceptCandidates;
	}
}
