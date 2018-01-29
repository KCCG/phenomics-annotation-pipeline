package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.search;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.dictionary.TASimpleDictionary;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config.DataSourceMetadata;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.DS_ConceptInfo;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.StatsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IndexSearch {

	private static final Logger logger = LoggerFactory.getLogger(IndexSearch.class);

	private boolean valid;
	
	private int maxThreads;
	
	private Map<String, IndexDataSource> dataSources;
	
	private Map<String, Double> stats;

	public IndexSearch(String resourcesFolder, int maxThreads) {
		this.valid = false;
		this.maxThreads = maxThreads;
		this.dataSources = new HashMap<>();
		this.stats = new LinkedHashMap<>();
		
		this.initialize(resourcesFolder);
	}
	
	private void initialize(String resourcesFolder) {
		File file = new File(resourcesFolder);
		if (file.exists() && file.isDirectory()) {
			File[] dsFiles = file.listFiles();
			
			for (File dsFile : dsFiles) {
				if (dsFile.getName().endsWith(".bin")) {
					IndexDataSource dataSource = new IndexDataSource(dsFile.getAbsolutePath(), maxThreads);
					boolean valid = dataSource.initialize();
					if (valid) {
						this.dataSources.put(dataSource.getMetadata().getMetadata().get(DataSourceMetadata.ACRONYM), dataSource);
					}
				}
			}
			
			if (!dataSources.isEmpty()) {
				logger.info("Index Search initialized. Using " + maxThreads + " threads.");
				valid = true;
			}
			
			return;
		}
		
		valid = false;
	}

	public Map<DataSourceMetadata, Map<ConceptCandidate, DS_ConceptInfo>> search(Map<String, List<String>> vocabulary, TASimpleDictionary lexVarDictionary, Map<String, ConceptCandidate> conceptCandidates) {
		Map<DataSourceMetadata, Map<ConceptCandidate, DS_ConceptInfo>> result = new HashMap<DataSourceMetadata, Map<ConceptCandidate,DS_ConceptInfo>>();
		for (IndexDataSource dataSource : dataSources.values()) {
			double sTime = System.currentTimeMillis();
			Map<ConceptCandidate, DS_ConceptInfo> data = dataSource.search(vocabulary, lexVarDictionary, conceptCandidates);
			result.put(dataSource.getMetadata(), data);
			double eTime = System.currentTimeMillis();
			stats.put(StatsUtil.INDEX_SEARCH_TIME + dataSource.getMetadata().getMetadata().get(DataSourceMetadata.ACRONYM) + "]: ", (eTime - sTime));
		}
		return result;
	}

	public boolean isValid() {
		return valid;
	}
	
	public Map<String, Double> getStats() {
		return stats;
	}

	public void close() {
		for (IndexDataSource dataSource : dataSources.values()) {
			dataSource.close();
		}
	}

}
