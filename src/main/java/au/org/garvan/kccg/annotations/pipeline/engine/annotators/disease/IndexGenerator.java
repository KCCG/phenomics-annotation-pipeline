package au.org.garvan.kccg.annotations.pipeline.engine.annotators.disease;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease.APDisease;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APPhrase;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.CoreNLPManager;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IndexGenerator {

    private final Logger slf4jLogger = LoggerFactory.getLogger(IndexGenerator.class);
    private String fileName;


    private final String VERSION = "2018-04-15";
    private final String STANDARD = "MONDO";
    private final String ANNOTATIONS = AnnotationType.DISEASE.toString();

    private static final List<String> diseaseIdsControlList = Arrays.asList("MONDO:0021150","MONDO:0021151","MONDO:0000001","MONDO:0021153","MONDO:0021140","MONDO:0021141", "MONDO:0002254", "MONDO:0021137", "MONDO:0021178", "MONDO:0021136", "MONDO:0021137" );

    public void generateIndex(String fName) {
        fileName = fName;
        List<APDisease> rawDiseases = readFile();
        List<APDisease> filteredDiseases = rawDiseases.stream().filter(d-> !diseaseIdsControlList.contains(d.getMondoID())).collect(Collectors.toList());

        int tokenLengthThreshold = 4;
        int tokenNumberThreshold = 8;
        DocumentPreprocessor.init();


        List<APDisease> cleanedDiseases = new ArrayList<>();
        List<APDisease> discardedDiseases = new ArrayList<>();

        for (APDisease apDisease : filteredDiseases) {
            //Prepare list for diseases
            slf4jLogger.debug("");
            slf4jLogger.debug(String.format("========= Starting processing disease:%s ==========", apDisease.getMondoID()));

            //Point:Clean all parenthesis containing word and space items from text

            Stack<String> stackedDiseases = new Stack<>();
            apDisease.getSynonyms().stream().forEach(s -> stackedDiseases.push(cleanText(s)));
            stackedDiseases.push(cleanText(apDisease.getLabel()));

            List<String> rulesAppliedDisease = new ArrayList<>();
            boolean discard = false;
            boolean labelDiscarded = false;
            int iterator = 0;

            while (!stackedDiseases.empty()) {
                String oneForm = stackedDiseases.pop();

                List<String> discardedStrings = new ArrayList<>();
                APPhrase apPhrase = DocumentPreprocessor.preprocessPhrase(oneForm);
                List<APToken> tokens = apPhrase.getTokens();
                TokenAnalysis tokenAnalysis = analyseTokens(tokens);

                //Point Rule 0: If there is a semicolon and it is second last token then consider there is a short form ahead. Split form and out it as abbreviation
                if (tokens.size() > 2 && tokenAnalysis.getIndexOfSemicolon() == tokens.size() - 2) {
                    slf4jLogger.debug(String.format("Disease:%s | Found semicolon token at second last index: %s.", apDisease.getMondoID(), oneForm));
                    List<APToken> initialTokens = tokens.subList(0, tokenAnalysis.getIndexOfSemicolon());
                    oneForm = initialTokens.stream().map(t -> t.getOriginalText()).collect(Collectors.joining(" "));
                    stackedDiseases.push(tokens.get(tokens.size() - 1).getOriginalText());

                }


                //Point: Rule 1a: Token length. If single then length should be more than 3(tokenLengthThreshold)
                //Point: Rule 1b: If number of tokens is greater that 6(tokenNumberThreshold) and have 2 commas and threshold then discard
                if (!discard && !labelDiscarded) {


                    if (tokens.size() == 1  ) {
                        if(tokens.get(0).getOriginalText().length() < tokenLengthThreshold) {
                            slf4jLogger.debug(String.format("Disease:%s | Found single token and less than length: %s.", apDisease.getMondoID(), oneForm));
                            discardedStrings.add(oneForm);
                            discard = true;
                            if (iterator == 0)
                                labelDiscarded = true;
                        }
                        else if(tokens.get(0).getOriginalText().length()<1.5*tokenLengthThreshold && onlyText(tokens.get(0).getOriginalText())) {
                            if(tokens.get(0).getPartOfSpeech().equals("JJ")){
                                discardedStrings.add(oneForm);
                                discard = true;
                                if (iterator == 0)
                                    labelDiscarded = true;
                            }

                        }
                    } else if (tokens.size() > tokenNumberThreshold && (tokenAnalysis.getCountOfComma() > 1 || tokenAnalysis.getCountOfParenthesis() > 0 || tokenAnalysis.getCountOfSlash() > 0)) {
                        slf4jLogger.debug(String.format("Disease:%s | Found long string with symbols: %s.", apDisease.getMondoID(), oneForm));
                        discardedStrings.add(oneForm);
                        discard = true;
                        if (iterator == 0)
                            labelDiscarded = true;
                    }


                }

                //Point: Rule2: If count of or is greater than zero and parenthesis are present
                if (!discard && !labelDiscarded) {
                    if (tokenAnalysis.getCountOfOr() > 0 && tokenAnalysis.getCountOfParenthesis() > 1) {
                        //Point: Step2: Check of or is present in string with parenthesis, if yes remove string
                        slf4jLogger.debug(String.format("Disease:%s | Found or in string: %s.", apDisease.getMondoID(), oneForm));
                        discardedStrings.add(oneForm);
                        discard = true;
                        if (iterator == 0)
                            labelDiscarded = true;
                    }
                }


                if (!discard && !labelDiscarded) {
                    //Point: Rule3: If count of comma is 1 then swap string. If more than 1 then discard
                    if (tokenAnalysis.getCountOfComma() > 0) {
                        slf4jLogger.debug(String.format("Disease:%s | Found comma:%s.", apDisease.getMondoID(), oneForm));
                        if (tokenAnalysis.getCountOfComma() == 1) {
                            String swappedString = getCommaSwappedString(tokens);
                            if (swappedString != null) {
                                oneForm = swappedString;
                            }
                        } else {
                            discardedStrings.add(oneForm);
                            discard = true;
                            if (iterator == 0)
                                labelDiscarded = true;
                        }
                    }


                    if (tokenAnalysis.getCountOfSlash() > 0) {
                        slf4jLogger.debug(String.format("Disease:%s | Found slash:%s.", apDisease.getMondoID(), oneForm));
                        discardedStrings.add(oneForm);
                        discard = true;
                        if (iterator == 0)
                            labelDiscarded = true;
                    }
                }


                if (!discard) {
                    rulesAppliedDisease.add(oneForm);
                }
                discard = false;
                iterator++;
            }

            String label;

            if (labelDiscarded) {
                slf4jLogger.debug(String.format("Label is discarded"));

                APDisease cleanedDisease = new APDisease(
                        apDisease.getOboURI(),
                        apDisease.getMondoID(),
                        apDisease.getDefinition(),
                        apDisease.getLabel(),
                        rulesAppliedDisease,
                        true
                );
                discardedDiseases.add(cleanedDisease);
            } else {
                if (rulesAppliedDisease.size() > 0) {
                    Set<String> uniqueLabels = new HashSet<>();
                    List<String> finalSynonyms = new ArrayList<>();
                    label = rulesAppliedDisease.get(0);

                    uniqueLabels.add(label.toLowerCase());


                    for (Integer x = 1; x < rulesAppliedDisease.size(); x++) {
                        if (!uniqueLabels.contains(rulesAppliedDisease.get(x).toLowerCase())) {
                            uniqueLabels.add(rulesAppliedDisease.get(x).toLowerCase());
                            finalSynonyms.add(rulesAppliedDisease.get(x));
                        } else
                            slf4jLogger.debug(String.format("Duplicated by case so discarding: id:%s | string:%s", apDisease.getMondoID(), rulesAppliedDisease.get(x)));
                    }


                    slf4jLogger.debug(String.format("Final Disease: id:%s | label:%s | synonyms:%s", apDisease.getMondoID(), label, uniqueLabels.stream().collect(Collectors.joining("|"))));


                    APDisease cleanedDisease = new APDisease(
                            apDisease.getOboURI(),
                            apDisease.getMondoID(),
                            apDisease.getDefinition(),
                            label,
                            finalSynonyms,
                            false
                    );
                    cleanedDiseases.add(cleanedDisease);


                }


            }//Loop

        }
        slf4jLogger.info(String.format("Completed the cleaning process. Total collected diseases:%d | Total discarded diseases:%d", cleanedDiseases.size(), discardedDiseases.size()));
        writeIndexFiles(cleanedDiseases);


    }

    private String getCommaSwappedString(List<APToken> tokens) {
        Integer commaIndex = -1;
        for (APToken token : tokens) {
            commaIndex++;
            if (token.getOriginalText().equals(","))
                break;
        }
        List<APToken> swappedList = new ArrayList<>();
        if (commaIndex > 0 && commaIndex < tokens.size() - 1) {
            swappedList.addAll(tokens.subList(commaIndex + 1, tokens.size()));
            swappedList.addAll(tokens.subList(0, commaIndex));
            return swappedList.stream().map(t -> t.getOriginalText()).collect(Collectors.joining(" "));

        }
        return null;
    }

    private TokenAnalysis analyseTokens(List<APToken> tokens) {
        TokenAnalysis tokenAnalysis = new TokenAnalysis();
        List<String> parenthesis = Arrays.asList("(", ")");
        Integer index = 0;
        for (APToken t : tokens) {
            if (t.getOriginalText().toLowerCase().equals("or"))
                tokenAnalysis.incrementOr();

            else if (t.getOriginalText().toLowerCase().equals("/"))
                tokenAnalysis.incrementSlash();

            else if (t.getOriginalText().toLowerCase().equals(","))
                tokenAnalysis.incrementComma();

            else if (t.getOriginalText().toLowerCase().equals(";"))
                tokenAnalysis.setIndexOfSemicolon(index);

            else if (parenthesis.contains(t.getOriginalText()))
                tokenAnalysis.incrementParenthesis();

            index++;
        }
        return tokenAnalysis;
    }


    public String cleanText(String input) {
        String modified;
        Pattern p = Pattern.compile("\\s\\(([a-zA-Z ]+)\\)");
        Matcher m = p.matcher(input);
        modified = m.replaceAll("");

        List<Character> puncs = Arrays.asList(';', '.', '-', ':');

        if (puncs.contains(modified.charAt(modified.length() - 1))) {
            modified = modified.substring(0, modified.length() - 1);
        }

        return modified;
    }

    private boolean onlyText(String input){
        return input.matches("[a-zA-Z]+");
    }

    private void writeIndexFiles(List<APDisease> selectedDiseases) {
        slf4jLogger.info("Writing index files for selected disease. ");

        //TODO: Create file for pipeline, needed for annotation, autocomplete etc.
        JSONObject diseaseAnnotationIndex = new JSONObject();
        diseaseAnnotationIndex.put("standard", STANDARD);
        diseaseAnnotationIndex.put("version", VERSION);


        JSONArray jsonDiseasesArray = new JSONArray();
        selectedDiseases.stream().forEach(d-> jsonDiseasesArray.add(d.getAnnotationJsonForAnnotator()));

        diseaseAnnotationIndex.put("diseases", jsonDiseasesArray);
        writeAnnotationIndexFile(diseaseAnnotationIndex, "mondoAnnotationIndex.json");


        //TODO: Create file for Affinity
        JSONObject diseaseAdfinityIndex = new JSONObject();
        diseaseAdfinityIndex.put("standard", STANDARD);
        diseaseAdfinityIndex.put("version", VERSION);

        JSONArray jsonDiseasesAffinityArray = new JSONArray();
        selectedDiseases.stream().forEach(d-> jsonDiseasesAffinityArray.add(d.getAnnotationJsonForAffinity()));
        diseaseAdfinityIndex.put("diseases", jsonDiseasesAffinityArray);
        writeAnnotationIndexFile(diseaseAdfinityIndex, "mondoAffinityIndex.json");

    }

    public void writeAnnotationIndexFile(JSONObject diseaseIndex,String fileName){

        String localPath =  System.getProperty("user.dir") + "/Analysis/";
        try (FileWriter file = new FileWriter(localPath + fileName )) {
            file.write(diseaseIndex.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<APDisease> readFile() {
        List<APDisease> mondoDiseases = new ArrayList<>();
        try {
            slf4jLogger.info(String.format("Reading lexicon. Filename:%s", fileName));
            String path = "indexingFiles/" + fileName;
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



    }

    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MondoJsonFile {

        @JsonProperty("graphs")
        JSONArray graphs;


        public ArrayList getDiseaseNodes() {

            for (Object obj : graphs) {
                LinkedHashMap linkedGraph = (LinkedHashMap) obj;
                if (linkedGraph.containsKey("id") && linkedGraph.get("id").toString().equals("http://purl.obolibrary.org/obo/mondo.owl")) {
                    return (ArrayList) linkedGraph.get("nodes");

                }


            }

            return new JSONArray();
        }

    }



    @Data
    private class TokenAnalysis {
        Integer countOfOr = 0;
        Integer countOfSlash = 0;
        Integer countOfComma = 0;
        Integer countOfParenthesis = 0;
        Integer indexOfSemicolon = -1;

        public void incrementOr() {
            countOfOr++;
        }

        public void incrementSlash() {
            countOfSlash++;
        }

        public void incrementComma() {
            countOfComma++;
        }

        public void incrementParenthesis() {
            countOfParenthesis++;
        }


    }
}
