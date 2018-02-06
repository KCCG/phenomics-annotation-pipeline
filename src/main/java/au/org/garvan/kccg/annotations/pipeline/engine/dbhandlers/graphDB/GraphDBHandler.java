package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.model.query.*;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.ArticleWiseConcepts;
import com.google.common.base.Strings;
import edu.stanford.nlp.util.Sets;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.*;
import iot.jcypher.query.values.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 30/10/17.
 */
@Deprecated
@Component
public class GraphDBHandler {


    private final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBHandler.class);



    public void createArticleQuery(Article article) {
        GraphDBNatives.createArticleQuery(article);
    }


    /*
    ***********************************************
    The following section is for search operations.
    ***********************************************
     */


//    public DBManagerResultSet fetchArticles(Map<SearchQueryParams, Object> params, PaginationRequestParams qParams) {
//        Set<String> shortListedArticles = new HashSet<>();
//
//        //Results storage for all params, and later will be sorted out.
//        LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults = new LinkedHashMap<>();
//
//        if (params.containsKey(SearchQueryParams.AUTHOR)) {
//            Author author = (Author) params.get(SearchQueryParams.AUTHOR);
//            shortListedArticles = runAuthorQuery(collectedResults, author, shortListedArticles);
//            if (shortListedArticles.size() == 0)
//                return new DBManagerResultSet();
//        }
//
//
//        if (params.containsKey(SearchQueryParams.PUBLICATION)) {
//            Publication publication = (Publication) params.get(SearchQueryParams.PUBLICATION);
//            shortListedArticles = runPublicationQuery(collectedResults, publication, shortListedArticles);
//            if (shortListedArticles.size() == 0)
//                return new DBManagerResultSet();
//        }
//
//        if (params.containsKey(SearchQueryParams.GENES)) {
//            Pair<String, List<String>> gene = (Pair<String, List<String>>) params.get(SearchQueryParams.GENES);
//            shortListedArticles = runGenesQueryCompact(collectedResults, gene.getFirst(), gene.getSecond(), shortListedArticles);
//            if (shortListedArticles.size() == 0)
//                return new DBManagerResultSet();
//        }
//
//        if (params.containsKey(SearchQueryParams.DATERANGE)) {
//            Pair<Long, Long> dateRange = (Pair<Long, Long>) params.get(SearchQueryParams.DATERANGE);
//            shortListedArticles = runDateRangeQuery(collectedResults, dateRange.getFirst(), dateRange.getSecond(), shortListedArticles);
//            if (shortListedArticles.size() == 0)
//                return new DBManagerResultSet();
//        }
//
//
//        // If it comes to this line, that means there is something to return.
//        DBManagerResultSet finalResultSet = new DBManagerResultSet();
//        finalResultSet.setRankedArticles();
//        return finalResultSet;
//
//    }


