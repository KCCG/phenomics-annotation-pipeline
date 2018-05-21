//package au.org.garvan.kccg.annotations.pipeline.model.query;
//
//import au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers.DynamoDBHandler;
//import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
//import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
//import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
//import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
//import org.json.simple.JSONObject;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//public class SearchResultV2Test {
//
////    articletablename: test-phenomics-articles
////    annotationtablename: test-phenomics-annotations
////    subscriptiontablename: test-phenomics-subscriptions
////    l2cachetablename: phenomics-l2-cache
//
//    DynamoDBHandler dynamoDBHandler = new DynamoDBHandler("test-phenomics-articles", "test-phenomics-annotations", "","");
//
//    @Test
//    public void resolveOverLapping() {
//
//        Integer PMID = 29767458;
//        SearchResultV2 testResult = prepareResult(PMID);
//        testResult.fillAnnotations(getAnnotations(PMID));
//
//    }
//
//
//    private SearchResultV2 prepareResult (int PMID){
//        SearchResultV2 resultV2 = new SearchResultV2();
//
//        JSONObject jsonArticle = dynamoDBHandler.getArticle(PMID);
//        Article article =  new Article(new DynamoDBObject(jsonArticle, EntityType.Article), false);
//        SearchResultV2 searchResult = new SearchResultV2();
//        searchResult.setPmid(article.getPubMedID());
//        searchResult.setArticleAbstract(article.getArticleAbstract().getOriginalText());
//        searchResult.setDatePublished(article.getDatePublished().toString());
//        searchResult.setArticleTitle(article.getArticleTitle());
//        searchResult.setLanguage(article.getLanguage());
//        searchResult.setAuthors(article.getAuthors());
//        searchResult.setPublication(article.getPublication());
//        return  resultV2;
//    }
//    private List<JSONObject> getAnnotations(int PMID){
//        List<JSONObject> jsonAnnotations = new ArrayList<>();
//
//        JSONObject genes =  dynamoDBHandler.getAnnotations(PMID, AnnotationType.GENE);
//        JSONObject phenotypes =  dynamoDBHandler.getAnnotations(PMID, AnnotationType.PHENOTYPE);
//        JSONObject diseases =  dynamoDBHandler.getAnnotations(PMID, AnnotationType.DISEASE);
//
//        if(genes.containsKey("annotationType"))
//            jsonAnnotations.add(genes);
//        if(phenotypes.containsKey("annotationType"))
//            jsonAnnotations.add(phenotypes);
//        if(diseases.containsKey("annotationType"))
//            jsonAnnotations.add(diseases);
//
//        return jsonAnnotations;
//    }
//}