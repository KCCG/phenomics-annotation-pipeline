package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APPhenotype;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.SearchQueryParams;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.model.query.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public PaginatedSearchResultV1 processQueryV1(SearchQueryV1 query, Integer pageSize, Integer pageNo) {
        slf4jLogger.info(String.format("Processing search query with id:%s and content:%s", query.getQueryId(), query.toString()));

        PaginationRequestParams qParams = new PaginationRequestParams(pageSize, pageNo);
        slf4jLogger.info(String.format("Query id:%s params are pageSize:%d, pageNo:%d", query.getQueryId(), qParams.getPageSize(), qParams.getPageNo()));


        List<SearchResultV1> results = new ArrayList<>();
        DBManagerResultSet resultSet = new DBManagerResultSet();

        if(query.getSearchItems().size()>0)
        {
            List<APGene> requestedGenes = DocumentPreprocessor.getHGNCGeneHandler().geteGenesWithIDs(query.getGeneIDs());

            Map<SearchQueryParams, Object> params = new HashMap<>();
            if (requestedGenes.size()>0)
                params.put(SearchQueryParams.GENES, new Pair<String, List<String>>("OR", requestedGenes.stream().map(g-> g.getApprovedSymbol()).collect(Collectors.toList())));

            if (params.size() > 0) {
                resultSet = dbManager.searchArticles(params, qParams);
                for (RankedArticle entry : resultSet.getRankedArticles()) {
                    results.add(constructSearchResultV1(entry));
                }
            }
        }


        slf4jLogger.info(String.format("Finished processing search query with id: %s. Total Articles:%d Result set:%d",
                query.getQueryId(), qParams.getTotalArticles(), results.size()));
        return constructFinalResultV1(results , resultSet, qParams , query);

    }




    public PaginatedSearchResult constructFinalResult( List<SearchResult> results, DBManagerResultSet resultSet, PaginationRequestParams qParams ){
        PaginatedSearchResult finalResult = new PaginatedSearchResult();
        finalResult.setArticles(results);
        finalResult.setPagination(qParams);

        List<ConceptFilter> lstGeneFilter = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : resultSet.getGeneCounts().entrySet()) {
            String geneSymbol = entry.getKey();
            Integer count = entry.getValue();
            String id = String.valueOf(DocumentPreprocessor.getHGNCGeneHandler().getGene(geneSymbol).getHGNCID());
            lstGeneFilter.add(new ConceptFilter(
                    id, AnnotationType.GENE.toString(),
                    geneSymbol,
                    count, count));
        }
        List<ConceptFilter> sortedLstGeneFilter = lstGeneFilter.stream().sorted(Comparator.comparing(ConceptFilter::getRank).reversed()).collect(Collectors.toList());
        finalResult.setFilters(sortedLstGeneFilter);
        return  finalResult;

    }

    public PaginatedSearchResultV1 constructFinalResultV1( List<SearchResultV1> results, DBManagerResultSet resultSet, PaginationRequestParams qParams, SearchQueryV1 query ){
        PaginatedSearchResultV1 finalResult = new PaginatedSearchResultV1();
        finalResult.setArticles(results);
        finalResult.setPagination(qParams);

        List<ConceptFilter> lstGeneFilter = new ArrayList<>();
        List<String> geneIds = query.getGeneIDs();
        for (Map.Entry<String, Integer> entry : resultSet.getGeneCounts().entrySet()) {
            String geneSymbol = entry.getKey();
            Integer count = entry.getValue();
            String id = String.valueOf(DocumentPreprocessor.getHGNCGeneHandler().getGene(geneSymbol).getHGNCID());
            Integer rank = geneIds.contains(id)? count+1000: count;
            lstGeneFilter.add(new ConceptFilter(
                    id, AnnotationType.GENE.toString(),
                    geneSymbol,
                    rank,
                    count));
        }
        List<ConceptFilter> sortedLstGeneFilter = lstGeneFilter.stream().sorted(Comparator.comparing(ConceptFilter::getRank).reversed()).collect(Collectors.toList());
        finalResult.setFilters(sortedLstGeneFilter);
        finalResult.setQueryId(query.getQueryId());
        return  finalResult;

    }

    public List<String> getAutocompleteList(String infix) {
        int baseRank = 1000;
        int resultLimit = 15;

        List<String> geneSymbols = DocumentPreprocessor.getHGNCGeneHandler().serchGenes(infix);
        //Ranking results
        Map<String, Integer> rankedGenes = geneSymbols.stream().collect(Collectors.toMap(Function.identity(), (a) -> 0));


        for (Map.Entry<String, Integer> entry : rankedGenes.entrySet()) {
            int rank = baseRank;
            if (entry.getKey().indexOf(infix) == 0) {
                rank = rank*2 - (entry.getKey().length()-infix.length());
            } else {
                rank = rank - (5*entry.getKey().indexOf(infix)) - (entry.getKey().length()-infix.length());
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
        JSONObject annotations = rankedArticle.getJsonAnnotations();
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

    private SearchResultV1 constructSearchResultV1(RankedArticle rankedArticle) {
        Article article = rankedArticle.getArticle();
        JSONObject annotations = rankedArticle.getJsonAnnotations();
        // ^ This change is made to have one DTO throughout the hierarchy for simplicity of code.
        SearchResultV1 searchResult = new SearchResultV1();
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



    /***
     * Generic list for auto-complete
     * Currently supports genes and phenotypes
     * Sorting is based on index of search and length of the term.
     * @param infix
     * @return
     */
    public List<RankedAutoCompleteEntity> getAutocompleteGenericList(String infix) {
        int baseRank = 1000;
        int resultLimit = 15;
        List<RankedAutoCompleteEntity> returnList = new ArrayList<>();


        //Get and sort Genes
        List<APGene> shortlistedGenes = DocumentPreprocessor.getHGNCGeneHandler().searchGenes(infix);
        List<APPhenotype> shortListedPhenotypes = DocumentPreprocessor.getTempPhenotypeHandler().serchPhenotype(infix);
        //Ranking results
        Map<Object, Integer> rankedEntities = new HashMap<>();
        shortListedPhenotypes.stream().forEach(x-> rankedEntities.put(x, 0));
        shortlistedGenes.stream().forEach(x-> rankedEntities.put(x, 0));

        for (Map.Entry<Object, Integer> entry : rankedEntities.entrySet()) {
            int rank = baseRank;

            if(entry.getKey() instanceof APGene) {
                String symbol = ((APGene)entry.getKey()).getApprovedSymbol();
                if (symbol.indexOf(infix) == 0) {
                    rank = rank * 2 - (symbol.length() - infix.length());
                } else {
                    rank = rank - (5 * symbol.indexOf(infix)) - (symbol.length() - infix.length());
                }
                entry.setValue(rank);
            }
            if(entry.getKey() instanceof APPhenotype){
                List<String> symbols = Arrays.asList( ((APPhenotype)entry.getKey()).getText().toUpperCase().split(" "));
                Integer termNumber=0;
                for(String symbol:symbols) {
                    rank = baseRank;
                    if (symbol.indexOf(infix) == 0) {
                        rank = rank * 2 - (symbol.length() - infix.length())  - symbols.size();
                    } else {
                        rank = rank - (5 * symbol.indexOf(infix)) - (symbol.length() - infix.length() - symbols.size());
                    }
                    rank = rank - 10*termNumber;
                    if(entry.getValue()<rank)
                        entry.setValue(rank);
                    termNumber ++;
                }
            }
        }


        //Show equal number of items from auto-complete list.
        Integer collectedGeneSize = Math.min(resultLimit/2, shortlistedGenes.size());
        Integer collectedPhenotypeSize = Math.min(resultLimit-collectedGeneSize, shortListedPhenotypes.size());


        Map<Object,Integer> topRankedPhenotypes =
                rankedEntities.entrySet().stream().filter(x-> x.getKey() instanceof APPhenotype)
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(collectedPhenotypeSize)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Map<Object,Integer> topRankedGenes =
                rankedEntities.entrySet().stream().filter(x-> x.getKey() instanceof APGene)
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(collectedGeneSize)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));



        for(Map.Entry<Object, Integer> entry: topRankedGenes.entrySet()){
            Object object = entry.getKey();
            RankedAutoCompleteEntity entity = new RankedAutoCompleteEntity();
                APGene gene = (APGene) object;
                entity.setId(String.valueOf(gene.getHGNCID()));
                entity.setText(gene.getApprovedSymbol());
                entity.setType(AnnotationType.GENE.toString());
                entity.setRank(entry.getValue());
                returnList.add(entity);
        }

        for(Map.Entry<Object, Integer> entry: topRankedPhenotypes.entrySet()){
            Object object = entry.getKey();
            RankedAutoCompleteEntity entity = new RankedAutoCompleteEntity();
                APPhenotype phenotype = (APPhenotype) object;
                entity.setId(String.valueOf(phenotype.getId()));
                entity.setText(phenotype.getText());
                entity.setType(AnnotationType.PHENOTYPE.toString());
                entity.setRank(entry.getValue());
                returnList.add(entity);
        }

        returnList.sort(Comparator.comparing(RankedAutoCompleteEntity::getRank).reversed());

        return returnList;
    }


}
