package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.beans;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ConceptAnnotationRelation {

	private String relationType;

	private ConceptAnnotation target;
	
	public ConceptAnnotationRelation() {
		
	}

	public ConceptAnnotationRelation(String relationType) {
		this.relationType = relationType;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public ConceptAnnotation getTarget() {
		return target;
	}

	public void setTarget(ConceptAnnotation target) {
		this.target = target;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ConceptAnnotationRelation that = (ConceptAnnotationRelation) o;

        return new EqualsBuilder()
                .append(relationType, that.relationType)
                .append(target, that.target)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(relationType)
                .append(target)
                .toHashCode();
    }

	
}
