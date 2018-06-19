package au.org.garvan.kccg.annotations.pipeline.model.query;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.Utilities;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APPhenotype;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import edu.stanford.nlp.util.Sets;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 29/11/17.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResultV2 {



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
    List<dtoAnnotation> annotations;

    @JsonProperty
    Publication publication;




    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class dtoAnnotation {

//
//        {
//            "annotations":
//    [
//            {
//                "startIndex": 11,
//                    "endIndex": 27,
//                    "highlights":
//            [
//                {
//                        "id": "999",
//                        "text": "cancer",
//                        "type": "DISEASE",
//                        "description": "this is the black disease.",
//                        "feedbackId": "xyz:999",
//                        "standard": "MONDO",
//                        "link": ""
//                },
//                {
//                    "id": "HP:101",
//                        "text": "cancer shape",
//                        "type": "PHENOTYPE",
//                        "description": "",
//                        "feedbackId": "xyz:101",
//                        "standard": "HPO",
//                        "link": ""
//                }
//            ]
//            }
//    ]
//        }

        @JsonProperty
        int startIndex;
        @JsonProperty
        int endIndex;
        @JsonProperty
        List<dtoHighlight> highlights;


    }
    @Data
    @AllArgsConstructor
    private class dtoHighlight{

        @JsonProperty
        String id;
        @JsonProperty
        String type;
        @JsonProperty
        String text;
        @JsonProperty
        String description;
        @JsonProperty
        String standard;
        @JsonProperty
        String link;
        @JsonProperty
        String feedbackId;

    }

    public void fillAnnotations(List<JSONObject> jsonAnnotationsObject)
    {
        Integer descriptionTruncateSize = 120;
        List<dtoAnnotation> convertedAnnotations = new ArrayList<>();
        for(JSONObject annotationGroup: jsonAnnotationsObject){

            String type = annotationGroup.get("annotationType").toString();
            JSONArray jsonAnnotations = (JSONArray) annotationGroup.get("annotations");

            switch (type){
                case "GENE":
                    for (Object obj:jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        dtoAnnotation tempAnnotation = new dtoAnnotation();

                        String text = jsonObject.get("annotationId").toString();
                        //In case of Gene we have Approved symbol stored as ID
                        APGene thisGene = DocumentPreprocessor.getHGNCGeneHandler().getGene(text);
                        String id = String.valueOf(thisGene.getHGNCID());
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description = Strings.isNullOrEmpty(thisGene.getApprovedName())?"N/A" : Utilities.getFirstHypotheticalSentence(thisGene.getApprovedName(), descriptionTruncateSize);
                        String link = String.format("https://monarchinitiative.org/gene/HGNC:%s",id);
                        String feedbackId = String.format("%s;%s;%s", pmid, id, offset);

                        Pair offsetPair = constructOffset(offset);
                        tempAnnotation.startIndex = (Integer)offsetPair.getFirst();
                        tempAnnotation.endIndex = (Integer)offsetPair.getSecond();

                        dtoHighlight highlight = new dtoHighlight(
                                id, type, text, description, standard, link, feedbackId
                        );
                        tempAnnotation.highlights = Arrays.asList( highlight);

                        convertedAnnotations.add(tempAnnotation);

                    }

                    break ;
                case "PHENOTYPE":
                    for (Object obj:jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        dtoAnnotation tempAnnotation = new dtoAnnotation();
                        String id = jsonObject.get("annotationId").toString();
                        String label =  DocumentPreprocessor.getPhenotypeHandler().getPhenotypeLabelWithId(id);
                        String text = label;
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description =  Utilities.getFirstHypotheticalSentence( DocumentPreprocessor.getPhenotypeHandler().getPhenotypeDefinitionWithId(id), descriptionTruncateSize);
                        String link = String.format("https://monarchinitiative.org/%s",id);
                        String feedbackId = String.format("%s;%s;%s", pmid, id, offset);
                        Pair offsetPair = constructOffset(offset);
                        tempAnnotation.startIndex = (Integer)offsetPair.getFirst();
                        tempAnnotation.endIndex = (Integer)offsetPair.getSecond();

                        dtoHighlight highlight = new dtoHighlight(
                                id, type, text, description, standard, link, feedbackId
                        );
                        tempAnnotation.highlights = Arrays.asList(highlight);
                        convertedAnnotations.add(tempAnnotation);


                    }

                    break;
                case "DISEASE":
                    for (Object obj:jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        dtoAnnotation tempAnnotation = new dtoAnnotation();
                        String id = jsonObject.get("annotationId").toString();
                        String label = DocumentPreprocessor.getMondoHandler().getMondoLabelWithId(id);
                        String text = label;
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description =Utilities.getFirstHypotheticalSentence(DocumentPreprocessor.getMondoHandler().getMondoDefinitionWithId(id),descriptionTruncateSize);
                        String link = String.format("https://monarchinitiative.org/%s",id);
                        String feedbackId = String.format("%s;%s;%s", pmid, id, offset);
                        Pair offsetPair = constructOffset(offset);
                        tempAnnotation.startIndex = (Integer)offsetPair.getFirst();
                        tempAnnotation.endIndex = (Integer)offsetPair.getSecond();

                        dtoHighlight highlight = new dtoHighlight(
                                id, type, text, description, standard, link, feedbackId
                        );
                        tempAnnotation.highlights = Arrays.asList( highlight);
                        convertedAnnotations.add(tempAnnotation);


                    }

                    break;

                case "DRUG":
                    for (Object obj:jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        dtoAnnotation tempAnnotation = new dtoAnnotation();
                        String id = jsonObject.get("annotationId").toString();
                        String label = DocumentPreprocessor.getDrugBankHandler().getDrugBankLabelWithId(id);
                        String text = label;
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description =Utilities.getFirstHypotheticalSentence(DocumentPreprocessor.getDrugBankHandler().getDrugBankDefinitionWithId(id),descriptionTruncateSize);
                        String link = DocumentPreprocessor.getDrugBankHandler().getDrugBankUrlWithId(id);
                        String feedbackId = String.format("%s;%s;%s", pmid, id, offset);
                        Pair offsetPair = constructOffset(offset);
                        tempAnnotation.startIndex = (Integer)offsetPair.getFirst();
                        tempAnnotation.endIndex = (Integer)offsetPair.getSecond();

                        dtoHighlight highlight = new dtoHighlight(
                                id, type, text, description, standard, link, feedbackId
                        );
                        tempAnnotation.highlights = Arrays.asList( highlight);
                        convertedAnnotations.add(tempAnnotation);
                    }

                    break;
            }
        }

        List<dtoAnnotation> overlappedAnnotations = resolveOverLapping(convertedAnnotations);
        annotations = overlappedAnnotations;

    }

    public List<dtoAnnotation> resolveOverLapping(List<dtoAnnotation> convertedAnnotations) {
        List<dtoAnnotation> finalAnnotations = new ArrayList<>();

        Integer platformSize = articleAbstract.length();
        ArrayList<String>[] platform = (ArrayList<String>[])new ArrayList[platformSize];
        for(Integer x= 0; x< platformSize; x++)
        {
            platform[x] = new ArrayList<>();
        }


        HashMap<String,dtoHighlight> processingMap = new HashMap<>();

        for(dtoAnnotation oneItem: convertedAnnotations){
            Integer stIndex = oneItem.startIndex;
            Integer enIndex = oneItem.endIndex;

            String id = oneItem.getHighlights().get(0).feedbackId;

            processingMap.put(id, oneItem.getHighlights().get(0));
            for(Integer y = stIndex; y<enIndex; y++){
                    platform[y].add(id);
            }
        }
        
        
        
        Integer previousCarClass = 0;
        Integer carStartIndex = -1;
        Integer carEndIndex = -1;
        for(Integer it=0; it<platformSize; it++)
        {
            Integer currentCarClass = platform[it].size();
            if(previousCarClass!=currentCarClass){
                if(carStartIndex>-1)
                {
                    carEndIndex = it;
                    dtoAnnotation completedCar = new dtoAnnotation();
                    completedCar.startIndex = carStartIndex;
                    completedCar.endIndex = carEndIndex;
                    completedCar.highlights = new ArrayList<>();
                    for(String classId: platform[it-1] ){
                        completedCar.highlights.add(processingMap.get(classId));
                    }
                    finalAnnotations.add(completedCar);

                }
                if (currentCarClass == 0) {
                    carStartIndex = -1;
                } else {
                    carStartIndex = it;
                }



            }//If class changed

            if(it==platformSize-1 && currentCarClass>0)
            {
                dtoAnnotation completedCar = new dtoAnnotation();
                completedCar.startIndex = carStartIndex;
                completedCar.endIndex = it+1;
                completedCar.highlights = new ArrayList<>();
                for(String classId: platform[it] ){
                    completedCar.highlights.add(processingMap.get(classId));
                }
                finalAnnotations.add(completedCar);

            }

            previousCarClass = currentCarClass;

            
        }


        return finalAnnotations;
    }


    private Pair<Integer, Integer> constructOffset(String globalOffset){
        String[] offsets =  globalOffset.split(":");
        return new Pair<Integer, Integer>(Integer.parseInt(offsets[0]), Integer.parseInt(offsets[1]));
    }






}
