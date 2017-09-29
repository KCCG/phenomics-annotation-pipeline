package au.org.garvan.kccg.annotations.pipeline.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APToken;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ahmed on 21/8/17.
 */

public class ShortFormExtractor {

    private static final List<String> INDEX_LIST = Arrays.asList(
            "(i)",
            "(ii)",
            "(iii)",
            "(iv)",
            "(v)",
            "(vi)",
            "(vii)",
            "(viii)",
            "(ix)",
            "(x)",
            "(I)",
            "(II)",
            "(III)",
            "(IV)",
            "(V)",
            "(VI)",
            "(VII)",
            "(VIII)",
            "(IX)",
            "(X)");

    private static final List<String> SPECIAL_WORDS_PARTIALS = Arrays.asList(
            "<",
            ">",
            "%",
            "e.g",
            "figure",
            "see",
            "table"

    ) ;

    public static void main(String[] args) {

    }

    public static void markShortForms(List<APToken> tokenList)
    {
        for (APToken tok : tokenList){

            String text =  tok.isPunctuation()? tok.getOriginalText().substring(0, tok.getOriginalText().length()-1) : tok.getOriginalText();
            tok.setShortForm(isParenthesizedWithCorrectLength(text) && hasAtleastOneAlphabet(text) && isNotListIndex(text) && doesNotContainSpecialWords(text));
        }

    }

    static boolean isParenthesizedWithCorrectLength(String input){ return (!input.isEmpty() && input.length()>2 && input.length()<13 && input.charAt(0) =='(' && input.charAt(input.length()-1)==')');}

    static boolean hasAtleastOneAlphabet(String input){
        return input.matches(".*[a-zA-Z]+.*");
    }

    static boolean isNotListIndex(String input){ return (!INDEX_LIST.contains(input) && !input.matches("\\([a-zA-Z]\\)"));}

    static boolean doesNotContainSpecialWords(String input){

        for (String specialPartial : SPECIAL_WORDS_PARTIALS){
            if (input.toLowerCase().contains(specialPartial))
            {
                return false;
            }
        }
        return true;
    }







}