//    /***
//     * This method is added to fetch all the counts of annotations for two purposes.
//        1: Article counts to rank the articles for pagination
//        2: Fetch Gene Symbols so that filtration can be provided from backend.
//
//     * @param shortListedArticles
//     * @param qParams
//     * @return
//     */
//    private DBManagerResultSet paginateSearchResults(Set<String> shortListedArticles, PaginationRequestParams qParams) {
//
//        //Prepare Query
//        qParams.setTotalArticles(shortListedArticles.size());
//        JcNode article = new JcNode("a");
//        JcNode entity = new JcNode("e");
//
//        List<IClause> queryClauses = new ArrayList<>();
//        JcRelation c = new JcRelation("c");
//        JcNumber nCount = new JcNumber("nCount");
//        JcString PMID = new JcString("a.PMID");
//        JcString entitySymbol = new JcString("e.Symbol");
//        //GetRelations along with gene symbols
//        queryClauses.add(MATCH.node(article).label("Article")
//                .relation(c).out().type("CONTAINS")
//                .node(entity).label("Entity"));
//
//        //Where to limit it to found articles.
//        queryClauses.add(WHERE.valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
//        queryClauses.add(RETURN.value(article.property("PMID")));
//        queryClauses.add(RETURN.value(entity.property("Symbol")));
//        queryClauses.add(RETURN.count().value(c).AS(nCount));
//
//        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
//        //NOTE: This result may not contain all short listed article especially if only filter queries, publication, author or date range is given.
//
//        //Get sorted rank result. Result set will have all shortlisted articles, with updated rank from result of this query.
////        List<ArticleWiseConcepts> lstConcepts = GraphDBNatives.processResultForConcepts( result.resultOf(PMID),result.resultOf(entitySymbol),  result.resultOf(nCount));
//
//        // This is the inception of result set. This has been introduced to fetch all articles, as well as filters [Genes count] from graph DB.
//        DBManagerResultSet dbManagerResultSet = getRankedArticles(shortListedArticles, lstConcepts);
//        //Update total number of pages in query params.
//        qParams.setTotalPages (dbManagerResultSet.getRankedArticles().size()% qParams.getPageSize()==0? dbManagerResultSet.getRankedArticles().size()/qParams.getPageSize(): (dbManagerResultSet.getRankedArticles().size()/qParams.getPageSize()) +1 );
//        //Get required page and return and new object.
//        DBManagerResultSet finalResultSet = new DBManagerResultSet();
//        //Filter list is added
//        finalResultSet.setGeneCounts(dbManagerResultSet.getGeneCounts());
//        finalResultSet.setRankedArticles(GraphDBNatives.getRequiredPage(dbManagerResultSet.getRankedArticles(), qParams));
//        return finalResultSet;
//    }



//    /***
//     * This method gets short listed articles, rank them and return sorted list as a field of DBManagerResultSet
//     * @param shortListedPMIDs
//     * @param countedConcepts
//     * @return
//     */
//    private DBManagerResultSet getRankedArticles(Set<String> shortListedPMIDs, List<ArticleWiseConcepts> countedConcepts) {
//        /*Ranking Logic:
//        // Currently Ranking is based on annotations attached with an article.
//        // However with filter queries it is possible that some of the articles do not have any annotation.
//        // So result set contains all elements.
//        */
//        Map<String, BigDecimal> articleConceptCountTotal = new HashMap<>();
//        Map<String, Integer> geneArticleCount = new HashMap<>();
//
//        for (ArticleWiseConcepts articleWiseConcepts: countedConcepts){
//            if(articleConceptCountTotal.containsKey(articleWiseConcepts.getPMID())){
//                BigDecimal tCount = articleConceptCountTotal.get(articleWiseConcepts.getPMID());
//                articleConceptCountTotal.put(articleWiseConcepts.getPMID(), articleWiseConcepts.getCount().add(tCount));
//            }
//            else
//            {
//                articleConceptCountTotal.put(articleWiseConcepts.getPMID(), articleWiseConcepts.getCount());
//            }
//
//            if(geneArticleCount.containsKey(articleWiseConcepts.getIdentifier())){
//                Integer tCount = geneArticleCount.get(articleWiseConcepts.getIdentifier());
//                geneArticleCount.put(articleWiseConcepts.getIdentifier(), tCount+1);
//            }
//            else
//            {
//                geneArticleCount.put(articleWiseConcepts.getIdentifier(),1);
//            }
//        }
//        // Now all counts are updated. Time to create the Ranked articles for complete result set.
//        List<RankedArticle> countedArticles = new ArrayList<>();
//
//        for (String PMID: shortListedPMIDs)
//        {
//            BigDecimal count = BigDecimal.ZERO;
//            if(articleConceptCountTotal.containsKey(PMID)){
//                count = articleConceptCountTotal.get(PMID);
//            }
//            countedArticles.add(new RankedArticle(PMID, count ,0,0, 0, null, null));
//
//
//        }
//        // Sort the collection with highest totalConceptHits at index 0.
//        Collections.sort(countedArticles, new RankedArticleComparitor());
//
//        DBManagerResultSet result = new DBManagerResultSet();
//        result.setGeneCounts(geneArticleCount);
//        result.setRankedArticles(countedArticles);
//
//        return result;
//    }

    private Pair<String, String> getPubIdentifier(Publication publication) {

        if (!Strings.isNullOrEmpty(publication.getIssnNumber())) {
            return Pair.of("IssnNumber", publication.getIssnNumber());
        }
        if (!Strings.isNullOrEmpty(publication.getIsoAbbreviation())) {
            return Pair.of("IsoAbbreviation", publication.getIsoAbbreviation());
        }
        if (!Strings.isNullOrEmpty(publication.getTitle())) {
            return Pair.of("Title", publication.getTitle());
        }

        return null;

    }


