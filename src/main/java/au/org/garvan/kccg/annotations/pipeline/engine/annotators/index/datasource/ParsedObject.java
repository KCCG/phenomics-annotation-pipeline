package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config.OntoDataSourceSpecChecker;

public class ParsedObject {

	private String uri;
	private String preferredLabel;
	private Map<String, String> synonyms = new HashMap<String, String>();
	private String type;
	private List<String> stopWords = new ArrayList<String>();
	private Map<String, List<String>> expansionWords = new HashMap<String, List<String>>();
	private boolean processExact = false;

	public ParsedObject(OntoDataSourceSpecChecker dataSourceSpecChecker) {
		this.type = dataSourceSpecChecker.getDataSourceSpec().getType();
		this.stopWords = dataSourceSpecChecker.getDataSourceSpec().getUri_pattern().getStopWords();
		this.expansionWords = processExpansionWords(dataSourceSpecChecker.getDataSourceSpec().getUri_pattern().getTermExpansion());
		this.processExact = dataSourceSpecChecker.getDataSourceSpec().getUri_pattern().isProcessExact();
	}
	
	private Map<String, List<String>> processExpansionWords(List<String> termExpansion) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (String term : termExpansion) {
			String[] parts = term.split(":");
			if (parts.length == 2) {
				String head = parts[0].trim();
				String tail = parts[1].trim();
				
				List<String> list = map.containsKey(head) ? map.get(head) : new ArrayList<String>();
				list.add(tail);
				map.put(head, list);
			}
		}
		return map;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void addSynonym(String value) {
		this.synonyms.put(value, "");
	}


	public void setPreferredLabel(String label) {
		this.preferredLabel = label;
	}

	public String getPreferredLabel() {
		return preferredLabel;
	}

	public Map<String, String> getSynonyms() {
		return synonyms;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public boolean isProcessExact() {
		return processExact;
	}

	public void setProcessExact(boolean processExact) {
		this.processExact = processExact;
	}
}
