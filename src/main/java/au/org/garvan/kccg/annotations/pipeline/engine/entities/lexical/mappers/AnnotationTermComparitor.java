package au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers;

import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

/**
 * Created by ahmed on 9/1/18.
 */
public class AnnotationTermComparitor implements Comparator<AnnotationTerm> {
    @Override
    public int compare(AnnotationTerm o1, AnnotationTerm o2) {
        return new CompareToBuilder()
                .append(o1.getTokenStartIndex(), o2.getTokenStartIndex()).toComparison();
    }
}

