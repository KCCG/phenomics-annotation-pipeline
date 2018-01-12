package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.model.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.model.RankedArticle;
import au.org.garvan.kccg.annotations.pipeline.model.RankedArticleComparitor;
import com.google.common.base.Strings;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.*;
import iot.jcypher.query.values.*;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
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

@Component
public class GraphDBHandler {


    private final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBHandler.class);
    Properties props = new Properties();
    IDBAccess remote;
    @Value("${spring.dbhandlers.graphdb.graphprinting}")
    private boolean ENABLE_PRINTING;

    @Autowired
    public GraphDBHandler(@Value("${spring.dbhandlers.graphdb.endpoint}") String neo4jDbEndpoint,
                          @Value("${spring.dbhandlers.graphdb.username}") String userName,
                          @Value("${spring.dbhandlers.graphdb.password}") String password) {

        props.setProperty(DBProperties.SERVER_ROOT_URI, neo4jDbEndpoint);
        remote = DBAccessFactory.createDBAccess(DBType.REMOTE, props, userName, password);
        slf4jLogger.info(String.format("GraphDBHandler wired with endpoint:%s", neo4jDbEndpoint));

    }

    /**
     * map to CYPHER statements and map to JSON, print the mapping results to System.out
     *
     * @param query
     * @param title
     * @param format
     */
    private static void print(JcQuery query, String title, Format format) {
        System.out.println("QUERY: " + title + " --------------------");
        // map to Cypher
        String cypher = iot.jcypher.util.Util.toCypher(query, format);

        System.out.println("CYPHER --------------------");
        System.out.println(cypher);

        // map to JSON
        String json = iot.jcypher.util.Util.toJSON(query, format);
        System.out.println("");
        System.out.println("JSON   --------------------");
        System.out.println(json);

        System.out.println("");
    }

    private static void print(JcQueryResult queryResult, String title) {
        System.out.println("RESULT OF QUERY: " + title + " --------------------");
        String resultString = Util.writePretty(queryResult.getJsonResult());
        System.out.println(resultString);
    }

    public void createArticleQuery(Article article) {

        try {
            List<IClause> queryClauses = new ArrayList<>();

            //Create Article node and creation clause
            JcNode nodeArticle = new JcNode("nodeArticle");
            IClause articleClause = MERGE.node(nodeArticle).label("Article")
                    .property("PMID").value(Integer.toString(article.getPubMedID()))
                    .property("Language").value(article.getLanguage())
                    .property("ProcessingDate").value(article.getProcessingDate());

            //Create authors nodes and their respective clauses (This includes both creation and mapping clauses)
            List<JcNode> nodeListAuthors = new ArrayList<>();
            List<IClause> authorsClauses = new ArrayList<>();

            for (int x = 0; x < article.getAuthors().size(); x++) {
                Author currentAuthor = article.getAuthors().get(x);
                if (currentAuthor.checkValidName()) {
                    JcNode tempAuthor = new JcNode("nodeAuthor" + Integer.toString(x));
                    nodeListAuthors.add(tempAuthor);
                    authorsClauses.add(MERGE.node(tempAuthor).label("Author")
                            .property("Initials").value(currentAuthor.getInitials())
                            .property("ForeName").value(currentAuthor.getForeName())
                            .property("LastName").value(currentAuthor.getLastName()));
                    authorsClauses.add(MERGE.node(tempAuthor).relation().out()
                            .type("WROTE").property("Order").value(x + 1)
                            .node(nodeArticle));

                }


            }

            //Create publicational node and creation clause
            JcNode nodePublication = new JcNode("nodePublication");
            IClause publicationClause = MERGE.node(nodePublication).label("Publication")
                    .property("Title").value(article.getPublication().getTitle())
                    .property("IsoAbbreviation").value(article.getPublication().getIsoAbbreviation())
                    .property("IssnType").value(article.getPublication().getIssnType())
                    .property("IssnNumber").value(article.getPublication().getIssnNumber());
            IClause publicationLinkClause = MERGE.node(nodeArticle).relation().out().type("PUBLISHED")
                    .property("DatePublished")
                    .value(article.getDatePublished())
                    .node(nodePublication);

            queryClauses.add(articleClause);

            queryClauses.addAll(authorsClauses);
            queryClauses.add(publicationClause);
            queryClauses.add(publicationLinkClause);

            fillEntitiesGraphData(queryClauses, article, nodeArticle);

            JcQueryResult result = executeQueryClauses(queryClauses);

            if (result == null)
                slf4jLogger.info(String.format("Graph DB Insertion Failed"));
            else
                slf4jLogger.info(String.format("Graph DB Insertion done without errors for Article ID: %d", article.getPubMedID()));
        } catch (Exception e) {
            slf4jLogger.info(String.format("Graph DB Insertion Failed with exception: ", e.getMessage()));
        }


    }

    private void fillEntitiesGraphData(List<IClause> queryClauses, Article article, JcNode nodeArticle) {
        //TODO: Find entities from Abstract and fill node and relationship
        Map<APSentence, List<APToken>> entities = article.getArticleAbstract().getTokensWithEntities();
        if (entities.size() > 0) {
            for (Map.Entry<APSentence, List<APToken>> entry : entities.entrySet()) {
                APSentence sent = entry.getKey();
                List<APToken> tokens = entry.getValue();
                for (APToken token : tokens) {
                    for (LexicalEntity lex : token.getLexicalEntityList()) {
                        if (lex instanceof APGene) {
                            APGene gene = (APGene) lex;
                            JcNode nodeGene = new JcNode(String.format("nodeGene%d_%d", sent.getId(), token.getId()));
                            IClause geneClause = MERGE.node(nodeGene).label("Gene").label("Entity")
                                    .property("HGNCID").value(gene.getHGNCID())
                                    .property("Symbol").value(gene.getApprovedSymbol());
                            IClause geneLinkClause =
                                    MERGE.node(nodeArticle).relation().out().type("CONTAINS")
                                            .property("SentID").value(sent.getId())
                                            .property("DocOffsetBegin").value(sent.getDocOffset().getX() + token.getSentOffset().getX())
                                            .property("Field").value("Abstract")
                                            .node(nodeGene);
                            queryClauses.add(geneClause);
                            queryClauses.add(geneLinkClause);

                        } else {

                        }
                    }
                }

            }

        }

        //TODO: Find entities from Title if required
    }

    private IClause[] getClausesArray(List<IClause> input) {

        IClause[] arr = new IClause[input.size()];
        for (int x = 0; x < input.size(); x++)
            arr[x] = input.get(x);

        return arr;
    }
    /*
    ***********************************************
    The following section is for search operations.
    ***********************************************
     */


    public DBManagerResultSet fetchArticles(Map<SearchQueryParams, Object> params, PaginationRequestParams qParams) {
        Set<String> shortListedArticles = new HashSet<>();

        //Results storage for all params, and later will be sorted out.
        LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults = new LinkedHashMap<>();

        if (params.containsKey(SearchQueryParams.AUTHOR)) {
            Author author = (Author) params.get(SearchQueryParams.AUTHOR);
            shortListedArticles = runAuthorQuery(collectedResults, author, shortListedArticles);
            if (shortListedArticles.size() == 0)
                return new DBManagerResultSet();
        }


        if (params.containsKey(SearchQueryParams.PUBLICATION)) {
            Publication publication = (Publication) params.get(SearchQueryParams.PUBLICATION);
            shortListedArticles = runPublicationQuery(collectedResults, publication, shortListedArticles);
            if (shortListedArticles.size() == 0)
                return new DBManagerResultSet();
        }

        if (params.containsKey(SearchQueryParams.GENES)) {
            Pair<String, List<String>> gene = (Pair<String, List<String>>) params.get(SearchQueryParams.GENES);
            shortListedArticles = runGenesQueryCompact(collectedResults, gene.getFirst(), gene.getSecond(), shortListedArticles);
            if (shortListedArticles.size() == 0)
                return new DBManagerResultSet();
        }

        if (params.containsKey(SearchQueryParams.DATERANGE)) {
            Pair<Long, Long> dateRange = (Pair<Long, Long>) params.get(SearchQueryParams.DATERANGE);
            shortListedArticles = runDateRangeQuery(collectedResults, dateRange.getFirst(), dateRange.getSecond(), shortListedArticles);
            if (shortListedArticles.size() == 0)
                return new DBManagerResultSet();
        }


        // If it comes to this line, that means there is something to return.
        return paginateSearchResultAlongWithFilters(shortListedArticles, qParams);

    }

    /***
     * This method is added to fetch all the counts of annotations for two purposes.
        1: Article counts to rank the articles for pagination
        2: Fetch Gene Symbols so that filtration can be provided from backend.

     * @param shortListedArticles
     * @param qParams
     * @return
     */
    private DBManagerResultSet paginateSearchResultAlongWithFilters(Set<String> shortListedArticles, PaginationRequestParams qParams) {

        //Prepare Query
        qParams.setTotalArticles(shortListedArticles.size());
        JcNode article = new JcNode("a");
        JcNode entity = new JcNode("e");

        List<IClause> queryClauses = new ArrayList<>();
        JcRelation c = new JcRelation("c");
        JcNumber nCount = new JcNumber("nCount");
        JcString PMID = new JcString("a.PMID");
        JcString entitySymbol = new JcString("e.Symbol");
        //GetRelations along with gene symbols
        queryClauses.add(MATCH.node(article).label("Article")
                .relation(c).out().type("CONTAINS")
                .node(entity).label("Gene"));

        //Where to limit it to found articles.
        queryClauses.add(WHERE.valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
        queryClauses.add(RETURN.value(article.property("PMID")));
        queryClauses.add(RETURN.value(entity.property("Symbol")));
        queryClauses.add(RETURN.count().value(c).AS(nCount));

        JcQueryResult result = executeQueryClauses(queryClauses);
        //NOTE: This result may not contain all short listed article especially if only filter queries, publication, author or date range is given.

        //Get sorted rank result. Result set will have all shortlisted articles, with updated rank from result of this query.
        List<ArticleWiseConcepts> lstConcepts = processResultForConcepts( result.resultOf(PMID),result.resultOf(entitySymbol),  result.resultOf(nCount));

        // This is the inception of result set. This has been introduced to fetch all articles, as well as filters [Genes count] from graph DB.
        DBManagerResultSet dbManagerResultSet = getRankedArticles(shortListedArticles, lstConcepts);
        //Update total number of pages in query params.
        qParams.setTotalPages (dbManagerResultSet.getRankedArticles().size()% qParams.getPageSize()==0? dbManagerResultSet.getRankedArticles().size()/qParams.getPageSize(): (dbManagerResultSet.getRankedArticles().size()/qParams.getPageSize()) +1 );
        //Get required page and return and new object.
        DBManagerResultSet finalResultSet = new DBManagerResultSet();
        //Filter list is added
        finalResultSet.setGeneCounts(dbManagerResultSet.getGeneCounts());
        finalResultSet.setRankedArticles(getRequiredPage(dbManagerResultSet.getRankedArticles(), qParams));
        return finalResultSet;
    }

    /***
     * This method is very localized and process result from pagination method.
     * @param PMIDs
     * @param symbols
     * @param counts
     * @return
     */
    private List<ArticleWiseConcepts> processResultForConcepts(List<String> PMIDs, List<String> symbols, List<BigDecimal> counts) {
        List<ArticleWiseConcepts> lst = new ArrayList<>();
        for (int x = 0; x < PMIDs.size(); x++) {
            lst.add( new ArticleWiseConcepts(PMIDs.get(x), symbols.get(x), counts.get(x)));
        }
        return lst;
    }


    /***
     * This function is a sub-listing one to just get the desired page from sorted results.
     * @param countedArticles : Assuming that this is a sorted/ranked list of articles.
     * @param qParams
     * @return
     */
    private List<RankedArticle> getRequiredPage(List<RankedArticle> countedArticles, PaginationRequestParams qParams){
        int startIndex = qParams.getPageSize()* (qParams.getPageNo()-1);
        int endIndex = Math.min((qParams.getPageNo()*qParams.getPageSize()), countedArticles.size());
        if(startIndex>endIndex)
            return new ArrayList<>();
        List<RankedArticle> page = countedArticles.subList(startIndex,endIndex);

        for (int x=0; x<page.size() ;x++)
        {
            page.get(x).setRank(page.size()-x);
        }
        return page;
    }


    /***
     * This method gets short listed articles, rank them and return sorted list as a field of DBManagerResultSet
     * @param shortListedPMIDs
     * @param countedConcepts
     * @return
     */
    private DBManagerResultSet getRankedArticles(Set<String> shortListedPMIDs, List<ArticleWiseConcepts> countedConcepts) {
        /*Ranking Logic:
        // Currently Ranking is based on annotations attached with an article.
        // However with filter queries it is possible that some of the articles do not have any annotation.
        // So result set contains all elements.
        */
        Map<String, BigDecimal> articleConceptCountTotal = new HashMap<>();
        Map<String, Integer> geneArticleCount = new HashMap<>();

        for (ArticleWiseConcepts articleWiseConcepts: countedConcepts){
            if(articleConceptCountTotal.containsKey(articleWiseConcepts.getPMID())){
                BigDecimal tCount = articleConceptCountTotal.get(articleWiseConcepts.getPMID());
                articleConceptCountTotal.put(articleWiseConcepts.getPMID(), articleWiseConcepts.getCount().add(tCount));
            }
            else
            {
                articleConceptCountTotal.put(articleWiseConcepts.getPMID(), articleWiseConcepts.getCount());
            }

            if(geneArticleCount.containsKey(articleWiseConcepts.getSymbol())){
                Integer tCount = geneArticleCount.get(articleWiseConcepts.getSymbol());
                geneArticleCount.put(articleWiseConcepts.getSymbol(), tCount+1);
            }
            else
            {
                geneArticleCount.put(articleWiseConcepts.getSymbol(),1);
            }
        }
        // Now all counts are updated. Time to create the Ranked articles for complete result set.
        List<RankedArticle> countedArticles = new ArrayList<>();

        for (String PMID: shortListedPMIDs)
        {
            BigDecimal count = BigDecimal.ZERO;
            if(articleConceptCountTotal.containsKey(PMID)){
                count = articleConceptCountTotal.get(PMID);
            }
            countedArticles.add(new RankedArticle(PMID, count , 0, null, null));


        }
        // Sort the collection with highest totalConceptHits at index 0.
        Collections.sort(countedArticles, new RankedArticleComparitor());

        DBManagerResultSet result = new DBManagerResultSet();
        result.setGeneCounts(geneArticleCount);
        result.setRankedArticles(countedArticles);

        return result;
    }


    private Set<String> processQueryResult(JcQueryResult result, SearchQueryParams qType) {
        Set<String> idList = new HashSet<>();
        if (result == null) {
            slf4jLogger.info(String.format("Nothing to process as result is null. "));
        } else {

            JcString PMID = new JcString("PMID");
            idList.addAll(result.resultOf(PMID));
            slf4jLogger.info(String.format("Successful search query for %s. ResultSet size: %d", qType.toString(), idList.size()));

        }

        return idList;
    }


    private JcQueryResult executeQueryClauses(List<IClause> lstQueryClauses) {
        JcQuery query = new JcQuery();
        query.setClauses(getClausesArray(lstQueryClauses));

        if (ENABLE_PRINTING)
            print(query, "Search", Format.PRETTY_3);

        JcQueryResult result = remote.execute(query);

        if (result.getGeneralErrors().size() > 0) {
            List<String> messages = result.getGeneralErrors().stream().filter(x -> x.getMessage().contains("(Connection refused)")).map(t -> t.getMessage()).collect(Collectors.toList());
            if (messages.size() > 0) {
                slf4jLogger.info(String.format("General errors while executing NEO4J query. Message: %s and Exception: %s", messages.get(0), result.getGeneralErrors().toString()));
                reconnectDb();
            } else {
                slf4jLogger.info(String.format("General errors while executing NEO4J query. Message: %s", result.getGeneralErrors().toString()));

            }
        } else if (result.getDBErrors().size() > 0) {
            slf4jLogger.info(String.format("Database errors while executing NEO4J query. Errors:%s", result.getGeneralErrors().toString()));

        } else {
            if (ENABLE_PRINTING)
                print(result, "Result");
            return result;
        }

        return null;


    }

    private void reconnectDb() {
        slf4jLogger.info(String.format("Reconnecting Graph Database"));
        this.remote = DBAccessFactory.createDBAccess(DBType.REMOTE, props);


    }

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


    private Set<String> runAuthorQuery(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, Author author, Set<String> shortListedArticles) {
        JcNode article = new JcNode("a");
        List<IClause> queryClauses = new ArrayList<>();
        List<Pair<String, String>> lstParams = checkAuthorNameAndGetCaseNumber(author);


        JcNode auth = new JcNode("auth");
        JcRelation c = new JcRelation("c");
        JcString PMID = new JcString("PMID");
        queryClauses.add(MATCH.node(article).label("Article")
                .relation(c).in().type("WROTE")
                .node(auth).label("Author"));

        if (lstParams.size() == 1) {
            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond()));
        } else if (lstParams.size() == 2) {
            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond())
                    .AND().valueOf(auth.property(lstParams.get(1).getFirst())).EQUALS(lstParams.get(1).getSecond()));
        } else if (lstParams.size() == 3) {
            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond())
                    .AND().valueOf(auth.property(lstParams.get(1).getFirst())).EQUALS(lstParams.get(1).getSecond())
                    .AND().valueOf(auth.property(lstParams.get(2).getFirst())).EQUALS(lstParams.get(2).getSecond()));
        }

        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));


        JcQueryResult result = executeQueryClauses(queryClauses);
        collectedResults.put(SearchQueryParams.AUTHOR, result);

        return processQueryResult(result, SearchQueryParams.AUTHOR);


    }

    private List<Pair<String, String>> checkAuthorNameAndGetCaseNumber(Author author) {
        List<Pair<String, String>> lstParams = new ArrayList<>();
        if (!Strings.isNullOrEmpty(author.getForeName()))
            lstParams.add(Pair.of("ForeName", author.getForeName()));
        if (!Strings.isNullOrEmpty(author.getLastName()))
            lstParams.add(Pair.of("LastName", author.getLastName()));
        if (!Strings.isNullOrEmpty(author.getInitials()))
            lstParams.add(Pair.of("Initials", author.getInitials()));

        return lstParams;
    }

    private Set<String> runPublicationQuery(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, Publication publication, Set<String> shortListedArticles) {

        Pair<String, String> pubIdentifier = getPubIdentifier(publication);
        if (pubIdentifier != null) {
            JcNode article = new JcNode("a");
            List<IClause> queryClauses = new ArrayList<>();

            JcNode pub = new JcNode("p");
            JcRelation c = new JcRelation("c");
            JcString PMID = new JcString("PMID");
            queryClauses.add(MATCH.node(article).label("Article")
                    .relation(c).out().type("PUBLISHED")
                    .node(pub).label("Publication"));

            if (shortListedArticles.size() > 0) {
                queryClauses.add(WHERE.valueOf(pub.property(pubIdentifier.getFirst())).EQUALS(pubIdentifier.getSecond())
                        .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));

            } else {
                queryClauses.add(WHERE.valueOf(pub.property(pubIdentifier.getFirst())).EQUALS(pubIdentifier.getSecond()));

            }
            queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));
            JcQueryResult result = executeQueryClauses(queryClauses);
            collectedResults.put(SearchQueryParams.PUBLICATION, result);


            return processQueryResult(result, SearchQueryParams.PUBLICATION);

        } else {
            slf4jLogger.info(String.format("Incorrect publication parameters provided. Will result in zero result."));
            return new HashSet<>();
        }
    }

    private Set<String> runDateRangeQuery(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, Long sDate, Long eDate, Set<String> shortListedArticles) {

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
        JcQueryResult result = executeQueryClauses(queryClauses);
        collectedResults.put(SearchQueryParams.DATERANGE, result);


        return processQueryResult(result, SearchQueryParams.DATERANGE);

    }

    @Deprecated
    private Set<String> runGenesQuery(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, String condition, List<String> lstGenes, Set<String> shortListedArticles) {
        List<String> genes = lstGenes.stream().map(g -> g.toUpperCase()).collect(Collectors.toList());
        JcNode article = new JcNode("a");
        List<IClause> queryClauses = new ArrayList<>();

        switch (condition) {
            case "AND":
                if (genes.size() > 3)
                    genes = genes.subList(0, 3);

                switch (genes.size()) {

                    case 0:
                        collectedResults.put(SearchQueryParams.GENES, null);
                        return processQueryResult(null, SearchQueryParams.GENES);

                    case 1:
                        JcNode gene1 = new JcNode("g1");
                        JcString PMID = new JcString("PMID");
                        queryClauses.add(MATCH.node(article).label("Article")
                                .relation().out().type("CONTAINS")
                                .node(gene1).label("Gene"));

                        if (shortListedArticles.size() > 0) {
                            queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).EQUALS(genes.get(0))
                                    .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));

                        } else {
                            queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).EQUALS(genes.get(0)));

                        }

                        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));

                        break;
                    case 2:

                        JcNode gene21 = new JcNode("g1");
                        JcNode gene22 = new JcNode("g2");
                        JcString PMID2 = new JcString("PMID");

                        queryClauses.add(MATCH.node(article).label("Article"));
                        queryClauses.add(MATCH.node(article)
                                .relation().out().type("CONTAINS")
                                .node(gene21).label("Gene"));
                        queryClauses.add(MATCH.node(article)
                                .relation().out().type("CONTAINS")
                                .node(gene22).label("Gene"));

                        if (shortListedArticles.size() > 0) {
                            queryClauses.add(WHERE.valueOf(gene21.property("Symbol")).EQUALS(genes.get(0))
                                    .AND().valueOf(gene22.property("Symbol")).EQUALS(genes.get(1))
                                    .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
                        } else {
                            queryClauses.add(WHERE.valueOf(gene21.property("Symbol")).EQUALS(genes.get(0))
                                    .AND().valueOf(gene22.property("Symbol")).EQUALS(genes.get(1)));
                        }
                        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID2));

                        break;
                    case 3:
                        JcNode gene31 = new JcNode("g1");
                        JcNode gene32 = new JcNode("g2");
                        JcNode gene33 = new JcNode("g3");

                        JcString PMID3 = new JcString("PMID");

                        queryClauses.add(MATCH.node(article).label("Article"));
                        queryClauses.add(MATCH.node(article)
                                .relation().out().type("CONTAINS")
                                .node(gene31).label("Gene"));
                        queryClauses.add(MATCH.node(article)
                                .relation().out().type("CONTAINS")
                                .node(gene32).label("Gene"));
                        queryClauses.add(MATCH.node(article)
                                .relation().out().type("CONTAINS")
                                .node(gene33).label("Gene"));


                        if (shortListedArticles.size() > 0) {
                            queryClauses.add(WHERE.valueOf(gene31.property("Symbol")).EQUALS(genes.get(0))
                                    .AND().valueOf(gene32.property("Symbol")).EQUALS(genes.get(1))
                                    .AND().valueOf(gene33.property("Symbol")).EQUALS(genes.get(2))
                                    .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));
                        } else {
                            queryClauses.add(WHERE.valueOf(gene31.property("Symbol")).EQUALS(genes.get(0))
                                    .AND().valueOf(gene32.property("Symbol")).EQUALS(genes.get(1))
                                    .AND().valueOf(gene33.property("Symbol")).EQUALS(genes.get(2)));
                        }
                        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID3));
                        break;
                }//Size
                break;

            case "OR":
                JcNode gene1 = new JcNode("g1");
                JcString PMID = new JcString("PMID");
                queryClauses.add(MATCH.node(article).label("Article")
                        .relation().out().type("CONTAINS")
                        .node(gene1).label("Gene"));

                if (shortListedArticles.size() > 0) {
                    queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).IN(new JcCollection(new ArrayList<>(genes)))
                            .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));

                } else {
                    queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).IN(new JcCollection(new ArrayList<>(genes))));
                }

                queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));

                break;


        }


        JcQueryResult result = executeQueryClauses(queryClauses);
        collectedResults.put(SearchQueryParams.GENES, result);
        return processQueryResult(result, SearchQueryParams.GENES);


    }

    /***
     * This method is to enhance th search query with AND parameters by eliminating the limit of 3.
     * @param collectedResults
     * @param condition
     * @param lstGenes
     * @param shortListedArticles
     * @return
     */
    private Set<String> runGenesQueryCompact(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, String condition, List<String> lstGenes, Set<String> shortListedArticles) {
        List<String> genes = lstGenes.stream().map(g -> g.toUpperCase()).collect(Collectors.toList());
        JcNode article = new JcNode("a");
        List<IClause> queryClauses = new ArrayList<>();

        if (genes.size() == 0) {
            collectedResults.put(SearchQueryParams.GENES, null);
            return processQueryResult(null, SearchQueryParams.GENES);
        }

        if (condition.equals("AND")) {
            for (int x = 0; x < genes.size(); x++) {
                queryClauses = new ArrayList<>();
                JcNode gene1 = new JcNode("g1");
                JcString PMID = new JcString("PMID");
                queryClauses.add(MATCH.node(article).label("Article")
                        .relation().out().type("CONTAINS")
                        .node(gene1).label("Gene"));

                if (shortListedArticles.size() > 0) {
                    queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).EQUALS(genes.get(x))
                            .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));

                } else {
                    queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).EQUALS(genes.get(x)));

                }

                queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));
                JcQueryResult result = executeQueryClauses(queryClauses);
                shortListedArticles = new HashSet<>(result.resultOf(PMID));

                if (shortListedArticles.size() == 0 || x == genes.size() - 1) {
                    collectedResults.put(SearchQueryParams.GENES, result);
                    return processQueryResult(result, SearchQueryParams.GENES);
                }
            }


        }//AND
        else {
            JcNode gene1 = new JcNode("g1");
            JcString PMID = new JcString("PMID");
            queryClauses.add(MATCH.node(article).label("Article")
                    .relation().out().type("CONTAINS")
                    .node(gene1).label("Gene"));

            if (shortListedArticles.size() > 0) {
                queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).IN(new JcCollection(new ArrayList<>(genes)))
                        .AND().valueOf(article.property("PMID")).IN(new JcCollection(new ArrayList<>(shortListedArticles))));

            } else {
                queryClauses.add(WHERE.valueOf(gene1.property("Symbol")).IN(new JcCollection(new ArrayList<>(genes))));
            }

            queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));


        }

        //Should not come here in case of AND
        JcQueryResult result = executeQueryClauses(queryClauses);
        collectedResults.put(SearchQueryParams.GENES, result);
        return processQueryResult(result, SearchQueryParams.GENES);


    }

    // A class to collect article wise concepts and counts
    // This is used for ranking and collecting query wide filters.
    @Data
    @AllArgsConstructor
    private class ArticleWiseConcepts{
        String PMID;
        String symbol;
        BigDecimal count;
    }






}
