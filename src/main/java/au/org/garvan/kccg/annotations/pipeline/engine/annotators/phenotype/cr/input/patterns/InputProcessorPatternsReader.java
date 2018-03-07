package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns;

import java.io.File;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;

public class InputProcessorPatternsReader {

	private static final Logger logger = LoggerFactory.getLogger(InputProcessorPatternsReader.class);

	private boolean valid;
	
	private DB inputDB;
	
	private Map<Integer, String> generalPatterns;
	
	private Map<Integer, String> ncPatterns;

	public InputProcessorPatternsReader(String patternsFile) {
		try {
			logger.info("Reading patterns from: " + patternsFile);
			this.inputDB = DBMaker.fileDB(new File(patternsFile)).transactionDisable().encryptionEnable(TAConstants.DICT_PASSWORD).readOnly().closeOnJvmShutdown().make();
			this.generalPatterns = inputDB.treeMap(TAConstants.GENERAL_PATTERNS);
			this.ncPatterns = inputDB.treeMap(TAConstants.NC_PATTERNS);
			valid = true;
		} catch (Exception e) {
			logger.error("Unable to read patterns: " + e.getMessage(), e);
			valid = false;
		}
	}

	public boolean isValid() {
		return valid;
	}

	public Map<Integer, String> getGeneralPatterns() {
		return generalPatterns;
	}

	public Map<Integer, String> getNcPatterns() {
		return ncPatterns;
	}

	public void close() {
		inputDB.close();
	}
}
