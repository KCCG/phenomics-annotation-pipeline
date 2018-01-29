package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.DS_ConceptInfo;


//CR: Removed TA dependency from this class. It is functionally clean now.

public class ConceptAnnotation implements Serializable {

	private static final long serialVersionUID = 2098202542865491490L;

	private int startOffset;

	private int endOffset;

	private int length;
	
	private String originalText;

	private DS_ConceptInfo concept;
	
	private boolean negated = false;
	
	private List<ConceptAnnotationRelation> target_relations = new ArrayList<ConceptAnnotationRelation>();

	public ConceptAnnotation() {
		
	}

	public ConceptAnnotation(int startOffset, int endOffset, String originalText) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.originalText = originalText;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public DS_ConceptInfo getConcept() {
		return concept;
	}

	public void setConcept(DS_ConceptInfo concept) {
		this.concept = concept;
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public List<ConceptAnnotationRelation> getTarget_relations() {
		return target_relations;
	}

	public void setTarget_relations(List<ConceptAnnotationRelation> target_relations) {
		this.target_relations = target_relations;
	}
	
	public void addRelation(String relationType, ConceptCandidate relationConcept, DS_ConceptInfo conceptInfo) {
		ConceptAnnotationRelation car = new ConceptAnnotationRelation(relationType);
		//CR: Cleaned offsets
		int startIndex = relationConcept.getStartOffset() - relationConcept.getSentence().getDocOffset().x;
		int endIndex = relationConcept.getEndOffset() - relationConcept.getSentence().getDocOffset().x;
		String target = relationConcept.getSentence().getOriginalText().substring(startIndex, endIndex);
		
		ConceptAnnotation annotation = new ConceptAnnotation(relationConcept.getStartOffset(), relationConcept.getEndOffset(), target);
		annotation.setConcept(conceptInfo);
		annotation.setLength(endIndex - startIndex);
		car.setTarget(annotation);
		target_relations.add(car);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ConceptAnnotation that = (ConceptAnnotation) o;

        return new EqualsBuilder()
                .append(startOffset, that.startOffset)
                .append(endOffset, that.endOffset)
                .append(length, that.length)
                .append(originalText, that.originalText)
                .append(concept, that.concept)
                .append(negated, that.negated)
                .append(target_relations, that.target_relations)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(startOffset)
                .append(endOffset)
                .append(length)
                .append(originalText)
                .append(concept)
                .append(negated)
                .append(target_relations)
                .toHashCode();
    }

	@Override
	public String toString() {
		return "[" + Integer.toString(startOffset) + "::" + Integer.toString(endOffset) + "] = " + concept + " (" + negated + ") == " + target_relations;
	}

}
