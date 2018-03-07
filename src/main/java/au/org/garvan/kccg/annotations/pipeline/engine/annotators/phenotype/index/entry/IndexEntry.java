package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tudor on 18/01/17.
 */
public class IndexEntry {

    private String uri;

    private String label;

    private String type;

    private List<String> synonyms = new ArrayList<>();

    private boolean processExact;

    private List<String> stopWords = new ArrayList<>();

    private Map<String, List<String>> expansionWords = new HashMap<>();

    public IndexEntry(String uri, String label, String type) {
        this.uri = uri;
        this.label = label;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public boolean isProcessExact() {
        return processExact;
    }

    public void setProcessExact(boolean processExact) {
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

}