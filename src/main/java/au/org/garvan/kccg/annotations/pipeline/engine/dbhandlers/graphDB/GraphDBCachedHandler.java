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
import com.google.common.collect.Sets;
import org.apache.el.parser.BooleanNode;
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
    private final Integer HISTORICAL_ARTICLES_CACHE_LIMIT= 10000;

    private static final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBCachedHandler.class);

    @Autowired
    private PersistentFiltersArticlesCache L2Cache;

    public void createArticleQuery(Article article) {
        GraphDBNatives.createArticleQuery(article);
    }

    /***
     * This is master method called from DatabaseManager. Based on historical choice, sub functions are called.
     * @param query
     * @param qParams
     * @param getFiltersAndCount : It distinguished between just a page call or new query.
     * @param cachedFilters : This will be updated in case of getFilters and Count is set true.
     * @return
     */
    public DBManagerResultSet fetchArticlesWithFilters(SearchQueryV2 query, PaginationRequestParams qParams, Boolean getFiltersAndCount, FiltersCacheObject cachedFilters) {
        DBManagerResultSet finalResultSet = new DBManagerResultSet();

        //Very Important for cache and filter
        Collections.sort(query.getSearchItems());
        Collections.sort(query.getFilterItems());

        if (!qParams.getIncludeHistorical()) {
            slf4jLogger.info(String.format("Query id"));
            finalResultSet = fetchArticlesWithFiltersLive(query, qParams, getFiltersAndCount);
            //Point: The responsibility to update filter object has been delegated to this class.
            if(getFiltersAndCount){
                cachedFilters.setArticlesCount(qParams.getTotalArticles());
                cachedFilters.setFinalFilters(finalResultSet.getConceptCounts());
            }
        } else {

            if(getFiltersAndCount) {
                //Process Live result
                SearchQueryV2 localLiveQuery = new SearchQueryV2(query.getQueryId() + "-LocalLive", query.getSearchItems(), query.getFilterItems(), query.getSearchAll());
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
                    localLiveArticles = fetchArticlesWithFiltersLive(query, localLiveParams, false).getRankedArticles();

                }
                else
                {
                    DBManagerResultSet localLiveResultSet = fetchArticlesWithFiltersLive(query, localLiveParams, true);
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
                        Integer foundLocally = localLiveArticleCount - (qParams.getPageSize() * (qParams.getPageNo() - 1));
                        pageOverLap = true;
                        historicalArticlesParams.setSkip(0);
                        historicalArticlesParams.setLimit(qParams.getPageSize()- foundLocally);
                    } else {
                        pageOverLap = false;
                        historicalArticlesParams.setSkip( (qParams.getPageSize() * (qParams.getPageNo() - 1)) - localLiveArticleCount);
                        historicalArticlesParams.setLimit(qParams.getPageSize());
                    }


                }

                DBManagerResultSet historicalResultSet = fetchArticlesWithFiltersHistorical(query, !gotArticlesFromLocal, historicalArticlesParams);

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
                    if (liveArticlesCount > (qParams.getPageSize() * (qParams.getPageNo() - 1))) {
                        Integer foundLocally = liveArticlesCount - (qParams.getPageSize() * (qParams.getPageNo() - 1));
                        pageOverLap = true;
                        historicalArticlesParams.setSkip(0);
                        historicalArticlesParams.setLimit(qParams.getPageSize()- foundLocally);
                    } else {
                        pageOverLap = false;
                        historicalArticlesParams.setSkip( (qParams.getPageSize() * (qParams.getPageNo() - 1)) - liveArticlesCount);
                        historicalArticlesParams.setLimit(qParams.getPageSize());
                    }


                }

                List<RankedArticle > finalRankedArticles = new ArrayList<>();
                List<RankedArticle> localLiveArticles = new ArrayList<>();

                if(gotArticlesFromLocal || pageOverLap){
                    localLiveArticles = fetchArticlesWithFiltersLive(query,qParams,false).getRankedArticles();
                }
                //Prepare the article set
                if(gotArticlesFromLocal){
                    finalRankedArticles = localLiveArticles;
                }
                else{
                    List<RankedArticle> historicalArticles =  fetchArticlesHistorical(query, historicalArticlesParams);
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
        //Point: Creating a new list (clone) as we intend to modify the counts
        Map<String,ConceptFilter> indexedHistoricalFilters = historicalFilters.stream().collect(Collectors.toMap(ConceptFilter::getId, s->s.clone()));

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

    private DBManagerResultSet fetchArticlesWithFiltersLive(SearchQueryV2 query, PaginationRequestParams qParams, Boolean getFiltersAndCount) {

        //Steps:
        // 1: Get filters for S items
        //  (Check cache, if miss run query)
        // 2: Get articles count
        // 3: if Filters then get filters for S+F items
        //  3a: Concatenate S + F counts
        // 4: Get required page


        slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Live called | SearchItemsCount:%d | FilterItemsCount:%d | PageSize:%d | PageNo:%d | GetFiltersAndCount:%s",
                query.getQueryId(), query.getSearchItems().size(), query.getFilterItems().size(), qParams.getPageSize(), qParams.getPageNo(), getFiltersAndCount.toString()));

        DBManagerResultSet finalResultSet = new DBManagerResultSet();

        if (getFiltersAndCount) {

            //Step 1: Getting filters for only search items. These are called global filters. Check them in cache first
            List<ConceptFilter> searchFilters = getSearchItemsFilters(query.getQueryId(), query.getSearchItems(), qParams, query.getSearchAll());
            //Step 2: Getting article counts. This would be based on search and filter items. Tell Natives that it is a live call
            //Update query params
            Integer articlesCount = GraphDBCachedNatives.runQueryForArticleCount(query.getSearchItems(), query.getFilterItems(), false, new ArrayList<>(),query.getSearchAll());
            qParams.setTotalArticles(articlesCount);
            qParams.setTotalPages((int) Math.ceil((double) articlesCount / qParams.getPageSize()));

            //Step 3: If there are filters in query then find filters based on search and filter items.
            //Concatenate filters count.
            List<ConceptFilter> searchAndFilterItemsConceptFilters = new ArrayList<>();
            List<ConceptFilter> consolidatedConceptFilters = new ArrayList<>();
            if (query.getFilterItems().size() > 0) {
                searchAndFilterItemsConceptFilters = GraphDBCachedNatives.runSearchQueryForFilters(query.getSearchItems(), query.getFilterItems(), false, new ArrayList<>(),query.getSearchAll());
                consolidatedConceptFilters = updateSearchFilterCount(searchFilters, searchAndFilterItemsConceptFilters);

            }
            else
            {
                consolidatedConceptFilters = searchFilters;
            }

            finalResultSet.setConceptCounts(consolidatedConceptFilters);

        }

        List<Map> articlesMap = GraphDBCachedNatives.runQueryForArticles(query.getSearchItems(), query.getFilterItems(), ((qParams.getPageNo() - 1) * qParams.getPageSize()), qParams.getPageSize(), false, new ArrayList<>(), query.getSearchAll());
        finalResultSet.setRankedArticles(prepareRankedArticles(articlesMap));

        if(getFiltersAndCount)
        {
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Live completed. Filters count:%d | Total Articles count:%d | Articles count:%d", query.getQueryId(), finalResultSet.getConceptCounts().size(), qParams.getTotalArticles(), finalResultSet.getRankedArticles().size()));

        }
        else {
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Live completed. Articles count:%d", query.getQueryId(), finalResultSet.getRankedArticles().size()));
        }

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

    private List<ConceptFilter> getSearchItemsFilters(String queryId, List<String> searchItems, PaginationRequestParams qParams, Boolean searchAll) {
        List<ConceptFilter> searchFilters;
        //First check in cache : L1 cache was missed for whole query, however there is possibility that sear items based filters were fetched.
        SearchQueryV2 query = new SearchQueryV2(queryId, searchItems, new ArrayList<>(), searchAll);
        FiltersCacheObject cachedFilters = FiltersResponseCache.getFilters(query, qParams.getIncludeHistorical());
        if (cachedFilters != null)
            searchFilters = cachedFilters.getFinalFilters();
        else {
            //Make sure that it goes to live server
            searchFilters = GraphDBCachedNatives.runSearchQueryForFilters(searchItems, new ArrayList<>(), false, new ArrayList<>(), searchAll);
            Integer rank = searchFilters.size();
            for (ConceptFilter conceptFilter : searchFilters) {
                conceptFilter.setRank(rank);
                rank--;
            }
        }
        return searchFilters;
    }

    private List<ConceptFilter> updateSearchFilterCount(List<ConceptFilter> searchFilters, List<ConceptFilter> searchAndFilterItemsConceptFilters) {

        //Point: Creating new filter objects as these are cached entries.
        List<ConceptFilter> updatedFilters = searchFilters.stream().map(s->s.clone()).collect(Collectors.toList());
        HashMap<String, Integer> searchAndFilterItemsMap = new HashMap<>();

        for (ConceptFilter aConcept : searchAndFilterItemsConceptFilters) {
            searchAndFilterItemsMap.put(aConcept.getId(), aConcept.getArticleCount());
        }

        for (ConceptFilter aConcept : updatedFilters) {
            if (searchAndFilterItemsMap.containsKey(aConcept.getId())) {
                aConcept.setFilteredArticleCount(searchAndFilterItemsMap.get(aConcept.getId()));
            } else
                aConcept.setFilteredArticleCount(0);
        }
        return updatedFilters;
    }

    //////////////////////////////////////////////////////        Live Server Process End        ///////////////////////////////////////////////////////////////////////////////////




    //////////////////////////////////////////////////////        Historical Server Process End        ///////////////////////////////////////////////////////////////////////////////////



    private List<RankedArticle> fetchArticlesHistorical(SearchQueryV2 query, L2CacheArticleParams articleParams){
        //POINT: This should not be called unless the articles are already cached and stored in L2
        List<RankedArticle> rankedArticles = new ArrayList<>();

        slf4jLogger.info(String.format("QueryId:%s | Fetch Historical Articles Called with Params:%s.", query.getQueryId(), articleParams.toString()));

        String key = CacheKeyGenerator.getL2CacheKey(query.getSearchItems(), query.getFilterItems(), query.getSearchAll());
        L2CacheObject cachedResult = L2Cache.getL2CachedData(key, true);
        if (cachedResult != null) {
            return getRequiredArticlesHistorical(cachedResult, articleParams);
        } else {
            slf4jLogger.error("An article call to historical server should not be made directly (Without cache result).");
        }

        return rankedArticles;

    }

    private DBManagerResultSet fetchArticlesWithFiltersHistorical(SearchQueryV2 query, Boolean fetchArticles, L2CacheArticleParams articleParams) {

        slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical called | SearchItemsCount:%d | FilterItemsCount:%d | Fetch Articles flag:%s | Article Params :%s ",
                query.getQueryId(), query.getSearchItems().size(), query.getFilterItems().size(), fetchArticles.toString(), articleParams.toString()));

        DBManagerResultSet historicalResultSet = new DBManagerResultSet();

        String key = CacheKeyGenerator.getL2CacheKey(query.getSearchItems(), query.getFilterItems(), query.getSearchAll());
        L2CacheObject cachedResult = L2Cache.getL2CachedData(key,fetchArticles);

        if (cachedResult != null) {
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical got cached data", query.getQueryId()));
            articleParams.setTotalArticlesCount(cachedResult.getArticlesCount());
            historicalResultSet.setConceptCounts(cachedResult.getFinalFilters());
            if (fetchArticles) {
                historicalResultSet.setRankedArticles(getRequiredArticlesHistorical(cachedResult, articleParams));
            }

        }
        else {

            //Point: Updated logic for speedup - Try to see if L2 has document ids with any combination of filters except main query.
            List<String> shortListedIDs = new ArrayList<>();
            if(query.getFilterItems().size()>1) {
                shortListedIDs = tryAndHitOneCachedFilterCombo(query);
                if(shortListedIDs.size()>0)
                    slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical found shortlisted PMIDs for a sub query.", query.getQueryId()));

            }

            ////

            //Steps:
            // 1: Get filters for S items
            // 2: Get articles count
            // 3: if Filters then get filters for S+F items
            //  3a: Concatenate S + F counts
            // 4: Get articles if required
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step1: Getting search items filters.", query.getQueryId()));
            List<ConceptFilter> searchFilters = getSearchItemsFiltersHistorical(query.getQueryId(), query.getSearchItems(), query.getSearchAll());

            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step1: Got search items filters:%d.",query.getQueryId(), searchFilters.size()));

            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step2: Getting articles count.",query.getQueryId()));
            Integer articlesCount = GraphDBCachedNatives.runQueryForArticleCount(query.getSearchItems(), query.getFilterItems(), true, shortListedIDs, query.getSearchAll());
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step2: Got articles count:%d.",query.getQueryId(), articlesCount));

            List<ConceptFilter> searchAndFilterItemsConceptFilters = new ArrayList<>();
            List<ConceptFilter> consolidatedFilters = new ArrayList<>();

            if (query.getFilterItems().size() > 0) {
                slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step3: Getting search+filter items filters.",query.getQueryId()));
                searchAndFilterItemsConceptFilters = GraphDBCachedNatives.runSearchQueryForFilters(query.getSearchItems(), query.getFilterItems(), true, shortListedIDs, query.getSearchAll());
                slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step3: Got search+filter items filters:%d.",query.getQueryId(), searchAndFilterItemsConceptFilters.size()));
                consolidatedFilters= updateSearchFilterCount(searchFilters, searchAndFilterItemsConceptFilters);

            }
            else{
                consolidatedFilters = searchFilters;
            }

            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step4: Getting articles.",query.getQueryId()));

            List<Map> articlesMap;
            List<RankedArticle> topRankedArticle = new ArrayList<>();
            List<RankedArticle> bottomRankedArticle = new ArrayList<>();
            Integer bottomBatchSkip = -1;

            if(articlesCount<=HISTORICAL_ARTICLES_CACHE_LIMIT){
                articlesMap = GraphDBCachedNatives.runQueryForArticles(query.getSearchItems(), query.getFilterItems(), 0, HISTORICAL_ARTICLES_CACHE_LIMIT, true, shortListedIDs,query.getSearchAll());
                List<RankedArticle> rankedArticles = prepareRankedArticles(articlesMap);
                topRankedArticle = rankedArticles;
            }
            else
            {
                bottomBatchSkip = articlesCount-(HISTORICAL_ARTICLES_CACHE_LIMIT /2);
                List<Map> articlesMapTop = GraphDBCachedNatives.runQueryForArticles( query.getSearchItems() , query.getFilterItems(), 0, HISTORICAL_ARTICLES_CACHE_LIMIT/2, true, shortListedIDs, query.getSearchAll());
                List<Map> articlesMapBottom = GraphDBCachedNatives.runQueryForArticles(query.getSearchItems() , query.getFilterItems(),  bottomBatchSkip, HISTORICAL_ARTICLES_CACHE_LIMIT/2, true, shortListedIDs, query.getSearchAll());
                topRankedArticle = prepareRankedArticles(articlesMapTop);
                bottomRankedArticle = prepareRankedArticles(articlesMapBottom);

            }
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step4: Got articles. Top:%d. | Bottom:%d ",query.getQueryId(), topRankedArticle.size(), bottomRankedArticle.size()));



            //Caching Data fetched from Query
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step5: Caching Data",query.getQueryId()));

            L2CacheObject L2DataToBeCached = new L2CacheObject(key, articlesCount,consolidatedFilters.size(), topRankedArticle.size(), bottomRankedArticle.size(), bottomBatchSkip,null, consolidatedFilters, topRankedArticle, bottomRankedArticle);
            L2Cache.putL2CachedData(L2DataToBeCached);

            //Prepare result to return
            articleParams.setTotalArticlesCount(articlesCount);
            historicalResultSet.setConceptCounts(consolidatedFilters);
            if(fetchArticles){
                slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical Step6: Getting historical articles.",query.getQueryId()));
                historicalResultSet.setRankedArticles(getRequiredArticlesHistorical(L2DataToBeCached, articleParams));
            }
        }


        if(fetchArticles)
        {
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical completed. Filters count:%d | Total Articles count:%d | Articles count:%d", query.getQueryId(), historicalResultSet.getConceptCounts().size(), articleParams.getTotalArticlesCount(), historicalResultSet.getRankedArticles().size()));

        }
        else {
            slf4jLogger.info(String.format("QueryId:%s GraphDBCachedHandler-Historical completed. Filters count:%d", query.getQueryId(), historicalResultSet.getConceptCounts().size()));
        }

        return historicalResultSet;


    }


    private List<RankedArticle> getRequiredArticlesHistorical(L2CacheObject articlesObject, L2CacheArticleParams params){

        List<RankedArticle> articles = new ArrayList<>();
        //TODO: Double Check the logic : Debugging cases

        Integer startIndex = params.getSkip();
        Integer endIndex= startIndex + params.getLimit();

        //If there is only top cached articles and last page is required then fetch end of list.
        if(articlesObject.getBottomArticlesCount()<1)
            endIndex = Math.min(endIndex,articlesObject.getTopArticlesCount());


        if(articlesObject.getTopArticlesCount()>=endIndex && endIndex>startIndex){
            articles = articlesObject.getTopRankedArticles().subList(startIndex, endIndex);
            return articles;
        }

        if(articlesObject.getBottomBatchSkip()+articlesObject.getBottomArticlesCount()> params.getSkip()){

            //Point: In case of last pages, this will help to fetch the results.
            articles = articlesObject.getBottomRankedArticles()
                    .subList(startIndex- articlesObject.getBottomBatchSkip()
                            , Math.min(endIndex-articlesObject.getBottomBatchSkip(), articlesObject.getBottomArticlesCount()));
            return articles;

        }
        return articles;


    }

    public  List<ConceptFilter> getSearchItemsFiltersHistorical(String queryId, List<String> searchItems, Boolean searchAll) {
        List<ConceptFilter> searchFilters;
        //First check in cache :
        String key = CacheKeyGenerator.getL2CacheKey(searchItems, new ArrayList<>(), searchAll);
        L2CacheObject l2CacheObject = L2Cache.getL2CachedData(key, false);

        if(l2CacheObject!=null)
            searchFilters =  l2CacheObject.getFinalFilters();
        else{
            searchFilters = GraphDBCachedNatives.runSearchQueryForFilters(searchItems, new ArrayList<>(), true, new ArrayList<>(), searchAll);
            Integer rank = searchFilters.size();
            for (ConceptFilter conceptFilter : searchFilters) {
                conceptFilter.setRank(rank);
                rank--;
            }

        }
        return  searchFilters;
    }

    private List<String> tryAndHitOneCachedFilterCombo(SearchQueryV2 query){
        List<String> filterIDs = new ArrayList<>();
        List<List<String>> possibleHistoricFilters = getFiltersSubset(query.getFilterItems());

        for(List<String> historicFilter : possibleHistoricFilters)
        {
            String speedKey = CacheKeyGenerator.getL2CacheKey( query.getSearchItems(), historicFilter, query.getSearchAll());
            filterIDs  = L2Cache.checkIfL2CachedDataIsThereForSpeedQuery(speedKey, HISTORICAL_ARTICLES_CACHE_LIMIT);
            if(filterIDs.size()>0){
                return filterIDs;
            }
        }
        return filterIDs;

    }

    public List<List<String>> getFiltersSubset(List<String> filters){
        List<List<String>> returnList = new ArrayList<>();
        Set<String> masterSet = Sets.newHashSet(filters);
        Set<Set<String>> powerSet = Sets.powerSet(masterSet);
        for(Set oneSet: powerSet){
            if(!oneSet.isEmpty() && (oneSet.size()!=filters.size())){
                List<String> oneFilterCombination = new ArrayList();
                oneFilterCombination.addAll(oneSet);
                Collections.sort(oneFilterCombination);
                returnList.add(oneFilterCombination);
            }
        }
        Collections.sort(returnList, new Comparator<List>(){
            public int compare(List a1, List a2) {
                return a2.size() - a1.size(); // assumes you want biggest to smallest
            }
        });


        return returnList;


    }



}

