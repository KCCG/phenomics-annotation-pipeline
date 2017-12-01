package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.model.SearchQuery;
import au.org.garvan.kccg.annotations.pipeline.model.SearchResult;
import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    DatabaseManager dbManager;

    public void init(){

        slf4jLogger.info(String.format("Query Manager init() called."));
//        dbManager = new DatabaseManager();
    }


    public List<SearchResult> processQuery(SearchQuery query){
        slf4jLogger.info(String.format("Processing search query with id: %s",query.getQueryId()));

        List<SearchResult> results = new ArrayList<>();
        Map<SearchQueryParams, Object> params = new HashMap<>();
        if (query.getAuthor()!=null)
            params.put(SearchQueryParams.AUTHOR, query.getAuthor());
        if (query.getGenes()!=null)
            params.put(SearchQueryParams.GENES, query.getGenes());
//        if (query.getDateRange()!=null)
//            params.put(SearchQueryParams.DATERANGE, query.getDateRange());
         if (query.getPublication()!=null)
            params.put(SearchQueryParams.PUBLICATION, query.getPublication());

         if (params.size()>0)
         {

             Map<Article, JSONObject> searchedArticles =  dbManager.searchArticles(params);
             for (Map.Entry<Article, JSONObject> entry : searchedArticles.entrySet()) {
                 results.add(constructSearchResult(entry.getKey(),entry.getValue()));
             }

         }

        slf4jLogger.info(String.format("Finished processing search query with id: %s. Final result count:%d",query.getQueryId(),results.size()));

        return results;

    }


    private SearchResult constructSearchResult(Article article, JSONObject annotations)
    {
        SearchResult searchResult = new SearchResult();
        searchResult.setPMID(article.getPubMedID());
        searchResult.setArticleAbstract(article.getArticleAbstract().getOriginalText());
        searchResult.setDatePublished(article.getDatePublished().toString());
        searchResult.setArticleTitle(article.getArticleTitle());
        searchResult.setLanguage(article.getLanguage());
        searchResult.setAuthors(article.getAuthors());

        if(!annotations.isEmpty())
        {

            if(annotations.containsKey("annotations")) {
                JSONArray genes = (JSONArray) annotations.get("annotations");
                searchResult.fillGenes(genes);

            }

        }
        else{

        }



        return searchResult;
    }

}
