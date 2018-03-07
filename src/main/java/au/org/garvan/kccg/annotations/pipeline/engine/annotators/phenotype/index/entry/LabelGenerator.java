package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process.PseudoTA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelGenerator {

	private static final Logger logger = LoggerFactory.getLogger(LabelGenerator.class);

	private List<String> alternatives;
	private Map<String, List<String>> tokenAlternatives;
	private Map<String, String> interimLabels;

	public LabelGenerator(String label, List<String> stopWords, Map<String, List<String>> expansionTerms) {
		alternatives = new ArrayList<String>();
		tokenAlternatives = new LinkedHashMap<String, List<String>>();
		interimLabels = new HashMap<String, String>();
		
		String[] tokens = label.split(" ");
		for (String token : tokens) {
			List<String> list = new ArrayList<String>();
			list.add(token.toLowerCase());
			
			List<String> entries = PseudoTA.getCrResources().getSynonym(token);
			for (String entry : entries) {
				entry = entry.toLowerCase();
				if (!list.contains(entry)) {
					list.add(entry);
				}
			}
			
			tokenAlternatives.put(token, list);
		}

		processAlternatives(tokens);
		processStopWords(label, stopWords);
		processExpansionWords(label, expansionTerms);
	}

	private void processExpansionWords(String label, Map<String, List<String>> expansionTerms) {
		processExpansion(label, expansionTerms);
		List<String> tmpAlternatives = new ArrayList<String>();
		tmpAlternatives.addAll(alternatives);
		for (String entry : tmpAlternatives) {
			processExpansion(entry, expansionTerms);
		}
		
		logger.debug("After expansion: " + alternatives);
	}

	private void processExpansion(String entry, Map<String, List<String>> expansionTerms) {
		String[] tokens = entry.split(" ");
		for (String expansionToken : expansionTerms.keySet()) {
			List<String> list = expansionTerms.get(expansionToken);
			for (String item : list) {
				String newLabel = "";
				for (String token : tokens) {
					if (token.equalsIgnoreCase(expansionToken)) {
						newLabel += item + " ";
					} else {
						newLabel += token + " ";
					}
				}
				
				newLabel = newLabel.trim();
				if (!newLabel.equalsIgnoreCase("")) {
					if (!interimLabels.containsKey(newLabel.toLowerCase())) {
						alternatives.add(newLabel);
						interimLabels.put(newLabel.toLowerCase(), "");
					}
				}
				
			}
		}
	}
	
	private void processStopWords(String label, List<String> stopWords) {
		for (String altLabel : alternatives) {
			interimLabels.put(altLabel.toLowerCase(), "");
		}
		
		String[] tokens = label.split(" ");
		for (String stopWord : stopWords) {
			String newLabel = "";
			for (String token : tokens) {
				if (!token.equalsIgnoreCase(stopWord)) {
					newLabel += token + " ";
				}
			}
			newLabel = newLabel.trim();
			if (!newLabel.equalsIgnoreCase("")) {
				if (!interimLabels.containsKey(newLabel.toLowerCase())) {
					alternatives.add(newLabel);
					interimLabels.put(newLabel.toLowerCase(), "");
				}
			}
		}
		
		
		logger.debug("After stop words: " + alternatives);
	}

	private void processAlternatives(String[] tokens) {
		for (String token : tokens) {
			List<String> list = tokenAlternatives.get(token);
			if (alternatives.isEmpty()) {
				for (String s : list) {
					alternatives.add(s + " ");
				}
			} else {
				List<String> newLabels = new ArrayList<String>();
				
				for (String label : alternatives) {
					for (String s : list) {
						String newLabel = label.trim() + " " + s.trim() + " ";
						newLabels.add(newLabel.trim());
					}
				}
				
				alternatives.clear();
				alternatives.addAll(newLabels);
			}
		}	
		
		logger.debug("Alternatives: " + alternatives);
	}

	public List<String> getAlternatives() {
		return alternatives;
	}

}
