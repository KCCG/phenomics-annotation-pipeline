package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.model.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ahmed on 28/11/17.
 */
@Service
public class QueryManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(QueryManager.class);
    DatabaseManager dbManager;

    public void init(){

        slf4jLogger.info(String.format("Initializing Query Manager"));
        dbManager = new DatabaseManager();
    }


    public void processQuery(SearchQuery query){
        Map<SearchQueryParams, Object> params = new HashMap<>();
        if (query.getAuthor()!=null)
            params.put(SearchQueryParams.AUTHOR, query.getAuthor());
        if (query.getGenes()!=null)
            params.put(SearchQueryParams.GENES, query.getGenes());
        if (query.getDateRange()!=null)
            params.put(SearchQueryParams.DATERANGE, query.getDateRange());
         if (query.getPublication()!=null)
            params.put(SearchQueryParams.PUBLICATION, query.getPublication());

         dbManager.searchArticles(params);

    }

}
