package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.dictionary;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;


public class TAMultipleDictionary implements TADictionary {

	private DB dictionaryDB;
	
	private NavigableSet<Object[]> dictionarySet;
	
	public TAMultipleDictionary() {
		
	}

	@Override
	public boolean load(String dictFile, String dictName) {
		try {
			dictionaryDB = DBMaker.fileDB(new File(dictFile)).encryptionEnable(TAConstants.DICT_PASSWORD).readOnly().closeOnJvmShutdown().make();
			dictionarySet = dictionaryDB.treeSet(dictName);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean hasKey(String key) {
		return Fun.filter(dictionarySet, key.hashCode()).iterator().hasNext();
	}
	
	@Override
	public String getValue(String key) {
		return null;
	}
	
	@Override
	public List<String> getListValue(String key) {
		List<String> list = new ArrayList<String>();
		for (Object[] entry : Fun.filter(dictionarySet, key.hashCode())) {
			String value = (String) entry[1];
			list.add(value);
		}
		return list;
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