//    private Set<String> runAuthorQuery(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, Author author, Set<String> shortListedArticles) {
//        JcNode article = new JcNode("a");
//        List<IClause> queryClauses = new ArrayList<>();
//        List<Pair<String, String>> lstParams = checkAuthorNameAndGetCaseNumber(author);
//
//
//        JcNode auth = new JcNode("auth");
//        JcRelation c = new JcRelation("c");
//        JcString PMID = new JcString("PMID");
//        queryClauses.add(MATCH.node(article).label("Article")
//                .relation(c).in().type("WROTE")
//                .node(auth).label("Author"));
//
//        if (lstParams.size() == 1) {
//            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond()));
//        } else if (lstParams.size() == 2) {
//            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond())
//                    .AND().valueOf(auth.property(lstParams.get(1).getFirst())).EQUALS(lstParams.get(1).getSecond()));
//        } else if (lstParams.size() == 3) {
//            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond())
//                    .AND().valueOf(auth.property(lstParams.get(1).getFirst())).EQUALS(lstParams.get(1).getSecond())
//                    .AND().valueOf(auth.property(lstParams.get(2).getFirst())).EQUALS(lstParams.get(2).getSecond()));
//        }
//
//        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));
//
//
//        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
//        collectedResults.put(SearchQueryParams.AUTHOR, result);
//
//        return GraphDBNatives.processQueryResult(result, SearchQueryParams.AUTHOR);
//
//
//    }

//    private List<Pair<String, String>> checkAuthorNameAndGetCaseNumber(Author author) {
//        List<Pair<String, String>> lstParams = new ArrayList<>();
//        if (!Strings.isNullOrEmpty(author.getForeName()))
//            lstParams.add(Pair.of("ForeName", author.getForeName()));
//        if (!Strings.isNullOrEmpty(author.getLastName()))
//            lstParams.add(Pair.of("LastName", author.getLastName()));
//        if (!Strings.isNullOrEmpty(author.getInitials()))
//            lstParams.add(Pair.of("Initials", author.getInitials()));
//
//        return lstParams;
//    }

