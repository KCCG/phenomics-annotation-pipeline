package au.org.garvan.kccg.annotations.pipeline.model.query;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.Utilities;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ahmed on 29/11/17.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotatedTextDocument {


    @JsonProperty
    String documentId;

    @JsonProperty
    String documentText;

    @JsonProperty
    List<dtoAnnotationForTextDocuemnt> annotations;


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class dtoAnnotationForTextDocuemnt {

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
//                        "uniqueId": "xyz:999",
//                        "standard": "MONDO",
//                        "link": ""
//                },
//                {
//                    "id": "HP:101",
//                        "text": "cancer shape",
//                        "type": "PHENOTYPE",
//                        "description": "",
//                        "uniqueId": "xyz:101",
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
        List<dtoHighlightForTextDocument> highlights;


    }
    @Data
    @AllArgsConstructor
    private class dtoHighlightForTextDocument {

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
        @JsonIgnore
        String uniqueId;

    }

    public void fillAnnotations(List<JSONObject> jsonAnnotationsObject)
    {
        Integer descriptionTruncateSize = 120;
        List<dtoAnnotationForTextDocuemnt> convertedAnnotations = new ArrayList<>();
        for(JSONObject annotationGroup: jsonAnnotationsObject){

            String type = annotationGroup.get("annotationType").toString();
            JSONArray jsonAnnotations = (JSONArray) annotationGroup.get("annotations");

            switch (type){
                case "GENE":
                    for (Object obj:jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        dtoAnnotationForTextDocuemnt tempAnnotation = new dtoAnnotationForTextDocuemnt();

                        String text = jsonObject.get("annotationId").toString();
                        //In case of Gene we have Approved symbol stored as ID
                        APGene thisGene = DocumentPreprocessor.getHGNCGeneHandler().getGene(text);
                        String id = String.valueOf(thisGene.getHGNCID());
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description = Strings.isNullOrEmpty(thisGene.getApprovedName())?"N/A" : Utilities.getFirstHypotheticalSentence(thisGene.getApprovedName(), descriptionTruncateSize);
                        String link = String.format("https://monarchinitiative.org/gene/HGNC:%s",id);
                        String uniqueId = String.format("%s;%s;%s", documentId, id, offset);

                        Pair offsetPair = constructOffset(offset);
                        tempAnnotation.startIndex = (Integer)offsetPair.getFirst();
                        tempAnnotation.endIndex = (Integer)offsetPair.getSecond();

                        dtoHighlightForTextDocument highlight = new dtoHighlightForTextDocument(
                                id, type, text, description, standard, link, uniqueId
                        );
                        tempAnnotation.highlights = Arrays.asList( highlight);

                        convertedAnnotations.add(tempAnnotation);

                    }

                    break ;
                case "PHENOTYPE":
                    for (Object obj:jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        dtoAnnotationForTextDocuemnt tempAnnotation = new dtoAnnotationForTextDocuemnt();
                        String id = jsonObject.get("annotationId").toString();
                        String label =  DocumentPreprocessor.getPhenotypeHandler().getPhenotypeLabelWithId(id);
                        String text = label;
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description =  Utilities.getFirstHypotheticalSentence( DocumentPreprocessor.getPhenotypeHandler().getPhenotypeDefinitionWithId(id), descriptionTruncateSize);
                        String link = String.format("https://monarchinitiative.org/%s",id);
                        String uniqueId = String.format("%s;%s;%s", documentId, id, offset);
                        Pair offsetPair = constructOffset(offset);
                        tempAnnotation.startIndex = (Integer)offsetPair.getFirst();
                        tempAnnotation.endIndex = (Integer)offsetPair.getSecond();

                        dtoHighlightForTextDocument highlight = new dtoHighlightForTextDocument(
                                id, type, text, description, standard, link, uniqueId
                        );
                        tempAnnotation.highlights = Arrays.asList(highlight);
                        convertedAnnotations.add(tempAnnotation);


                    }

                    break;
                case "DISEASE":
                    for (Object obj:jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        dtoAnnotationForTextDocuemnt tempAnnotation = new dtoAnnotationForTextDocuemnt();
                        String id = jsonObject.get("annotationId").toString();
                        String label = DocumentPreprocessor.getMondoHandler().getMondoLabelWithId(id);
                        String text = label;
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description =Utilities.getFirstHypotheticalSentence(DocumentPreprocessor.getMondoHandler().getMondoDefinitionWithId(id),descriptionTruncateSize);
                        String link = String.format("https://monarchinitiative.org/%s",id);
                        String uniqueId = String.format("%s;%s;%s", documentId, id, offset);
                        Pair offsetPair = constructOffset(offset);
                        tempAnnotation.startIndex = (Integer)offsetPair.getFirst();
                        tempAnnotation.endIndex = (Integer)offsetPair.getSecond();

                        dtoHighlightForTextDocument highlight = new dtoHighlightForTextDocument(
                                id, type, text, description, standard, link, uniqueId
                        );
                        tempAnnotation.highlights = Arrays.asList( highlight);
                        convertedAnnotations.add(tempAnnotation);


                    }

                    break;

                case "DRUG":
                    for (Object obj:jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        dtoAnnotationForTextDocuemnt tempAnnotation = new dtoAnnotationForTextDocuemnt();
                        String id = jsonObject.get("annotationId").toString();
                        String label = DocumentPreprocessor.getDrugBankHandler().getDrugBankLabelWithId(id);
                        String text = label;
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description =Utilities.getFirstHypotheticalSentence(DocumentPreprocessor.getDrugBankHandler().getDrugBankDefinitionWithId(id),descriptionTruncateSize);
                        String link = DocumentPreprocessor.getDrugBankHandler().getDrugBankUrlWithId(id);
                        String uniqueId = String.format("%s;%s;%s", documentId, id, offset);
                        Pair offsetPair = constructOffset(offset);
                        tempAnnotation.startIndex = (Integer)offsetPair.getFirst();
                        tempAnnotation.endIndex = (Integer)offsetPair.getSecond();

                        dtoHighlightForTextDocument highlight = new dtoHighlightForTextDocument(
                                id, type, text, description, standard, link, uniqueId
                        );
                        tempAnnotation.highlights = Arrays.asList( highlight);
                        convertedAnnotations.add(tempAnnotation);
                    }

                    break;
            }
        }

        List<dtoAnnotationForTextDocuemnt> overlappedAnnotations = resolveOverLapping(convertedAnnotations);
        annotations = overlappedAnnotations;

    }

    public List<dtoAnnotationForTextDocuemnt> resolveOverLapping(List<dtoAnnotationForTextDocuemnt> convertedAnnotations) {
        List<dtoAnnotationForTextDocuemnt> finalAnnotations = new ArrayList<>();

        Integer platformSize = documentText.length();
        ArrayList<String>[] platform = (ArrayList<String>[])new ArrayList[platformSize];
        for(Integer x= 0; x< platformSize; x++)
        {
            platform[x] = new ArrayList<>();
        }


        HashMap<String,dtoHighlightForTextDocument> processingMap = new HashMap<>();

        for(dtoAnnotationForTextDocuemnt oneItem: convertedAnnotations){
            Integer stIndex = oneItem.startIndex;
            Integer enIndex = oneItem.endIndex;

            String id = oneItem.getHighlights().get(0).uniqueId;

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
                    dtoAnnotationForTextDocuemnt completedCar = new dtoAnnotationForTextDocuemnt();
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
                dtoAnnotationForTextDocuemnt completedCar = new dtoAnnotationForTextDocuemnt();
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
