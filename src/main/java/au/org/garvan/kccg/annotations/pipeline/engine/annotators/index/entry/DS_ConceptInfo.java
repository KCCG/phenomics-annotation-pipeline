package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.GeneralUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DS_ConceptInfo implements Serializable, Comparable<DS_ConceptInfo> {

	private static final long serialVersionUID = 314895526652951150L;

	private String uri;

	private String type;

	private String completeURI;

	private String preferredLabel;
	
	private List<String> alternativeLabels = new ArrayList<String>();
	
	public DS_ConceptInfo(String uri, String preferredLabel, String type) {
		this.completeURI = uri;
		this.uri = GeneralUtil.stripURI(uri).replace("_", ":");
		this.preferredLabel = preferredLabel;
		this.type = type;
	}

	public void addAlternativeLabel(String label) {
		this.alternativeLabels.add(label);
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getPreferredLabel() {
		return preferredLabel;
	}

	public void setPreferredLabel(String preferredLabel) {
		this.preferredLabel = preferredLabel;
	}

	public List<String> getAlternativeLabels() {
		return alternativeLabels;
	}

	public void setAlternativeLabels(List<String> alternativeLabels) {
		this.alternativeLabels = alternativeLabels;
	}
	
	public String getCompleteURI() {
		return completeURI;
	}

	public void setCompleteURI(String completeURI) {
		this.completeURI = completeURI;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DS_ConceptInfo that = (DS_ConceptInfo) o;

        return new EqualsBuilder()
                .append(uri, that.uri)
                .append(type, that.type)
                .append(completeURI, that.completeURI)
                .append(preferredLabel, that.preferredLabel)
                .append(alternativeLabels, that.alternativeLabels)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(uri)
                .append(type)
                .append(completeURI)
                .append(preferredLabel)
                .append(alternativeLabels)
                .toHashCode();
    }

	@Override
	public int compareTo(DS_ConceptInfo conceptInfo) {
		return uri.compareTo(conceptInfo.getUri());
	}
	
	public String toString() {
		return uri + " (" + preferredLabel + ") == " + alternativeLabels;
	}
}
