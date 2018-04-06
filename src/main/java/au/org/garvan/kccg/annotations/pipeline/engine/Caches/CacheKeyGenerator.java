package au.org.garvan.kccg.annotations.pipeline.engine.Caches;

import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchQueryV1;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CacheKeyGenerator {

    public static String getL1CacheKeyForPaginatedArticles(SearchQueryV1 query, PaginationRequestParams param, Boolean isHistoricalRequest){
        String key= "S";
        List<String> searchIds =  query.getSearchItems().stream().map(x->x.getId()).collect(Collectors.toList());
        List<String> filterIds =  query.getFilterItems().stream().map(x->x.getId()).collect(Collectors.toList());
        Collections.sort(searchIds);
        Collections.sort(filterIds);



    }
}
