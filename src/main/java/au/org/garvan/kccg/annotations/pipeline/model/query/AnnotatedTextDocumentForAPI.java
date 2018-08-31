package au.org.garvan.kccg.annotations.pipeline.model.query;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.Utilities;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
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
import java.util.HashMap;
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
public class AnnotatedTextDocumentForAPI {

    @JsonProperty
    String documentId;

    @JsonProperty
    String documentText;

    @JsonProperty
    List<dtoAnnotationForAnnotatedTextDocumentForAPI> annotations;

    public void fillAnnotations(List<JSONObject> jsonAnnotationsObject) {
        Integer descriptionTruncateSize = 120;

        HashMap<String, dtoAnnotationForAnnotatedTextDocumentForAPI> convertedAnnotations = new HashMap<>();
        for (JSONObject annotationGroup : jsonAnnotationsObject) {

            String type = annotationGroup.get("annotationType").toString();
            JSONArray jsonAnnotations = (JSONArray) annotationGroup.get("annotations");

            switch (type) {
                case "GENE":
                    for (Object obj : jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;

                        String text = jsonObject.get("annotationId").toString();
                        //In case of Gene we have Approved symbol stored as ID
                        APGene thisGene = DocumentPreprocessor.getHGNCGeneHandler().getGene(text);
                        String id = String.format("HGNC:%s", String.valueOf(thisGene.getHGNCID()));
                        String offset = jsonObject.get("globalOffset").toString();
                        Pair offsetPair = constructOffset(offset);
                        dtoAnnotationForAnnotatedTextDocumentForAPI tempAnnotation = new dtoAnnotationForAnnotatedTextDocumentForAPI();

                        if (convertedAnnotations.containsKey(id)) {
                            tempAnnotation = convertedAnnotations.get(id);
                            tempAnnotation.add_offset((Integer) offsetPair.getFirst(), (Integer) offsetPair.getSecond());
                        } else {

                            String standard = jsonObject.get("standard").toString();
                            String version = jsonObject.get("version").toString();
                            String description = Strings.isNullOrEmpty(thisGene.getApprovedName()) ? "N/A" : Utilities.getFirstHypotheticalSentence(thisGene.getApprovedName(), descriptionTruncateSize);
                            String link = String.format("https://monarchinitiative.org/gene/%s", id);
                            tempAnnotation = new dtoAnnotationForAnnotatedTextDocumentForAPI(id, type, text, description, standard, version, link, new ArrayList<>(), new JSONObject());
                            tempAnnotation.add_offset((Integer) offsetPair.getFirst(), (Integer) offsetPair.getSecond());
                            convertedAnnotations.put(id, tempAnnotation);
                        }

                    }

                    break;
                case "PHENOTYPE":
                    for (Object obj : jsonAnnotations) {


                        JSONObject jsonObject = (JSONObject) obj;
                        String id = jsonObject.get("annotationId").toString();
                        String offset = jsonObject.get("globalOffset").toString();
                        Pair offsetPair = constructOffset(offset);

                        dtoAnnotationForAnnotatedTextDocumentForAPI tempAnnotation = new dtoAnnotationForAnnotatedTextDocumentForAPI();
                        if (convertedAnnotations.containsKey(id)) {
                            tempAnnotation = convertedAnnotations.get(id);
                            tempAnnotation.add_offset((Integer) offsetPair.getFirst(), (Integer) offsetPair.getSecond());
                        } else {
                            String label = DocumentPreprocessor.getPhenotypeHandler().getPhenotypeLabelWithId(id);
                            String text = label;
                            String standard = jsonObject.get("standard").toString();
                            String version = jsonObject.get("version").toString();
                            String description = Utilities.getFirstHypotheticalSentence(DocumentPreprocessor.getPhenotypeHandler().getPhenotypeDefinitionWithId(id), descriptionTruncateSize);
                            String link = String.format("https://monarchinitiative.org/%s", id);
                            tempAnnotation = new dtoAnnotationForAnnotatedTextDocumentForAPI(id, type, text, description, standard, version, link, new ArrayList<>(), new JSONObject());
                            tempAnnotation.add_offset((Integer) offsetPair.getFirst(), (Integer) offsetPair.getSecond());
                            convertedAnnotations.put(id, tempAnnotation);
                        }


                    }

                    break;
                case "DISEASE":
                    for (Object obj : jsonAnnotations) {
                        JSONObject jsonObject = (JSONObject) obj;
                        String id = jsonObject.get("annotationId").toString();
                        String offset = jsonObject.get("globalOffset").toString();
                        Pair offsetPair = constructOffset(offset);

                        dtoAnnotationForAnnotatedTextDocumentForAPI tempAnnotation = new dtoAnnotationForAnnotatedTextDocumentForAPI();
                        if (convertedAnnotations.containsKey(id)) {
                            tempAnnotation = convertedAnnotations.get(id);
                            tempAnnotation.add_offset((Integer) offsetPair.getFirst(), (Integer) offsetPair.getSecond());
                        } else {
                            String label = DocumentPreprocessor.getMondoHandler().getMondoLabelWithId(id);
                            String text = label;
                            String standard = jsonObject.get("standard").toString();
                            String version = jsonObject.get("version").toString();
                            String description = Utilities.getFirstHypotheticalSentence(DocumentPreprocessor.getMondoHandler().getMondoDefinitionWithId(id), descriptionTruncateSize);
                            String link = String.format("https://monarchinitiative.org/%s", id);
                            tempAnnotation = new dtoAnnotationForAnnotatedTextDocumentForAPI(id, type, text, description, standard, version, link, new ArrayList<>(), new JSONObject());
                            tempAnnotation.add_offset((Integer) offsetPair.getFirst(), (Integer) offsetPair.getSecond());
                            convertedAnnotations.put(id, tempAnnotation);
                        }

                    }

                    break;

                case "DRUG":
                    for (Object obj : jsonAnnotations) {


                        JSONObject jsonObject = (JSONObject) obj;
                        String id = jsonObject.get("annotationId").toString();
                        String offset = jsonObject.get("globalOffset").toString();
                        Pair offsetPair = constructOffset(offset);

                        dtoAnnotationForAnnotatedTextDocumentForAPI tempAnnotation = new dtoAnnotationForAnnotatedTextDocumentForAPI();
                        if (convertedAnnotations.containsKey(id)) {
                            tempAnnotation = convertedAnnotations.get(id);
                            tempAnnotation.add_offset((Integer) offsetPair.getFirst(), (Integer) offsetPair.getSecond());
                        } else {
                            String label = DocumentPreprocessor.getDrugBankHandler().getDrugBankLabelWithId(id);
                            String text = label;
                            String standard = jsonObject.get("standard").toString();
                            String version = jsonObject.get("version").toString();

                            String description = Utilities.getFirstHypotheticalSentence(DocumentPreprocessor.getDrugBankHandler().getDrugBankDefinitionWithId(id), descriptionTruncateSize);
                            String link = String.format("https://monarchinitiative.org/%s", id);
                            tempAnnotation = new dtoAnnotationForAnnotatedTextDocumentForAPI(id, type, text, description, standard, version, link, new ArrayList<>(), new JSONObject());
                            tempAnnotation.add_offset((Integer) offsetPair.getFirst(), (Integer) offsetPair.getSecond());
                            convertedAnnotations.put(id, tempAnnotation);
                        }
                    }


                    break;
            }
        }

        List<dtoAnnotationForAnnotatedTextDocumentForAPI> finalAnnotations = convertedAnnotations.values().stream().collect(Collectors.toList());
        annotations = finalAnnotations;

    }


    private Pair<Integer, Integer> constructOffset(String globalOffset) {
        String[] offsets = globalOffset.split(":");
        return new Pair<Integer, Integer>(Integer.parseInt(offsets[0]), Integer.parseInt(offsets[1]));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class dtoAnnotationForAnnotatedTextDocumentForAPI {

        @JsonProperty
        String id;
        @JsonProperty
        String type;
        @JsonProperty
        String label;
        @JsonProperty
        String description;
        @JsonProperty
        String standard;
        @JsonProperty
        String version;
        @JsonProperty
        String link;


        @JsonProperty
        List<dtoGlobalOffset> offsets = new ArrayList<>();
        @JsonProperty
        JSONObject metaData;

        public void add_offset(int x, int y) {
            dtoGlobalOffset tempDtoOffset = new dtoGlobalOffset(x, y);
            this.offsets.add(tempDtoOffset);
        }

        @Data
        @AllArgsConstructor
        private class dtoGlobalOffset {
            @JsonProperty
            int startIndex;
            @JsonProperty
            int endIndex;
        }


    }


}
