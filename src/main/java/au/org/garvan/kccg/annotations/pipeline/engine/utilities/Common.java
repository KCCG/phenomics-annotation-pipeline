package au.org.garvan.kccg.annotations.pipeline.engine.utilities;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.json.simple.JSONArray;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ahmed on 5/10/17.
 */
public final class Common {
    private static List<Character> puncs = Arrays.asList('(',')','.',';',':',',','-','_','[',']','{','}' );

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
    public static String getPunctuationTrimmedText(APToken token) {
        String trimmedlText = token.getOriginalText()
                .trim();
        if(trimmedlText.length()>0) {

            if (puncs.contains(trimmedlText.charAt(0))){
                trimmedlText = trimmedlText.substring(1,trimmedlText.length());
            }

            if (puncs.contains(trimmedlText.charAt(trimmedlText.length()-1))){
                trimmedlText = trimmedlText.substring(0,trimmedlText.length()-1);
            }

        }
        return  trimmedlText;

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
}
