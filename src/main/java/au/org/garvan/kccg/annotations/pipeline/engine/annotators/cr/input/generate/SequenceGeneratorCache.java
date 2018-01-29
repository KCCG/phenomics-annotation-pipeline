package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class SequenceGeneratorCache {

	private Map<Integer, SequenceGenerator> cache;

	public SequenceGeneratorCache() {
		cache = new LinkedHashMap<Integer, SequenceGenerator>();
	}
	
	public void initialize() {
		for (int i = 2; i < 10; i++) {
			cache.put(i, new SequenceGenerator(i));
		}
	}

	public SortedMap<Integer, List<int[]>> getSequences(int length) {
		if (cache.containsKey(length)) {
			return cache.get(length).getFinalSequences();
		} else {
			SequenceGenerator generator = new SequenceGenerator(length);
			cache.put(length, generator);
			return generator.getFinalSequences();
		}
	}
}
