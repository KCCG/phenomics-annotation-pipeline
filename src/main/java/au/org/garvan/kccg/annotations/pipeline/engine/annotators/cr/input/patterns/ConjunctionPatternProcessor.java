package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.patterns;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConjunctionPatternProcessor {

	private Map<Integer, List<List<Integer>>> patternTails;
	private Map<Integer, String> headShapes;
	
	private int tailCount;

	public ConjunctionPatternProcessor(Map<Integer, String> generalPatterns) {
		tailCount = 0;
		headShapes = new LinkedHashMap<Integer, String>();
		patternTails = new LinkedHashMap<Integer, List<List<Integer>>>();
		
		loadPatterns(generalPatterns);
	}

	private void loadPatterns(Map<Integer, String> generalPatterns) {
		for (String pattern : generalPatterns.values()) {
			addPattern(pattern);
		}
	}

	private void addPattern(String pattern) {
		int index = pattern.indexOf("=>");
		if (index != -1) {
			String head = pattern.substring(0, index).trim();
			String tail = pattern.substring(index + 2).trim();
			
			String[] tailSegments = tail.split(";");
			
			List<List<Integer>> tailPattern = parseTail(tailSegments);
			patternTails.put(tailCount, tailPattern);
			headShapes.put(tailCount, head);
			
			tailCount++;
		}
	}

	private List<List<Integer>> parseTail(String[] tailSegments) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		
		for (String segment : tailSegments) {
			segment = segment.trim();
			
			if (segment.startsWith("[")) {
				segment = segment.substring(1);
			}
			if (segment.endsWith("]")) {
				segment = segment.substring(0, segment.length() - 1);
			}
			
			List<Integer> list = new ArrayList<Integer>();
			String[] parts = segment.split(",");
			for (String part : parts) {
				part = part.trim();
				if (!part.equalsIgnoreCase("")) {
					list.add(Integer.parseInt(part));
				}
			}
			result.add(list);
		}
		
		return result;
	}
	
	public Map<Integer, List<List<Integer>>> getPatternTails() {
		return patternTails;
	}

	public Map<Integer, String> getHeadShapes() {
		return headShapes;
	}
}
