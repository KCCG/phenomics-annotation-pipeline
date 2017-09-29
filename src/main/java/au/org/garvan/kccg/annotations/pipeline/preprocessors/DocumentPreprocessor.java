package au.org.garvan.kccg.annotations.pipeline.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APDocument;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ahmed on 1/8/17.
 */
public class DocumentPreprocessor {

    public static void main(String[] args) {

    }

    public static void preprocessDocument(APDocument doc) {
            doc.setCleanedText(addSpaceAfterFullStop(doc.getOriginalText()));

    }


    private static String addSpaceAfterFullStop(String input){

        String modified;
        Pattern p = Pattern.compile("\\.([A-Z])");
        Matcher m = p.matcher(input);
        modified = m.replaceAll(". $1");
        return modified;
    }


}
