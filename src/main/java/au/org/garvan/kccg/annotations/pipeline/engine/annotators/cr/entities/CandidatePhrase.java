package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CandidatePhrase {

    private List<Integer> tokenPositions;
    private List<Integer> verbPositions;
    private List<Integer> punctuationPositions;
    private List<Integer> conjunctionPositions;

    private int start = Integer.MAX_VALUE;
    private int end = -1;

    public CandidatePhrase() {
        tokenPositions = new ArrayList<Integer>();
        verbPositions = new ArrayList<Integer>();
        punctuationPositions = new ArrayList<Integer>();
        conjunctionPositions = new ArrayList<Integer>();
    }

    public void addToken(int idx, boolean isVerb, boolean isPunct, boolean isConj) {
        tokenPositions.add(idx);
        if (idx < start) {
            start = idx;
        }
        if (idx > end) {
            end = idx;
        }
        if (isVerb) {
            verbPositions.add(idx);
        }
        if (isPunct) {
            punctuationPositions.add(idx);
        }
        if (isConj) {
            conjunctionPositions.add(idx);
        }
    }

    public Map<Integer, CandidatePhrase> splitVerbs() {
        Map<Integer, CandidatePhrase> map = new LinkedHashMap<Integer, CandidatePhrase>();

        int count = 0;
        CandidatePhrase current = null;
        for (int idx : tokenPositions) {
            if (verbPositions.contains(idx)) {
                if (current != null) {
                    map.put(count++, current);
                    current = null;
                }
            } else {
                if (current == null) {
                    current = new CandidatePhrase();
                }
                current.addToken(idx, false, punctuationPositions.contains(idx), conjunctionPositions.contains(idx));
            }
        }
        if (current != null) {
            map.put(count++, current);
        }

        return map;
    }

    public Map<CandidatePhrase, Boolean> splitPunctuation() {
        Map<CandidatePhrase, Boolean> map = new LinkedHashMap<CandidatePhrase, Boolean>();

        CandidatePhrase current = null;
        CandidatePhrase full = new CandidatePhrase();
        for (int idx : tokenPositions) {
            if (punctuationPositions.contains(idx)) {
                if (current != null) {
                    map.put(current, true);
                    current = null;
                }
            } else {
                if (current == null) {
                    current = new CandidatePhrase();
                }
                current.addToken(idx, false, false, conjunctionPositions.contains(idx));
                full.addToken(idx, false, false, conjunctionPositions.contains(idx));
            }
        }
        /**
         * Add this back if you want for candidates to be generated across punctuation marks.
         */
		/*
		if (!map.containsKey(full)) {
			map.put(full, false);
		}
		*/

        if (current != null) {
            if (!map.containsKey(current)) {
                map.put(current, true);
            }
        }

        return map;
    }

    public Map<CandidatePhrase, Integer> splitConjunctions() {
        Map<CandidatePhrase, Integer> map = new LinkedHashMap<CandidatePhrase, Integer>();

        CandidatePhrase current = null;
        for (int idx : tokenPositions) {
            if (conjunctionPositions.contains(idx)) {
                if (current != null) {
                    map.put(current, 0);
                    current = null;
                }
            } else {
                if (current == null) {
                    current = new CandidatePhrase();
                }
                current.addToken(idx, false, false, false);
            }
        }
        if (current != null) {
            map.put(current, 0);
        }

        return map;
    }


    public Map<CandidatePhrase, Boolean> splitOther(APSentence sentence, boolean currentFlag) {
        Map<CandidatePhrase, Boolean> map = new LinkedHashMap<CandidatePhrase, Boolean>();

        CandidatePhrase full = new CandidatePhrase();
        CandidatePhrase fullWithRB = new CandidatePhrase();
        CandidatePhrase current = null;
        boolean found = false;
        for (int idx : tokenPositions) {
            String pos = sentence.getTokens().get(idx).getPartOfSpeech();

            if (!valid(pos, sentence.getTokens().get(idx).getOriginalText())) {
                if (current != null) {
                    map.put(current, true);
                    current = null;
                    found = true;
                } else {
                    found = true;
                }
            } else {
                if (pos.equalsIgnoreCase(TAConstants.POS_RB)) {
                    if (current != null) {
                        map.put(current, true);
                        current = null;
                        found = true;
                    } else {
                        found = true;
                    }

                    fullWithRB.addToken(idx, verbPositions.contains(idx),
                            punctuationPositions.contains(idx),
                            conjunctionPositions.contains(idx));
                } else {
                    full.addToken(idx, verbPositions.contains(idx),
                            punctuationPositions.contains(idx),
                            conjunctionPositions.contains(idx));
                    fullWithRB.addToken(idx, verbPositions.contains(idx),
                            punctuationPositions.contains(idx),
                            conjunctionPositions.contains(idx));
                }

                if (!pos.equalsIgnoreCase(TAConstants.POS_RB)) {
                    if (current == null) {
                        current = new CandidatePhrase();
                    }
                    current.addToken(idx, verbPositions.contains(idx),
                            punctuationPositions.contains(idx),
                            conjunctionPositions.contains(idx));
                }
            }
        }

        if (current != null) {
            if (map.isEmpty()) {
                map.put(current, currentFlag);
            } else {
                map.put(current, true);
            }
        }
        if (found) {
            map.put(full, true);
            if (!full.equals(fullWithRB)) {
                map.put(fullWithRB, true);
            }
        }

        return map;
    }

    private boolean valid(String pos, String word) {
        //CR: Find a workaround or put it back

//        if (pos.equalsIgnoreCase(TAConstants.POS_IN)) {
//            if (!TAPipeline.taResources.getSimpleDictionary(TAConstants.DICT_POS).hasKey(word)) {
//                return false;
//            } else {
//                String dictEntry = TAPipeline.taResources.getSimpleDictionary(TAConstants.DICT_POS).getValue(word);
//                if (dictEntry.equalsIgnoreCase(TAConstants.POS_IN)) {
//                    return false;
//                }
//            }
//        }

        return pos.startsWith(TAConstants.POS_NN) ||
                pos.startsWith(TAConstants.POS_VB) ||
                pos.startsWith(TAConstants.POS_JJ) ||
                pos.startsWith(TAConstants.POS_CD) ||
                pos.startsWith(TAConstants.POS_RB) ||
                pos.startsWith(TAConstants.POS_FW) ||
                pos.startsWith(TAConstants.POS_PRP) ||
                pos.startsWith(TAConstants.POS_UNKNOWN);
    }

    public CandidatePhrase generate(int[] sequence) {
        CandidatePhrase phrase = new CandidatePhrase();
        for (int i : sequence) {
            int tokenIDX = tokenPositions.get(i - 1);
            phrase.addToken(tokenIDX, verbPositions.contains(tokenIDX),
                    punctuationPositions.contains(tokenIDX),
                    conjunctionPositions.contains(tokenIDX));
        }
        return phrase;
    }

    public boolean hasVerbs() {
        return !verbPositions.isEmpty();
    }

    public boolean hasPunctuation() {
        return !punctuationPositions.isEmpty();
    }

    public boolean hasConjunctions() {
        return !conjunctionPositions.isEmpty();
    }

    public boolean isEmpty() {
        return tokenPositions.isEmpty();
    }

    public String toString() {
        return tokenPositions.toString();
    }

    public List<Integer> getTokenPositions() {
        return tokenPositions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        CandidatePhrase phrase = (CandidatePhrase) obj;

        boolean eq = (tokenPositions.size() == phrase.getTokenPositions().size()) && (this.hasVerbs() && phrase.hasVerbs()) && (this.hasPunctuation() && phrase.hasPunctuation());
        boolean ok = true;
        if (eq) {
            for (int i = 0; i < tokenPositions.size(); i++) {
                if (tokenPositions.get(i) != phrase.getTokenPositions().get(i)) {
                    ok = false;
                    break;
                }
            }
        } else {
            return false;
        }

        return ok;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (int i = 0; i < tokenPositions.size(); i++) {
            result = prime * result + tokenPositions.get(i).hashCode();
        }
        return result;
    }
}
