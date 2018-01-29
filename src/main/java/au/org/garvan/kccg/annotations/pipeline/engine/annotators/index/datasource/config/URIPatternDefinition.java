package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class URIPatternDefinition implements Serializable {

	private static final long serialVersionUID = 2956940562077946724L;

	private String namespace;
	
	private List<String> include = new ArrayList<String>();

	private List<String> exclude = new ArrayList<String>();

	private List<String> stopWords = new ArrayList<String>();

	private List<String> termExpansion = new ArrayList<String>();

	private boolean processExact = false;
	
	public URIPatternDefinition () {
		
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public List<String> getInclude() {
		return include;
	}

	public void setInclude(List<String> include) {
		this.include = include;
	}

	public List<String> getExclude() {
		return exclude;
	}

	public void setExclude(List<String> exclude) {
		this.exclude = exclude;
	}

	public List<String> getStopWords() {
		return stopWords;
	}

	public void setStopWords(List<String> stopWords) {
		this.stopWords = stopWords;
	}

	public List<String> getTermExpansion() {
		return termExpansion;
	}

	public void setTermExpansion(List<String> termExpansion) {
		this.termExpansion = termExpansion;
	}

	public boolean isProcessExact() {
		return processExact;
	}

	public void setProcessExact(boolean processExact) {
		this.processExact = processExact;
	}
}
