package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class GraphDBCachedNativesTest {

    GraphDBCachedNatives.GraphDBQueryStringBuilder graphDBQueryStringBuilder = new GraphDBCachedNatives.GraphDBQueryStringBuilder();

    @Test
    public void runQueryForArticles() {
       String testString =  graphDBQueryStringBuilder.buildQueryForArticlePage(Arrays.asList("1100", "1101"), Arrays.asList("HP:0000718", "HP:0012125"), new ArrayList<>(), 0,10);
        Assert.assertEquals("MATCH(e:Entity)-[c:CONTAINS]-(a:Article)\n" +
                "WHERE e.EID in [\"1100\", \"1101\"]\n" +
                "WITH a, c, e\n" +
                "MATCH (e1:Entity {EID:\"HP:0000718\"})-[c1:CONTAINS]-(a)\n" +
                "WITH a, c, e, c1 \n" +
                "MATCH (e2:Entity {EID:\"HP:0012125\"})-[c2:CONTAINS]-(a)\n" +
                "RETURN distinct (a.PMID) as pmid, count(distinct(e)) as shits , ( count(distinct(c)) + count(distinct(c1)) + count(distinct(c2)) ) as hits \n" +
                "ORDER BY shits desc, hits desc, a.PMID desc \n" +
                "SKIP 0\n" +
                "LIMIT 10", testString);

    }
}