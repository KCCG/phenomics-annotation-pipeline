package au.org.garvan.kccg.annotations.pipeline.engine.annotators.disease;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease.APDisease;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class IndexGenerator {

    private final Logger slf4jLogger = LoggerFactory.getLogger(IndexGenerator.class);
    private String fileName;

    public void generateIndex(String fName){
        fileName = fName;
        List<APDisease> rawDiseases = readFile();


        for (APDisease apDisease: rawDiseases){

            String dID = apDisease.getMondoID();
            slf4jLogger.debug(String.format("Disease ID:%s",dID));
            String synonyms =  apDisease.getSynonyms().stream().map(s->s.getVal()).collect(Collectors.toList()).stream().collect(Collectors.joining(" | "));
            slf4jLogger.debug(String.format( " [%s]  ->  { %s }\n" , apDisease.getLabel(), synonyms));


        }




    }




    public List<APDisease> readFile() {
        List<APDisease> mondoDiseases = new ArrayList<>();
        try {
            slf4jLogger.info(String.format("Reading lexicon. Filename:%s", fileName));
            String path = "lexicons/" + fileName;
            InputStream input = getClass().getResourceAsStream("resources/" + path);
            if (input == null) {
                // this is how we load file within editor (eg eclipse)
                input = DiseaseHandler.class.getClassLoader().getResourceAsStream(path);
            }

            ObjectMapper mapper = new ObjectMapper();
            MondoJsonFile jsonMondo = mapper.readValue(input, MondoJsonFile.class);
            ArrayList diseaseNodes = jsonMondo.getDiseaseNodes();

            for (Object object : diseaseNodes) {
                LinkedHashMap linkedDisease = (LinkedHashMap) object;
                APDisease tempDisease = new APDisease(linkedDisease);
                if (!tempDisease.isDeprecated()) {
                    if (Strings.isNullOrEmpty(tempDisease.getLabel()))
                        tempDisease.setDeprecated(true);
                    else {
                        mondoDiseases.add(tempDisease);
//                        List<String> names = tempDisease.getSynonyms().stream().map(x -> x.getVal().toLowerCase()).collect(Collectors.toList());
//                        names.add(tempDisease.getLabel().toLowerCase());
//                        for (String name : names) {
//                            if (!Strings.isNullOrEmpty(name))
//                                diseaseLabelToMondo.put(name, tempDisease.getMondoID());
//                        }
                    }

                }
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mondoDiseases;


//        List<String> allLines = mondoDiseases.values().stream().map(s->s.getStringForFile()).collect(Collectors.toList());
//        APFileWriter.writeSmallTextFile(allLines, "mondo");

    }

    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MondoJsonFile{

        @JsonProperty("graphs")
        JSONArray graphs;


        public ArrayList getDiseaseNodes(){

            for(Object obj: graphs){
                LinkedHashMap linkedGraph = (LinkedHashMap) obj;
                if(linkedGraph.containsKey("id") && linkedGraph.get("id").toString().equals("http://purl.obolibrary.org/obo/mondo.owl")){
                    return (ArrayList)linkedGraph.get("nodes");

                }


            }

            return new JSONArray();
        }

    }
}
