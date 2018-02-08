package au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APPhenotype;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.LexicalEntity;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 30/10/17.
 */
@AllArgsConstructor
public class Article {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    @Getter
    @Setter
    private int pubMedID;

    @Getter
    @Setter
    private long processingDate;

    @Getter
    @Setter
    private LocalDate datePublished;

    @Getter
    @Setter
    private LocalDate dateCreated;

    @Getter
    @Setter
    private LocalDate dateRevised;

    @Getter
    @Setter
    private String articleTitle;

    @Getter
    @Setter
    private APDocument articleAbstract;

    @Getter
    @Setter
    private String language;

    @Getter
    @Setter
    private List<Author> authors;

    @Getter
    @Setter
    private Publication publication;


    public Article(JSONObject inputObject) {

        pubMedID = Integer.parseInt(inputObject.get("PMID").toString());
        language = inputObject.get("language").toString();
        articleTitle = inputObject.get("articleTitle").toString();
        articleAbstract = new APDocument(inputObject.get("articleAbstract").toString());
        publication = inputObject.containsKey("publication") ? new Publication((JSONObject) inputObject.get("publication")) : null;


        authors = new ArrayList<>();
        if (inputObject.containsKey("authors")) {
            JSONArray jsonArrayAuthor = (JSONArray) inputObject.get("authors");

            for (Object jsonAuthor : jsonArrayAuthor) {
                authors.add(new Author((JSONObject) jsonAuthor));
            }
        }
        dateCreated = LocalDate.parse(inputObject.get("dateCreated").toString());
        datePublished = LocalDate.parse(inputObject.get("articleDate").toString());
        dateRevised = LocalDate.parse(inputObject.get("dateRevised").toString());

    }

    public Article(DynamoDBObject dbObject, boolean loadLinguisticStructure){
        if(dbObject.getEntityType().equals(EntityType.Article))
        {

            pubMedID = Integer.parseInt(dbObject.getJsonObject().get("pubMedID").toString());

            datePublished = LocalDate.parse(dbObject.getJsonObject().get("datePublished").toString());
            dateCreated = LocalDate.parse(dbObject.getJsonObject().get("dateCreated").toString());
            dateRevised = LocalDate.parse(dbObject.getJsonObject().get("dateRevised").toString());
            articleTitle = dbObject.getJsonObject().get("articleTitle").toString();
            language = dbObject.getJsonObject().get("language").toString();


            if(dbObject.getJsonObject().containsKey("processingDate")) {
                processingDate = Long.parseLong(dbObject.getJsonObject().get("processingDate").toString());
            }
            else
                processingDate = dateCreated.toEpochDay();

            if(dbObject.getJsonObject().containsKey("authors"))
            {
                authors = new ArrayList<>();
                JSONArray jsonAuthors = (JSONArray) dbObject.getJsonObject().get("authors");
                jsonAuthors.forEach(a->
                        {
                            DynamoDBObject tempObject = new DynamoDBObject((JSONObject)a,EntityType.Author);
                            Author temp = new Author(tempObject);
                            authors.add(temp);
                        }
                );

            }

            publication = new Publication(new DynamoDBObject((JSONObject) dbObject.getJsonObject().get("publication"), EntityType.Publication));

            if(dbObject.getJsonObject().containsKey("articleAbstract"))

            {
                if (loadLinguisticStructure) {

                    articleAbstract = new APDocument(new DynamoDBObject((JSONObject) dbObject.getJsonObject().get("articleAbstract"), EntityType.APDocument));
                }
                else
                {
                    articleAbstract = new APDocument(dbObject.getJsonObject().get("articleAbstract").toString());
                }
            }
            else
                articleAbstract = new APDocument("");

        }
        else{

        }



    }


