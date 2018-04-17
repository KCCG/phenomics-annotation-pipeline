package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.graphDB;

import au.org.garvan.kccg.annotations.pipeline.model.query.PaginationRequestParams;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GraphDBCachedHandlerTest {
//
//    endpoint:
//    graphprinting: false
//    username: neo4j
//    password: neodev

    GraphDBNatives graphDBNatives = new GraphDBNatives("http://52.64.25.182:7474/", "neo4j", "neodev");
    GraphDBCachedHandler graphDBCachedHandler = new GraphDBCachedHandler();
    @Before
    public void init(){

    }

    @Test
    public void fetchArticlesWithFilters() {
        List<String> searchItems = Arrays.asList("1100");
        List<String> filterItems = Arrays.asList("1101");

        PaginationRequestParams params = new PaginationRequestParams(10,2);
        graphDBCachedHandler.fetchArticlesWithFilters("1", searchItems, filterItems, params);




    }
}