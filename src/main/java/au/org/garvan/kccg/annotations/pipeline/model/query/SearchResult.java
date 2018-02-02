package au.org.garvan.kccg.annotations.pipeline.model.query;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 29/11/17.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {



    @JsonProperty
    int pmid;


    @JsonProperty
    int articleRank;

    @JsonProperty
    String articleTitle;

    @JsonProperty
    String datePublished;

    @JsonProperty
    String language;

    @JsonProperty
    String articleAbstract;

    @JsonProperty
    List<Author> authors;

    @JsonProperty
    dtoAnnotations annotations;

    @JsonProperty
    Publication publication;

    public void fillGenes (JSONArray jsonGenes)
    {
        annotations = new dtoAnnotations();
        List<dtoOutputGene> geneList = new ArrayList<>();

        for (Object obj:jsonGenes)
        {
            JSONObject jsonObject = (JSONObject) obj;
            dtoOutputGene tempGene;
            //Check if gene is already in the list then append offset, otherwise add it.
            boolean geneExists = geneList.stream()
                    .map(dtoOutputGene::getGeneSymbol)
                    .anyMatch(jsonObject.get("annotationId")::equals);
            if (geneExists){
                tempGene = geneList.stream().filter(g-> g.geneSymbol.equals(jsonObject.get("annotationId"))).collect(Collectors.toList()).get(0);
                tempGene.offsets.add(constructOffset(jsonObject.get("globalOffset").toString()));
            }
            else
            {
                tempGene = new dtoOutputGene(jsonObject.get("annotationId").toString(),
                        jsonObject.get("field").toString(),
                        jsonObject.get("standard").toString(),
                        new ArrayList<>(Arrays.asList(constructOffset(jsonObject.get("globalOffset").toString())))
                );
                geneList.add(tempGene);
            }

        }
        annotations.genes = geneList;

    }

    private class dtoAnnotations {
        @JsonProperty
        List<dtoOutputGene> genes;

    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class dtoOutputGene {

        @JsonProperty
        String geneSymbol;
        @JsonProperty
        String field;
        @JsonProperty
        String standard;
        @JsonProperty
        List<dtoOffset> offsets;


    }
    @Data
    @AllArgsConstructor
    private class dtoOffset{
        @JsonProperty
        int startIndex;
        @JsonProperty
        int endIndex;
    }

    private dtoOffset constructOffset(String globalOffset){
        String[] offsets =  globalOffset.split(":");
        return new dtoOffset(Integer.parseInt(offsets[0]), Integer.parseInt(offsets[1]));
    }


}
