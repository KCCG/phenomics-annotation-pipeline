package au.org.garvan.kccg.annotations.pipeline.model.query;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Author;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Publication;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
                        String id = String.valueOf(DocumentPreprocessor.getHGNCGeneHandler().getGene(text).getHGNCID());
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description = "N/A";
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
                        String label = DocumentPreprocessor.getPhenotypeHandler().getPhenotypeLabelWithId(id);
                        String text = label;
                        String offset = jsonObject.get("globalOffset").toString();
                        String standard = jsonObject.get("standard").toString();
                        String description = "N/A";
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
                        String description = DocumentPreprocessor.getMondoHandler().getMondoDefinitionWithId(id);
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

        private List<dtoAnnotation> resolveOverLappingTry(List<dtoAnnotation> convertedAnnotations) {
        List<dtoAnnotation> finalAnnotations = new ArrayList<>();


        List<dtoAnnotation> isolatedAnnotations = new ArrayList<>();
        //TODO: Reconstruct annotation
        HashMap<String,dtoAnnotation> processingMap = new HashMap<>();
        for(dtoAnnotation oid: convertedAnnotations){
            processingMap.put(oid.getHighlights().get(0).feedbackId, oid);
        }

        List<String> keys = processingMap.keySet().stream().collect(Collectors.toList());
        List<Set<String>> overlappedAnnotationIds = new ArrayList<>();
        for(Integer pivot = 0; pivot<keys.size()-1; pivot ++){
            boolean isPivotIsolated = true;
            for(Integer compared = pivot+1; compared<keys.size(); compared++){

                dtoAnnotation pivotAnnotation = processingMap.get(keys.get(pivot));
                dtoAnnotation comparedAnnotation = processingMap.get(keys.get(compared));
                Integer coveredSpan = Math.max(pivotAnnotation.endIndex, comparedAnnotation.endIndex) - Math.min(pivotAnnotation.startIndex, comparedAnnotation.startIndex);
                Integer actualLength = (pivotAnnotation.endIndex - pivotAnnotation.startIndex) + (comparedAnnotation.endIndex-comparedAnnotation.startIndex);
                if(actualLength>=coveredSpan){
                 isPivotIsolated = false;
                 HashSet<String> tempGroup = new HashSet();
                 tempGroup.add(keys.get(pivot));
                 tempGroup.add(keys.get(compared));
                 overlappedAnnotationIds.add(tempGroup);
                }
            }
            if(isPivotIsolated)
            {
                isolatedAnnotations.add(processingMap.get(keys.get(pivot)));
            }


        }

        //Point: Now merge overlapped annotations.
        boolean listChanged = false;

        do {
            Integer match1 = -1;
            Integer match2 = -1;

            for (Integer x = 0; !listChanged && x < overlappedAnnotationIds.size() - 1; x++) {
                for (Integer y = x + 1; listChanged && y < overlappedAnnotationIds.size(); y++) {
                    if (Sets.intersection(overlappedAnnotationIds.get(x), overlappedAnnotationIds.get(y)).size() > 0) {
                        listChanged = true;
                        match1 = x;
                        match2 = y;
                    }
                }
            }
            if (listChanged) {
                Set<String> matchSet1 = overlappedAnnotationIds.get(match1);
                Set<String> matchSet2 = overlappedAnnotationIds.get(match2);
                overlappedAnnotationIds.remove(match1);
                overlappedAnnotationIds.remove(match2);
                overlappedAnnotationIds.add(Sets.intersection(matchSet1, matchSet2));
            }
        }
        while (listChanged);


        List<dtoAnnotation> overLappedAnnotations = new ArrayList<>();
        for(Set<String> anOverlap: overlappedAnnotationIds)
        {
            List<dtoAnnotation> oneSetofAnnotations= new ArrayList<>();
            for(String id: anOverlap){
                oneSetofAnnotations.add(processingMap.get(id));
            }



        }








        return finalAnnotations;



    }

    private Pair<Integer, Integer> constructOffset(String globalOffset){
        String[] offsets =  globalOffset.split(":");
        return new Pair<Integer, Integer>(Integer.parseInt(offsets[0]), Integer.parseInt(offsets[1]));
    }






}
