package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities;

import java.io.Serializable;
import java.util.List;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LabelObject implements Serializable, Comparable<LabelObject> {

	private static final long serialVersionUID = -8869126942563479214L;

	private String label;
	
	private List<APToken> tokenList;
	
	public LabelObject(String label, List<APToken> tokenList) {
		this.label = label;
		this.tokenList = tokenList;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<APToken> getTokenList() {
		return tokenList;
	}

	public void setTokenList(List<APToken> tokenList) {
		this.tokenList = tokenList;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

       LabelObject that = (LabelObject) o;

        return new EqualsBuilder()
                .append(label, that.label)
                .append(tokenList, that.tokenList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(label)
                .append(tokenList)
                .toHashCode();
    }

	@Override
	public int compareTo(LabelObject labelObject) {
		return label.compareTo(labelObject.getLabel());
	}
	
}
