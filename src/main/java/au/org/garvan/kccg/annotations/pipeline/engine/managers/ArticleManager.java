package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.lambda.WorkerLambdaConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APPhenotype;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease.APDisease;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Drug.APDrug;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.profiles.ProcessingProfile;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Common;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.EngineEnvironment;
import au.org.garvan.kccg.annotations.pipeline.model.annotation.OnDemandText;
import au.org.garvan.kccg.annotations.pipeline.model.annotation.RawArticle;
import au.org.garvan.kccg.annotations.pipeline.model.query.AnnotatedTextDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ahmed on 22/11/17.
 */

@Service
public class ArticleManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(ArticleManager.class);

    @Autowired
    DatabaseManager dbManager;


    public void init(){

        slf4jLogger.info(String.format("Article Manager init() called."));
        DocumentPreprocessor.init();


    }

    @Async
    public void processArticles(List<RawArticle> articleList, String batchId)
    {
        slf4jLogger.info(String.format("Received articles batch for processing. Batch Id:%s Batch Size: %d", batchId,  articleList.size()));
        for (RawArticle input: articleList){

            Article article = constructArticle(input);
            try {
                slf4jLogger.info(String.format("Processing started for article ID: %d", article.getPubMedID()));

//                if (true)
                if (!isDuplicate(article))
                {
                    slf4jLogger.info(String.format("Article is identified as unique. ID: %d", article.getPubMedID()));
                    article.getArticleAbstract().hatch(article.getPubMedID());

                    slf4jLogger.info(String.format("Article processed successfully, ID: %d", article.getPubMedID()));
                    dbManager.persistArticle(article);

                } else {
                    slf4jLogger.info(String.format("Article is identified as duplicate. Processing is aborted. ID: %d", article.getPubMedID()));
                }
            }
            catch (Exception e){
                slf4jLogger.error(String.format("Error in processing article with ID: %d. Exception:%s", article.getPubMedID(),e.toString()));
            }


        }//Article Loop
        slf4jLogger.info(String.format("Finished articles batch for processing. Batch Id:%s ", batchId));

        if(EngineEnvironment.getSelfIngestionEnabled()){
            slf4jLogger.info(String.format("Self Ingestion is Enabled. Invoking Lambda. "));
            WorkerLambdaConnector.invokeWorkerLambda(EngineEnvironment.getWorkerID(), false);
        }
    }

    private boolean isDuplicate(Article article){
       JSONObject jsonArticle =  dbManager.fetchArticle(Integer.toString(article.getPubMedID()));
       if (jsonArticle.isEmpty())
           return false;
       else
           return true;

    }

    private Article constructArticle(RawArticle rawArticle){
        Article article = new Article(rawArticle.getPubMedID(),
                LocalDate.now().toEpochDay(),
                LocalDate.parse(rawArticle.getDatePublished()),
                LocalDate.parse(rawArticle.getDateCreated()),
                LocalDate.parse(rawArticle.getDateRevised()),
                rawArticle.getArticleTitle(),
                new APDocument(rawArticle.getArticleAbstract()),
                rawArticle.getLanguage(),
                rawArticle.getAuthors(),
                rawArticle.getMeshHeadingList(),
                rawArticle.getPublication());

        article.getArticleAbstract().setProcessingProfile(Common.getStandardProfile());
        return article;
    }



    public AnnotatedTextDocument processOnDemandText(OnDemandText onDemandText){

        APDocument textToBeAnnotated = new APDocument(onDemandText.getText());
        textToBeAnnotated.setProcessingProfile(createPricessingProfile(onDemandText.getAnnotationProfile()));
        textToBeAnnotated.hatch(textToBeAnnotated.getId());
        JSONArray jsonAnnotations = getAbstractEntities(textToBeAnnotated);


        AnnotatedTextDocument annotatedTextDocument = new AnnotatedTextDocument();
        annotatedTextDocument.setDocumentId(UUID.randomUUID().toString());
        annotatedTextDocument.setDocumentText(textToBeAnnotated.getCleanedText());
        annotatedTextDocument.fillAnnotations(jsonAnnotations);
        return annotatedTextDocument;

    }

    private JSONArray getAbstractEntities(APDocument apDocument){
        JSONArray returnArray = new JSONArray();


        //Point: Genes are stored differently at token level; Hence treated separately
        //TODO: move it to a generic place
        Map<APSentence, List<APToken>> geneAnnotations = apDocument.getTokensWithEntities();
        if (geneAnnotations.size()>0)
        {
            JSONObject returnObject = new JSONObject();
            returnObject.put("annotationType", AnnotationType.GENE.toString());
            JSONArray genes = new JSONArray();

            for (Map.Entry<APSentence,  List<APToken>> entry : geneAnnotations.entrySet()) {
                int sentId = entry.getKey().getId();
                Point sentDocOffset= entry.getKey().getDocOffset();
                for (APToken token : entry.getValue()){
                    int tokenId = token.getId();
                    for (LexicalEntity lex : token.getLexicalEntityList()) {

                        if (lex instanceof APGene) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("field","articleAbstract");
                            jsonObject.put("standard","HGNC");
                            jsonObject.put("sentId",sentId);
                            jsonObject.put("tokenId",tokenId);
                            jsonObject.put("annotationId",((APGene) lex).getApprovedSymbol());
                            jsonObject.put("globalOffset", constructGlobalOffset(sentDocOffset,token.getSentOffset()));
                            genes.add(jsonObject);
                        } else {

                        }
                    }

                }
            }
            returnObject.put("annotations",genes);
            returnArray.add(returnObject);
        }

        //Point: Phenotype and Diseases can be handled here.

        Map<APSentence, List<Annotation>> genericAnnotations= new LinkedHashMap<>();
        for(APSentence sentence: apDocument.getSentences()){
            if(sentence.getAnnotations().size() >0) {
                genericAnnotations.put(sentence, sentence.getAnnotations());
            }
        }

        if(genericAnnotations.size()>0){
            JSONArray phenotypes = new JSONArray();
            JSONArray diseases = new JSONArray();
            JSONArray drugs = new JSONArray();
            for (Map.Entry<APSentence, List<Annotation>> entry : genericAnnotations.entrySet()) {
                int sentId = entry.getKey().getId();
                Point sentDocOffset = entry.getKey().getDocOffset();
                for (Annotation annotation : entry.getValue()) {
                    AnnotationType type = annotation.getType();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("field", "articleAbstract");
                    jsonObject.put("standard", annotation.getStandard());
                    jsonObject.put("sentId", sentId);
                    jsonObject.put("tokenIds", annotation.getTokenIDs());
                    jsonObject.put("globalOffset", constructGlobalOffset(sentDocOffset, annotation.getOffset()));
                    jsonObject.put("isNegated", annotation.getNegated());

                    if (type.equals(AnnotationType.PHENOTYPE)) {
                        jsonObject.put("annotationId", ((APPhenotype) (annotation.getEntity())).getHpoID());
                        phenotypes.add(jsonObject);

                    } else if (type.equals(AnnotationType.DISEASE)) {
                        jsonObject.put("annotationId", ((APDisease) (annotation.getEntity())).getMondoID());
                        diseases.add(jsonObject);
                    } else if (type.equals(AnnotationType.DRUG)) {
                        jsonObject.put("annotationId", ((APDrug) (annotation.getEntity())).getDrugBankID());
                        drugs.add(jsonObject);
                    }


                }
            }

            if(phenotypes.size()>0) {
                JSONObject phenotypeJsonObject = new JSONObject();
                phenotypeJsonObject.put("annotationType", AnnotationType.PHENOTYPE.toString());
                phenotypeJsonObject.put("annotations", phenotypes);
                returnArray.add(phenotypeJsonObject);

            }
            if(diseases.size()>0) {
                JSONObject diseaseJsonObject = new JSONObject();
                diseaseJsonObject.put("annotationType", AnnotationType.DISEASE.toString());
                diseaseJsonObject.put("annotations", diseases);
                returnArray.add(diseaseJsonObject);
            }
            if(drugs.size()>0) {
                JSONObject drugJsonObject = new JSONObject();
                drugJsonObject.put("annotationType", AnnotationType.DRUG.toString());
                drugJsonObject.put("annotations", drugs);
                returnArray.add(drugJsonObject);
            }

        }

        return returnArray;

    }

    private String constructGlobalOffset(Point sentOffset, Point tokenOffset){
        return String.format("%d:%d", sentOffset.x +tokenOffset.x, sentOffset.x + tokenOffset.y);
    }



    private ProcessingProfile createPricessingProfile(List<String> annotationProfile)
    {
        ProcessingProfile processingProfile = new ProcessingProfile();
        if (annotationProfile == null) {
            processingProfile.addAnnotation(AnnotationType.PHENOTYPE);
        }
        else{
            for(String input : annotationProfile)
            {
                switch (input) {
                    case "PHENOTYPE":
                        processingProfile.addAnnotation(AnnotationType.PHENOTYPE);
                        break;
                    case "DISEASE":
                        processingProfile.addAnnotation(AnnotationType.DISEASE);
                        break;
                    case "DRUG":
                        processingProfile.addAnnotation(AnnotationType.DRUG);
                        break;
                    case "GENE":
                        processingProfile.addAnnotation(AnnotationType.GENE);
                        break;

                }
            }
        }

        return processingProfile;

    }
}
