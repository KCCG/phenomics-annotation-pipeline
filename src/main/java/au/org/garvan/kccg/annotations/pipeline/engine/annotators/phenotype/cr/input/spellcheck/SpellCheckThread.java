package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.spellcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellCheckThread implements Runnable {

	private String key;
	private String value;
	private CRSpellCheck spellCheck;
	private Map<String, List<String>> spellCheckedMap;
	private boolean doSpellCheck;
	
	public SpellCheckThread(String key, String value, CRSpellCheck spellCheck, Map<String, List<String>> spellCheckedMap, boolean doSpellCheck) {
		this.key = key;
		this.value = value;
		this.spellCheck = spellCheck;
		this.spellCheckedMap = spellCheckedMap;
		this.doSpellCheck = doSpellCheck;
	}
	
	@Override
	public void run() {
		List<String> list = new ArrayList<String>();
		list.add(value);
		if (spellCheck.isValid() && doSpellCheck) {
			if (!spellCheck.hasEntry(value)) {
				List<String> suggestions = spellCheck.suggest(value);
				list.addAll(suggestions);
			}
		}
		spellCheckedMap.put(key, list);
	}

}
