package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.search;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.dictionary.TASimpleDictionary;
import lombok.Getter;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.DataSourceMetadata;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.dictionary.TASimpleDictionary;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.IndexConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;

public class IndexDataSource {

	private static final Logger logger = LoggerFactory.getLogger(IndexDataSource.class);

	private String dsFile;
	
	private int maxThreads;
	
	private DB searchDB;

	// ORIGINAL TOKEN HASH -> SYMBOL ID
	private Map<Integer, String> symbolMap;

	@Getter
	private Map<Integer, DS_ConceptInfo> conceptInfoMap;

	// SYMBOL -> CONCEPT HASH -> LABEL HASH -> COUNT
	private NavigableSet<Object[]> documentMap;

	// CONCEPT HASH -> LABEL HASH -> LABEL SIZE
	private NavigableSet<Object[]> documentSizeMap;

	private DataSourceMetadata metadata;
	
	public IndexDataSource(String dsFile, int maxThreads) {
		this.dsFile = dsFile;
		this.maxThreads = maxThreads;
	}
	
	public boolean initialize() {
		try {
			logger.info("Loading index from (hard Coded File): " + dsFile);
			searchDB = DBMaker.fileDB(new File(dsFile)).encryptionEnable(TAConstants.DICT_PASSWORD).readOnly().closeOnJvmShutdown().make();
			symbolMap = searchDB.treeMap(IndexConstants.SET_SYMBOL_MAP);
			conceptInfoMap = searchDB.treeMap(IndexConstants.SET_CONCEPT_INFO);
			documentMap = searchDB.treeSet(IndexConstants.SET_DOCUMENT_MAP);
			documentSizeMap = searchDB.treeSet(IndexConstants.SET_DOCUMENT_SIZE_MAP);
			
			Map<String, String> metadataMap = searchDB.treeMap(IndexConstants.MAP_METADATA);
			metadata = new DataSourceMetadata(metadataMap);

			logger.info("Loaded datasource: " + metadataMap.get(DataSourceMetadata.ACRONYM));
			return true;
		} catch (Exception e) {
			logger.error("Unable to initialize search index: " + e.getMessage(), e);
		}
		return false;
	}
	
	public Map<ConceptCandidate, DS_ConceptInfo> search(Map<String, List<String>> vocabulary, TASimpleDictionary lexVarDictionary, Map<String, ConceptCandidate> conceptCandidates) {
		return new IndexSearchExecutor(maxThreads, vocabulary, lexVarDictionary, conceptCandidates, symbolMap, conceptInfoMap, documentMap, documentSizeMap).getConceptCandidates();
	}

	public DataSourceMetadata getMetadata() {
		return metadata;
	}

	public void close() {
		searchDB.close();
	}
}
