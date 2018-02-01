package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class TAConstants {

	public static final String LOG_LEVEL = "log.level";

	public static final String TA_RESOURCES = "ta.resources";

	public static final String TA_MODEL_POS = "en-pos-maxent.bin";

	public static final String TA_MODEL_SENTENCE = "en-sent.bin";

	public static final String TA_MODEL_TOKEN = "en-token.bin";

	public static final String TA_MODEL_PARSER = "en-parser-chunking.bin";

	/**
	 * Dictionaries
	 */

	public static final String DICT_PASSWORD = "whyareyouloooking?";

	public static final String DICT_ORDINALS = "DICT_ORDINALS";

	public static final String DICT_ORDINALS_FILE_IN = "ordinals.dict";

	public static final String DICT_ORDINALS_FILE = "dict1.dict";

	public static final String DICT_POS = "DICT_POS";

	public static final String DICT_POS_FILE_IN = "pos.dict";

	public static final String DICT_POS_FILE = "dict2.dict";

	public static final String DICT_LEXVAR = "DICT_LEXVAR";

	public static final String DICT_LEXVAR_FILE_IN = "lexvar.dict";

	public static final String DICT_LEXVAR_FILE = "dict3.dict";

	public static final String DICT_SYNONYMS = "DICT_SYNONYMS";

	public static final String DICT_SYNONYMS_FILE_IN = "synonym.dict";

	public static final String DICT_SYNONYMS_FILE = "dict4.dict";

	public static final List<String> DICT_LIST_FILES = createList();

	public static final List<String> DICT_LIST = createDictList();

	public static final String[] DICTIONARY_FILES_IN = new String[] {
		DICT_ORDINALS_FILE_IN,
		DICT_POS_FILE_IN,
		DICT_LEXVAR_FILE_IN
	};
	
	public static String dictFileForInput(String fileName) {
		if (fileName.equalsIgnoreCase(DICT_ORDINALS_FILE_IN)) {
			return DICT_ORDINALS_FILE;
		}
		if (fileName.equalsIgnoreCase(DICT_POS_FILE_IN)) {
			return DICT_POS_FILE;
		}
		if (fileName.equalsIgnoreCase(DICT_LEXVAR_FILE_IN)) {
			return DICT_LEXVAR_FILE;
		}
		return null;
	}

	public static String dictForFile(String fileName) {
		if (fileName.equalsIgnoreCase(DICT_LEXVAR_FILE)) {
			return DICT_LEXVAR;
		}
		if (fileName.equalsIgnoreCase(DICT_ORDINALS_FILE)) {
			return DICT_ORDINALS;
		}
		if (fileName.equalsIgnoreCase(DICT_SYNONYMS_FILE)) {
			return DICT_SYNONYMS;
		}
		if (fileName.equalsIgnoreCase(DICT_POS_FILE)) {
			return DICT_POS;
		}
		return null;
	}
	
	private static List<String> createDictList() {
		List<String> list = new ArrayList<String>();
		list.add(DICT_LEXVAR);
		list.add(DICT_ORDINALS);
		list.add(DICT_POS);
		return list;
	}

	private static List<String> createList() {
		List<String> list = new ArrayList<String>();
		list.add(DICT_LEXVAR_FILE);
		list.add(DICT_ORDINALS_FILE);
		list.add(DICT_POS_FILE);
		return list;
	}

	/**
	 * Input Processor
	 */
	
	public static final String INPUTPROC_PATTERN_FILE = "patterns.bin";

	public static final String GENERAL_PATTERNS = "GENERAL_PATTERNS";

	public static final String NC_PATTERNS = "NC_PATTERNS";

	public static final Map<String, String> VERB_MAP = createVerbMap();

	private static Map<String, String> createVerbMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("am", "be");
		map.put("are", "be");
		map.put("is", "be");
		map.put("was", "be");
		map.put("were", "be");
		map.put("be", "be");
		map.put("has", "have");
		map.put("had", "have");
		map.put("have", "have");
		return map;
	}
	
	/**
	 * POS Tags
	 */

	public static final String POS_UNKNOWN = "UNKNOWN";

	public static final String POS_CD = "CD";

	public static final String POS_IN = "IN";

	public static final String POS_DT = "DT";

	public static final String POS_TO = "TO";

	public static final String POS_WDT = "WDT";

	public static final String POS_VB = "VB";

	public static final String POS_VBN = "VBN";

	public static final String POS_VBG = "VBG";

	public static final String POS_CC = "CC";

	public static final String POS_RB = "RB";

	public static final String POS_FW = "FW";

	public static final String POS_PRP = "PRP";

	public static final String POS_NN = "NN";

	public static final String POS_JJ = "JJ";

	public static final String POS_LBR = "-LRB-";

	public static final String POS_RBR = "-RRB-";

	public static boolean isBracket(String pos) {
		return pos.equals("-LRB-") || pos.equals("-RRB-") ||
				pos.equals("-LSB-") || pos.equals("-RSB-") ||
				pos.equals("-LCB-") || pos.equals("-RCB-");

	}
	
	public static String shape(String originalToken) {
		String shape = "";
		for (int i = 0; i < originalToken.length() ; i++) {
			String current = "a";
			char ch = originalToken.charAt(i);

			if (Character.isDigit(ch)) {
				current = "d";
			} else {
				if (Character.isUpperCase(ch)) {
					current = "A";
				} else {
					if (!StringUtils.isAlphanumeric(Character.toString(ch))) {
						current = "x";
					}
				}
			}
			
			shape += current;
		}
		
		return shape;
	}
	
	public static final Map<String, String> POS_EXCLUDE = createPosExclude();

	public static final String FOLDER_SPELLCHECK = "spellcheck";

	private static Map<String, String> createPosExclude() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(POS_DT, "");
		map.put(POS_CC, "");
		map.put(POS_IN, "");
		map.put(POS_TO, "");
		map.put(POS_WDT, "");
		return map;
	}
}
