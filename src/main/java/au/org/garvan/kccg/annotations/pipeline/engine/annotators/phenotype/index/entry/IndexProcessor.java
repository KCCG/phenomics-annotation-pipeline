package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process.PseudoTA;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.DataSourceMetadata;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.LabelObject;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.IndexConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;

public class IndexProcessor {

	private static final Logger logger = LoggerFactory.getLogger(IndexProcessor.class);

	private DB indexDB;

	private DB processDB;

	private Map<Integer, String> symbolSet;

	// ORIGINAL TOKEN HASH -> SYMBOL ID
	private Map<Integer, String> symbolMap;

	private Map<Integer, DS_ConceptInfo> conceptInfoMap;

	// SYMBOL -> CONCEPT HASH -> LABEL HASH -> COUNT
	private NavigableSet<Object[]> documentMap;

	// CONCEPT HASH -> LABEL HASH -> LABEL SIZE
	private NavigableSet<Object[]> documentSizeMap;

	private String outFolder;

	public IndexProcessor(String outFolder) {
		this.outFolder = outFolder;
		processDB = DBMaker.memoryDB().make();
		symbolSet = processDB.treeMapCreate(IndexConstants.SET_SYMBOL_HASH).make();
	}

	public boolean initialize() {
		try {
			indexDB = DBMaker.fileDB(new File(outFolder + IndexConstants.INDEX_DATA)).encryptionEnable(TAConstants.DICT_PASSWORD).closeOnJvmShutdown().make();
			symbolMap = indexDB.treeMapCreate(IndexConstants.SET_SYMBOL_MAP).make();
			conceptInfoMap = indexDB.treeMapCreate(IndexConstants.SET_CONCEPT_INFO).make();
			documentMap = indexDB.treeSetCreate(IndexConstants.SET_DOCUMENT_MAP).serializer(BTreeKeySerializer.ARRAY4).make();
			documentSizeMap = indexDB.treeSetCreate(IndexConstants.SET_DOCUMENT_SIZE_MAP).serializer(BTreeKeySerializer.ARRAY4).make();
		} catch (Exception e) {
			logger.error("Unable to initialize index: " + e.getMessage(), e);
			return false;
		}
		return true;
	}

	public void setMetadata(DataSourceMetadata metadata) {
		Map<String, String> metadataMap = indexDB.treeMapCreate(IndexConstants.MAP_METADATA).make();
		metadataMap.putAll(metadata.getMetadata());
		indexDB.commit();
	}

	public void addConcept(DS_ConceptInfo conceptInfo) {
		conceptInfoMap.put(conceptInfo.getUri().hashCode(), conceptInfo);
		logger.debug("Added [" + conceptInfo.getUri().hashCode() + "]: " + conceptInfo.getUri());
	}

	// CLEAN LABEL -> CONCEPT HASH
	public void buildLabelMap(Map<String, Integer> labelMap, Map<Integer, LabelObject> labelSet) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (String label : labelMap.keySet()) {
			executor.execute(new IndexDocumentEntryThread(labelMap.get(label), labelSet.get(label.hashCode()), symbolMap, documentMap, documentSizeMap));
		}
		executor.shutdown();
	    while (!executor.isTerminated()) {
	    }
	}

	public void createTokenSet(Map<Integer, LabelObject> labelSet) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int cleanLabelHash : labelSet.keySet()) {
			LabelObject cleanLabelObject = labelSet.get(cleanLabelHash);
			executor.execute(new IndexEntryLabelThread(cleanLabelObject.getTokenList(), symbolSet));
		}
		executor.shutdown();
	    while (!executor.isTerminated()) {
	    }
	}
	
	public void consolidate() {
		Map<String, List<Integer>> invertedIndex = new HashMap<String, List<Integer>>();
		
		for (int tokenHash : symbolSet.keySet()) {
			String cleanToken = symbolSet.get(tokenHash);
			//CR:DONE Check and go

			String id = PseudoTA.getCrResources().getSimpleDictionary(TAConstants.DICT_LEXVAR).getValue(cleanToken);
			if (id == null) {
				id = PseudoTA.getCrResources().getSimpleDictionary(TAConstants.DICT_ORDINALS).getValue(cleanToken);
				if (id == null) {
					List<Integer> hashes = invertedIndex.containsKey(cleanToken) ? invertedIndex.get(cleanToken) : new ArrayList<Integer>();
					hashes.add(tokenHash);
					invertedIndex.put(cleanToken, hashes);
				} else {
					symbolMap.put(tokenHash, id);
					logger.debug(cleanToken + " => " + id);
				}
			} else {
				symbolMap.put(tokenHash, id);
				logger.debug(cleanToken + " => " + id);
			}
		}
		
		int count = 1;
		for (String cleanToken : invertedIndex.keySet()) {
			logger.debug("[NEW] " + cleanToken + " => " + "S" + count);
			List<Integer> hashes = invertedIndex.get(cleanToken);
			for (int tokenHash : hashes) {
				symbolMap.put(tokenHash, "S" + count);
			}
			count++;
		}
 	}

	public Map<Integer, String> getSymbolMap() {
		return symbolMap;
	}

	public void commit() {
		indexDB.commit();
	}
	
	public void closeIndex() {
		processDB.delete(IndexConstants.SET_SYMBOL_HASH);
		processDB.close();
		
		indexDB.commit();
		indexDB.close();
	}
}
