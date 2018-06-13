package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.spellcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellCheckThreadPipeline implements Runnable {

	private String key;
	private CRSpellCheck spellCheck;
	private List<String> suggestions;

	public SpellCheckThreadPipeline(String key ,CRSpellCheck spellCheck, List<String> suggestions) {
		this.key = key;
		this.spellCheck = spellCheck;
		this.suggestions = suggestions;
	}
	
	@Override
	public void run() {
		List<String> list = new ArrayList<String>();
		if (spellCheck.isValid()) {
			if (spellCheck.hasEntry(key)) {
				list.add(key);
			}
			suggestions.addAll(list);
		}
	}

}
