package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.lexicons.GenesHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.model.SearchQuery;
import au.org.garvan.kccg.annotations.pipeline.model.SearchResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    }


    public List<SearchResult> processQuery(SearchQuery query){
        slf4jLogger.info(String.format("Processing search query with id:%s and content:%s",query.getQueryId(),query.toString()));

        List<SearchResult> results = new ArrayList<>();
        Map<SearchQueryParams, Object> params = new HashMap<>();
        if (query.getAuthor()!=null)
            params.put(SearchQueryParams.AUTHOR, query.getAuthor());
        if (query.getGene()!=null)
            params.put(SearchQueryParams.GENES, new Pair<String,List<String>> (query.getGene().getCondition(), query.getGene().getSymbols()));
        if (query.getDateRange()!=null)
            params.put(SearchQueryParams.DATERANGE,  new Pair<>(query.getDateRange().getStartDate(), query.getDateRange().getEndDate()));
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

        return rankResults(results);

    }

    public List<String> getAutocompleteList(String infix){
        int baseRank = 1000;
        int resultLimit = 15;

        List<String> geneSymbols =  DocumentPreprocessor.getHGNCGeneHandler().serchGenes(infix);
        //Ranking results
       Map<String, Integer> rankedGenes = geneSymbols.stream().collect(Collectors.toMap(Function.identity(),(a)->0));
        int rank = baseRank;
        for (Map.Entry<String, Integer> entry : rankedGenes.entrySet()) {
            if (entry.getKey().indexOf(infix)==0){
                rank = rank + infix.length();
            }
            else{
                rank = entry.getValue() - entry.getKey().indexOf(infix);
            }
            entry.setValue(rank);
        }

        List<String> returnList = rankedGenes.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(m->m.getKey())
                .collect(Collectors.toList());

        if(returnList.size()>=resultLimit)
            returnList = returnList.subList(0,resultLimit);

        return returnList;
    }


    private List<SearchResult> rankResults (List<SearchResult> inputResults){
        inputResults.sort(Comparator.comparing(SearchResult::getArticleRank).reversed());
        int newRank = inputResults.size();
        for (SearchResult result: inputResults)
        {
            result.setArticleRank(newRank);
            newRank --;
        }
        return inputResults;
    }


    private SearchResult constructSearchResult(Article article, JSONObject annotations)
    {
        SearchResult searchResult = new SearchResult();
        searchResult.setPmid(article.getPubMedID());
        searchResult.setArticleAbstract(article.getArticleAbstract().getOriginalText());
        searchResult.setDatePublished(article.getDatePublished().toString());
        searchResult.setArticleTitle(article.getArticleTitle());
        searchResult.setLanguage(article.getLanguage());
        searchResult.setAuthors(article.getAuthors());
        searchResult.setPublication(article.getPublication());

        if(!annotations.isEmpty())
        {
            if(annotations.containsKey("annotations")) {
                JSONArray genes = (JSONArray) annotations.get("annotations");
                searchResult.fillGenes(genes);
                searchResult.setArticleRank(genes.size());
            }

        }
        else{

        }

        return searchResult;
    }

}