//    private Set<String> runPublicationQuery(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, Publication publication, Set<String> shortListedArticles) {
//
//        Pair<String, String> pubIdentifier = getPubIdentifier(publication);
//        if (pubIdentifier != null) {
//            JcNode article = new JcNode("a");
//            List<IClause> queryClauses = new ArrayList<>();
//
//            JcNode pub = new JcNode("p");
//            JcRelation c = new JcRelation("c");
//            JcString PMID = new JcString("PMID");
//            queryClauses.add(MATCH.node(article).label("Article")
//                    .relation(c).out().type("PUBLISHED")
//                    .node(pub).label("Publication"));
//
//            if (shortListedArticles.size() > 0) {
//                queryClauses.add(WHERE.valueOf(pub.property(pubIdentifier.getFirst())).EQUALS(pubIdentifier.getSecond())
//                        .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
//
//            } else {
//                queryClauses.add(WHERE.valueOf(pub.property(pubIdentifier.getFirst())).EQUALS(pubIdentifier.getSecond()));
//
//            }
//            queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));
//            JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
//            collectedResults.put(SearchQueryParams.PUBLICATION, result);
//
//
//            return GraphDBNatives.processQueryResult(result, SearchQueryParams.PUBLICATION);
//
//        } else {
//            slf4jLogger.info(String.format("Incorrect publication parameters provided. Will result in zero result."));
//            return new HashSet<>();
//        }
//    }
//
//    private Set<String> runDateRangeQuery(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, Long sDate, Long eDate, Set<String> shortListedArticles) {
//
//        JcNode article = new JcNode("a");
//        List<IClause> queryClauses = new ArrayList<>();
//        JcString PMID = new JcString("PMID");
//        queryClauses.add(MATCH.node(article).label("Article"));
//
//        if (shortListedArticles.size() > 0) {
//            queryClauses.add(WHERE.valueOf(article.property("ProcessingDate")).GT(sDate)
//                    .AND().valueOf(article.property("ProcessingDate")).LTE(eDate)
//                    .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
//
//        } else {
//            queryClauses.add(WHERE.valueOf(article.property("ProcessingDate")).GT(sDate)
//                    .AND().valueOf(article.property("ProcessingDate")).LTE(eDate));
//
//        }
//        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));
//        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
//        collectedResults.put(SearchQueryParams.DATERANGE, result);
//
//
//        return GraphDBNatives.processQueryResult(result, SearchQueryParams.DATERANGE);
//
//    }
//
//    /***
//     * This method is to enhance th search query with AND parameters by eliminating the limit of 3.
//     * @param collectedResults
//     * @param condition
//     * @param lstGenes
//     * @param shortListedArticles
//     * @return
//     */
//    private Set<String> runGenesQueryCompact(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, String condition, List<String> lstGenes, Set<String> shortListedArticles) {
//        List<String> genes = lstGenes.stream().map(g -> g.toUpperCase()).collect(Collectors.toList());
//        JcNode article = new JcNode("a");
//        List<IClause> queryClauses = new ArrayList<>();
//
//        if (genes.size() == 0) {
//            collectedResults.put(SearchQueryParams.GENES, null);
//            return GraphDBNatives.processQueryResult(null, SearchQueryParams.GENES);
//        }
//
//        if (condition.equals("AND")) {
//            for (int x = 0; x < genes.size(); x++) {
//                queryClauses = new ArrayList<>();
//                JcNode gene1 = new JcNode("g1");
//                JcString PMID = new JcString("PMID");
//                queryClauses.add(MATCH.node(article).label("Article")
//                        .relation().out().type("CONTAINS")
//                        .node(gene1).label("Gene"));
//
//                if (shortListedArticles.size() > 0) {
//                    queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).EQUALS(genes.get(x))
//                            .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
//
//                } else {
//                    queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).EQUALS(genes.get(x)));
//
//                }
//
//                queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));
//                JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
//                shortListedArticles = new HashSet<>(result.resultOf(PMID));
//
//                if (shortListedArticles.size() == 0 || x == genes.size() - 1) {
//                    collectedResults.put(SearchQueryParams.GENES, result);
//                    return GraphDBNatives.processQueryResult(result, SearchQueryParams.GENES);
//                }
//            }
//
//
//        }//AND
//        else {
//            JcNode gene1 = new JcNode("g1");
//            JcString PMID = new JcString("PMID");
//            queryClauses.add(MATCH.node(article).label("Article")
//                    .relation().out().type("CONTAINS")
//                    .node(gene1).label("Gene"));
//
//            if (shortListedArticles.size() > 0) {
//                queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).IN(new JcCollection(new ArrayList<>(genes)))
//                        .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
//
//            } else {
//                queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).IN(new JcCollection(new ArrayList<>(genes))));
//            }
//
//            queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));
//
//
//        }
//
//        //Should not come here in case of AND
//        JcQueryResult result = GraphDBNatives.executeQueryClauses(queryClauses);
//        collectedResults.put(SearchQueryParams.GENES, result);
//        return GraphDBNatives.processQueryResult(result, SearchQueryParams.GENES);
//
//
//    }







}