    public Article(DynamoDBObject dbObject, JSONObject annotations , boolean loadLinguisticStructure) {
        this(dbObject, loadLinguisticStructure);
        if(annotations.containsKey("pubMedID") && ((annotations.get("pubMedID")).toString().equals(Integer.toString(pubMedID)))){
            JSONArray lstAnnotations = new JSONArray();
            AnnotationType annotationType = AnnotationType.valueOf(annotations.get("annotationType").toString());
            switch (annotationType){
                case GENE:
                    for(Object obj: lstAnnotations){
                        JSONObject annotation = (JSONObject) obj;
                        int sentId = (int) annotation.get("sentId");
                        int tokenId = (int) annotation.get("tokenId");
                        String geneSymbol = annotation.get("annotationId").toString();
                        APGene gene = DocumentPreprocessor.getHGNCGeneHandler().getGene(geneSymbol);
                        articleAbstract.getSentences().stream().filter(s->s.getId()==sentId).collect(Collectors.toList()).get(0)
                                .getTokens().stream().filter(t->t.getId()==tokenId).collect(Collectors.toList()).get(0)
                                .getLexicalEntityList().add(gene);

                    }

                    break;

            }



        }

    }


    public JSONObject constructJson(){
        JSONObject returnObject = new JSONObject();
        returnObject.put("pubMedID", Integer.toString(pubMedID));
        returnObject.put("processingDate", processingDate);
        returnObject.put("datePublished", datePublished.toString());
        returnObject.put("dateCreated", dateCreated.toString());
        returnObject.put("dateRevised", dateRevised.toString());
        returnObject.put("articleTitle", articleTitle);
        if(!articleAbstract.getOriginalText().isEmpty())
            returnObject.put("articleAbstract", articleAbstract.getCleanedText());
        returnObject.put("language", language);
        returnObject.put("publication", publication.constructJson());

        JSONArray jsonAuthors = new JSONArray();
        for (Author a : authors) {
            if(a.checkValidName())
                jsonAuthors.add(a.constructJson());
        }
        if(jsonAuthors.size()>0)
            returnObject.put("authors",jsonAuthors);


        return returnObject;

    }

    /***
     * This method is written for DynamodDB persistence only
     * 1: Genes
     * 2: Phenotypes
     * Are Annotations to be stored in dynamodb for time being.
     * @return
     */

    public JSONArray getAbstractEntities(){
        JSONArray returnArray = new JSONArray();

        Map<APSentence, List<APToken>> geneAnnotations = articleAbstract.getTokensWithEntities();
        if (geneAnnotations.size()>0)
        {
            JSONObject returnObject = new JSONObject();
            returnObject.put("pubMedID", Integer.toString(pubMedID));
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

        Map<APSentence, List<Annotation>> phenotypeAnnotations= new LinkedHashMap<>();
        for(APSentence sentence: articleAbstract.getSentences()){
            if(sentence.getAnnotations().stream().filter(f->f.getType().equals(AnnotationType.PHENOTYPE)).count()>0) {
                phenotypeAnnotations.put(sentence, sentence.getAnnotations());
            }
        }

        if(phenotypeAnnotations.size()>0){

            JSONObject returnObject = new JSONObject();
            returnObject.put("pubMedID", Integer.toString(pubMedID));
            returnObject.put("annotationType", AnnotationType.PHENOTYPE.toString());
            JSONArray phenotypes = new JSONArray();
            for (Map.Entry<APSentence,  List<Annotation>> entry : phenotypeAnnotations.entrySet()) {
                int sentId = entry.getKey().getId();
                Point sentDocOffset= entry.getKey().getDocOffset();
                for (Annotation annotation : entry.getValue()){

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("field","articleAbstract");
                            jsonObject.put("standard", annotation.getStandard());
                            jsonObject.put("sentId",sentId);
                            jsonObject.put("tokenIds", annotation.getTokenIDs());
                            jsonObject.put("annotationId", ((APPhenotype) (annotation.getEntity())).getHpoID());
                            jsonObject.put("globalOffset", constructGlobalOffset(sentDocOffset,annotation.getOffet()));
                            jsonObject.put("isNegated", annotation.getNegated());
                            phenotypes.add(jsonObject);

                    }
            }
            returnObject.put("annotations",phenotypes);
            returnArray.add(returnObject);
        }
        return returnArray;

    }

    private String constructGlobalOffset(Point sentOffset, Point tokenOffset){
        return String.format("%d:%d", sentOffset.x +tokenOffset.x, sentOffset.x + tokenOffset.y);
    }











}
