package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.search;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.dictionary.TASimpleDictionary;

import java.util.List;
import java.util.Map;


public class SymbolLookupThread implements Runnable {

	private Map<String, List<String>> vocabulary;
	private String token;
	private Map<Integer, String> symbolMap;
	private Map<String, String> symbols;
	private TASimpleDictionary lexVarDictionary;
	
	public SymbolLookupThread(String token, Map<String, List<String>> vocabulary, Map<Integer, String> symbolMap, TASimpleDictionary lexVarDictionary, Map<String, String> symbols) {
		this.vocabulary = vocabulary;
		this.symbolMap = symbolMap;
		this.token = token;
		this.symbols = symbols;
		this.lexVarDictionary = lexVarDictionary;
	}
	
	@Override
	public void run() {
		List<String> alternatives = vocabulary.get(token);
		String initial = alternatives.get(0);
		String found = null;
		
		if (symbolMap.containsKey(initial.hashCode())) {
			found = symbolMap.get(initial.hashCode());
		} else {
			if (alternatives.size() > 1) {
				for (int i = 1; i < alternatives.size(); i++) {
					String symbol = symbolMap.get(alternatives.get(i).hashCode());
					if (found == null) {
						found = symbol;
					} else {
						break;
					}
				}
			}
		}

		symbols.put(token, found == null ? lexVarDictionary.getValue(token.toLowerCase()) : found);
	}

}
