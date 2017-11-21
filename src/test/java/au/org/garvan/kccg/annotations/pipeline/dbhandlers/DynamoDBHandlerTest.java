package au.org.garvan.kccg.annotations.pipeline.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.connectors.BaseConnector;
import au.org.garvan.kccg.annotations.pipeline.connectors.JsonConnector;
import au.org.garvan.kccg.annotations.pipeline.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.enums.CommonParams;
import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import jdk.nashorn.internal.runtime.Debug;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.debugger.Debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by ahmed on 10/11/17.
 */
public class DynamoDBHandlerTest {

    private final Logger slf4jLogger = LoggerFactory.getLogger(DynamoDBHandlerTest.class);


    DynamoDBHandler dbHanlde = new DynamoDBHandler();
    private BaseConnector testConnector;
    private List<Article> articles;


    JSONObject jsonObject = new JSONObject();
    @Before
    public void setUp() throws Exception {
        jsonObject.put("PMID","123");
        testConnector = new JsonConnector();
        articles =  testConnector.getArticles("test1000.json", CommonParams.FILENAME);


    }

    @Test
    public void insertItems() throws Exception {

        for (Article testArticle: articles) {
            testArticle.getArticleAbstract().hatch();
            dbHanlde.insertItem(testArticle.constructJson(), testArticle.getAbstractEntities());
        }
    }

    @Test
    public void insertItem() throws Exception {
        int pubMedId = 29088305;
        Article testArticle = articles.stream().filter(a -> a.getPubMedID() == pubMedId).collect(Collectors.toList()).get(0);
        testArticle.getArticleAbstract().hatch();
        dbHanlde.insertItem(testArticle.constructJson(), testArticle.getAbstractEntities());

    }

    @Test
    public void getItem() throws Exception {
        int pubMedId = 29088578;
        JSONObject jsonObject=  dbHanlde.getArticle(pubMedId);
        JSONObject jsonObject1 = dbHanlde.getAnnotations(pubMedId, AnnotationType.GENE);
        Article a = new Article(new DynamoDBObject(jsonObject, EntityType.Article), jsonObject1);
        a = a;

    }

    @Test
    public void insertAndGet()throws Exception{
//        for (Article testArticle: articles.subList(0,100)) {
//            slf4jLogger.info(String.format("processing article with ID:%d", testArticle.getPubMedID()));
//            testArticle.getArticleAbstract().hatch();
//            dbHanlde.insertItem(testArticle.constructJson(), testArticle.getAbstractEntities());
//        }

        List<Article> returnedArtciles = new ArrayList<>();
        for (Article testArticle: articles.subList(0,100)) {
            slf4jLogger.info(String.format("getting article with ID:%d", testArticle.getPubMedID()));

            testArticle.getArticleAbstract().hatch();
            JSONObject jsonObject=  dbHanlde.getArticle(testArticle.getPubMedID());
            JSONObject jsonObject1 = dbHanlde.getAnnotations(testArticle.getPubMedID(), AnnotationType.GENE);
            Article a = new Article(new DynamoDBObject(jsonObject, EntityType.Article), jsonObject1);
            returnedArtciles.add(a);
        }
        for (int x = 0; x<articles.subList(0,100).size(); x++)
        {
            Article original = articles.get(x);
            Article retrieved = returnedArtciles.get(x);

            slf4jLogger.info("Stop and See");
        }
    }


}