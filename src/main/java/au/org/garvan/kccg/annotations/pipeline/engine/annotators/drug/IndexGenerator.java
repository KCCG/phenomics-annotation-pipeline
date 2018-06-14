package au.org.garvan.kccg.annotations.pipeline.engine.annotators.drug;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.Constants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Drug.APDrug;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APPhrase;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.StringUtils;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IndexGenerator {

    private final Logger slf4jLogger = LoggerFactory.getLogger(IndexGenerator.class);
    private String fileName;


    private final String VERSION = "2018-05-01";
    private final String STANDARD = "DrugBank";
    private final String ANNOTATIONS = AnnotationType.DRUG.toString();

    private static final List<String> drugIdsControlList = Arrays.asList();

    public void generateIndex(String fName) {
        fileName = fName;
        List<RawDrug> rawDrugs = readFile();
        List<RawDrug> filteredDrugs = rawDrugs.stream().filter(d-> !drugIdsControlList.contains(d.drugbankId)).collect(Collectors.toList());

        int tokenLengthThresholdMin = 4;
        int tokenLengthThresholdMax = 12;
        int tokenNumberThreshold = 8;
        DocumentPreprocessor.init();


        List<APDrug> cleanedDrugs = new ArrayList<>();
        List<APDrug> discardedDrugs = new ArrayList<>();

        HashSet<String> singleValidDictionaryWord = new HashSet<>();

        for (RawDrug rawDrug  : filteredDrugs) {
            //Prepare list for diseases
            slf4jLogger.debug("");
            slf4jLogger.debug(String.format("========= Starting processing disease:%s ==========", rawDrug.drugbankId));

            //Point:Clean all parenthesis containing word and space items from text

            Stack<String> stackedDrugs = new Stack<>();
            rawDrug.synonyms.stream().forEach(s -> stackedDrugs.push(cleanText(s)));
            rawDrug.internationalBrands.stream().forEach(s -> stackedDrugs.push(cleanText(s)));
//            rawDrug.products.stream().forEach(s -> stackedDrugs.push(cleanText(s)));
//            rawDrug.mixtures.stream().forEach(s -> stackedDrugs.push(cleanText(s.name)));

            stackedDrugs.push(cleanText(cleanText(rawDrug.drugText)));

            List<String> rulesAppliedDrugs = new ArrayList<>();
            boolean discard = false;
            boolean labelDiscarded = false;
            int iterator = 0;

            while (!stackedDrugs.empty()) {
                String oneForm = stackedDrugs.pop();
                if(Strings.isNullOrEmpty(oneForm))
                    continue;


                List<String> discardedStrings = new ArrayList<>();
                APPhrase apPhrase = DocumentPreprocessor.preprocessPhrase(oneForm);
                List<APToken> tokens = apPhrase.getTokens();
                TokenAnalysis tokenAnalysis = analyseTokens(tokens, oneForm);


                //Point: Rule 1a: Token length. If single then length should be more than 3(tokenLengthThreshold)
                //Point: Rule 1b: If number of tokens is greater that 6(tokenNumberThreshold) and have 2 commas and threshold then discard
                if (!discard && !labelDiscarded) {

                    if (tokens.size() == 1  ) {



                        if(tokens.get(0).getOriginalText().length() < tokenLengthThresholdMin) {
                            slf4jLogger.debug(String.format("Drug:%s | Found single token and less than length: %s.", rawDrug.drugbankId, oneForm));
                            discardedStrings.add(oneForm);
                            discard = true;
                            if (iterator == 0)
                                labelDiscarded = true;
                        }
                        else if(tokens.get(0).getOriginalText().length()<1.5*tokenLengthThresholdMin
                                && onlyText(tokens.get(0).getOriginalText())
                                && tokens.get(0).getPartOfSpeech().equals("JJ")) {
                                discardedStrings.add(oneForm);
                                discard = true;
                                if (iterator == 0)
                                    labelDiscarded = true;

                        }

                        else
                        {
                            String tokenText = tokens.get(0).getOriginalText().toLowerCase();
                            List<String> suggestions = DocumentPreprocessor.checkSpellings(tokenText);
                            if(suggestions.size()>0 && Constants.getDrugFilters().contains(tokenText)) {
                                    slf4jLogger.debug(String.format("Drug:%s | Found single token in filters: %s.", rawDrug.drugbankId, oneForm));
                                    discardedStrings.add(oneForm);
                                    discard = true;
                                    if (iterator == 0)
                                        labelDiscarded = true;

                                }
                            else if (suggestions.size()>0)
                                singleValidDictionaryWord.add(tokenText);


                        }

                    }  else if (tokens.size() > tokenNumberThreshold && (tokenAnalysis.getCountOfComma() > 1 || tokenAnalysis.getCountOfParenthesis() > 0 || tokenAnalysis.getCountOfSlash() > 0)) {
                        slf4jLogger.debug(String.format("Drug:%s | Found long string with symbols: %s.", rawDrug.drugbankId, oneForm));
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
                        slf4jLogger.debug(String.format("Drug:%s | Found or in string: %s.", rawDrug.drugbankId, oneForm));
                        discardedStrings.add(oneForm);
                        discard = true;
                        if (iterator == 0)
                            labelDiscarded = true;
                    }
                    else if(tokenAnalysis.strHyphens>3 || tokenAnalysis.strCommas>1 || tokenAnalysis.strSquareBracketRight+tokenAnalysis.strSquareBracketLeft>1){
                        slf4jLogger.debug(String.format("Drug:%s | Found str flags in string: %s.", rawDrug.drugbankId, oneForm));
                        discardedStrings.add(oneForm);
                        discard = true;
                        if (iterator == 0)
                            labelDiscarded = true;
                    }

                }


                if (!discard && !labelDiscarded) {
                    //Point: Rule3: If count of comma is 1 then swap string. If more than 1 then discard
                    if (tokenAnalysis.getCountOfComma() > 0) {
                        slf4jLogger.debug(String.format("Drug:%s | Found comma:%s.", rawDrug.drugbankId, oneForm));
                        if (tokenAnalysis.getCountOfComma() == 1) {
//                            String swappedString = getCommaSwappedString(tokens);
//                            if (swappedString != null) {
//                                oneForm = swappedString;
                        } else {
                            discardedStrings.add(oneForm);
                            discard = true;
                            if (iterator == 0)
                                labelDiscarded = true;
                        }
                    }


                    if (tokenAnalysis.getCountOfSlash() > 0) {
                        slf4jLogger.debug(String.format("Drug:%s | Found slash:%s.", rawDrug.drugbankId, oneForm));
                        discardedStrings.add(oneForm);
                        discard = true;
                        if (iterator == 0)
                            labelDiscarded = true;
                    }
                }



                if (!discard) {
                    rulesAppliedDrugs.add(oneForm);
                }
                discard = false;
                iterator++;
            }

            String label;

            if (labelDiscarded) {
                slf4jLogger.debug(String.format("Label is discarded"));

                APDrug cleanedDrug = new APDrug("", rawDrug.drugbankId, rawDrug.description, rawDrug.drugText, new ArrayList<>());
                discardedDrugs.add(cleanedDrug);
            } else {
                if (rulesAppliedDrugs.size() > 0) {
                    Set<String> uniqueLabels = new HashSet<>();
                    List<String> finalSynonyms = new ArrayList<>();
                    label = rulesAppliedDrugs.get(0);

                    uniqueLabels.add(getAlphaPattern(label.toLowerCase()));

//                    Collections.sort(rulesAppliedDrugs, Comparator.comparing(String::length).reversed());

                    for (Integer x = 1; x < rulesAppliedDrugs.size(); x++) {


                        if(getAlphaPattern(rulesAppliedDrugs.get(x)).toLowerCase().contains(getAlphaPattern(label.toLowerCase()))){
                            slf4jLogger.debug(String.format("Duplicated by alpha pattern so discarding: id:%s | string:%s", rawDrug.drugbankId, rulesAppliedDrugs.get(x)));
                        }

                        if(uniqueLabels.contains(getAlphaPattern(rulesAppliedDrugs.get(x).toLowerCase()))){
                            slf4jLogger.debug(String.format("Duplicated by alpha pattern so discarding: id:%s | string:%s", rawDrug.drugbankId, rulesAppliedDrugs.get(x)));
                        }

                        if (!uniqueLabels.contains(getAlphaPattern(rulesAppliedDrugs.get(x).toLowerCase()))) {
                            uniqueLabels.add(rulesAppliedDrugs.get(x).toLowerCase());
                            finalSynonyms.add(rulesAppliedDrugs.get(x));
                        }
                    }


                    slf4jLogger.debug(String.format("Final Drug: id:%s | label:%s | synonyms:%s", rawDrug.drugbankId, label, uniqueLabels.stream().collect(Collectors.joining("|"))));
                    String drugUrl = String.format("https://www.drugbank.ca/drugs/%s", rawDrug.drugbankId);

                    Collections.sort(finalSynonyms, Comparator.comparing(String::length));

                    if(finalSynonyms.size()>50){
                        finalSynonyms = finalSynonyms.stream().filter(x-> x.split(" ").length<3).collect(Collectors.toList());
                        if(finalSynonyms.size()>50)
                            finalSynonyms = finalSynonyms.subList(0,50);
                    }



                    APDrug cleanedDrug = new APDrug(drugUrl, rawDrug.drugbankId, rawDrug.description, rawDrug.drugText, finalSynonyms);
//                    List<String> selectedSynonyms = cleanedDrug.getSynonyms().stream().map(s->s.toLowerCase()).collect(Collectors.toList());
//                    Set<String> uniqueMixtures = new HashSet<>();
//                    for (RawDrug.Mixture mixture:rawDrug.mixtures){
//                        String [] ingredients = mixture.ingredientsString.split("\\+");
//                        if (ingredients.length>1){
//                           if(selectedSynonyms.contains(mixture.name.toLowerCase()) && !uniqueMixtures.contains(mixture.name.toLowerCase()))
//                        {
//                            cleanedDrug.addMixture(mixture.name, Arrays.asList(ingredients));
//                            uniqueMixtures.add(mixture.name.toLowerCase());
//                        }
//                        }
//
//                    }

                    cleanedDrugs.add(cleanedDrug);


                }


            }//Loop

        }
        slf4jLogger.info(String.format("Completed the cleaning process. Total collected Drug:%d | Total discarded Drug:%d", cleanedDrugs.size(), discardedDrugs.size()));
        String collectedSingle = String.join("\n", singleValidDictionaryWord);
        writeIndexFiles(cleanedDrugs);


    }


    private TokenAnalysis analyseTokens(List<APToken> tokens, String input) {
        TokenAnalysis tokenAnalysis = new TokenAnalysis();
        List<String> parenthesis = Arrays.asList("(", ")");
//        List<String> redFlagWords = Arrays.asList("/mg", "/ml", "(iv)");

        Integer index = 0;
        for (APToken t : tokens) {
            if (t.getOriginalText().toLowerCase().equals("or"))
                tokenAnalysis.incrementOr();

            else if (t.getOriginalText().toLowerCase().equals(","))
                tokenAnalysis.incrementComma();

            else if (t.getOriginalText().toLowerCase().equals(";"))
                tokenAnalysis.setIndexOfSemicolon(index);

            else if (t.getOriginalText().toLowerCase().equals(":"))
                tokenAnalysis.setIndexOfSemicolon(index);

            else if (parenthesis.contains(t.getOriginalText()))
                tokenAnalysis.incrementParenthesis();

            index++;
        }



        tokenAnalysis.countOfSlash = Ints.checkedCast(input.chars().filter(ch-> ch =='/').count());
        tokenAnalysis.strCommas = Ints.checkedCast(input.chars().filter(ch-> ch ==',').count());
        tokenAnalysis.strHyphens = Ints.checkedCast(input.chars().filter(ch-> ch =='-').count());
        tokenAnalysis.strSquareBracketLeft = Ints.checkedCast(input.chars().filter(ch-> ch =='[').count());
        tokenAnalysis.strSquareBracketRight = Ints.checkedCast(input.chars().filter(ch-> ch ==']').count());

        return tokenAnalysis;
    }

    public String getAlphaPattern(String input){
        String newstr = input.replaceAll("\\P{L}+", "");
        return newstr;
    }


    public String cleanText(String input) {
        String modified;
        Pattern p = Pattern.compile("\\((.*?)\\)");
        Matcher m = p.matcher(input);
        modified = m.replaceAll("");

        if(Strings.isNullOrEmpty(modified))
            return modified;

        if(modified.contains("%"))
            return "";

        List<Character> puncs = Arrays.asList(';', '.', '-', ':');

        if (puncs.contains(modified.charAt(modified.length() - 1))) {
            modified = modified.substring(0, modified.length() - 1);
        }

        return modified;
    }

    private boolean onlyText(String input){
        return input.matches("[a-zA-Z]+");
    }

    private void writeIndexFiles(List<APDrug> selectedDrugs) {
        slf4jLogger.info("Writing index files for selected disease. ");


        //Engine Index file writing
        writeAnnotationIndexFile(selectedDrugs, "drugBankAnnotationIndex.txt");


        //Affinity index writing
        JSONObject drugAffinityIndex = new JSONObject();
        drugAffinityIndex.put("standard", STANDARD);
        drugAffinityIndex.put("version", VERSION);

        JSONArray jsonDrugsAffinityArray = new JSONArray();
        selectedDrugs.stream().forEach(d-> jsonDrugsAffinityArray.add(d.getAnnotationJsonForAffinity()));
        drugAffinityIndex.put("drugs", jsonDrugsAffinityArray);
        writeAnnotationIndexFileAffinity(drugAffinityIndex, "drugBankAffinityIndex.json");

    }

    public void writeAnnotationIndexFileAffinity(JSONObject diseaseIndex,String fileName){

        String localPath =  System.getProperty("user.dir") + "/Analysis/";
        try (FileWriter file = new FileWriter(localPath + fileName )) {
            file.write(diseaseIndex.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeAnnotationIndexFile(List<APDrug> selectedDrugs,String fileName){

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(String.format("AnnotationType=%s", ANNOTATIONS));
        stringBuilder.append(System.getProperty("line.separator"));

        stringBuilder.append(String.format("standard=%s", STANDARD));
        stringBuilder.append(System.getProperty("line.separator"));

        stringBuilder.append(String.format("version=%s", VERSION));
        stringBuilder.append(System.getProperty("line.separator"));

        for(APDrug drug: selectedDrugs){
            stringBuilder.append ("[ENTITY]");
            stringBuilder.append(System.getProperty("line.separator"));

            stringBuilder.append("[ID]");
            stringBuilder.append(System.getProperty("line.separator"));
            stringBuilder.append(drug.getDbankID());
            stringBuilder.append(System.getProperty("line.separator"));


            stringBuilder.append("[LBL]");
            stringBuilder.append(System.getProperty("line.separator"));
            stringBuilder.append(drug.getLabel());
            stringBuilder.append(System.getProperty("line.separator"));

            stringBuilder.append("[DEF]");
            stringBuilder.append(System.getProperty("line.separator"));
            stringBuilder.append(drug.getDefinition());
            stringBuilder.append(System.getProperty("line.separator"));

            stringBuilder.append("[URL]");
            stringBuilder.append(System.getProperty("line.separator"));
            stringBuilder.append(drug.getDbankUrl());
            stringBuilder.append(System.getProperty("line.separator"));


            if(drug.getSynonyms().size()>0){
                stringBuilder.append("[SYN]");
                stringBuilder.append(System.getProperty("line.separator"));
                for(String syn:drug.getSynonyms()){
                    stringBuilder.append(syn);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            }
            stringBuilder.append("[~ENTITY]");
            stringBuilder.append(System.getProperty("line.separator"));
            stringBuilder.append(System.getProperty("line.separator"));

        }

        String localPath =  System.getProperty("user.dir") + "/Analysis/";
        try (FileWriter file = new FileWriter(localPath + fileName )) {
            file.write(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<RawDrug> readFile() {
        try {
            slf4jLogger.info(String.format("Reading lexicon. Filename:%s", fileName));
            String path = "indexingFiles/" + fileName;
            InputStream input = getClass().getResourceAsStream("resources/" + path);
            if (input == null) {
                // this is how we load file within editor (eg eclipse)
                input = IndexGenerator.class.getClassLoader().getResourceAsStream(path);
            }

            ObjectMapper mapper = new ObjectMapper();

            List<RawDrug> myObjects = mapper.readValue(input, new TypeReference<List<RawDrug>>(){});
            return myObjects;

        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();


    }



    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawDrug {


        @JsonProperty("index")
        Integer index;

        @JsonProperty("drugbank_id")
        String drugbankId;

        @JsonProperty("description")
        String description;

        @JsonProperty("drug_text")
        String drugText;

        @JsonProperty("synonyms")
        List<String> synonyms;

        @JsonProperty("products")
        List<String> products;

        @JsonProperty("international_brands")
        List<String> internationalBrands;

        @JsonProperty("mixtures")
        List<Mixture> mixtures;


        private static class Mixture{

            @JsonProperty("ingredients_string")
            String ingredientsString;

            @JsonProperty("name")
            String name;

        }


    }

    @Data
    private class TokenAnalysis {
        Integer countOfOr = 0;
        Integer countOfSlash = 0;
        Integer countOfComma = 0;
        Integer countOfParenthesis = 0;
        Integer indexOfSemicolon = -1;
        Integer strCommas = 0;
        Integer strSquareBracketLeft = 0;
        Integer strSquareBracketRight = 0;
        Integer strHyphens = 0;

        public void incrementOr() {
            countOfOr++;
        }

        public void incrementComma() {
            countOfComma++;
        }

        public void incrementParenthesis() {
            countOfParenthesis++;
        }


    }
}
