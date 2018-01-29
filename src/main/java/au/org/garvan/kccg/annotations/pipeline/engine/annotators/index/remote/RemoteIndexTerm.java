package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.remote;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Created by tudor on 18/01/17.
 */
public class RemoteIndexTerm {

    private String uri;

    private String label;

    private List<String> synonyms;

    private String type;

    private String dataSource;

    private ProcessingMetadata processingMetadata;

    public RemoteIndexTerm() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ProcessingMetadata getProcessingMetadata() {
        return processingMetadata;
    }

    public void setProcessingMetadata(ProcessingMetadata processingMetadata) {
        this.processingMetadata = processingMetadata;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RemoteIndexTerm that = (RemoteIndexTerm) o;

        return new EqualsBuilder()
                .append(uri, that.uri)
                .append(label, that.label)
                .append(synonyms, that.synonyms)
                .append(type, that.type)
                .append(dataSource, that.dataSource)
                .append(processingMetadata, that.processingMetadata)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(uri)
                .append(label)
                .append(type)
                .append(synonyms)
                .append(dataSource)
                .append(processingMetadata)
                .toHashCode();
    }
}
