package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.AnnotationControl;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.Utilities;
import au.org.garvan.kccg.annotations.pipeline.engine.caches.L1cache.ArticleResponseCache;
import au.org.garvan.kccg.annotations.pipeline.engine.caches.L1cache.FiltersResponseCache;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.cache.FiltersCacheObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DBManagerResultSet;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.APMultiWordAnnotationMapper;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.model.query.*;
import info.aduna.text.StringUtil;
import org.apache.commons.lang3.StringUtils;
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



    public PaginatedSearchResult processQuery(SearchQueryV1 query, Integer pageSize, Integer pageNo) {
        slf4jLogger.info(String.format("Processing search query with id:%s and content:%s", query.getQueryId(), query.toString()));

        PaginationRequestParams qParams = new PaginationRequestParams(pageSize, pageNo);
        slf4jLogger.info(String.format("Query id:%s params are pageSize:%d, pageNo:%d", query.getQueryId(), qParams.getPageSize(), qParams.getPageNo()));

        List<SearchResultV1> results = new ArrayList<>();
        DBManagerResultSet resultSet = new DBManagerResultSet();

        if (query.getSearchItems().size() > 0) {

            List<Pair<String, String>> searchItems = query.getSearchItems()
                    .stream()
                    .map(x -> new Pair<String, String>(x.getType(), x.getId()))
                    .collect(Collectors.toList());

            List<Pair<String, String>> filterItems = new ArrayList<>();
            filterItems = query.getFilterItems()
                    .stream()
                    .map(x -> new Pair<String, String>(x.getType(), x.getId()))
                    .collect(Collectors.toList());

            resultSet = dbManager.searchArticlesWithFilters(query.getQueryId(), searchItems, filterItems, qParams);
            for (RankedArticle entry : resultSet.getRankedArticles()) {
                if (entry.getArticle() != null)
                    results.add(constructSearchResult(entry));
            }
        }
        slf4jLogger.info(String.format("Finished processing search query with id: %s. Total Articles:%d Result set:%d",
                query.getQueryId(), qParams.getTotalArticles(), results.size()));
        return constructFinalResult(results, resultSet, qParams, query);

    }


    public PaginatedSearchResultV2 processQueryV2(SearchQueryV2 query, Integer pageSize, Integer pageNo, Boolean isHistorical) {
        slf4jLogger.debug(String.format("********************QueryID:%s*********************", query.getQueryId()));

        boolean filterCacheHit = false;
        boolean articleCacheHit = false;

        slf4jLogger.info(String.format("Processing search query with id:%s and content:%s", query.getQueryId(), query.toString()));

        if (query.getSearchItems().size() > 0) {

            PaginationRequestParams qParams = new PaginationRequestParams(pageSize, pageNo);
            qParams.setIncludeHistorical(isHistorical);
            slf4jLogger.info(String.format("Query id:%s params are pageSize:%d, pageNo:%d, isHistorical:%s", query.getQueryId(), qParams.getPageSize(), qParams.getPageNo(), qParams.getIncludeHistorical().toString()));


            FiltersCacheObject cachedFilters = FiltersResponseCache.getFilters(query, qParams.getIncludeHistorical());
            List<SearchResultV2> results = new ArrayList<>();
            if (cachedFilters != null) {
                filterCacheHit = true;
                qParams.setTotalArticles(cachedFilters.getArticlesCount());
                qParams.setTotalPages((int) Math.ceil((double) qParams.getTotalArticles() / qParams.getPageSize()));
                results = ArticleResponseCache.getArticles(query, qParams);
                if (results != null)
                    articleCacheHit = true;
                else
                    results = new ArrayList<>();
            }


            if (filterCacheHit && articleCacheHit) {
                slf4jLogger.info(String.format("L1 Cache Hit(Complete): Finished processing search query with id: %s. Total Articles:%d Result set:%d",
                        query.getQueryId(), cachedFilters.getArticlesCount(), results.size()));
                return constructCachedFinalResultV2(results, cachedFilters, qParams, query);
            } else if (filterCacheHit) {
                DBManagerResultSet resultSet = new DBManagerResultSet();
                resultSet = dbManager.searchArticlesWithFiltersV2(query.getQueryId(), query.getSearchItems(), query.getFilterItems(), qParams, false, cachedFilters);
                for (RankedArticle entry : resultSet.getRankedArticles()) {
                    if (entry.getArticle() != null)
                        results.add(constructSearchResultV2(entry));
                }
                resultSet.setConceptCounts(cachedFilters.getFinalFilters());
                //Cache articles
                ArticleResponseCache.putArticles(query, qParams, results);

                slf4jLogger.info(String.format("L1 Cache Hit(Filters): Finished processing search query with id: %s. Total Articles:%d Result set:%d",
                        query.getQueryId(), cachedFilters.getArticlesCount(), results.size()));
                //Construct result and return
                return constructFinalResultV2(results, resultSet, qParams, query);


            } else if (!filterCacheHit && !articleCacheHit) {
                DBManagerResultSet resultSet = new DBManagerResultSet();
                //Point: CachedFilter object is filled down the line
                cachedFilters = new FiltersCacheObject();
                resultSet = dbManager.searchArticlesWithFiltersV2(query.getQueryId(), query.getSearchItems(), query.getFilterItems(), qParams, true, cachedFilters);
                for (RankedArticle entry : resultSet.getRankedArticles()) {
                    if (entry.getArticle() != null)
                        results.add(constructSearchResultV2(entry));
                }
                //Cache result
                FiltersResponseCache.putFilters(query, qParams.getIncludeHistorical(), cachedFilters);
                ArticleResponseCache.putArticles(query, qParams, results);
                //Log, Construct result and return

                slf4jLogger.info(String.format("Finished processing search query with id: %s. Total Articles:%d Result set:%d",
                        query.getQueryId(), qParams.getTotalArticles(), results.size()));
                return constructFinalResultV2(results, resultSet, qParams, query);
            }


        }

        return new PaginatedSearchResultV2();
    }


    public PaginatedSearchResult constructFinalResult(List<SearchResultV1> results, DBManagerResultSet resultSet, PaginationRequestParams qParams, SearchQueryV1 query) {
        PaginatedSearchResult finalResult = new PaginatedSearchResult();
        finalResult.setArticles(results);
        finalResult.setPagination(qParams);

        List<ConceptFilter> lstGeneFilter = resultSet.getConceptCounts();
        List<ConceptFilter> sortedLstGeneFilter = lstGeneFilter.stream().sorted(Comparator.comparing(ConceptFilter::getRank).reversed()).collect(Collectors.toList());
        finalResult.setFilters(sortedLstGeneFilter);
        finalResult.setQuery(getQueryEcho(query));
        return finalResult;

    }


    public PaginatedSearchResultV2 constructFinalResultV2(List<SearchResultV2> results, DBManagerResultSet resultSet, PaginationRequestParams qParams, SearchQueryV2 query) {
        PaginatedSearchResultV2 finalResult = new PaginatedSearchResultV2();
        finalResult.setArticles(results);
        finalResult.setPagination(qParams);

        List<ConceptFilter> lstGeneFilter = resultSet.getConceptCounts();
        List<ConceptFilter> sortedLstGeneFilter = lstGeneFilter.stream().sorted(Comparator.comparing(ConceptFilter::getRank).reversed()).collect(Collectors.toList());
        finalResult.setFilters(AnnotationControl.getControlledFilters(sortedLstGeneFilter));

        finalResult.setQuery(getQueryEcho(query));
        return finalResult;

    }

    public PaginatedSearchResultV2 constructCachedFinalResultV2(List<SearchResultV2> cachedSearchResult, FiltersCacheObject cachedFilters, PaginationRequestParams qParams, SearchQueryV2 query) {
        PaginatedSearchResultV2 finalResult = new PaginatedSearchResultV2();
        finalResult.setArticles(cachedSearchResult);

        qParams.setTotalArticles(cachedFilters.getArticlesCount());
        qParams.setTotalPages((int) Math.ceil((double) qParams.getTotalArticles() / qParams.getPageSize()));
        finalResult.setPagination(qParams);

        List<ConceptFilter> sortedLstGeneFilter = cachedFilters.getFinalFilters().stream().sorted(Comparator.comparing(ConceptFilter::getRank).reversed()).collect(Collectors.toList());
        finalResult.setFilters(AnnotationControl.getControlledFilters(sortedLstGeneFilter));
        finalResult.setQuery(getQueryEcho(query));
        return finalResult;

    }

    private SearchQueryEcho getQueryEcho(SearchQueryV2 query){
        List<ConceptFilter> sItems = query.getSearchItems().stream()
                                            .map(s-> (Utilities.getFilterBasedOnId(s)))
                                            .collect(Collectors.toList());

        List<ConceptFilter> fItems = query.getFilterItems().stream()
                                        .map(f-> Utilities.getFilterBasedOnId(f))
                                        .collect(Collectors.toList());

        SearchQueryEcho searchQueryEcho = new SearchQueryEcho(
                query.getQueryId(),
                sItems,
                fItems
        );

        return searchQueryEcho;

    }

    private SearchQueryEcho getQueryEcho(SearchQueryV1 query){
        List<ConceptFilter> sItems = query.getSearchItems().stream()
                .map(s-> (Utilities.getFilterBasedOnId(s.getId())))
                .collect(Collectors.toList());

        List<ConceptFilter> fItems = query.getFilterItems().stream()
                .map(f-> Utilities.getFilterBasedOnId(f.getId()))
                .collect(Collectors.toList());

        SearchQueryEcho searchQueryEcho = new SearchQueryEcho(
                query.getQueryId(),
                sItems,
                fItems
        );

        return searchQueryEcho;

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
                rank = rank * 2 - (entry.getKey().length() - infix.length());
            } else {
                rank = rank - (5 * entry.getKey().indexOf(infix)) - (entry.getKey().length() - infix.length());
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


    private SearchResultV1 constructSearchResult(RankedArticle rankedArticle) {
        Article article = rankedArticle.getArticle();
        List<JSONObject> annotations = rankedArticle.getJsonAnnotations();
        // ^ This change is made to have one DTO throughout the hierarchy for simplicity of code.
        SearchResultV1 searchResult = new SearchResultV1();
        searchResult.setPmid(article.getPubMedID());
        searchResult.setArticleAbstract(article.getArticleAbstract().getOriginalText());
        searchResult.setDatePublished(article.getDatePublished().toString());
        searchResult.setArticleTitle(article.getArticleTitle());
        searchResult.setLanguage(article.getLanguage());
        searchResult.setAuthors(article.getAuthors());
        searchResult.setPublication(article.getPublication());

        if (annotations.size() > 0) {
            searchResult.setArticleRank(rankedArticle.getRank());
            for (JSONObject annotation : annotations) {
                if (annotation.get("annotationType").toString().equals(AnnotationType.GENE.toString())) {
                    JSONArray genes = (JSONArray) annotation.get("annotations");
                    searchResult.fillAnnotations(genes, AnnotationType.GENE);
                }

                if (annotation.get("annotationType").toString().equals(AnnotationType.PHENOTYPE.toString())) {
                    JSONArray phenotypes = (JSONArray) annotation.get("annotations");
                    searchResult.fillAnnotations(phenotypes, AnnotationType.PHENOTYPE);
                }


            }
        }


        return searchResult;
    }



    private SearchResultV2 constructSearchResultV2(RankedArticle rankedArticle) {
        Article article = rankedArticle.getArticle();
        List<JSONObject> annotations = rankedArticle.getJsonAnnotations();
        // ^ This change is made to have one DTO throughout the hierarchy for simplicity of code.
        SearchResultV2 searchResult = new SearchResultV2();
        searchResult.setPmid(article.getPubMedID());
        searchResult.setArticleAbstract(article.getArticleAbstract().getOriginalText());
        searchResult.setDatePublished(article.getDatePublished().toString());
        searchResult.setArticleTitle(article.getArticleTitle());
        searchResult.setLanguage(article.getLanguage());
        searchResult.setAuthors(article.getAuthors());
        searchResult.setPublication(article.getPublication());

        if (annotations.size() > 0) {
            searchResult.setArticleRank(rankedArticle.getRank());
            searchResult.fillAnnotations(annotations);
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
        boolean smartSearch = false;
        AnnotationType smartQueryType = AnnotationType.ENTITY;
        List<RankedAutoCompleteEntity> returnList = new ArrayList<>();

        if (infix.contains(":")) {
            String originalInfix = infix;
            String[] splits = infix.split(":",2);
            if (splits.length > 1) {
                String suffix = splits[0].toLowerCase();
                infix = splits[1];
                smartSearch = true;
                switch (suffix) {
                    case "g":
                        smartQueryType = AnnotationType.GENE;
                        break;
                    case "hgnc":
                        smartQueryType = AnnotationType.GENE;
                        break;
                    case "d":
                        smartQueryType = AnnotationType.DISEASE;
                        break;
                    case "mondo":
                        smartQueryType = AnnotationType.DISEASE;
                        break;
                    case "p":
                        smartQueryType = AnnotationType.PHENOTYPE;
                        break;
                    case "hp":
                        smartQueryType = AnnotationType.PHENOTYPE;
                        break;
                    case "hpo":
                        smartQueryType = AnnotationType.PHENOTYPE;
                        break;
                    default:
                        infix = originalInfix;
                        smartSearch = false;
                }


            }
        }
        if (infix.length()<1)
        {
            smartQueryType = AnnotationType.ENTITY;
        }


        //Get and sort Genes
        List<APGene> shortlistedGenes = new ArrayList<>();
        List<APMultiWordAnnotationMapper> shortListedPhenotypes = new ArrayList<>();
        List<APMultiWordAnnotationMapper> shortListedDiseases = new ArrayList<>();

        if (!smartSearch || smartQueryType.equals(AnnotationType.GENE)) {
            shortlistedGenes =  AnnotationControl.getControlledGeneAnnotations(
                    DocumentPreprocessor.getHGNCGeneHandler().searchGenes(infix));
        }
        if (!smartSearch || smartQueryType.equals(AnnotationType.PHENOTYPE)) {

            shortListedPhenotypes = AnnotationControl.getControlledMWAnnotations(
                    DocumentPreprocessor.getPhenotypeHandler().searchPhenotype(infix));
        }

        if (!smartSearch || smartQueryType.equals(AnnotationType.DISEASE)) {
            shortListedDiseases = AnnotationControl.getControlledMWAnnotations(
                    DocumentPreprocessor.getMondoHandler().searchDisease(infix));
        }

        //Ranking results
        Map<Object, Integer> rankedEntities = new HashMap<>();
        shortListedPhenotypes.stream().forEach(x -> rankedEntities.put(x, 0));
        shortlistedGenes.stream().forEach(x -> rankedEntities.put(x, 0));
        shortListedDiseases.stream().forEach(x -> rankedEntities.put(x, 0));


        for (Map.Entry<Object, Integer> entry : rankedEntities.entrySet()) {
            int rank = baseRank;

            if (entry.getKey() instanceof APGene) {
                String symbol = ((APGene) entry.getKey()).getApprovedSymbol();
                if (symbol.indexOf(infix) == 0) {
                    rank = rank * 2 - (symbol.length() - infix.length());
                } else {
                    rank = rank - (5 * symbol.indexOf(infix)) - (symbol.length() - infix.length());
                }
                entry.setValue(rank);
            }
            if (entry.getKey() instanceof APMultiWordAnnotationMapper) {
                List<String> symbols = Arrays.asList(((APMultiWordAnnotationMapper) entry.getKey()).getText().toUpperCase().split(" "));

                List<String> infixes = Arrays.asList(infix.toUpperCase().split(" "));
                for(String anInfix: infixes) {
                    Integer termNumber = 0;
                    Integer localRank = 0;
                    for (String symbol : symbols) {
                        rank = baseRank;
                        if (symbol.indexOf(anInfix) == 0) {
                            rank = rank * 2 - (symbol.length() - anInfix.length()) - symbols.size();
                        } else {
                            rank = rank - (5 * symbol.indexOf(anInfix)) - (symbol.length() - anInfix.length() - symbols.size());
                        }
                        rank = rank - 10 * termNumber;
                        if (localRank < rank)
                            localRank = rank;
                        termNumber++;
                    }
                    entry.setValue(entry.getValue()+localRank);
                }
            }
        }


        //Show equal number of items from auto-complete list.
        Integer collectedGeneSize = Math.min(smartQueryType.equals(AnnotationType.GENE) ? resultLimit : resultLimit / 3, shortlistedGenes.size());
        Integer remainingBucket = resultLimit - collectedGeneSize;
        Integer collectedPhenotypeSize = Math.min(smartQueryType.equals(AnnotationType.PHENOTYPE) ? resultLimit : remainingBucket / 2, shortListedPhenotypes.size());
        remainingBucket = resultLimit - (collectedGeneSize + collectedPhenotypeSize);
        Integer collectedDiseaseSize = Math.min(smartQueryType.equals(AnnotationType.DISEASE) ? resultLimit : remainingBucket, shortListedDiseases.size());

        Map<Object, Integer> topRankedDiseases =
                rankedEntities.entrySet().stream().filter(x -> x.getKey() instanceof APMultiWordAnnotationMapper)
                        .filter(y -> ((APMultiWordAnnotationMapper) y.getKey()).getId().contains("MONDO:"))
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(collectedDiseaseSize)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Map<Object, Integer> topRankedPhenotypes =
                rankedEntities.entrySet().stream().filter(x -> x.getKey() instanceof APMultiWordAnnotationMapper)
                        .filter(y -> ((APMultiWordAnnotationMapper) y.getKey()).getId().contains("HP:"))
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(collectedPhenotypeSize)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Map<Object, Integer> topRankedGenes =
                rankedEntities.entrySet().stream().filter(x -> x.getKey() instanceof APGene)
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(collectedGeneSize)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


        //Fill Genes
        for (Map.Entry<Object, Integer> entry : topRankedGenes.entrySet()) {
            Object object = entry.getKey();
            RankedAutoCompleteEntity entity = new RankedAutoCompleteEntity();
            APGene gene = (APGene) object;
            entity.setId(String.valueOf(gene.getHGNCID()));
            entity.setText(gene.getApprovedSymbol());
            entity.setType(AnnotationType.GENE.toString());
            entity.setRank(entry.getValue());
            returnList.add(entity);
        }

        //Fill Phenotypes
        for (Map.Entry<Object, Integer> entry : topRankedPhenotypes.entrySet()) {
            Object object = entry.getKey();
            RankedAutoCompleteEntity entity = new RankedAutoCompleteEntity();
            APMultiWordAnnotationMapper phenotype = (APMultiWordAnnotationMapper) object;
            entity.setId(String.valueOf(phenotype.getId()));
            entity.setText(phenotype.getText());
            entity.setType(AnnotationType.PHENOTYPE.toString());
            entity.setRank(entry.getValue());
            returnList.add(entity);
        }

        //Fill Diseases
        for (Map.Entry<Object, Integer> entry : topRankedDiseases.entrySet()) {
            Object object = entry.getKey();
            RankedAutoCompleteEntity entity = new RankedAutoCompleteEntity();
            APMultiWordAnnotationMapper disease = (APMultiWordAnnotationMapper) object;
            entity.setId(String.valueOf(disease.getId()));
            entity.setText(disease.getText());
            entity.setType(AnnotationType.DISEASE.toString());
            entity.setRank(entry.getValue());
            returnList.add(entity);
        }


        returnList.sort(Comparator.comparing(RankedAutoCompleteEntity::getRank).reversed());

        return returnList;
    }




}
