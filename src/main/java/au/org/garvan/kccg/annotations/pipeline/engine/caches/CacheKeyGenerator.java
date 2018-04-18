package au.org.garvan.kccg.annotations.pipeline.engine.caches;

import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchQueryV1;
import au.org.garvan.kccg.annotations.pipeline.model.query.SearchQueryV2;
import edu.stanford.nlp.util.StringUtils;
import org.apache.el.parser.BooleanNode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CacheKeyGenerator {

    public static String getL1CacheKeyForPaginatedArticles(SearchQueryV2 query, PaginationRequestParams param){
        String key= "S-";
        List<String> searchIds =  query.getSearchItems();
        Collections.sort(searchIds);
        String searchKeyPart = StringUtils.join(searchIds, ",");
        key = key + searchKeyPart;
        if(query.getFilterItems().size()>0) {
            List<String> filterIds = query.getFilterItems();
            Collections.sort(filterIds);
            String filterKeyPart = StringUtils.join(filterIds, ",");
            key = key + ";F-" + filterKeyPart;
        }
        key = key + ";H-"+ param.getIncludeHistorical().toString();
        key = key + ";PS-" + param.getPageSize().toString();
        key = key + ";PN-" + param.getPageNo().toString();
        return key;
    }

    public static String getL1CacheKeyForFilters(SearchQueryV2 query, Boolean isHistoricalRequest){
        String key= "S-";
        List<String> searchIds =  query.getSearchItems();
        Collections.sort(searchIds);
        String searchKeyPart = StringUtils.join(searchIds, ",");
        key = key + searchKeyPart;
        if(query.getFilterItems().size()>0) {
            List<String> filterIds = query.getFilterItems();
            Collections.sort(filterIds);
            String filterKeyPart = StringUtils.join(filterIds, ",");
            key = key + ";F-" + filterKeyPart;
        }
        key = key + ";H-"+ isHistoricalRequest.toString();
        return key;
    }



    public static String getL2CacheKey(List<String> searchIds, List<String> filterIds){
        String key= "S-";
        Collections.sort(searchIds);
        String searchKeyPart = StringUtils.join(searchIds, ",");
        key = key + searchKeyPart;
        if(filterIds.size()>0) {
            Collections.sort(filterIds);
            String filterKeyPart = StringUtils.join(filterIds, ",");
            key = key + ";F-" + filterKeyPart;
        }
        return key;
    }
}
