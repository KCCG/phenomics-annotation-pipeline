package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import com.google.common.base.Strings;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.*;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 30/10/17.
 */

@Component
public class GraphDBHandler {


    @Value("${spring.dbhandlers.graphdb.graphprinting}")
    private boolean ENABLE_PRINTING;

    private final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBHandler.class);
    Properties props = new Properties();
    IDBAccess remote;

    @Autowired
    public GraphDBHandler(@Value("${spring.dbhandlers.graphdb.endpoint}") String neo4jDbEndpoint ,
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

        List<IClause> queryClauses = new ArrayList<>();

        //Create Article node and creation clause
        JcNode nodeArticle = new JcNode("nodeArticle");
        IClause articleClause = MERGE.node(nodeArticle).label("Article")
                .property("PMID").value(Integer.toString(article.getPubMedID()))
                .property("Language").value(article.getLanguage());


        //Create authors nodes and their respective clauses (This includes both creation and mapping clauses)
        List<JcNode> nodeListAuthors = new ArrayList<>();
        List<IClause> authorsClauses = new ArrayList<>();

        for (int x = 0; x < article.getAuthors().size(); x++) {
            Author currentAuthor = article.getAuthors().get(x);
            if(currentAuthor.checkValidName())
            {
                JcNode tempAuthor = new JcNode("nodeAuthor" + Integer.toString(x));
                nodeListAuthors.add(tempAuthor);
                authorsClauses.add(MERGE.node(tempAuthor).label("Author")
                        .property("Initials").value(currentAuthor.getInitials())
                        .property("ForeName").value(currentAuthor.getForeName())
                        .property("LastName").value(currentAuthor.getLastName()));
                authorsClauses.add(CREATE.node(tempAuthor).relation().out()
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
        IClause publicationLinkClause = CREATE.node(nodeArticle).relation().out().type("PUBLISHED")
                .property("DatePublished")
                .value(article.getDatePublished())
                .node(nodePublication);

        queryClauses.add(articleClause);

        queryClauses.addAll(authorsClauses);
        queryClauses.add(publicationClause);
        queryClauses.add(publicationLinkClause);

        fillEntitiesGraphData(queryClauses, article, nodeArticle);

        JcQueryResult result = executeQueryClauses(queryClauses);

        if (result==null)
            slf4jLogger.info(String.format("Graph DB Insertion Failed"));
        else
            slf4jLogger.info(String.format("Graph DB Insertion done without errors for Article ID: %d", article.getPubMedID()));


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
                                    CREATE.node(nodeArticle).relation().out().type("CONTAINS")
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


    public Set<String> fetchArticles(Map<SearchQueryParams, Object> params) {
        Set<String> shortListedArticles = new HashSet<>();

        //Results storage for all params, and later will be sorted out.
        LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults = new LinkedHashMap<>();

        if (params.containsKey(SearchQueryParams.AUTHOR)) {
            Author author = (Author) params.get(SearchQueryParams.AUTHOR);
            shortListedArticles = runAuthorQuery(collectedResults, author, shortListedArticles);
            if (shortListedArticles.size() == 0)
                return shortListedArticles;
        }


        if (params.containsKey(SearchQueryParams.PUBLICATION)) {
            Publication publication = (Publication) params.get(SearchQueryParams.PUBLICATION);
            shortListedArticles = runPublicationQuery(collectedResults, publication, shortListedArticles);
            if (shortListedArticles.size() == 0)
                return shortListedArticles;
        }

        if (params.containsKey(SearchQueryParams.GENES)) {
            Pair<String,List<String>> gene = (Pair<String,List<String>>) params.get(SearchQueryParams.GENES);
            shortListedArticles = runGenesQuery(collectedResults,gene.getFirst(),  gene.getSecond(), shortListedArticles);
            if (shortListedArticles.size() == 0)
                return shortListedArticles;
        }
        return shortListedArticles;

    }


    private Set<String> processQueryResult(JcQueryResult result, SearchQueryParams qType) {
        Set<String> idList = new HashSet<>();
        if (result==null) {
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

        if(result.getGeneralErrors().size()>0)
        {
            List<String> messages =  result.getGeneralErrors().stream().filter(x->x.getMessage().contains("(Connection refused)")).map(t->t.getMessage()).collect(Collectors.toList());
            if(messages.size()>0) {
                slf4jLogger.info(String.format("General errors while executing NEO4J query. Message: %s and Exception: %s", messages.get(0), result.getGeneralErrors().toString()));
                reconnectDb();
            }
            else
            {
                slf4jLogger.info(String.format("General errors while executing NEO4J query. Message: %s", result.getGeneralErrors().toString()));

            }
        }
        else if (result.getDBErrors().size()>0){
            slf4jLogger.info(String.format("Database errors while executing NEO4J query. Errors:%s", result.getGeneralErrors().toString()));

        }
        else{
            if (ENABLE_PRINTING)
                print(result, "Result");
             return result;
        }

        return null;


    }

    private void reconnectDb()
    {
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
        List<Pair<String,String>> lstParams = checkAuthorNameAndGetCaseNumber(author);


        JcNode auth = new JcNode("auth");
        JcRelation c = new JcRelation("c");
        JcString PMID = new JcString("PMID");
        queryClauses.add(MATCH.node(article).label("Article")
                .relation(c).in().type("WROTE")
                .node(auth).label("Author"));

        if(lstParams.size()==1)
        {
            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond()));
        }
        else  if(lstParams.size()==2)
        {
            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond())
                    .AND().valueOf(auth.property(lstParams.get(1).getFirst())).EQUALS(lstParams.get(1).getSecond()));
        }
        else  if(lstParams.size()==3)
        {
            queryClauses.add(WHERE.valueOf(auth.property(lstParams.get(0).getFirst())).EQUALS(lstParams.get(0).getSecond())
                    .AND().valueOf(auth.property(lstParams.get(1).getFirst())).EQUALS(lstParams.get(1).getSecond())
                    .AND().valueOf(auth.property(lstParams.get(2).getFirst())).EQUALS(lstParams.get(2).getSecond()));
        }

        queryClauses.add(RETURN.value(article.property("PMID")).AS(PMID));


        JcQueryResult result = executeQueryClauses(queryClauses);
        collectedResults.put(SearchQueryParams.AUTHOR, result);

        return processQueryResult(result, SearchQueryParams.AUTHOR);


    }

    private List<Pair<String,String>> checkAuthorNameAndGetCaseNumber(Author author) {
        List<Pair<String,String>> lstParams = new ArrayList<>();
        if(!Strings.isNullOrEmpty(author.getForeName()))
            lstParams.add(Pair.of("ForeName", author.getForeName() ));
        if(!Strings.isNullOrEmpty(author.getLastName()))
            lstParams.add(Pair.of("LastName", author.getLastName() ));
        if(!Strings.isNullOrEmpty(author.getInitials()))
            lstParams.add(Pair.of("Initials", author.getInitials() ));

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


    private Set<String> runGenesQuery(LinkedHashMap<SearchQueryParams, JcQueryResult> collectedResults, String condition, List<String> genes, Set<String> shortListedArticles) {

        JcNode article = new JcNode("a");
        List<IClause> queryClauses = new ArrayList<>();

        switch (condition){
            case "AND":
                if(genes.size()>3)
                    genes = genes.subList(0,3);

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


}
