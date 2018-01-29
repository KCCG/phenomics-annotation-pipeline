package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.remote;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Map;

/**
 * Created by tudor on 18/01/17.
 */
public class ProcessingMetadata {

    private Boolean processExact;

    private List<String> stopWords;

    private Map<String, List<String>> expansionWords;

    public ProcessingMetadata() {
    }

    public Boolean getProcessExact() {
        return processExact;
    }

    public void setProcessExact(Boolean processExact) {
        this.processExact = processExact;
    }

    public List<String> getStopWords() {
        return stopWords;
    }

    public void setStopWords(List<String> stopWords) {
        this.stopWords = stopWords;
    }

    public Map<String, List<String>> getExpansionWords() {
        return expansionWords;
    }

    public void setExpansionWords(Map<String, List<String>> expansionWords) {
        this.expansionWords = expansionWords;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProcessingMetadata that = (ProcessingMetadata) o;

        return new EqualsBuilder()
                .append(processExact, that.processExact)
                .append(stopWords, that.stopWords)
                .append(expansionWords, that.expansionWords)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(processExact)
                .append(stopWords)
                .append(expansionWords)
                .toHashCode();
    }
}
