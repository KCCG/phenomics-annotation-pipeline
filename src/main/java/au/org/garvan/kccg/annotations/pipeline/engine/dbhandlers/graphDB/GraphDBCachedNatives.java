package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.engine.utilities.constants.GraphDBConstants;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.neo4j.driver.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class GraphDBCachedNatives {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBCachedNatives.class);
    private static Driver liveDriver;
    private static Driver historicalDriver;


    private static GraphDBQueryStringBuilder queryStringBuilder = new GraphDBQueryStringBuilder();

    static {
        liveDriver = GraphDatabase.driver("bolt://52.64.25.182:7687/", AuthTokens.basic("neo4j", "neodev"));
    }


    @Autowired
    public GraphDBCachedNatives(@org.springframework.beans.factory.annotation.Value("${spring.dbhandlers.graphdb.endpoint}") String neo4jDbEndpoint,
                                @org.springframework.beans.factory.annotation.Value("${spring.dbhandlers.graphdb.username}") String userName,
                                @Value("${spring.dbhandlers.graphdb.password}") String password) {

        liveDriver = GraphDatabase.driver(neo4jDbEndpoint, AuthTokens.basic(userName, password));
        slf4jLogger.info(String.format("GraphDBCached liveDriver wired with endpoint:%s", neo4jDbEndpoint));

    }

    public static List<ConceptFilter> runSearchQueryForFilters(List<String> searchIds, List<String> filterIds) {
        List<ConceptFilter> foundFilters = new ArrayList<>();

        String query;
        if(searchIds.size()>0 && filterIds.size()==0)
            query = queryStringBuilder.buildQueryForSearchItemsFilters(searchIds);
        else
            query = queryStringBuilder.buildQueryForSearchAndFilterItemsFilters(searchIds, filterIds);

        try (Session session = liveDriver.session()) {
            session.readTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run(query);

                    while (result.hasNext()) {
                        Map map = result.next().asMap();
                        if (map.keySet().size() > 3) {
                            foundFilters.add(createConceptFilterFromResult(map));
                        }

                    }
                    return "";
                }
            });
        }
        return foundFilters;
    }


    public static Integer runQueryForArticleCount(List<String> searchIds, List<String> filterIds) {
        List<Integer> articleCount = new ArrayList<>();

        try (Session session = liveDriver.session()) {
            session.readTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run(queryStringBuilder.buildQueryForArticleCount(searchIds, filterIds));
                    articleCount.add(Integer.parseInt(result.next().get(0).toString()));
                    return "";
                }
            });
        }
        return articleCount.get(0);
    }



    public static List<Map> runQueryForArticles(List<String> searchIds, List<String> filterIds, Integer skip, Integer limit) {
        List<Map> articles = new ArrayList<>();

        try (Session session = liveDriver.session()) {
            session.readTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run(queryStringBuilder.buildQueryForArticlePage(searchIds, filterIds,skip,limit));
                    while (result.hasNext()) {
                        articles.add(result.next().asMap());

                    }
                    return "";
                }
            });
        }
        return articles;
    }


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


    public static class GraphDBQueryStringBuilder {

        public  String buildQueryForSearchItemsFilters(List<String> searchIds) {
            String joined = searchIds.stream()
                    .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                    .collect(Collectors.joining(", "));


            String query = "MATCH(e:Entity)-[:CONTAINS]-(a:Article)\n" +
                    "WHERE e.EID in [" + joined + "]\n" +
                    "WITH a \n" +
                    "MATCH (a)-[:CONTAINS]-(e2:Entity)\n" +
                    "RETURN e2.EID as EID, e2.Text as Text, e2.Type as Type ,count(distinct(a)) as articleCount \n" +
                    "ORDER BY articleCount desc \n ";

            return printQuery(query);

        }

        public  String buildQueryForSearchAndFilterItemsFilters(List<String> searchIds, List<String> filterIds ) {
            String joined = searchIds.stream()
                    .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                    .collect(Collectors.joining(", "));

            String query = "MATCH(e:Entity)-[:CONTAINS]-(a:Article)\n" +
                    "WHERE e.EID in [" + joined + "]\n";

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

        public String buildQueryForArticleCount(List<String> searchIds, List<String> filterIds){
//
//            MATCH (e:Entity)-[c:CONTAINS]-(a:Article)
//            WHERE e.EID in ["1100"]
//            WITH a
//            MATCH (e1:Entity {EID:"1101"})-[c1:CONTAINS]-(a)
//                    RETURN count(distinct(a))


            String joined = searchIds.stream()
                    .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                    .collect(Collectors.joining(", "));
            String query = "MATCH(e:Entity)-[:CONTAINS]-(a:Article) " +
                    "WHERE e.EID in [" + joined + "]\n" ;

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

        public String buildQueryForArticlePage(List<String> searchIds, List<String> filterIds, Integer skip, Integer limit){
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

            Stack<String> orderByStack = new Stack();
            String joined = searchIds.stream()
                    .map(plain -> '"' + StringEscapeUtils.escapeJava(plain) + '"')
                    .collect(Collectors.joining(", "));

            String query = "MATCH(e:Entity)-[c:CONTAINS]-(a:Article)\n" +
                    "WHERE e.EID in [" + joined + "]\n";

            orderByStack.push("counts desc");
            String returnClause = String.format("RETURN distinct (a.PMID) as %s, count(distinct(c)) as %s ", GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_PAID_LABEL, GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_SEARCH_COUNTS_LABEL);
            String withClause = "WITH a, c";

            if(filterIds.size()>0)
            {

                String tempQuery;
                Integer iterator = 1;
                for(String fId:filterIds)
                {
                    tempQuery = withClause + "\n";
                    tempQuery  = tempQuery + String.format("MATCH (e%d:Entity {EID:\"%s\"})-[c%d:CONTAINS]-(a)", iterator, fId, iterator) + "\n";
                    query = query + tempQuery;
                    //Update clauses
                    withClause = withClause + String.format(", c%d ",iterator);
                    String countsLabel = GraphDBConstants.CACHED_QUERY_ARTICLE_RESULT_SET_SEARCH_COUNTS_LABEL + iterator.toString();
                    returnClause = returnClause + String.format(", count(distinct(c%d)) as %s ", iterator,countsLabel);
                    orderByStack.push(String.format("%s desc",countsLabel));
                    iterator++;
                }

            }
            else
            {
                returnClause = returnClause + "\n";

            }
            String orderByClause = "ORDER BY ";
            while(!orderByStack.isEmpty()){
                orderByClause = orderByClause + orderByStack.pop();
                if(!orderByStack.isEmpty()){
                    orderByClause = orderByClause + ",";
                }
                else
                    orderByClause = orderByClause + "\n";
            }

            String paginationClause = String.format("SKIP %d \nLIMIT %d", skip, limit);
            String returnString = query +  returnClause + orderByClause + paginationClause;
            return  printQuery(returnString);


        }

        private String printQuery(String query){
            slf4jLogger.debug("Cypher Query: \n s%", query);
            return query;
        }


    }


}
