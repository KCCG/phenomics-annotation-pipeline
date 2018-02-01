package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.CandidatePhrase;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;

public class ConjunctionPatternMatcher {

    private static final Logger logger = LoggerFactory.getLogger(ConjunctionPatternMatcher.class);

    private Map<Integer, List<List<Integer>>> patternTails;
    private Map<Integer, String> headShapes;

    private Map<CandidatePhrase, Integer> generated;

    public ConjunctionPatternMatcher(ConjunctionPatternProcessor patternProcessor) {
        this.patternTails = patternProcessor.getPatternTails();
        this.headShapes = patternProcessor.getHeadShapes();
    }

    public void process(CandidatePhrase phrase, APSentence sentence) {
        generated = new LinkedHashMap<CandidatePhrase, Integer>();

        logger.debug("PHRASE: " + phrase.getTokenPositions());

        String phraseShape = "";
        Map<Integer, Integer> posIndex = new HashMap<Integer, Integer>();

        int count = 0;
        for (int tokenIndex : phrase.getTokenPositions()) {
            String posTag = sentence.getTokens().get(tokenIndex).getPartOfSpeech();
            if (posTag.length() > 2) {
                posTag = posTag.substring(0, 2);
            }

            if (Character.isLetter(posTag.charAt(0))) {
                if (!posTag.equalsIgnoreCase(TAConstants.POS_DT)) {
                    phraseShape += posTag + " ";

                    posIndex.put(count, tokenIndex);
                    count++;
                }
            }
        }
        phraseShape = phraseShape.trim();

        logger.debug("POS INDEX: " + posIndex);
        logger.debug("Phrase shape: " + phraseShape);

        String longestPattern = "";
        int pCode = -1;
        for (int patternCode : headShapes.keySet()) {
            String patternShape = headShapes.get(patternCode);

            if (phraseShape.contains(patternShape)) {
                if (patternShape.length() > longestPattern.length()) {
                    longestPattern = patternShape;
                    pCode = patternCode;
                }
            }
        }

        if (pCode != -1) {
            logger.debug("Longest pattern: " + longestPattern);

            int index = phraseShape.indexOf(longestPattern);
            String before = phraseShape.substring(0, index).trim();

            logger.debug("Index: " + index);
            logger.debug("Before: " + before);

            String[] tokensBefore = before.split(" ");
            String[] patternSegments = longestPattern.split(" ");
            int patternStart = before.equalsIgnoreCase("") ? 0 : tokensBefore.length;
            int patternEnd = patternStart + patternSegments.length;

            logger.debug("Pattern: " + patternStart + " -> " + patternEnd);

            generate(posIndex, sentence, patternStart, patternEnd, pCode);
            Map<CandidatePhrase, Integer> standardGenerated = phrase.splitConjunctions();
            generated.putAll(standardGenerated);
        } else {
            generated = phrase.splitConjunctions();
        }
    }

    private void generate(Map<Integer, Integer> posIndex, APSentence sentence, int patternStart, int patternEnd, int patternCode) {
        List<List<Integer>> patternTail = patternTails.get(patternCode);

        Map<Integer, APToken> patternTokens = new LinkedHashMap<Integer, APToken>();
        Map<Integer, Integer> locations = new HashMap<Integer, Integer>();

        int count = 0;
        for (int i = patternStart; i < patternEnd; i++) {
            int tokenActualIndex = posIndex.get(i);
            patternTokens.put(count, sentence.getTokens().get(tokenActualIndex));
            locations.put(count, tokenActualIndex);
            count++;
        }

        for (List<Integer> pattern : patternTail) {
            CandidatePhrase phrase = new CandidatePhrase();
            for (int pos : pattern) {
                int idx = locations.get(pos);
                phrase.addToken(idx, sentence.getVerbPositions().containsKey(idx),
                        sentence.getPunctuation().containsKey(idx),
                        sentence.getConjunctions().containsKey(idx));
            }
            generated.put(phrase, 0);
        }
    }

    public Map<CandidatePhrase, Integer> getGenerated() {
        return generated;
    }
}
