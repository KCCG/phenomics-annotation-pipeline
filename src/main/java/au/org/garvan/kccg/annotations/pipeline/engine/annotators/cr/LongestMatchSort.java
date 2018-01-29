package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.beans.ConceptAnnotation;

public class LongestMatchSort {

	private static final Logger logger = LoggerFactory.getLogger(LongestMatchSort.class);

	private List<ConceptAnnotation> currentAnnotations;

	public LongestMatchSort(List<ConceptAnnotation> annotations) {
		currentAnnotations = new ArrayList<ConceptAnnotation>();
		currentAnnotations.addAll(annotations);

		logger.debug("Before sort::");
		sort();
		
		logger.debug("After sort::");
		for (ConceptAnnotation candidate : currentAnnotations) {
			logger.debug("Candidate:: " + candidate.toString());
		}
	}

	private void sort() {
		List<ConceptAnnotation> list = new ArrayList<ConceptAnnotation>();
		for (ConceptAnnotation candidate : currentAnnotations) {
			list.add(candidate);
			logger.debug("Candidate:: " + candidate.toString());
		}
		
		for (int i = 0; i < list.size(); i++) {
			ConceptAnnotation candidateA = list.get(i);
			boolean found = false;
			
			for (int j = 0; j < list.size(); j++) {
				ConceptAnnotation candidateB = list.get(j);
				if (candidateA.getStartOffset() == candidateB.getStartOffset() && candidateA.getEndOffset() == candidateB.getEndOffset()) {
					continue;
				}
				
				if (candidateA.getStartOffset() >= candidateB.getStartOffset() && candidateA.getEndOffset() <= candidateB.getEndOffset()) {
					found = true;
					break;
				}
			}
			
			if (found) {
				currentAnnotations.remove(candidateA);
			}
		}
	}

	public List<ConceptAnnotation> getAnnotations() {
		return currentAnnotations;
	}
}
