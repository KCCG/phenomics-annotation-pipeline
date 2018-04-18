package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.engine.caches.CacheKeyGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.caches.L1cache.FiltersResponseCache;
import au.org.garvan.kccg.annotations.pipeline.engine.caches.L2cache.PersistentFiltersArticlesCache;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.FiltersCacheObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.L2CacheArticleParams;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.L2CacheObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.constants.GraphDBConstants;
import au.org.garvan.kccg.annotations.pipeline.model.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.CollationElementIterator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/***
 * This class has been written to improve the search functionality.
 * It is first step towards generic entities search based on filters.
 */
@Component
public class GraphDBCachedHandler {
    //This limit is used in logic for L2 Cache (Articles)
    private final Integer HISTORICAL_ARTICLES_CACHE_LIMIT= 800;

    private static final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBCachedHandler.class);

    @Autowired
    private PersistentFiltersArticlesCache L2Cache;

    public void createArticleQuery(Article article) {
        GraphDBNatives.createArticleQuery(article);
    }

    /***
     * This is master method called from DatabaseManager. Based on historical choice, sub functions are called.
     * @param queryId
     * @param searchItems
     * @param filterItems
     * @param qParams
     * @param getFiltersAndCount : It distinguished between just a page call or new query.
     * @param cachedFilters : This will be updated in case of getFilters and Count is set true.
     * @return
     */
    public DBManagerResultSet fetchArticlesWithFilters(String queryId, List<String> searchItems, List<String> filterItems, PaginationRequestParams qParams, Boolean getFiltersAndCount, FiltersCacheObject cachedFilters) {
        DBManagerResultSet finalResultSet = new DBManagerResultSet();

        //Very Important for cache and filter
        Collections.sort(searchItems);
        Collections.sort(filterItems);

        if (!qParams.getIncludeHistorical()) {
            finalResultSet = fetchArticlesWithFiltersLive(queryId, searchItems, filterItems, qParams, getFiltersAndCount);
            //Point: The responsibility to update filter object has been delegated to this class.
            if(getFiltersAndCount){
                cachedFilters.setArticlesCount(qParams.getTotalArticles());
                cachedFilters.setFinalFilters(finalResultSet.getConceptCounts());
            }
        } else {

            if(getFiltersAndCount) {
                //Process Live result
                SearchQueryV2 localLiveQuery = new SearchQueryV2(queryId + "-LocalLive", searchItems, filterItems);
                PaginationRequestParams localLiveParams = new PaginationRequestParams(qParams.getPageSize(), qParams.getPageNo());
                localLiveParams.setIncludeHistorical(false);

                //See of live filters are already there
                FiltersCacheObject liveCachedFilters =  FiltersResponseCache.getFilters(localLiveQuery, false);

                List<ConceptFilter> localLiveFilters = new ArrayList<>();
                List<RankedArticle> localLiveArticles = new ArrayList<>();
                Integer localLiveArticleCount = 0;

                //Check live filter cache to save calls
                if(liveCachedFilters!=null)
                {
                    localLiveArticleCount = liveCachedFilters.getArticlesCount();
                    localLiveFilters = liveCachedFilters.getFinalFilters();
                    localLiveParams.setTotalArticles(localLiveArticleCount);
                    localLiveArticles = fetchArticlesWithFiltersLive(queryId, searchItems, filterItems, localLiveParams, false).getRankedArticles();

                }
                else
                {
                    DBManagerResultSet localLiveResultSet = fetchArticlesWithFiltersLive(queryId, searchItems, filterItems, localLiveParams, true);
                    localLiveArticleCount = localLiveParams.getTotalArticles();
                    localLiveFilters = localLiveResultSet.getConceptCounts();
                    localLiveArticles = localLiveResultSet.getRankedArticles();


                }
                //Got Live Result Set ready
                List<RankedArticle> finalRankedArticles = new ArrayList<>();
                L2CacheArticleParams historicalArticlesParams = new L2CacheArticleParams();

                boolean gotArticlesFromLocal = false;
                boolean pageOverLap = false;
                if (localLiveArticleCount >= (qParams.getPageSize() * (qParams.getPageNo()))) {
                    gotArticlesFromLocal = true;
                } else {
                    gotArticlesFromLocal = false;
                    if (localLiveArticleCount > (qParams.getPageSize() * (qParams.getPageNo() - 1))) {
                        pageOverLap = true;
                        historicalArticlesParams.setSkip(localLiveArticleCount - (qParams.getPageSize() * (qParams.getPageNo() - 1)));
                        historicalArticlesParams.setLimit(qParams.getPageSize()- historicalArticlesParams.getSkip());
                    } else {
                        pageOverLap = false;
                        historicalArticlesParams.setSkip( (qParams.getPageSize() * (qParams.getPageNo() - 1)) - localLiveArticleCount);
                        historicalArticlesParams.setLimit(qParams.getPageSize());
                    }


                }

                DBManagerResultSet historicalResultSet = fetchArticlesWithFiltersHistorical(queryId,searchItems,filterItems, !gotArticlesFromLocal, historicalArticlesParams);

                Integer totalArticleCount = localLiveArticleCount + historicalArticlesParams.getTotalArticlesCount();
                qParams.setTotalArticles(totalArticleCount);
                qParams.setTotalPages((int) Math.ceil((double) totalArticleCount / qParams.getPageSize()));

                //Prepare the article set
                if(gotArticlesFromLocal){
                   finalRankedArticles = localLiveArticles;
                }
                else{
                    if (pageOverLap){
                        finalRankedArticles.addAll(localLiveArticles);
                        finalRankedArticles.addAll(historicalResultSet.getRankedArticles());
                    }
                    else
                    {
                        finalRankedArticles = historicalResultSet.getRankedArticles();
                    }
                }

                //Merging filters from live and historical server
                List<ConceptFilter> consolidatedFilters = mergeLiveAndHistoricalFilters(localLiveFilters, historicalResultSet.getConceptCounts());

                finalResultSet.setConceptCounts(consolidatedFilters);
                finalResultSet.setRankedArticles(finalRankedArticles);

                //Very important to fill the cache object as it will be cached by caller.
                cachedFilters.setFinalFilters(consolidatedFilters);
                cachedFilters.setArticlesCount(totalArticleCount);
                cachedFilters.setLiveArticleCount(localLiveArticleCount);

            }
            else
            {
                L2CacheArticleParams historicalArticlesParams = new L2CacheArticleParams();
                Integer liveArticlesCount = cachedFilters.getLiveArticleCount();

                boolean gotArticlesFromLocal = false;
                boolean pageOverLap = false;
                if (liveArticlesCount >= (qParams.getPageSize() * (qParams.getPageNo()))) {
                    gotArticlesFromLocal = true;
                } else {
                    gotArticlesFromLocal = false;
                    if (liveArticlesCount > (qParams.getPageSize() * (qParams.getPageNo()) - 1)) {
                        pageOverLap = true;
                        historicalArticlesParams.setSkip(liveArticlesCount - (qParams.getPageSize() * (qParams.getPageNo()) - 1));
                        historicalArticlesParams.setLimit(qParams.getPageSize()- historicalArticlesParams.getSkip());
                    } else {
                        pageOverLap = false;
                        historicalArticlesParams.setSkip( (qParams.getPageSize() * (qParams.getPageNo() - 1)) - liveArticlesCount);
                        historicalArticlesParams.setLimit(qParams.getPageSize());
                    }


                }

                List<RankedArticle > finalRankedArticles = new ArrayList<>();
                List<RankedArticle> localLiveArticles = new ArrayList<>();

                if(gotArticlesFromLocal || pageOverLap){
                    localLiveArticles = fetchArticlesWithFiltersLive(queryId,searchItems,filterItems,qParams,false).getRankedArticles();
                }
                //Prepare the article set
                if(gotArticlesFromLocal){
                    finalRankedArticles = localLiveArticles;
                }
                else{
                    List<RankedArticle> historicalArticles =  fetchArticlesHistorical(queryId, searchItems, filterItems, historicalArticlesParams);
                    if (pageOverLap){
                        finalRankedArticles.addAll(localLiveArticles);
                        finalRankedArticles.addAll(historicalArticles);
                    }
                    else
                    {
                        finalRankedArticles = historicalArticles;
                    }
                }

                finalResultSet.setRankedArticles(finalRankedArticles);

            }



        }

        return finalResultSet;

    }

    private List<ConceptFilter> mergeLiveAndHistoricalFilters(List<ConceptFilter> liveFilters, List<ConceptFilter> historicalFilters){
        //Point: Considering historical filters to be a bigger list, it is indexed.
        //Point Continue: Then live filters are looped through and appended in the index list
        Map<String,ConceptFilter> indexedHistoricalFilters = historicalFilters.stream().collect(Collectors.toMap(ConceptFilter::getId, Function.identity()));
        for(ConceptFilter lFilter: liveFilters){
            if(indexedHistoricalFilters.containsKey(lFilter.getId()))
            {
                ConceptFilter tempFilter = indexedHistoricalFilters.get(lFilter.getId());
                tempFilter.incrementArticleCount(lFilter.getArticleCount());
                tempFilter.incrementFilteredArticleCount(lFilter.getFilteredArticleCount());
            }
            else
                indexedHistoricalFilters.put(lFilter.getId(), lFilter);
        }

        List<ConceptFilter> finalFilters = indexedHistoricalFilters.values().stream().collect(Collectors.toList());
        Collections.sort(finalFilters, Comparator.comparingInt(ConceptFilter::getArticleCount).reversed());
     return finalFilters;

    }



    //////////////////////////////////////////////////////        Live Server Process        ///////////////////////////////////////////////////////////////////////////////////

    private DBManagerResultSet fetchArticlesWithFiltersLive(String queryId, List<String> searchItems, List<String> filterItems, PaginationRequestParams qParams, Boolean getFiltersAndCount) {

        //Steps:
        // 1: Get filters for S items
        //  (Check cache, if miss run query)
        // 2: Get articles count
        // 3: if Filters then get filters for S+F items
        //  3a: Concatenate S + F counts
        // 4: Get required page


        slf4jLogger.info(String.format("GraphDBCachedHandler called with query ID:%s | SearchItemsCount:%d | FilterItemsCount:%d | PageSize:%d | PageNo:%d | GetFiltersAndCount:%s",
                queryId, searchItems.size(), filterItems.size(), qParams.getPageSize(), qParams.getPageNo(), getFiltersAndCount.toString()));

        DBManagerResultSet finalResultSet = new DBManagerResultSet();

        if (getFiltersAndCount) {

            //Step 1: Getting filters for only search items. These are called global filters. Check them in cache first
            List<ConceptFilter> searchFilters = getSearchItemsFilters(queryId, searchItems, qParams);
            //Step 2: Getting article counts. This would be based on search and filter items. Tell Natives that it is a live call
            //Update query params
            Integer articlesCount = GraphDBCachedNatives.runQueryForArticleCount(searchItems, filterItems, false);
            qParams.setTotalArticles(articlesCount);
            qParams.setTotalPages((int) Math.ceil((double) articlesCount / qParams.getPageSize()));

            //Step 3: If there are filters in query then find filters based on search and filter items.
            //Concatenate filters count.
            List<ConceptFilter> searchAndFilterItemsConceptFilters = new ArrayList<>();
            if (filterItems.size() > 0) {
                searchAndFilterItemsConceptFilters = GraphDBCachedNatives.runSearchQueryForFilters(searchItems, filterItems, false);
                updateSearchFilterCount(searchFilters, searchAndFilterItemsConceptFilters);

            }
            finalResultSet.setConceptCounts(searchFilters);

        }

        List<Map> articlesMap = GraphDBCachedNatives.runQueryForArticles(searchItems, filterItems, ((qParams.getPageNo() - 1) * qParams.getPageSize()), qParams.getPageSize(), false);
        finalResultSet.setRankedArticles(prepareRankedArticles(articlesMap));

        slf4jLogger.info(String.format("GraphDBCachedHandler completed query ID:%s", queryId));

        return finalResultSet;

    }


    private List<RankedArticle> prepareRankedArticles(List<Map> articlesMap) {
        List<RankedArticle> resultArticles = new ArrayList<>();
        Integer resultSizeAsRank = articlesMap.size();
        if (resultSizeAsRank > 0) {
            String PMIDLabel = GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_PAID_LABEL;
            String searchCountsLabel = GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_SEARCH_COUNTS_LABEL;

            for (Map aMap : articlesMap) {

                try {
                    String PMID = "0";
                    Integer searchCount = 0;
                    Integer filterCount = 0;

                    for (Object key : aMap.keySet()) {
                        if (key.toString().equals(PMIDLabel))
                            PMID = aMap.get(PMIDLabel).toString();
                        else if (key.toString().equals(searchCountsLabel))
                            searchCount = Integer.parseInt(aMap.get(searchCountsLabel).toString());
                        else
                            filterCount = filterCount + Integer.parseInt(aMap.get(key).toString());
                    }
                    BigDecimal totalHits = new BigDecimal(filterCount + searchCount);
                    RankedArticle rankedArticle = new RankedArticle(PMID, totalHits, searchCount, filterCount, resultSizeAsRank, null, null);
                    resultArticles.add(rankedArticle);
                    resultSizeAsRank--;
                } catch (Exception e) {
                    slf4jLogger.info("Exception in parsing article from graph db while preparing ranked article.");
                }


            }


        }
        return resultArticles;

    }

    private List<ConceptFilter> getSearchItemsFilters(String queryId, List<String> searchItems, PaginationRequestParams qParams) {
        List<ConceptFilter> searchFilters;
        //First check in cache : L1 cache was missed for whole query, however there is possibility that sear items based filters were fetched.
        SearchQueryV2 query = new SearchQueryV2(queryId, searchItems, new ArrayList<>());
        FiltersCacheObject cachedFilters = FiltersResponseCache.getFilters(query, qParams.getIncludeHistorical());
        if (cachedFilters != null)
            searchFilters = cachedFilters.getFinalFilters();
        else {
            //Make sure that it goes to live server
            searchFilters = GraphDBCachedNatives.runSearchQueryForFilters(searchItems, new ArrayList<>(), false);
            Integer rank = searchFilters.size();
            for (ConceptFilter conceptFilter : searchFilters) {
                conceptFilter.setRank(rank);
                rank--;
            }
        }
        return searchFilters;
    }

    private void updateSearchFilterCount(List<ConceptFilter> searchFilters, List<ConceptFilter> searchAndFilterItemsConceptFilters) {
        HashMap<String, Integer> searchAndFilterItemsMap = new HashMap<>();

        for (ConceptFilter aConcept : searchAndFilterItemsConceptFilters) {
            searchAndFilterItemsMap.put(aConcept.getId(), aConcept.getArticleCount());
        }

        for (ConceptFilter aConcept : searchFilters) {
            if (searchAndFilterItemsMap.containsKey(aConcept.getId())) {
                aConcept.setFilteredArticleCount(searchAndFilterItemsMap.get(aConcept.getId()));
            } else
                aConcept.setFilteredArticleCount(0);
        }
    }

    //////////////////////////////////////////////////////        Live Server Process End        ///////////////////////////////////////////////////////////////////////////////////




    //////////////////////////////////////////////////////        Historical Server Process End        ///////////////////////////////////////////////////////////////////////////////////



    private List<RankedArticle> fetchArticlesHistorical(String queryId, List<String> searchItems, List<String> filterItems, L2CacheArticleParams articleParams){
        //POINT: This should not be called unless the articles are already cached and stored in L2
        List<RankedArticle> rankedArticles = new ArrayList<>();

        slf4jLogger.info("QueryId:%s | Fetch Historical Articles Called with Params:%s.", queryId, articleParams.toString());

        String key = CacheKeyGenerator.getL2CacheKey(searchItems, filterItems);
        L2CacheObject cachedResult = L2Cache.getL2CachedData(key, true);
        if (cachedResult != null) {
            return getRequiredArticlesHistorical(cachedResult, articleParams);
        } else {
            slf4jLogger.error("An article call to historical server should not be made directly (Without cache result).");
        }

        return rankedArticles;

    }

    private DBManagerResultSet fetchArticlesWithFiltersHistorical(String queryId, List<String> searchItems, List<String> filterItems, boolean fetchArticles, L2CacheArticleParams articleParams) {

        DBManagerResultSet historicalResultSet = new DBManagerResultSet();

        String key = CacheKeyGenerator.getL2CacheKey(searchItems,filterItems);
        L2CacheObject cachedResult = L2Cache.getL2CachedData(key,fetchArticles);

        if (cachedResult != null) {
            articleParams.setTotalArticlesCount(cachedResult.getArticlesCount());
            historicalResultSet.setConceptCounts(cachedResult.getFinalFilters());
            if (fetchArticles) {
                historicalResultSet.setRankedArticles(getRequiredArticlesHistorical(cachedResult, articleParams));
            }

        }
        else {

            //Steps:
            // 1: Get filters for S items
            // 2: Get articles count
            // 3: if Filters then get filters for S+F items
            //  3a: Concatenate S + F counts
            // 4: Get articles if required

            List<ConceptFilter> searchFilters = getSearchItemsFiltersHistorical(queryId, searchItems);
            Integer articlesCount = GraphDBCachedNatives.runQueryForArticleCount(searchItems, filterItems, true);

            List<ConceptFilter> searchAndFilterItemsConceptFilters = new ArrayList<>();
            if (filterItems.size() > 0) {
                searchAndFilterItemsConceptFilters = GraphDBCachedNatives.runSearchQueryForFilters(searchItems, filterItems, true);
                updateSearchFilterCount(searchFilters, searchAndFilterItemsConceptFilters);

            }

            List<Map> articlesMap;
            List<RankedArticle> topRankedArticle = new ArrayList<>();
            List<RankedArticle> bottomRankedArticle = new ArrayList<>();
            Integer bottomBatchSkip = -1;

            if(articlesCount<=HISTORICAL_ARTICLES_CACHE_LIMIT){
                articlesMap = GraphDBCachedNatives.runQueryForArticles(searchItems, filterItems, 0, HISTORICAL_ARTICLES_CACHE_LIMIT, true);
                List<RankedArticle> rankedArticles = prepareRankedArticles(articlesMap);
                topRankedArticle = rankedArticles;
            }
            else
            {
                bottomBatchSkip = articlesCount-(HISTORICAL_ARTICLES_CACHE_LIMIT /2);
                List<Map> articlesMapTop = GraphDBCachedNatives.runQueryForArticles(searchItems, filterItems, 0, HISTORICAL_ARTICLES_CACHE_LIMIT/2, true);
                List<Map> articlesMapBottom = GraphDBCachedNatives.runQueryForArticles(searchItems, filterItems,  bottomBatchSkip, HISTORICAL_ARTICLES_CACHE_LIMIT/2, true);
                topRankedArticle = prepareRankedArticles(articlesMapTop);
                bottomRankedArticle = prepareRankedArticles(articlesMapBottom);

            }


            //Caching Data fetched from Query
            L2CacheObject L2DataToBeCached = new L2CacheObject(articlesCount,searchFilters.size(), topRankedArticle.size(), bottomRankedArticle.size(),bottomBatchSkip, searchFilters, topRankedArticle, bottomRankedArticle);
            L2Cache.putL2CachedData(key, L2DataToBeCached);


            //Prepare result to return
            articleParams.setTotalArticlesCount(articlesCount);
            historicalResultSet.setConceptCounts(searchFilters);
            if(fetchArticles){
                historicalResultSet.setRankedArticles(getRequiredArticlesHistorical(L2DataToBeCached, articleParams));
            }
        }

        return historicalResultSet;


    }


    private List<RankedArticle> getRequiredArticlesHistorical(L2CacheObject articlesObject, L2CacheArticleParams params){

        List<RankedArticle> articles = new ArrayList<>();
        //TODO: Double Check the logic : Debugging cases

        Integer startIndex = params.getSkip();
        Integer endIndex= startIndex + params.getLimit();

        //If there is only top cached articles and last page is required then fetch end of list.
        if(articlesObject.getCachedArticleBottomCount()<1)
            endIndex = Math.min(endIndex,articlesObject.getCachedArticleTopCount());


        if(articlesObject.getCachedArticleTopCount()>=endIndex){
            articles = articlesObject.getTopRankedArticles().subList(startIndex, endIndex);
            return articles;
        }

        if(articlesObject.getBottomBatchSkip()+articlesObject.getCachedArticleBottomCount()> params.getSkip()){

            //Point: In case of last pages, this will help to fetch the results.
            articles = articlesObject.getBottomRankedArticles()
                    .subList(startIndex- articlesObject.getBottomBatchSkip()
                            , Math.min(endIndex-articlesObject.getBottomBatchSkip(), articlesObject.getCachedArticleBottomCount()));
            return articles;

        }
        return articles;


    }

    private List<ConceptFilter> getSearchItemsFiltersHistorical(String queryId, List<String> searchItems) {
        List<ConceptFilter> searchFilters;
        //First check in cache :
        String key = CacheKeyGenerator.getL2CacheKey(searchItems, new ArrayList<>());
        searchFilters = L2Cache.getL2CachedFilters(key);
        if(searchFilters!=null)
            return searchFilters;
        else{
            searchFilters = GraphDBCachedNatives.runSearchQueryForFilters(searchItems, new ArrayList<>(), true);
            Integer rank = searchFilters.size();
            for (ConceptFilter conceptFilter : searchFilters) {
                conceptFilter.setRank(rank);
                rank--;
            }
            return  searchFilters;
        }
    }


    }
