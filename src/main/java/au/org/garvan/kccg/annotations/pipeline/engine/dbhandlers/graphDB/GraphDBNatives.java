package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.ArticleWiseConcepts;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import au.org.garvan.kccg.annotations.pipeline.model.query.RankedArticle;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MERGE;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GraphDBNatives {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBNatives.class);

    private static Properties props = new Properties();
    private static IDBAccess remote;
    @Value("${spring.dbhandlers.graphdb.graphprinting}")
    private static boolean ENABLE_PRINTING;

    @Autowired
    public GraphDBNatives(@Value("${spring.dbhandlers.graphdb.endpoint}") String neo4jDbEndpoint,
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

    public static void createArticleQuery(Article article) {

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

    private static void fillEntitiesGraphData(List<IClause> queryClauses, Article article, JcNode nodeArticle) {
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

    private static IClause[] getClausesArray(List<IClause> input) {

        IClause[] arr = new IClause[input.size()];
        for (int x = 0; x < input.size(); x++)
            arr[x] = input.get(x);

        return arr;
    }

    public static JcQueryResult executeQueryClauses(List<IClause> lstQueryClauses) {
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


    private static void reconnectDb() {
        slf4jLogger.info(String.format("Reconnecting Graph Database"));
        remote = DBAccessFactory.createDBAccess(DBType.REMOTE, props);

    }




    //////////////////////////////////////////////////////////////////////////////

    public static Set<String> processQueryResult(JcQueryResult result, SearchQueryParams qType) {
        Set<String> idList = new HashSet<>();
        if (result == null) {
            slf4jLogger.info(String.format("Nothing to process as result is null."));
        } else {

            JcString PMID = new JcString("PMID");
            idList.addAll(result.resultOf(PMID));
            slf4jLogger.info(String.format("Successful search query for %s. ResultSet size: %d", qType.toString(), idList.size()));

        }

        return idList;
    }


    /***
     * This method is very localized and process result from pagination method.
     * @param PMIDs
     * @param symbols
     * @param counts
     * @return
     */
    public static List<ArticleWiseConcepts> processResultForConcepts(List<String> PMIDs, List<String> symbols, List<BigDecimal> counts) {
        List<ArticleWiseConcepts> lst = new ArrayList<>();
        for (int x = 0; x < PMIDs.size(); x++) {
            lst.add( new ArticleWiseConcepts(PMIDs.get(x), String.valueOf(symbols.get(x)), counts.get(x)));
        }
        return lst;
    }




    /***
     * This function is a sub-listing one to just get the desired page from sorted results.
     * @param countedArticles : Assuming that this is a sorted/ranked list of articles.
     * @param qParams
     * @return
     */
    public static List<RankedArticle> getRequiredPage(List<RankedArticle> countedArticles, PaginationRequestParams qParams){
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




}