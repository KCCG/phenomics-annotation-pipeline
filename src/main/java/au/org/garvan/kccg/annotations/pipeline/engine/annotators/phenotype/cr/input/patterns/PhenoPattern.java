package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PhenoPattern {

	private String verb;
	private Map<Integer, String> patternShape;
	private List<List<Integer>> patternTail;
	
	public PhenoPattern(String pattern) {
		patternShape = new LinkedHashMap<Integer, String>();
		patternTail = new ArrayList<List<Integer>>();
		
		int index = pattern.indexOf("=>");
		if (index != -1) {
			String head = pattern.substring(0, index).trim();
			String tail = pattern.substring(index + 2).trim();
			
			String[] tailSegments = tail.split(";");
			
			parseHead(head);
			parseTail(tailSegments);
		}
		
	}
	
	private void parseHead(String pattern) {
		List<String> left = new ArrayList<String>();
		List<String> right = new ArrayList<String>();
		
		String[] parts = pattern.split(" ");
		int index = 0;
		boolean found = false;
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			part = part.trim();
			if (!part.equalsIgnoreCase("")) {
				if (part.startsWith("%")) {
					index = i;
					found = true;
				} else {
					if (found) {
						right.add(part);
					} else {
						left.add(part);
					}
				}
			}
		}
		
		if (found) {
			verb = parts[index].substring(1);
			if (verb.endsWith("%")) {
				verb = verb.substring(0, verb.length() - 1);
			}
			
			for (int i = 0; i < right.size(); i++) {
				int actualIndex = i + 1;
				patternShape.put(actualIndex, right.get(i));
			}
			
			Collections.reverse(left);
			for (int i = 0; i < left.size(); i++) {
				int actualIndex = - (i + 1);
				patternShape.put(actualIndex, left.get(i));
			}
		}		
	}
	
	private void parseTail(String[] tailSegments) {
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
			patternTail.add(list);
		}
	}

	public Map<Integer, String> getPatternShape() {
		return patternShape;
	}

	public List<List<Integer>> getPatternTail() {
		return patternTail;
	}

	public String getVerb() {
		return verb;
	}
	
	@Override
	public String toString() {
		return "[" + verb + "]: " + patternShape;
	}
}
