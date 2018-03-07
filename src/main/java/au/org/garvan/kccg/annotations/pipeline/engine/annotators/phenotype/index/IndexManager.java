package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.DataSourceManager;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.IndexEntryCreator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.IndexProcessor;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.IndexConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TimeUtil;

public class IndexManager {

	private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);

	private DataSourceManager dataSourceManager;
	private IndexProcessor indexProcessor;
	private IndexEntryCreator indexEntryCreator;

	private static IndexManager instance;
	private boolean valid = false;

	public static IndexManager getInstance(Properties properties) {
		if (instance == null) {
			instance = new IndexManager(properties);
		}

		return instance;
	}

	private IndexManager(Properties properties) {
		String dsConfigurationFile = properties.getProperty(IndexConstants.DS_INDEX);
		String outFolder = properties.getProperty(IndexConstants.DS_INDEX_OUT);
		if (outFolder != null) {
			if (!outFolder.endsWith("/")) {
				outFolder += "/";
			}
		}

		if (dsConfigurationFile == null) {
			logger.error("Datasource configuration file is missing.");
		} else {
			if (outFolder == null) {
				logger.error("Output folder is missing. Please specify output folder.");
			} else {
				dataSourceManager = new DataSourceManager(dsConfigurationFile);
				if (dataSourceManager.isValid()) {
					indexProcessor = new IndexProcessor(outFolder);
					indexEntryCreator = new IndexEntryCreator(indexProcessor);
					valid = true;
				} else {
					logger.error("Invalid data source configuration.");
				}
			}
		}
	}

	public boolean isValid() {
		return valid;
	}

	public boolean initialize() {
		boolean init = dataSourceManager.initDataSource(indexEntryCreator);
		init = init && indexProcessor.initialize();
		if (!init) {
			dataSourceManager.close();
		}
		return init;
	}

	public void index() {
		logger.info("Started indexing ...");
		double sTime = TimeUtil.start();
		dataSourceManager.index();
		logger.info("Finished indexing ...");
		logger.info("Total indexing time: " + TimeUtil.end(sTime));
	}

	public void close() {
		dataSourceManager.close();
		indexEntryCreator.close();
		indexProcessor.closeIndex();
	}
}
