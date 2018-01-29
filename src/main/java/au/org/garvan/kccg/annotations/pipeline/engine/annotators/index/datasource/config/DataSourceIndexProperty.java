package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config;

import java.io.Serializable;

public class DataSourceIndexProperty implements Serializable {

	private static final long serialVersionUID = -5560523325894158063L;

	private String property;
	
	private String subproperty;
	
	public DataSourceIndexProperty() {
		
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getSubproperty() {
		return subproperty;
	}

	public void setSubproperty(String subproperty) {
		this.subproperty = subproperty;
	}
	
	@Override
	public String toString() {
		return property + " == " + subproperty;
	}
}
