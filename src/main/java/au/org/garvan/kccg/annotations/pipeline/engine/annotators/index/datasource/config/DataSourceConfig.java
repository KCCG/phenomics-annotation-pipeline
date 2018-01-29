package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataSourceConfig implements Serializable {
	
	private static final long serialVersionUID = 5623375531612621162L;

	public static final String TYPE_PHENOTYPE = "PHENOTYPE";

	public static final String TYPE_DEGREE_OF_SEVERITY = "DEGREE_OF_SEVERITY";

	public static final String TYPE_MODE_OF_INHERITANCE = "MODE_OF_INHERITANCE";

	public static final String TYPE_ONSET = "ONSET";
	
	private String acronym;
	
	private String title;
	
	private String version;

	private String location;

	private String output;
	
	private String preferred_label_property;

	private List<DataSourceIndexProperty> index_properties = new ArrayList<DataSourceIndexProperty>();

	private List<DataSourceSpec> spec = new ArrayList<DataSourceSpec>();
	
	public DataSourceConfig() {
		
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public List<DataSourceSpec> getSpec() {
		return spec;
	}

	public void setSpec(List<DataSourceSpec> spec) {
		this.spec = spec;
	}

	public String getPreferred_label_property() {
		return preferred_label_property;
	}

	public void setPreferred_label_property(String preferred_label_property) {
		this.preferred_label_property = preferred_label_property;
	}

	public List<DataSourceIndexProperty> getIndex_properties() {
		return index_properties;
	}

	public void setIndex_properties(List<DataSourceIndexProperty> index_properties) {
		this.index_properties = index_properties;
	}
	
	public boolean isValid() {
		boolean valid = preferred_label_property != null;
		valid = valid && !index_properties.isEmpty();
		return valid;
	}
}
