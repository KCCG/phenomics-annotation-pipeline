package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.model.*;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Int;

import java.util.*;
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

    public void init() {

        slf4jLogger.info(String.format("Query Manager init() called."));
    }


    public PaginatedSearchResult processQuery(SearchQuery query, Integer pageSize, Integer pageNo) {
        slf4jLogger.info(String.format("Processing search query with id:%s and content:%s", query.getQueryId(), query.toString()));

        PaginationRequestParams qParams = new PaginationRequestParams(pageSize, pageNo);
        slf4jLogger.info(String.format("Query id:%s params are pageSize:%d, pageNo:%d", query.getQueryId(), qParams.getPageSize(), qParams.getPageNo()));


        List<SearchResult> results = new ArrayList<>();
        DBManagerResultSet resultSet = new DBManagerResultSet();


        Map<SearchQueryParams, Object> params = new HashMap<>();
        if (query.getAuthor() != null)
            params.put(SearchQueryParams.AUTHOR, query.getAuthor());
        if (query.getGene() != null)
            params.put(SearchQueryParams.GENES, new Pair<String, List<String>>(query.getGene().getCondition(), query.getGene().getSymbols()));
        if (query.getDateRange() != null)
            params.put(SearchQueryParams.DATERANGE, new Pair<>(query.getDateRange().getStartDate(), query.getDateRange().getEndDate()));
        if (query.getPublication() != null)
            params.put(SearchQueryParams.PUBLICATION, query.getPublication());

        if (params.size() > 0) {
            resultSet = dbManager.searchArticles(params, qParams);
            for (RankedArticle entry : resultSet.getRankedArticles()) {
                results.add(constructSearchResult(entry));
            }
        }

        slf4jLogger.info(String.format("Finished processing search query with id: %s. Total Articles:%d Result set:%d",
                query.getQueryId(), qParams.getTotalArticles(), results.size()));
        return constructFinalResult(results , resultSet, qParams);

    }

    public PaginatedSearchResult constructFinalResult( List<SearchResult> results, DBManagerResultSet resultSet, PaginationRequestParams qParams ){
        PaginatedSearchResult finalResult = new PaginatedSearchResult();
        finalResult.setArticles(results);
        finalResult.setPagination(qParams);

        List<GeneFilter> lstGeneFilter = new ArrayList<>();
        for (Map.Entry<String, Integer> entry: resultSet.getGeneCounts().entrySet()){
            lstGeneFilter.add(new GeneFilter(entry.getKey(), entry.getValue()));
        }
        finalResult.setFilters(new ConceptFilter(lstGeneFilter));
        return  finalResult;

    }

    public List<String> getAutocompleteList(String infix) {
        int baseRank = 1000;
        int resultLimit = 15;

        List<String> geneSymbols = DocumentPreprocessor.getHGNCGeneHandler().serchGenes(infix);
        //Ranking results
        Map<String, Integer> rankedGenes = geneSymbols.stream().collect(Collectors.toMap(Function.identity(), (a) -> 0));
        int rank = baseRank;
        for (Map.Entry<String, Integer> entry : rankedGenes.entrySet()) {
            if (entry.getKey().indexOf(infix) == 0) {
                rank = rank + infix.length();
            } else {
                rank = entry.getValue() - entry.getKey().indexOf(infix);
            }
            entry.setValue(rank);
        }

        List<String> returnList = rankedGenes.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(m -> m.getKey())
                .collect(Collectors.toList());

        if (returnList.size() >= resultLimit)
            returnList = returnList.subList(0, resultLimit);

        return returnList;
    }


    @Deprecated
    private List<SearchResult> rankResults(List<SearchResult> inputResults) {
        inputResults.sort(Comparator.comparing(SearchResult::getArticleRank).reversed());
        int newRank = inputResults.size();
        for (SearchResult result : inputResults) {
            result.setArticleRank(newRank);
            newRank--;
        }
        return inputResults;
    }


    private SearchResult constructSearchResult(RankedArticle rankedArticle) {
        Article article = rankedArticle.getArticle();
        JSONObject annotations = rankedArticle.getAnnotations();
        // ^ This change is made to have one DTO throughout the hierarchy for simplicity of code.
        SearchResult searchResult = new SearchResult();
        searchResult.setPmid(article.getPubMedID());
        searchResult.setArticleAbstract(article.getArticleAbstract().getOriginalText());
        searchResult.setDatePublished(article.getDatePublished().toString());
        searchResult.setArticleTitle(article.getArticleTitle());
        searchResult.setLanguage(article.getLanguage());
        searchResult.setAuthors(article.getAuthors());
        searchResult.setPublication(article.getPublication());

        if (!annotations.isEmpty()) {
            if (annotations.containsKey("annotations")) {
                JSONArray genes = (JSONArray) annotations.get("annotations");
                searchResult.fillGenes(genes);
                searchResult.setArticleRank(rankedArticle.getRank());
            }

        } else {

        }

        return searchResult;
    }

}
