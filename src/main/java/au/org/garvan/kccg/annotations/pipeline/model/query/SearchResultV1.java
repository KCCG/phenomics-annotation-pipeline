package au.org.garvan.kccg.annotations.pipeline.model.query;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
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
public class SearchResultV1 {



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
    List<OutputItemDto> annotations;

    @JsonProperty
    Publication publication;

    public void fillAnnotations(JSONArray jsonGenes, AnnotationType type)
    {
        List<OutputItemDto> annotationList = new ArrayList<>();
        for (Object obj:jsonGenes)
        {
            JSONObject jsonObject = (JSONObject) obj;
            OutputItemDto tempAnnotation;
            //Check if gene is already in the list then append offset, otherwise add it.
            boolean annotationExists = annotationList.stream()
                    .map(OutputItemDto::getText)
                    .anyMatch(jsonObject.get("annotationId")::equals);
            if (annotationExists){
                tempAnnotation = annotationList.stream().filter(g-> g.text.equals(jsonObject.get("annotationId"))).collect(Collectors.toList()).get(0);
                tempAnnotation.offsets.add(constructOffset(jsonObject.get("globalOffset").toString()));
            }
            else
            {
                String symbol = jsonObject.get("annotationId").toString();
                String id = String.valueOf(DocumentPreprocessor.getHGNCGeneHandler().getGene(symbol).getHGNCID());

                tempAnnotation = new OutputItemDto(id,
                        type.toString(),
                        symbol,
                        jsonObject.get("field").toString(),
                        jsonObject.get("standard").toString(),
                        new ArrayList<>(Arrays.asList(constructOffset(jsonObject.get("globalOffset").toString()))),
                        new JSONObject()
                );
                annotationList.add(tempAnnotation);
            }

        }
        if(annotations==null)
            annotations = new ArrayList<>();
        annotations.addAll(annotationList);

    }


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class OutputItemDto {


        @JsonProperty
        String id;
        @JsonProperty
        String type;
        @JsonProperty
        String text;
        @JsonProperty
        String field;
        @JsonProperty
        String standard;
        @JsonProperty
        List<dtoOffset> offsets;
        @JsonProperty
        JSONObject metaData;


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
