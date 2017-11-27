package au.org.garvan.kccg.annotations.pipeline.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.entities.publicational.Article;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.MERGE;
import iot.jcypher.query.values.JcNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ahmed on 30/10/17.
 */
public class GraphDBHandler {

    private final Logger slf4jLogger = LoggerFactory.getLogger(GraphDBHandler.class);


    Properties props = new Properties();
    IDBAccess remote ;

    GraphDBHandler(){

        props.setProperty(DBProperties.SERVER_ROOT_URI, "bolt://localhost:7687");

                remote = DBAccessFactory.createDBAccess(DBType.REMOTE, props);

    }



    public void createArticleQuery(Article article){

        List<IClause> queryClauses = new ArrayList<>();

        //Create Article node and creation clause
        JcNode nodeArticle = new JcNode("nodeArticle");
        IClause articleClause = CREATE.node(nodeArticle).label("Article")
                .property("PMID").value(Integer.toString(article.getPubMedID()))
                .property("Language").value(article.getLanguage());



        //Create authors nodes and their respective clauses (This includes both creation and mapping clauses)
        List<JcNode> nodeListAuthors = new ArrayList<>();
        List<IClause> authorsClauses = new ArrayList<>();

        for(int x = 0; x<article.getAuthors().size(); x++){
            JcNode tempAuthor = new JcNode("nodeAuthor" + Integer.toString(x));
            nodeListAuthors.add(tempAuthor);
            authorsClauses.add( MERGE.node(tempAuthor).label("Author")
                    .property("Initials").value(article.getAuthors().get(x).getInitials())
                    .property("ForeName").value(article.getAuthors().get(x).getForeName())
                    .property("LastName").value(article.getAuthors().get(x).getLastName()));
            authorsClauses.add(CREATE.node(tempAuthor).relation().out()
                    .type("WROTE").property("Order").value(x+1)
                    .node(nodeArticle));
        }

        //Create publicational node and creation clause
        JcNode nodePublication = new JcNode("nodePublication");
        IClause publicationClause = MERGE.node(nodePublication).label("Publication")
                .property("Title").value(article.getPublication().getTitle())
                .property("ISOAbbreviation").value(article.getPublication().getISOAbbreviation())
                .property("ISSNType").value(article.getPublication().getISSNType())
                .property("ISSNNumber").value(article.getPublication().getISSNNumber());
        IClause publicationLinkClause = CREATE.node(nodeArticle).relation().out().type("PUBLISHED")
                .property("DatePublished")
                .value(article.getDatePublished())
                .node(nodePublication);

        queryClauses.add(articleClause);

        queryClauses.addAll(authorsClauses);
        queryClauses.add(publicationClause);
        queryClauses.add(publicationLinkClause);

        fillEntitiesGraphData(queryClauses, article, nodeArticle);


        JcQuery query = new JcQuery();
        query.setClauses(getClausesArray(queryClauses));


        JcQueryResult result = remote.execute(query);


        if(result.getDBErrors().size()>0)
            slf4jLogger.info(String.format( "Graph DB Insertion Done. DB Error:%s ", result.getDBErrors().toString() ));
        else
            slf4jLogger.info(String.format( "Graph DB Insertion done without errors."));




    }

    private void fillEntitiesGraphData(List<IClause> queryClauses, Article article, JcNode nodeArticle) {
        //TODO: Find entities from Abstract and fill node and relationship
        Map<APSentence, List<APToken>> entities = article.getArticleAbstract().getTokensWithEntities();
        if(entities.size()>0)
        {
            for (Map.Entry<APSentence, List<APToken>> entry : entities.entrySet())
            {
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


    private IClause[] getClausesArray(List<IClause> input){

        IClause [] arr = new IClause[input.size()];
        for (int x = 0; x<input.size();x++)
            arr[x] = input.get(x);

        return arr;
    }




}
