package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.dictionary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;
import org.mapdb.DB;
import org.mapdb.DBMaker;


public class TASimpleDictionary implements TADictionary {

	private DB dictionaryDB;
	
	private Map<String, String> dictionaryMap;
	
	public TASimpleDictionary() {
		
	}

	@Override
	public boolean load(String dictFile, String dictName) {
		try {
			dictionaryDB = DBMaker.fileDB(new File(dictFile)).encryptionEnable(TAConstants.DICT_PASSWORD).readOnly().closeOnJvmShutdown().make();
			dictionaryMap = dictionaryDB.treeMap(dictName);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean hasKey(String key) {
		return dictionaryMap.containsKey(key);
	}
	
	@Override
	public String getValue(String key) {
		return dictionaryMap.get(key);
	}
	
	@Override
	public List<String> getListValue(String key) {
		return new ArrayList<String>();
	}

	@Override
	public boolean close() {
		try {
			dictionaryDB.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
