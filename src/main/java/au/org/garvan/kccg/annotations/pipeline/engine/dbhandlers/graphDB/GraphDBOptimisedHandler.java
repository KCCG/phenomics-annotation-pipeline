package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.constants.GraphDBConstants;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.ArticleWiseConcepts;

import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticleComparitor;
import com.github.jsonldjava.utils.Obj;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.Sets;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.values.*;
import javafx.beans.binding.ObjectExpression;
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

    public void createArticleQuery(Article article) {
        GraphDBNatives.createArticleQuery(article);
    }



    /*TODO [GRAPH]: Plan for making it generic filter search
    1: Update main function to fetch gene and phenotype articles



    */

    public DBManagerResultSet fetchArticlesWithFilters(String queryId, List<Pair<String, String>> searchItems, List<Pair<String, String>> filterItems, PaginationRequestParams qParams) {
        Set<String> shortListedArticles = new HashSet<>();
        LinkedHashMap<AnnotationType, JcQueryResult> collectedResults = new LinkedHashMap<>();


            List<String> annotationIDs = searchItems.stream()
                    .map(id-> id.getSecond())
                    .collect(Collectors.toList());
            if(annotationIDs.size()>0)
                shortListedArticles.addAll(runEntityQueryWithIds(collectedResults, annotationIDs));


        slf4jLogger.info(String.format("queryId:%s ShortListed articles with query. Count:%d", queryId, shortListedArticles.size()));

        // The function is called again for all shortlisted articles to get the entity counts. THIS MUST BE DONE ON ALL ARTICLES
        List<ArticleWiseConcepts> lstConcepts = getGlobalEntityCount(shortListedArticles);
        slf4jLogger.info(String.format("queryId:%s Fetched concepts. Count:%d", queryId, lstConcepts.size()));
        Map<String, ConceptFilter> returningConceptFilters = getConceptFilters(lstConcepts);


        // This is strictly for SUBSCRIPTION query check.
        shortListedArticles = checkAndProcessDateRange(queryId, collectedResults,filterItems, shortListedArticles);
        //Apply filters using graph DB
        // This flag will make sure that if filters are applied or not. The result set would be affected with this.

        boolean filterApplied = false;
        Set<String> filteredArticles = new HashSet<>();
        if(shortListedArticles.size() > 0) {
                List<String> filterIds = filterItems.stream()
                        .filter(f->  !f.getFirst().equals(AnnotationType.DATERANGE))
                        .map(p -> p.getSecond())
                        .collect(Collectors.toList());

                if (filterIds.size() > 0) {
                    slf4jLogger.info(String.format("queryId:%s Filters are provided to apply.", queryId));
                    filterApplied = true;
                    filteredArticles = runEntityFilterWithIds(collectedResults, filterIds, shortListedArticles);
                    slf4jLogger.info(String.format("queryId:%s Filtered articles with query. Count:%d", queryId, filteredArticles.size()));

                }
            }


        Set<String> resultantArticles;
        List<ArticleWiseConcepts> resultantConceptsForRanking;
        //If filters were there and applied, then use filtered articles for result array
        if(filterApplied) {
            resultantArticles = filteredArticles;
            resultantConceptsForRanking = getGlobalEntityCount(filteredArticles);
            Map<String, ConceptFilter> filteredConceptsConvertedtoFilters=getConceptFilters(resultantConceptsForRanking);

            for(Map.Entry<String, ConceptFilter> entry:returningConceptFilters.entrySet()){
                if(filteredConceptsConvertedtoFilters.containsKey(entry.getKey()))
                {
                    entry.getValue().setFilteredArticleCount(filteredConceptsConvertedtoFilters.get(entry.getKey()).getArticleCount());
                }
                else{
                    entry.getValue().setFilteredArticleCount(0);
                }
            }


        }
        else {
            resultantArticles = shortListedArticles;
            resultantConceptsForRanking = lstConcepts;
        }

        List<RankedArticle> rankedArticles = getRankedArticlesWithFilters(resultantArticles, searchItems, filterItems, resultantConceptsForRanking);
        slf4jLogger.info(String.format("queryId:%s Ranked articles. Count:%d", queryId, rankedArticles.size()));

        //Update total number of pages in query params.
        qParams.setTotalArticles(rankedArticles.size());
        qParams.setTotalPages (rankedArticles.size()% qParams.getPageSize()==0? rankedArticles.size()/qParams.getPageSize(): (rankedArticles.size()/qParams.getPageSize()) +1 );

        //Get required page and return and new object.
        DBManagerResultSet finalResultSet = new DBManagerResultSet();
        //Filter list is added
        finalResultSet.setConceptCounts(getRankedFilters(returningConceptFilters));
        finalResultSet.setRankedArticles(GraphDBNatives.getRequiredPage(rankedArticles, qParams));
        return finalResultSet;

    }

    private List<ConceptFilter> getRankedFilters(Map<String, ConceptFilter> returningConceptFilters){
        List<ConceptFilter> returnList = returningConceptFilters.values().stream().collect(Collectors.toList());
        returnList.sort(Comparator.comparing(ConceptFilter::getArticleCount).reversed());
        Integer maxRank = returnList.size();
        for (int r = 0; r<returnList.size(); r++)
        {
            returnList.get(r).setRank(maxRank-r);
        }
        return returnList;
    }

    private Map<String,ConceptFilter> getConceptFilters(List<ArticleWiseConcepts> lstConcepts) {
        Map<String,ConceptFilter> returnList = new HashMap<>();

        for(ArticleWiseConcepts awc: lstConcepts){
            ConceptFilter aConcept;
            if(returnList.containsKey(awc.getIdentifier())) {
                aConcept = returnList.get(awc.getIdentifier());
                aConcept.incrementArticleCount(1);
                aConcept.incrementFilteredArticleCount(1);
            }
            else{
                aConcept = new ConceptFilter();
                aConcept.setArticleCount(1);
                aConcept.setFilteredArticleCount(1);
                aConcept.setId(awc.getIdentifier());
                aConcept.setText(awc.getText());
                aConcept.setType(awc.getType());
                returnList.put(awc.getIdentifier(), aConcept);
            }
        }
        return returnList;
    }

    private Set<String> checkAndProcessDateRange(String queryId, LinkedHashMap<AnnotationType, JcQueryResult> collectedResults, List<Pair<String, String>> filterItems, Set<String> shortListedArticles) {
        List<String> dateRange = filterItems.stream()
                .filter(g -> g.getFirst().equals(SearchQueryParams.DATERANGE.toString()))
                .map(p -> p.getSecond())
                .collect(Collectors.toList());
        if(dateRange.isEmpty()) {
            return shortListedArticles;
        }
        else
        {
            slf4jLogger.info(String.format("queryId:%s Found dateRange filter. Subscription query suspected. DateRange :%s", queryId, dateRange.get(0)));
            String [] dates = dateRange.get(0).split(":");
            Long stDate = Long.parseLong(dates[0]);
            Long enDate = Long.parseLong(dates[1]);
            return runDateRangeQuery(collectedResults,stDate, enDate, shortListedArticles);
        }

    }


    /***
     * This is a generic method for entity search with type and list of ids.
     * @param collectedResults
     * @param lstEntityIds
     * @return
     */
    private Set<String> runEntityQueryWithIds(LinkedHashMap<AnnotationType, JcQueryResult> collectedResults, List<String> lstEntityIds) {
        List<IClause> queryClauses = new ArrayList<>();

        if (lstEntityIds.size() == 0) {
            collectedResults.put(AnnotationType.ENTITY, null);
            new ArrayList<>();
        }

        JcNode article = new JcNode("a");
        JcNode entity = new JcNode("e");
        JcString PMID = new JcString("PMID");
        queryClauses.add(MATCH.node(article).label("Article")
                .relation().out().type("CONTAINS")
                .node(entity).label("Entity"));

        queryClauses.add(WHERE.valueOf(entity.property(GraphDBConstants.ENTITY_NODE_ID)).IN(new JcCollection(new ArrayList<>(lstEntityIds))));
        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));

        //Should not come here in case of AND
        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
        collectedResults.put(AnnotationType.ENTITY, result);
        return GraphDBNatives.processQueryResultV1(result, AnnotationType.ENTITY);


    }

    private Set<String> runEntityFilterWithIds(LinkedHashMap<AnnotationType, JcQueryResult> collectedResults, List<String> lstEntityIds, Set<String> shortListedArticles) {
        JcNode article = new JcNode("a");
        List<IClause> queryClauses = new ArrayList<>();

        if (lstEntityIds.size() == 0) {
            collectedResults.put(AnnotationType.ENTITY, null);
            new ArrayList<>();
        }

        JcNode entity = new JcNode("e");
        JcString PMID = new JcString("PMID");
        queryClauses.add(MATCH.node(article).label("Article")
                .relation().out().type("CONTAINS")
                .node(entity).label("Entity"));
        for (int i= 0; i<lstEntityIds.size(); i++)
        {
            List<IClause> tempClauses = new ArrayList(queryClauses);

            tempClauses.add(WHERE.valueOf(entity.property(GraphDBConstants.ENTITY_NODE_ID)).EQUALS(lstEntityIds.get(i))
                    .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));

            tempClauses.add(RETURN.value(article.property("PMID")).AS(PMID));

            JcQueryResult result = GraphDBNatives.executeQueryClauses(tempClauses);
            //Have to get PMIDS without processQueryFunction here. Check if result is NOT NULL.
            if (result!=null)
                shortListedArticles = new HashSet<>(result.resultOf(PMID));

            if(shortListedArticles.size()==0 || i== lstEntityIds.size()-1){
                collectedResults.put(AnnotationType.ENTITY, result);
                return  GraphDBNatives.processQueryResultV1(result, AnnotationType.ENTITY);
            }
        }

        //It wont come here
        return new HashSet<>();
    }

    private Set<String> runDateRangeQuery(LinkedHashMap<AnnotationType, JcQueryResult> collectedResults, Long sDate, Long eDate, Set<String> shortListedArticles) {

        JcNode article = new JcNode("a");
        List<IClause> queryClauses = new ArrayList<>();
        JcString PMID = new JcString("PMID");
        queryClauses.add(MATCH.node(article).label("Article"));

        if (shortListedArticles.size() > 0) {
            queryClauses.add(WHERE.valueOf(article.property("ProcessingDate")).GT(sDate)
                    .AND().valueOf(article.property("ProcessingDate")).LTE(eDate)
                    .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));

        } else {
            queryClauses.add(WHERE.valueOf(article.property("ProcessingDate")).GT(sDate)
                    .AND().valueOf(article.property("ProcessingDate")).LTE(eDate));

        }
        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));
        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
        collectedResults.put(AnnotationType.DATERANGE, result);

        return GraphDBNatives.processQueryResult(result, SearchQueryParams.DATERANGE);

    }


    private List<ArticleWiseConcepts> getGlobalEntityCount(Set<String> shortListedArticles){
        //Prepare Query
        JcNode article = new JcNode("a");
        JcNode entity = new JcNode("e");

        List<IClause> queryClauses = new ArrayList<>();
        JcRelation c = new JcRelation("c");
        JcNumber nCount = new JcNumber("nCount");
        JcString PMID = new JcString("a.PMID");
        JcString entityID = new JcString("e."+ GraphDBConstants.ENTITY_NODE_ID);
        JcString entityType = new JcString("e."+ GraphDBConstants.ENTITY_NODE_TYPE);
        JcString entityText = new JcString("e."+ GraphDBConstants.ENTITY_NODE_TEXT);
        //GetRelations along with gene symbols
        queryClauses.add(MATCH.node(article).label("Article")
                .relation(c).out().type("CONTAINS")
                .node(entity).label("Entity"));

        //Where to limit it to found articles.
        queryClauses.add(WHERE.valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
        queryClauses.add(RETURN.value(article.property("PMID")));
        queryClauses.add(RETURN.value(entity.property(GraphDBConstants.ENTITY_NODE_ID)));
        queryClauses.add(RETURN.value(entity.property(GraphDBConstants.ENTITY_NODE_TYPE)));
        queryClauses.add(RETURN.value(entity.property(GraphDBConstants.ENTITY_NODE_TEXT)));
        queryClauses.add(RETURN.count().value(c).AS(nCount));

        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
        //Get sorted rank result. Result set will have all shortlisted articles, with updated rank from result of this query.
        List<ArticleWiseConcepts> lstConcepts = GraphDBNatives.processResultForConcepts( result.resultOf(PMID),result.resultOf(entityID),result.resultOf(entityType),result.resultOf(entityText), result.resultOf(nCount));
        return lstConcepts;

    }
    /***
     * This method gets short listed articles, rank them and return sorted list as a field of DBManagerResultSet
     * @return
     */
    private List<RankedArticle> getRankedArticlesWithFilters(
                                                            Set<String> finalArticles,
                                                            List<Pair<String, String>> searchItems,
                                                            List<Pair<String, String>> filterItems,
                                                            List<ArticleWiseConcepts> countedConcepts
                                                            ) {

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

        }
        // Now all counts are updated. Time to create the Ranked articles for complete result set.
        List<RankedArticle> countedArticles = new ArrayList<>();

        for (String PMID : finalArticles) {
            BigDecimal count= totalEntitiesInArticle.getOrDefault(PMID,BigDecimal.ZERO);
            Integer  sHits = totalSearchedItemsInArticle.getOrDefault(PMID, new ArrayList<>()).size();
            Integer fHits = totalFilteredItemsInArticle.getOrDefault(PMID, new ArrayList<>()).size();
            countedArticles.add(new RankedArticle(PMID, count, sHits, fHits, 0, null, null));
        }
        // Sort the collection with highest totalConceptHits at index 0.
        Collections.sort(countedArticles, new RankedArticleComparitor());

        return countedArticles;
    }

//    private String getEntityIdentifier(AnnotationType type){
//        String identifier = "";
//        switch (type){
//            case GENE:
//                identifier = "HGNCID";
//                break;
//            case PHENOTYPE:
//                identifier = GraphDBConstants.ENTITY_NODE_ID;
//        }
//        return identifier;
//    }

//    private  List<Object> getCorrectFormatList(AnnotationType type, List<Object> ids){
//        List<Object> returnList;
//        switch (type){
//            case GENE:
//                returnList = ids.stream().map(x-> Integer.valueOf((String)x)).collect(Collectors.toList());
//                break;
//            default:
//                returnList = ids;
//                break;
//        }
//        return returnList;
//    }





}
