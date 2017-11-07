package au.org.garvan.kccg.annotations.pipeline.entities.publicational;

import au.org.garvan.kccg.annotations.pipeline.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 30/10/17.
 */
public class Article {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    @Getter
    @Setter
    private int pubMedID;

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

    public Article(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.Article))
        {

        }
        else{

        }

    }


    private LocalDate constructDate(JSONObject jsonDate) {
        String strDate = String.format("%s-%s-%s", jsonDate.get("Day"), jsonDate.get("Month"), jsonDate.get("Year"));
        LocalDate containerDate = LocalDate.parse(strDate, formatter);
        return containerDate;

    }

    public JSONObject constructJson(){
        JSONObject returnObject = new JSONObject();
        returnObject.put("pubMedID", pubMedID);
        returnObject.put("datePublished", datePublished);
        returnObject.put("dateCreated", dateCreated);
        returnObject.put("dateRevised", dateRevised);
        returnObject.put("articleTitle", articleTitle);
        returnObject.put("articleAbstract", articleAbstract.constructJson());
        returnObject.put("language", language);
        returnObject.put("publication", publication.constructJson());
        JSONArray jsonAuthors = new JSONArray();
        authors.forEach(a-> jsonAuthors.add(a.constructJson()));
        returnObject.put("authors",jsonAuthors);


        return returnObject;


    }




}
