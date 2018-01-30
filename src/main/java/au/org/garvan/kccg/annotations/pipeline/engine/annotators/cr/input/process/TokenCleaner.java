package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.process;


import java.util.ArrayList;
import java.util.List;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import lombok.Getter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.omg.IOP.TAG_CODE_SETS;

//import au.org.garvan.kccg.hpo_cr.ta.TAPipeline;
//import au.org.garvan.kccg.hpo_cr.ta.domain.TAToken;
//import au.org.garvan.kccg.hpo_cr.util.TAConstants;

public class TokenCleaner {

    private boolean valid;

    @Getter
    private APToken singleToken;

    @Getter
    private List<APToken> tokens;

    private boolean isSplit;

    public TokenCleaner(String token, String pos, boolean splitIfExists, boolean processExact) {
        this.tokens = new ArrayList<APToken>();
        this.isSplit = false;

        String shape = TAConstants.shape(token);
        if (processExact) {
            this.valid = true;

            //CR: Workaround
            //singleToken = new APToken(token, token.toLowerCase(), pos, shape);
            singleToken = new APToken(token,pos,token);
        } else {
            if (shape.contains("x")) {
                if (shape.replaceAll("x", "").length() < 2) {
                    this.valid = false;
                } else {
                    if (splitIfExists) {
                        splitToken(token, shape, pos);
                    } else {
                        processValid(token, shape, pos);
                    }
                }
            } else {
                processValid(token, shape, pos);
            }
        }
    }

    private void splitToken(String word, String shape, String pos) {
        List<String> splits = new ArrayList<String>();

        String current = null;
        for (int i = 0; i < shape.length(); i++) {
            if (shape.charAt(i) == 'x') {
                if (current != null) {
                    splits.add(current);
                }
                current = null;
            } else {
                if (current == null) {
                    current = "";
                }
                current += word.charAt(i);
            }
        }
        if (current != null) {
            splits.add(current);
        }

        isSplit = true;
        for (String split : splits) {
            String ctShape = TAConstants.shape(split);
            processSplit(split, ctShape, TAConstants.POS_UNKNOWN);
        }
    }

    private void processSplit(String token, String shape, String pos) {
        this.valid = true;
        String cleanToken = clean(token.toLowerCase(), false);
        if (StringUtils.isNumeric(cleanToken)) {
            //tokens.add(new TAToken(token, cleanToken, TAConstants.POS_CD, shape));
            //CR: Workaround
            tokens.add(new APToken(token,TAConstants.POS_CD,token)); //Point: Don't forget to work on CleanToken function.
        } else {
            //CR: This is a workaround, currently pos is not updated based on dictionary, will solve it when Index is included. Commenting

//            pos = TAPipeline.crResources.getSimpleDictionary(TAConstants.DICT_POS).hasKey(cleanToken) ?
//                    TAPipeline.crResources.getSimpleDictionary(TAConstants.DICT_POS).getValue(cleanToken) :
//                    pos;
//            tokens.add(new TAToken(token, cleanToken, pos, shape));

            tokens.add(new APToken(token, pos, ""));
        }
    }

    private String clean(String token, boolean containsX) {
        String cleanToken = Jsoup.clean(token, Whitelist.none());
        cleanToken = StringEscapeUtils.unescapeHtml4(cleanToken);
        //CR: This is a workaround, currently token is not updated based on dictionary, will solve it when Index is included. Commenting
//        if (!containsX) {
//            cleanToken = TAPipeline.crResources.getSimpleDictionary(TAConstants.DICT_ORDINALS).hasKey(cleanToken) ?
//                    TAPipeline.crResources.getSimpleDictionary(TAConstants.DICT_ORDINALS).getValue(cleanToken) :
//                    cleanToken;
//        }
        return cleanToken;
    }


    private void processValid(String word, String shape, String pos) {
        this.valid = true;

        String cleanToken = clean(word.toLowerCase(), shape.contains("x"));
        if (StringUtils.isNumeric(cleanToken)) {

//            singleToken = new TAToken(word, cleanToken, TAConstants.POS_CD, TAConstants.shape(cleanToken));
            singleToken = new APToken(word, TAConstants.POS_CD,"");
        } else {

//            singleToken = new TAToken(word, cleanToken, pos, shape);
            singleToken = new APToken(word, pos, "");
        }
    }

    public boolean isSplit() {
        return isSplit;
    }

    public boolean isValid() {
        return valid;
    }

//    public TAToken getSingleToken() {
//        return singleToken;
//    }


//    public List<TAToken> getTokens() {
//        return tokens;
//    }
}
