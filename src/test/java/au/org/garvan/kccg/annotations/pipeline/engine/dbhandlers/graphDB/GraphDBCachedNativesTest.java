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

        graphDBQueryStringBuilder.buildQueryForArticlePage(Arrays.asList("1100", "1101"), Arrays.asList("HP:0000718", "HP:0012125"), new ArrayList<>(), 0,10, false);
        graphDBQueryStringBuilder.buildQueryForArticleCount(Arrays.asList("1100", "1101"), Arrays.asList("HP:0000718", "HP:0012125"), new ArrayList<>(),  false);
        graphDBQueryStringBuilder.buildQueryForSearchAndFilterItemsFilters(Arrays.asList("1100", "1101"), Arrays.asList("HP:0000718", "HP:0012125"), new ArrayList<>(), false);


    }




    @Test
    public void runQueryForArticlesSearchAll() {

        graphDBQueryStringBuilder.buildQueryForArticlePage(Arrays.asList("1100", "1101"), Arrays.asList("HP:0000718", "HP:0012125"), new ArrayList<>(), 0,10, true);
        graphDBQueryStringBuilder.buildQueryForArticleCount(Arrays.asList("1100", "1101"), Arrays.asList("HP:0000718", "HP:0012125"), new ArrayList<>(),  true);
        graphDBQueryStringBuilder.buildQueryForSearchAndFilterItemsFilters(Arrays.asList("1100", "1101"), Arrays.asList("HP:0000718", "HP:0012125"), new ArrayList<>(), true);


    }
}