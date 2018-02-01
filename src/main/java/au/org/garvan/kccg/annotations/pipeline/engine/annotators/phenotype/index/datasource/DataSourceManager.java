package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.DataSourceConfig;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.DataSourceConfigurationReader;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.wrapper.OntoDataSourceWrapper;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.IndexEntryCreator;

public class DataSourceManager {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);

	private DataSourceConfig dataSourceConfig;
	
	private IDataSourceWrapper dataSourceWrapper;

	public DataSourceManager(String dsConfigFile) {
		DataSourceConfigurationReader reader = new DataSourceConfigurationReader(dsConfigFile);
		if (!reader.isValid()) {
			logger.error("Invalid data source configuration file ...");
		} else {
			dataSourceConfig = reader.getDsConfiguration();
		}
	}

	public boolean isValid() {
		return dataSourceConfig != null;
	}

	public boolean initDataSource(IndexEntryCreator indexEntryCreator) {
		dataSourceWrapper = new OntoDataSourceWrapper(dataSourceConfig, indexEntryCreator);
		logger.info("Loading data source ...");
		return dataSourceWrapper.initialize();
	}
	
	public void index() {
		dataSourceWrapper.index();
	}
	
	public void close() {
		logger.info("Closing data source ...");
		dataSourceWrapper.close();
	}
}
