package au.org.garvan.kccg.annotations.pipeline.model.annotation;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 27/11/17.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawArticle {


    @JsonProperty("PMID")
    private int pubMedID;

    @JsonProperty(value = "articleDate", required = true)
    private final String datePublished;

    @JsonProperty(required = true)
    private final String dateCreated;

    @JsonProperty(required = true)
    private final String dateRevised;


    @JsonProperty(required = true)
    private final String articleTitle;

    @JsonProperty(required = true)
    private final String articleAbstract;


    @JsonProperty(required = true)
    private final String language;


    @JsonProperty(required = false)
    private List<Author> authors;

    @JsonProperty(required = true)
    private Publication publication;



    public List<Author> getAuthors(){
        if(this.authors ==null)
            return new ArrayList<>();
        else
            return authors;

    }

}
