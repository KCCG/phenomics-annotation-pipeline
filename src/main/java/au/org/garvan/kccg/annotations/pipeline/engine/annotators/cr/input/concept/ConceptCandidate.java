package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.beans.ConceptAnnotation;
//CR: Removed TA dependency from this class. It is functionally clean now.
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TAPositionedToken;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TASentence;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.domain.TAToken;

public class ConceptCandidate {

	private APSentence sentence;
	private Map<Integer, APToken> tokens;
	private List<APToken> alternative;
	
	private int startIndex;
	private int endIndex;
	private boolean isNC = false;
	private boolean negated = false;
	
	public ConceptCandidate() {
		this.tokens = new LinkedHashMap<Integer, APToken>();
		this.alternative = new ArrayList<APToken>();
		
		startIndex = Integer.MAX_VALUE;
		endIndex = -1;
	}

	public String getText() {
		String s = "";
		for (APToken token : tokens.values()) {
			//CR:Changed cleaned to original text : See the issues.
			s += token.getNormalizedText() + " ";
		}
		return s.trim();
	}

	public String getOriginalText() {
		String s = "";
		for (APToken token : tokens.values()) {
			s += token.getOriginalText() + " ";
		}
		return s.trim();
	}

	public int getStartOffset() {
		return tokens.get(startIndex).getSentOffset().x;
	}

	public int getEndOffset() {
		return tokens.get(endIndex).getSentOffset().y;
	}

	public void addToken(int index, APToken token) {
		this.tokens.put(index, token);
		if (index < startIndex) {
			startIndex = index;
		}
		if (index > endIndex) {
			endIndex = index;
		}
	}
	
	public void removeToken(int index) {
		this.tokens.remove(index);
	}
	
	public Map<Integer, APToken> getTokens() {
		return tokens;
	}

	public void setAlternative(List<APToken> alternative) {
		this.alternative.clear();
		this.alternative.addAll(alternative);
	}

	public boolean hasAlternative() {
		return !alternative.isEmpty();
	}

	public List<APToken> getAlternative() {
		return alternative;
	}

	public String getId() {
		return sentence.getId()+":"+ getStartOffset() + "::" + getEndOffset();
	}
	
	public ConceptAnnotation getConceptAnnotation() {
		return new ConceptAnnotation(tokens.get(startIndex).getSentOffset().x, tokens.get(endIndex).getSentOffset().x + tokens.get(endIndex).getOriginalText().length(), getOriginalText());
	}

    public APSentence getSentence() {
		return sentence;
	}

	public void setSentence(APSentence sentence) {
		this.sentence = sentence;
	}

	public boolean isNC() {
		return isNC;
	}

	public void setNC(boolean isNC) {
		this.isNC = isNC;
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ConceptCandidate that = (ConceptCandidate) o;

        return new EqualsBuilder()
                .append(sentence, that.sentence)
                .append(tokens, that.tokens)
                .append(alternative, that.alternative)
                .append(startIndex, that.startIndex)
                .append(endIndex, that.endIndex)
                .append(isNC, that.isNC)
                .append(negated, that.negated)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(sentence)
                .append(tokens)
                .append(alternative)
                .append(startIndex)
                .append(endIndex)
                .append(isNC)
                .append(negated)
                .toHashCode();
    }

	public String toString() {
		return "[" + getStartOffset() + "::" + getEndOffset() + "::" + isNC + "::" + negated + "]: " + getText() + " (" + tokens + ")";
	}

}
