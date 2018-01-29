package au.org.garvan.kccg.annotations.pipeline.engine.annotators.util;

import java.util.Map;

public class StatsUtil {

	public static final String NO_SENTENCES = "No. sentences: ";

	public static final String SENTENCE_PROC_TIME = "1. Sentence proc. time: ";

	public static final String INPUT_PROC_TIME = "2. Input proc time: ";

	public static final String VOCAB_PROC_TIME = "3. Vocabulary proc time: ";

	public static final String INDEX_SEARCH_TIME = "4. Index search [";

	public static final String POST_PROC_TIME = "5. Post proc time: ";

	public static void writeStats(Map<String, Double> stats) {
		for (String key : stats.keySet()) {
			System.out.println(key + stats.get(key));
		}
		System.out.println("=====");
	}
}
