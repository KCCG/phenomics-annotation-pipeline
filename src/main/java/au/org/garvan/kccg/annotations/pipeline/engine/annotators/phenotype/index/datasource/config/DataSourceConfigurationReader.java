package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataSourceConfigurationReader {

	private DataSourceConfig dsConfiguration;
	
	private boolean valid;
	
	public DataSourceConfigurationReader(String file) {
		dsConfiguration = new DataSourceConfig();
		valid = false;
		
		unmarshallConfiguration(file);
	}
	
	private void unmarshallConfiguration(String file) {
		ObjectMapper om = new ObjectMapper();
		try {
			dsConfiguration = om.readValue(new File(file), DataSourceConfig.class);
			verifyConfiguration();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void verifyConfiguration() {
		valid = dsConfiguration.getAcronym() != null && 
				dsConfiguration.getLocation() != null &&
				dsConfiguration.getOutput() != null && 
				dsConfiguration.isValid();
		
		if (valid) {
			File file = new File(dsConfiguration.getLocation());
			valid = file.exists();

			file = new File(dsConfiguration.getOutput());
			valid = valid && file.exists();
		}
	}

	public boolean isValid() {
		return valid;
	}
	
	public DataSourceConfig getDsConfiguration() {
		return dsConfiguration;
	}

	public static void main(String[] args) {
		System.out.println(new DataSourceConfigurationReader("/home/tudor/Deploy/hpo_cr/index/data_sources/mre/mre.ds").isValid());
	}
}
