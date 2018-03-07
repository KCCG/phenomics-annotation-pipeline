package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.output;

import java.util.List;
import java.util.Map;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.beans.ConceptAnnotation;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.DataSourceMetadata;

public class EvalFormatter {

	private StringBuffer sb = new StringBuffer();
	
	public EvalFormatter(Map<DataSourceMetadata, List<ConceptAnnotation>> dsAnnotations) {
		for (DataSourceMetadata ds : dsAnnotations.keySet()) {
			for (ConceptAnnotation candidate : dsAnnotations.get(ds)) {
				String negated = candidate.isNegated() ? "NOT" : "";
				sb.append("[").
					append(Long.toString(candidate.getStartOffset())).
					append("::").
					append(Long.toString(candidate.getEndOffset())).
					append("]\t").
					append(ds.getMetadata().get(DataSourceMetadata.ACRONYM)).
					append("\t").append(" | ").
					append(candidate.getConcept().getUri()).
					append("\t").append(" | ").
					append(negated).
					append("\t").append(" | ").
					append(candidate.getOriginalText()).
					append("\n");
			}
		}
	}
	
	public String getOutput() {
		return sb.toString();
	}
}
