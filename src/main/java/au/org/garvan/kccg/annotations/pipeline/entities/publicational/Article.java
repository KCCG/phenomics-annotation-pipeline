package au.org.garvan.kccg.annotations.pipeline.entities.publicational;

import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APDocument;
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
    private String publicationType;

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


    public Article(int PMID, LocalDate articleDate, String articleTitle, String articleAbstract, String language, List<Author> authors) {
        this.pubMedID = PMID;
        this.datePublished = articleDate;
        this.articleTitle = articleTitle;
        this.articleAbstract = new APDocument(articleAbstract);
        this.language = language;
        this.authors = authors;

    }

    private LocalDate constructDate(JSONObject jsonDate) {
        String strDate = String.format("%s-%s-%s", jsonDate.get("Day"), jsonDate.get("Month"), jsonDate.get("Year"));
        LocalDate containerDate = LocalDate.parse(strDate, formatter);
        return containerDate;

    }




}
