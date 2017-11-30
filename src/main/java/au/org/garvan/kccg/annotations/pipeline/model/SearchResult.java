package au.org.garvan.kccg.annotations.pipeline.model;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
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
    int PMID;

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

    public void fillGenes (JSONArray jsonGenes)
    {
        annotations = new dtoAnnotations();
        List<dtoGene> geneList = new ArrayList<>();

        for (Object obj:jsonGenes)
        {
            JSONObject jsonObject = (JSONObject) obj;
            dtoGene tempGene;
            boolean geneExists = geneList.stream()
                    .map(dtoGene::getGeneSymbol)
                    .anyMatch(jsonObject.get("annotationId")::equals);
            if (geneExists){
                tempGene = geneList.stream().filter(g-> g.geneSymbol.equals(jsonObject.get("annotationId"))).collect(Collectors.toList()).get(0);
                tempGene.offsets.add(constructOffset(jsonObject.get("globalOffset").toString()));
            }
            else
            {
                tempGene = new dtoGene(jsonObject.get("annotationId").toString(),
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
        List<dtoGene> genes;

    }
    @NoArgsConstructor
    @AllArgsConstructor
    private class dtoGene {

        @JsonProperty
        @Getter
        String geneSymbol;
        @JsonProperty
        String field;
        @JsonProperty
        String standard;
        @JsonProperty
        List<Point> offsets;


    }

    private Point constructOffset(String globalOffset){
        String[] offsets =  globalOffset.split(":");
        return new Point(Integer.parseInt(offsets[0]), Integer.parseInt(offsets[1]));
    }


}
