package au.org.garvan.kccg.annotations.pipeline.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

/**
 * Created by ahmed on 9/1/18.
 */
public class RankedArticleComparitor implements Comparator<RankedArticle>{
        @Override
        public int compare(RankedArticle o1, RankedArticle o2) {
            return new CompareToBuilder()
                    .append(o2.getTotalConceptHits(), o1.getTotalConceptHits())
                    .append(o2.getPMID(), o1.getPMID()).toComparison();
        }
    }

