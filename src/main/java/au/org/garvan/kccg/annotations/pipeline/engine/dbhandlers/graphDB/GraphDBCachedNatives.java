package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.constants.GraphDBConstants;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import com.amazonaws.services.dynamodbv2.xspec.S;
import org.apache.commons.lang3.StringEscapeUtils;
import org.neo4j.driver.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
@Component
public class GraphDBCachedNatives {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBCachedNatives.class);
    private static Driver liveDriver;
    private static Driver historicalDriver;


    private static GraphDBQueryStringBuilder queryStringBuilder = new GraphDBQueryStringBuilder();

//    static {
//        liveDriver = GraphDatabase.driver("bolt://52.64.25.182:7687/", AuthTokens.basic("neo4j", "neodev"));
//        historicalDriver = GraphDatabase.driver("bolt://52.63.6.209//:7687/", AuthTokens.basic("neo4j", "Neo4j@1Prod"));
//    }


    @Autowired
    public GraphDBCachedNatives(@Value("${spring.dbhandlers.graphdb.live.endpoint}") String neo4jDbEndpointLive,
                                @Value("${spring.dbhandlers.graphdb.live.username}") String userNameLive,
                                @Value("${spring.dbhandlers.graphdb.live.password}") String passwordLive,

                                @Value("${spring.dbhandlers.graphdb.historical.endpoint}") String neo4jDbEndpointHistorical,
                                @Value("${spring.dbhandlers.graphdb.historical.username}") String userNameHistorical,
                                @Value("${spring.dbhandlers.graphdb.historical.password}") String passwordHistorical) {

        liveDriver = GraphDatabase.driver(neo4jDbEndpointLive, AuthTokens.basic(userNameLive, passwordLive));
        slf4jLogger.info(String.format("GraphDBCached liveDriver wired with endpoint:%s", neo4jDbEndpointLive));

        historicalDriver = GraphDatabase.driver(neo4jDbEndpointHistorical, AuthTokens.basic(userNameHistorical, passwordHistorical));
        slf4jLogger.info(String.format("GraphDBCached historicalDriver wired with endpoint:%s", neo4jDbEndpointHistorical));

    }


    ////////////////////////////////////////////////////////         Live Server Calls   ///////////////////////////////////////////////////////////////

    public static List<ConceptFilter> runSearchQueryForFilters(List<String> searchIds, List<String> filterIds, boolean isHistorical, List<String> shortListedIds, boolean searchAll ) {
        List<ConceptFilter> foundFilters = new ArrayList<>();

        String query;
        if(searchIds.size()>0 && filterIds.size()==0)
            query = queryStringBuilder.buildQueryForSearchItemsFilters(searchIds, searchAll);
        else
            query = queryStringBuilder.buildQueryForSearchAndFilterItemsFilters(searchIds, filterIds, shortListedIds, searchAll);


        //Switch driver based on caller
        Driver localDriver = isHistorical? historicalDriver: liveDriver;
        try (Session session = localDriver.session()) {
            session.readTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    slf4jLogger.debug("Query for filters is going to be called.");
                    StatementResult result = tx.run(query);
                    slf4jLogger.debug("Query for filters is completed.");


                    while (result.hasNext()) {
                        Map map = result.next().asMap();
                        if (map.keySet().size() > 3) {
                            foundFilters.add(createConceptFilterFromResult(map));
                        }

                    }
                    slf4jLogger.debug("Query for filters result processed.");
                    return "";
                }
            });
        }
        slf4jLogger.debug("Query for filters result being returned.");
        return foundFilters;
    }


    public static Integer runQueryForArticleCount(List<String> searchIds, List<String> filterIds, boolean isHistorical, List<String> shortListedIds,boolean searchAll ) {
        List<Integer> articleCount = new ArrayList<>();

        //Switch driver based on caller
        Driver localDriver = isHistorical? historicalDriver: liveDriver;

        try (Session session = localDriver.session()) {
            session.readTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run(queryStringBuilder.buildQueryForArticleCount(searchIds, filterIds, shortListedIds, searchAll));
                    articleCount.add(Integer.parseInt(result.next().get(0).toString()));
                    return "";
                }
            });
        }
        return articleCount.get(0);
    }



    public static List<Map> runQueryForArticles(List<String> searchIds, List<String> filterIds, Integer skip, Integer limit, boolean isHistorical, List<String> shortListedIds, boolean searchAll  ) {
        List<Map> articles = new ArrayList<>();


        //Switch driver based on caller
        Driver localDriver = isHistorical? historicalDriver: liveDriver;

        try (Session session = localDriver.session()) {
            session.readTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run(queryStringBuilder.buildQueryForArticlePage(searchIds, filterIds, shortListedIds, skip,limit, searchAll));
                    while (result.hasNext()) {
                        articles.add(result.next().asMap());

                    }
                    return "";
                }
            });
        }
        return articles;
    }
    ////////////////////////////////////////////////////////  Live Server Calls End  ///////////////////////////////////////////////////////////////




    private static ConceptFilter createConceptFilterFromResult(Map graphRow) {
        ConceptFilter tempFilter = new ConceptFilter();

        if (graphRow.containsKey("EID"))
            tempFilter.setId(graphRow.get("EID").toString());
        if (graphRow.containsKey("Text"))
            tempFilter.setText(graphRow.get("Text").toString());
        if (graphRow.containsKey("Type"))
            tempFilter.setType(graphRow.get("Type").toString());
        if (graphRow.containsKey("articleCount")) {
            tempFilter.setArticleCount(Integer.parseInt(graphRow.get("articleCount").toString()));
            tempFilter.setFilteredArticleCount(Integer.parseInt(graphRow.get("articleCount").toString()));

        }
        return tempFilter;

    }



    /***
     * Query Builder Class for plain cypher to bolt
     */
    public static class GraphDBQueryStringBuilder {

        public String buildQueryForSearchItemsFilters(List<String> searchIds, boolean searchAll) {

            String query = "";
            if (searchAll && searchIds.size() > 1) {
                Pair<String, List<String>> splits = splitSearchItems(searchIds);
                query = String.format("MATCH(e:Entity {EID:\"%s\"})-[:CONTAINS]-(a:Article)\n", splits.getFirst());
                String tempQuery;
                for (String fId : splits.getSecond()) {
                    tempQuery = "WITH a\n";
                    tempQuery = tempQuery + String.format("MATCH (:Entity {EID:\"%s\"})-[:CONTAINS]-(a)", fId) + "\n";
                    query = query + tempQuery;
                }

                query = query + "WITH a \n" +
                        "MATCH (a)-[:CONTAINS]-(e2:Entity)\n" +
                        "RETURN e2.EID as EID, e2.Text as Text, e2.Type as Type ,count(distinct(a)) as articleCount\n" +
                        "ORDER BY articleCount desc\n";


            } else {


                String joined = searchIds.stream()
                        .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                        .collect(Collectors.joining(", "));


                query = "MATCH(e:Entity)-[:CONTAINS]-(a:Article)\n" +
                        "WHERE e.EID in [" + joined + "]\n" +
                        "WITH a \n" +
                        "MATCH (a)-[:CONTAINS]-(e2:Entity)\n" +
                        "RETURN e2.EID as EID, e2.Text as Text, e2.Type as Type ,count(distinct(a)) as articleCount \n" +
                        "ORDER BY articleCount desc \n ";
            }
            return printQuery(query);

        }

        public  String buildQueryForSearchAndFilterItemsFilters(List<String> searchIds, List<String> filterIds, List<String> shortListedIds ,boolean searchAll ) {
            String query = "";

            if (searchAll && searchIds.size() > 1) {
                Pair<String, List<String>> splits = splitSearchItems(searchIds);
                query = String.format("MATCH(e:Entity {EID:\"%s\"})-[:CONTAINS]-(a:Article)\n", splits.getFirst());
                //In case a sub filter is already found and injected
                if(shortListedIds.size()>0){
                    String articleFilter = shortListedIds.stream()
                            .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                            .collect(Collectors.joining(", "));
                    query = query + "WHERE a.PMID in [" + articleFilter + "]\n";
                }

                String tempQuery;
                for (String fId : splits.getSecond()) {
                    tempQuery = "WITH a\n";
                    tempQuery = tempQuery + String.format("MATCH (:Entity {EID:\"%s\"})-[:CONTAINS]-(a)", fId) + "\n";
                    query = query + tempQuery;
                }
            }else {


                String joined = searchIds.stream()
                        .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                        .collect(Collectors.joining(", "));

                query = "MATCH(e:Entity)-[:CONTAINS]-(a:Article)\n" +
                        "WHERE e.EID in [" + joined + "]\n";

                //In case a sub filter is already found and injected
                if(shortListedIds.size()>0){
                    String articleFilter = shortListedIds.stream()
                            .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                            .collect(Collectors.joining(", "));
                    query = query + "AND a.PMID in [" + articleFilter + "]\n";
                }

            }

            if(filterIds.size()>0)
            {
                String tempQuery;
                for(String fId:filterIds)
                {
                    tempQuery = "WITH a\n";
                    tempQuery  = tempQuery + String.format("MATCH (:Entity {EID:\"%s\"})-[:CONTAINS]-(a)", fId) + "\n";
                    query = query + tempQuery;
                }

            }
            String returnString = query +  "WITH a \n" +
                    "MATCH (a)-[:CONTAINS]-(e2:Entity)\n" +
                    "RETURN e2.EID as EID, e2.Text as Text, e2.Type as Type ,count(distinct(a)) as articleCount\n" +
                    "ORDER BY articleCount desc\n";

            return printQuery(returnString);

        }

        public String buildQueryForArticleCount(List<String> searchIds, List<String> filterIds, List<String> shortListedIds,boolean searchAll){
//
//            MATCH (e:Entity)-[c:CONTAINS]-(a:Article)
//            WHERE e.EID in ["1100"]
//            WITH a
//            MATCH (e1:Entity {EID:"1101"})-[c1:CONTAINS]-(a)
//                    RETURN count(distinct(a))

            String query = "";
            if (searchAll && searchIds.size() > 1) {
                Pair<String, List<String>> splits = splitSearchItems(searchIds);
                query = String.format("MATCH(e:Entity {EID:\"%s\"})-[:CONTAINS]-(a:Article)\n", splits.getFirst());

                //In case a sub filter is already found and injected
                if(shortListedIds.size()>0){
                    String articleFilter = shortListedIds.stream()
                            .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                            .collect(Collectors.joining(", "));
                    query = query + "WHERE a.PMID in [" + articleFilter + "]\n";
                }

                String tempQuery;
                for (String fId : splits.getSecond()) {
                    tempQuery = "WITH a\n";
                    tempQuery = tempQuery + String.format("MATCH (:Entity {EID:\"%s\"})-[:CONTAINS]-(a)", fId) + "\n";
                    query = query + tempQuery;
                }
            }else {


                String joined = searchIds.stream()
                        .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                        .collect(Collectors.joining(", "));

                query = "MATCH(e:Entity)-[:CONTAINS]-(a:Article) " +
                        "WHERE e.EID in [" + joined + "]\n";

                //In case a sub filter is already found and injected
                if (shortListedIds.size() > 0) {
                    String articleFilter = shortListedIds.stream()
                            .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                            .collect(Collectors.joining(", "));
                    query = query + "AND a.PMID in [" + articleFilter + "]\n";
                }
            }

            if(filterIds.size()>0)
            {
                String tempQuery;
                for(String fId:filterIds)
                {
                    tempQuery = "WITH a\n";
                    tempQuery  = tempQuery + String.format("MATCH (:Entity {EID:\"%s\"})-[:CONTAINS]-(a)", fId) + "\n";
                    query = query + tempQuery;
                }

            }
            query = query + "RETURN count(distinct(a))";
            return  printQuery(query);


        }

        public String buildQueryForArticlePage(List<String> searchIds, List<String> filterIds,  List<String> shortListedIds , Integer skip, Integer limit,boolean searchAll){
//
//            MATCH (e:Entity)-[c:CONTAINS]-(a:Article)
//            WHERE e.EID in ["1100"]
//
//            WITH a,c
//            MATCH (:Entity {EID:"1100"})-[c2:CONTAINS]-(a)
//
//            WITH a,c,c2
//            MATCH (:Entity {EID:"1101"})-[c3:CONTAINS]-(a)
//
//            RETURN distinct (a.PMID), count(distinct(c)) as counts , count(distinct(c2)) as counts2, count(distinct(c3)) as counts3
//            ORDER By counts3 desc ,counts2 desc, counts desc
//            SKIP 10
//            LIMIT 10

            if (searchAll && searchIds.size() > 1) {
                Pair<String, List<String>> splits = splitSearchItems(searchIds);
                String query = String.format("MATCH(e:Entity {EID:\"%s\"})-[sc:CONTAINS]-(a:Article)\n", splits.getFirst());

                //In case a sub filter is already found and injected
                if(shortListedIds.size()>0){
                    String articleFilter = shortListedIds.stream()
                            .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                            .collect(Collectors.joining(", "));
                    query = query + "WHERE a.PMID in [" + articleFilter + "]\n";
                }


                String returnClause = String.format("RETURN distinct(a.PMID) as %s , ( size(sc.SentID) ", GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_PAID_LABEL);
                String withClause = "WITH a, sc";

                String tempQuery1;
                Integer searchIterator = 1;
                for (String sId : splits.getSecond()) {
                    tempQuery1 = withClause + "\n";
                    tempQuery1 = tempQuery1 + String.format("MATCH (se%d:Entity {EID:\"%s\"})-[sc%d:CONTAINS]-(a)", searchIterator, sId, searchIterator) + "\n";
                    query = query + tempQuery1;
                    withClause = withClause + String.format(", sc%d ", searchIterator);
                    returnClause = returnClause + String.format("+ size(sc%d.SentID) ", searchIterator);
                    searchIterator++;
                }

                if (filterIds.size() > 0) {

                    String tempQuery;
                    Integer iterator = 1;
                    for (String fId : filterIds) {
                        tempQuery = withClause + "\n";
                        tempQuery = tempQuery + String.format("MATCH (e%d:Entity {EID:\"%s\"})-[c%d:CONTAINS]-(a)", iterator, fId, iterator) + "\n";
                        query = query + tempQuery;
                        //Update clauses
                        withClause = withClause + String.format(", c%d ", iterator);
                        returnClause = returnClause + String.format("+ size(c%d.SentID) ", iterator);
                        iterator++;
                    }

                }

                returnClause = returnClause + ") as hits \n";

                String orderByClause = "ORDER BY hits desc, a.PMID desc \n";

                String paginationClause = String.format("SKIP %d\nLIMIT %d", skip, limit);
                String returnString = query + returnClause + orderByClause + paginationClause;

                return printQuery(returnString);
            }

            else {

                String joined = searchIds.stream()
                        .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                        .collect(Collectors.joining(", "));

                String query = "MATCH(e:Entity)-[c:CONTAINS]-(a:Article)\n" +
                        "WHERE e.EID in [" + joined + "]\n";

                //In case a sub filter is already found and injected
                if (shortListedIds.size() > 0) {
                    String articleFilter = shortListedIds.stream()
                            .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                            .collect(Collectors.joining(", "));
                    query = query + "AND a.PMID in [" + articleFilter + "]\n";
                }


                String returnClause = String.format("RETURN distinct(a.PMID) as %s, count(distinct(e)) as %s , ( size(c.SentID) ", GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_PAID_LABEL, GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_SEARCH_COUNTS_LABEL);
                String withClause = "WITH a, c, e";

                if (filterIds.size() > 0) {

                    String tempQuery;
                    Integer iterator = 1;
                    for (String fId : filterIds) {
                        tempQuery = withClause + "\n";
                        tempQuery = tempQuery + String.format("MATCH (e%d:Entity {EID:\"%s\"})-[c%d:CONTAINS]-(a)", iterator, fId, iterator) + "\n";
                        query = query + tempQuery;
                        //Update clauses
                        withClause = withClause + String.format(", c%d ", iterator);
                        returnClause = returnClause + String.format("+ size(c%d.SentID) ", iterator);
                        iterator++;
                    }

                }

                returnClause = returnClause + ") as hits \n";

                String orderByClause = "ORDER BY shits desc, hits desc, a.PMID desc \n";

                String paginationClause = String.format("SKIP %d\nLIMIT %d", skip, limit);
                String returnString = query + returnClause + orderByClause + paginationClause;

                return printQuery(returnString);
            }


        }

        private String printQuery(String query){
            slf4jLogger.debug(String.format("Cypher Query: \n%s", query));
            return query;
        }

        private Pair<String, List<String>> splitSearchItems(List<String>searchIds)
        {

            Pair<String, List<String>> splits = new Pair<>(searchIds.get(0), searchIds.subList(1,searchIds.size()));
            return splits;
        }


    }


}
