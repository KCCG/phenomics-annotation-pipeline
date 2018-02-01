package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config;

import java.io.Serializable;

public class DataSourceSpec implements Serializable {

	private static final long serialVersionUID = -8859763981596177709L;
	
	private String type;
	
	private URIPatternDefinition uri_pattern;
	
	public DataSourceSpec() {
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public URIPatternDefinition getUri_pattern() {
		return uri_pattern;
	}

	public void setUri_pattern(URIPatternDefinition uri_pattern) {
		this.uri_pattern = uri_pattern;
	}
}
