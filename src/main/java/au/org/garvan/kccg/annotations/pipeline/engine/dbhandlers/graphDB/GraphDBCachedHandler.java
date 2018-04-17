package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.engine.caches.L1cache.FiltersResponseCache;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.FiltersCacheObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.constants.GraphDBConstants;
import au.org.garvan.kccg.annotations.pipeline.model.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;


/***
 * This class has been written to improve the search functionality.
 * It is first step towards generic entities search based on filters.
 */
@Component
public class GraphDBCachedHandler {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBCachedHandler.class);


    public void createArticleQuery(Article article) {
        GraphDBNatives.createArticleQuery(article);
    }



    public DBManagerResultSet fetchArticlesWithFilters(String queryId, List<String> searchItems, List<String> filterItems, PaginationRequestParams qParams, Boolean getFiltersAndCount) {

        //Steps:
        // 1: Get filters for S items
        //  (Check cache, if miss run query)
        // 2: Get articles count
        // 3: if Filters then get filters for S+F items
        //  3a: Concatenate S + F counts
        // 4: Get required page


        slf4jLogger.info(String.format("GraphDBCachedHandler called with query ID:%s | SearchItemsCount:%d | FilterItemsCount:%d | PageSize:%d | PageNo:%d | GetFiltersAndCount:%s",
                                                            queryId, searchItems.size(), filterItems.size(), qParams.getPageSize(), qParams.getPageNo(), getFiltersAndCount.toString()));
        Collections.sort(searchItems);
        Collections.sort(filterItems);

        DBManagerResultSet finalResultSet = new DBManagerResultSet();

        if(getFiltersAndCount) {

            //Step 1: Getting filters for only search items. These are called global filters. Check them in cache first
            List<ConceptFilter> searchFilters = getSearchItemsFilters(queryId, searchItems, qParams);
            //Step 2: Getting article counts. This would be based on search and filter items.
            //Update query params
            Integer articlesCount = GraphDBCachedNatives.runQueryForArticleCount(searchItems, filterItems);
            qParams.setTotalArticles(articlesCount);
            qParams.setTotalPages((int)Math.ceil( (double)articlesCount/qParams.getPageSize()));

            //Step 3: If there are filters in query then find filters based on search and filter items.
            //Concatenate filters count.
            List<ConceptFilter> searchAndFilterItemsConceptFilters = new ArrayList<>();
            if (filterItems.size() > 0) {
                searchAndFilterItemsConceptFilters = GraphDBCachedNatives.runSearchQueryForFilters(searchItems, filterItems);
                updateSearchFilterCount(searchFilters, searchAndFilterItemsConceptFilters);

            }
            finalResultSet.setConceptCounts(searchFilters);

        }

        List<Map> articlesMap =  GraphDBCachedNatives.runQueryForArticles(searchItems,filterItems,((qParams.getPageNo()-1)*qParams.getPageSize()),qParams.getPageSize());
        finalResultSet.setRankedArticles(prepareRankedArticles(articlesMap));

        slf4jLogger.info(String.format("GraphDBCachedHandler complete query ID:%s", queryId));

        return finalResultSet;

    }


    private List<RankedArticle> prepareRankedArticles(List<Map> articlesMap){
        List<RankedArticle> resultArticles = new ArrayList<>();
        Integer resultSizeAsRank = articlesMap.size();
        if(resultSizeAsRank>0) {
            String PMIDLabel = GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_PAID_LABEL;
            String searchCountsLabel = GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_SEARCH_COUNTS_LABEL;

            for(Map aMap: articlesMap){

                try{
                    String PMID="0";
                    Integer searchCount=0;
                    Integer filterCount=0;

                    for(Object key: aMap.keySet()){
                        if(key.toString().equals(PMIDLabel))
                            PMID = aMap.get(PMIDLabel).toString();
                        else if(key.toString().equals(searchCountsLabel))
                            searchCount = Integer.parseInt(aMap.get(searchCountsLabel).toString());
                        else
                            filterCount = filterCount + Integer.parseInt(aMap.get(key).toString());
                    }
                    BigDecimal totalHits = new BigDecimal(filterCount+searchCount);
                    RankedArticle rankedArticle = new RankedArticle(PMID, totalHits, searchCount, filterCount, resultSizeAsRank, null, null );
                    resultArticles.add(rankedArticle);
                    resultSizeAsRank--;
                }
                catch (Exception e){
                    slf4jLogger.info("Exception in parsing article from graph db while preparing ranked article.");
                }


            }


        }
        return resultArticles;

    }
    private List<ConceptFilter> getSearchItemsFilters(String queryId,  List<String> searchItems, PaginationRequestParams qParams){
        List<ConceptFilter> searchFilters ;
        //First check in cache : L1 cache was missed for whole query, however there is possibility that sear items based filters were fetched.
        SearchQueryV2 query = new SearchQueryV2(queryId, searchItems, new ArrayList<>());
        FiltersCacheObject cachedFilters = FiltersResponseCache.getFilters(query,qParams.getIncludeHistorical());
        if(cachedFilters !=null)
            searchFilters = cachedFilters.getFinalFilters();
        else{
           searchFilters = GraphDBCachedNatives.runSearchQueryForFilters(searchItems, new ArrayList<>());
           Integer rank = searchFilters.size();
           for(ConceptFilter conceptFilter: searchFilters)
           {
               conceptFilter.setRank(rank);
               rank--;
           }
        }
        return searchFilters;
    }

    private void updateSearchFilterCount(List<ConceptFilter> searchFilters, List<ConceptFilter> searchAndFilterItemsConceptFilters){
        HashMap <String, Integer> searchAndFilterItemsMap = new HashMap<>();

        for(ConceptFilter aConcept :searchAndFilterItemsConceptFilters){
            searchAndFilterItemsMap.put(aConcept.getId(), aConcept.getArticleCount());
        }

        for(ConceptFilter aConcept: searchFilters){
            if(searchAndFilterItemsMap.containsKey(aConcept.getId())){
                aConcept.setFilteredArticleCount(searchAndFilterItemsMap.get(aConcept.getId()));
            }
            else
                aConcept.setFilteredArticleCount(0);
        }
    }

}
