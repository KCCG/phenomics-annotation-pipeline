package au.org.garvan.kccg.annotations.pipeline.engine.utilities;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.profiles.ProcessingProfile;
import org.json.simple.JSONArray;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ahmed on 5/10/17.
 */
public final class Common {
    private static List<Character> puncs = Arrays.asList('(',')','.',';',':',',','-','_','[',']','{','}',' ' );


    public static List<String> STOPPING_POS = Arrays.asList("CC", "CD", "DT" , "IN", "TO");
    public static String getPunctuationLessText(APToken token) {
        return token.getOriginalText()
                .trim()
                .replace("(", "")
                .replace(")", "")
                .replace(".", "")
                .replace(";", "")
                .replace(":", "")
                .replace(",", "")
                .replace("-", "")
                .trim();

    }

    public static String changeHyphenToSpace(String input)
    {
        return input.replace("-"," ");
    }
    public static String getPunctuationTrimmedText(String trimmedText) {
        Boolean found = false;
        if(trimmedText.length()>0) {
            if (puncs.contains(trimmedText.charAt(0))){
                trimmedText = trimmedText.substring(1,trimmedText.length());
                found = true;
            }
            if(trimmedText.length()>0) {
                if (puncs.contains(trimmedText.charAt(trimmedText.length() - 1))) {
                    trimmedText = trimmedText.substring(0, trimmedText.length() - 1);
                    found = true;
                }
            }
        }
        if(found)
            return getPunctuationTrimmedText(trimmedText);
        else
            return  trimmedText;

    }

    public JSONArray getJsonArrayFromStringList(List<String> lstString){
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(lstString);
        return jsonArray;

    }

    public static String emptyStringToNA(String inputStr)
    {
       return  inputStr.isEmpty() ? "N/A" : inputStr;

    }

    public static ProcessingProfile getStandardProfile(){

        ProcessingProfile profile = new ProcessingProfile(false,false,false,false, Arrays.asList(AnnotationType.GENE, AnnotationType.PHENOTYPE, AnnotationType.DISEASE));
        return profile;


    }
}
