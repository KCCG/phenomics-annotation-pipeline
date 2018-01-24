package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.ArticleWiseConcepts;

import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticleComparitor;
import edu.stanford.nlp.util.Sets;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.values.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/***
 * This class has been written to improve the search functionality.
 * It is first step towards generic entities search based on filters.
 */
@Component
public class GraphDBOptimisedHandler {
    private static final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBOptimisedHandler.class);


    public DBManagerResultSet fetchArticlesWithFilters(String queryId, List<Pair<String, String>> searchItems, List<Pair<String, String>> filterItems, PaginationRequestParams qParams) {
        Set<String> shortListedArticles;
        LinkedHashMap<AnnotationType, JcQueryResult> collectedResults = new LinkedHashMap<>();


        //Get all genes IDs, The search will be applied on ID field. Search V1.0
        List<Integer> geneIds = searchItems.stream()
                .filter(g -> g.getFirst().equals(AnnotationType.GENE.toString()))
                .map(p -> Integer.valueOf(p.getSecond()))
                .collect(Collectors.toList());

        //These are the short listed articles based on search items. THis operation is OR, that means article should contain at least 1 search item.
        shortListedArticles = runEntityQueryWithIds(collectedResults,geneIds, AnnotationType.GENE);
        slf4jLogger.info(String.format("queryId:%s ShortListed articles with query. Count:%d", queryId, shortListedArticles.size()));

        //Apply filters using graph DB

        // This flag will make sure that if filters are applied or not. The result set would be affected with this.
        boolean filterApplied = false;
        Set<String> filteredArticles = new HashSet<>();
        if(filterItems.size()>0 && shortListedArticles.size()>0)
        {
            slf4jLogger.info(String.format("queryId:%s Filter is provided to apply.", queryId));

            filterApplied = true;
            List<Integer> filterGeneIds = filterItems.stream()
                    .filter(g -> g.getFirst().equals(AnnotationType.GENE.toString()))
                    .map(p -> Integer.valueOf(p.getSecond()))
                    .collect(Collectors.toList());
            filteredArticles = runEntityFilterWithIds(collectedResults,filterGeneIds,AnnotationType.GENE,shortListedArticles);
            slf4jLogger.info(String.format("queryId:%s Filtered articles with query. Count:%d", queryId, filteredArticles.size()));

        }

        // The function is called again for all shortlisted articles to get the entity counts. THIS MUST BE DONE ON ALL ARTICLES
        List<ArticleWiseConcepts> lstConcepts = getGlobalEntityCount(shortListedArticles, getEntityIdentifier(AnnotationType.GENE));
        slf4jLogger.info(String.format("queryId:%s Fetched concepts. Count:%d", queryId, lstConcepts.size()));

        // This is the inception of result set. This has been introduced to fetch all articles, as well as filters [Genes count] from graph DB.
        DBManagerResultSet dbManagerResultSet = getRankedArticlesWithFilters(shortListedArticles, filteredArticles, searchItems, filterItems, lstConcepts, filterApplied);
        slf4jLogger.info(String.format("queryId:%s Ranked articles. Count:%d", queryId, dbManagerResultSet.getRankedArticles().size()));

        //Update total number of pages in query params.
        qParams.setTotalArticles(dbManagerResultSet.getRankedArticles().size());
        qParams.setTotalPages (dbManagerResultSet.getRankedArticles().size()% qParams.getPageSize()==0? dbManagerResultSet.getRankedArticles().size()/qParams.getPageSize(): (dbManagerResultSet.getRankedArticles().size()/qParams.getPageSize()) +1 );

        //Get required page and return and new object.
        DBManagerResultSet finalResultSet = new DBManagerResultSet();
        //Filter list is added
        finalResultSet.setGeneCounts(dbManagerResultSet.getGeneCounts());
        finalResultSet.setRankedArticles(GraphDBNatives.getRequiredPage(dbManagerResultSet.getRankedArticles(), qParams));
        return finalResultSet;

    }


    /***
     * This is a generic method for entity search with type and list of ids.
     * @param collectedResults
     * @param lstEntityIds
     * @param type
     * @return
     */
    private Set<String> runEntityQueryWithIds(LinkedHashMap<AnnotationType, JcQueryResult> collectedResults, List<Integer> lstEntityIds, AnnotationType type) {
        List<IClause> queryClauses = new ArrayList<>();

        if (lstEntityIds.size() == 0) {
            collectedResults.put(type, null);
            new ArrayList<>();
        }
        //Get node identifier for this type of entity.
        String identifier = getEntityIdentifier(type);

        JcNode article = new JcNode("a");
        JcNode entity = new JcNode("e");
        JcString PMID = new JcString("PMID");
        queryClauses.add(MATCH.node(article).label("Article")
                .relation().out().type("CONTAINS")
                .node(entity).label("Entity"));

        queryClauses.add(WHERE.valueOf(entity.property(identifier)).IN(new JcCollection(new ArrayList<>(lstEntityIds))));
        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));

        //Should not come here in case of AND
        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
        collectedResults.put(type, result);
        return GraphDBNatives.processQueryResult(result, SearchQueryParams.GENES);


    }

    private Set<String> runEntityFilterWithIds(LinkedHashMap<AnnotationType, JcQueryResult> collectedResults, List<Integer> lstEntityIds, AnnotationType type , Set<String> shortListedArticles) {
        JcNode article = new JcNode("a");
        List<IClause> queryClauses = new ArrayList<>();

        if (lstEntityIds.size() == 0) {
            collectedResults.put(type, null);
            return GraphDBNatives.processQueryResult(null, SearchQueryParams.GENES);
        }
        String identifier = getEntityIdentifier(type);
        JcNode entity = new JcNode("e");
        JcString PMID = new JcString("PMID");
        queryClauses.add(MATCH.node(article).label("Article")
                .relation().out().type("CONTAINS")
                .node(entity).label("Entity"));
        for (int i= 0; i<lstEntityIds.size(); i++)
        {
            List<IClause> tempClauses = new ArrayList(queryClauses);

            tempClauses.add(WHERE.valueOf(entity.property(identifier)).EQUALS(lstEntityIds.get(i))
                    .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));

            tempClauses.add(RETURN.value(article.property("PMID")).AS(PMID));

            JcQueryResult result = GraphDBNatives.executeQueryClauses(tempClauses);
            //Have to get PMIDS without processQueryFunction here. Check if result is NOT NULL.
            if (result!=null)
                shortListedArticles = new HashSet<>(result.resultOf(PMID));

            if(shortListedArticles.size()==0 || i== lstEntityIds.size()-1){
                collectedResults.put(type, result);
                return  GraphDBNatives.processQueryResult(result, SearchQueryParams.GENES);
            }
        }

        //It wont come here
        return new HashSet<>();
    }


    private List<ArticleWiseConcepts> getGlobalEntityCount(Set<String> shortListedArticles, String identifier){
        //Prepare Query
        JcNode article = new JcNode("a");
        JcNode entity = new JcNode("e");

        List<IClause> queryClauses = new ArrayList<>();
        JcRelation c = new JcRelation("c");
        JcNumber nCount = new JcNumber("nCount");
        JcString PMID = new JcString("a.PMID");
        JcString entitySymbol = new JcString("e."+ identifier);
        //GetRelations along with gene symbols
        queryClauses.add(MATCH.node(article).label("Article")
                .relation(c).out().type("CONTAINS")
                .node(entity).label("Entity"));

        //Where to limit it to found articles.
        queryClauses.add(WHERE.valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
        queryClauses.add(RETURN.value(article.property("PMID")));
        queryClauses.add(RETURN.value(entity.property(identifier)));
        queryClauses.add(RETURN.count().value(c).AS(nCount));

        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
        //Get sorted rank result. Result set will have all shortlisted articles, with updated rank from result of this query.
        List<ArticleWiseConcepts> lstConcepts = GraphDBNatives.processResultForConcepts( result.resultOf(PMID),result.resultOf(entitySymbol),  result.resultOf(nCount));
        return lstConcepts;

    }
    /***
     * This method gets short listed articles, rank them and return sorted list as a field of DBManagerResultSet
     * @param shortListedPMIDs
     * @param countedConcepts
     * @return
     */
    private DBManagerResultSet getRankedArticlesWithFilters(Set<String> shortListedPMIDs, Set<String> filteredArticles, List<Pair<String, String>> searchItems, List<Pair<String, String>> filterItems, List<ArticleWiseConcepts> countedConcepts, Boolean filterApplied) {
        /*Ranking Logic:
        // Currently Ranking is based on annotations attached with an article.
        // However with filter queries it is possible that some of the articles do not have any annotation.
        // So result set contains all elements.
        */

        List<String> searchIds = searchItems.stream()
                .map(p -> p.getSecond())
                .collect(Collectors.toList());

        List<String> filterIds = filterItems.stream()
                .map(p -> p.getSecond())
                .collect(Collectors.toList());

        //Article and Entities count
        Map<String, BigDecimal> totalEntitiesInArticle = new HashMap<>();
        Map<String, List<String>> totalSearchedItemsInArticle = new HashMap<>();
        Map<String, List<String>> totalFilteredItemsInArticle = new HashMap<>();

        //Entities count for filter
        Map<String, Integer> countOfArticlesWithEntity = new HashMap<>();

        for (ArticleWiseConcepts articleWiseConcepts : countedConcepts) {
            if (totalEntitiesInArticle.containsKey(articleWiseConcepts.getPMID())) {
                BigDecimal tCount = totalEntitiesInArticle.get(articleWiseConcepts.getPMID());
                totalEntitiesInArticle.put(articleWiseConcepts.getPMID(), articleWiseConcepts.getCount().add(tCount));
            } else {
                totalEntitiesInArticle.put(articleWiseConcepts.getPMID(), articleWiseConcepts.getCount());
            }

            if (searchIds.contains(articleWiseConcepts.getIdentifier())) {
                //This is a searched concept, now we can check if this article is counted for it or not.
                if (totalSearchedItemsInArticle.containsKey(articleWiseConcepts.getPMID())) {
                    if (!totalSearchedItemsInArticle.get(articleWiseConcepts.getPMID()).contains(articleWiseConcepts.getIdentifier())) {
                        totalSearchedItemsInArticle.get(articleWiseConcepts.getPMID()).add(articleWiseConcepts.getIdentifier());
                    }
                } else {
                    totalSearchedItemsInArticle.put(articleWiseConcepts.getPMID(), new ArrayList<>(Arrays.asList(articleWiseConcepts.getIdentifier())));
                }
            }

            if (filterIds.contains(articleWiseConcepts.getIdentifier())) {
                //This is a filtered concept, now we can check if this article is counted for it or not.
                if (totalFilteredItemsInArticle.containsKey(articleWiseConcepts.getPMID())) {
                    if (!totalFilteredItemsInArticle.get(articleWiseConcepts.getPMID()).contains(articleWiseConcepts.getIdentifier())) {
                        totalFilteredItemsInArticle.get(articleWiseConcepts.getPMID()).add(articleWiseConcepts.getIdentifier());
                    }
                } else {
                    totalFilteredItemsInArticle.put(articleWiseConcepts.getPMID(), new ArrayList<>(Arrays.asList(articleWiseConcepts.getIdentifier())));
                }
            }

            if (countOfArticlesWithEntity.containsKey(articleWiseConcepts.getIdentifier())) {
                Integer tCount = countOfArticlesWithEntity.get(articleWiseConcepts.getIdentifier());
                countOfArticlesWithEntity.put(articleWiseConcepts.getIdentifier(), tCount + 1);
            } else {
                countOfArticlesWithEntity.put(articleWiseConcepts.getIdentifier(), 1);
            }
        }
        // Now all counts are updated. Time to create the Ranked articles for complete result set.
        List<RankedArticle> countedArticles = new ArrayList<>();
        Set<String> resultantArticles;

        //If filters were there and applied, then use filtered articles for result array
        if(filterApplied)
            resultantArticles= Sets.intersection(shortListedPMIDs, filteredArticles);
        else
            resultantArticles = shortListedPMIDs;

        for (String PMID : resultantArticles) {
            BigDecimal count= totalEntitiesInArticle.getOrDefault(PMID,BigDecimal.ZERO);
            Integer  sHits = totalSearchedItemsInArticle.getOrDefault(PMID, new ArrayList<>()).size();
            Integer fHits = totalFilteredItemsInArticle.getOrDefault(PMID, new ArrayList<>()).size();
            countedArticles.add(new RankedArticle(PMID, count, sHits, fHits, 0, null, null));
        }
        // Sort the collection with highest totalConceptHits at index 0.
        Collections.sort(countedArticles, new RankedArticleComparitor());
        DBManagerResultSet result = new DBManagerResultSet();
        result.setGeneCounts(countOfArticlesWithEntity);
        result.setRankedArticles(countedArticles);

        return result;
    }

    private String getEntityIdentifier(AnnotationType type){
        String identifier = "";
        switch (type){
            case GENE:
                identifier = "HGNCID";
                break;
        }
        return identifier;
    }





}
