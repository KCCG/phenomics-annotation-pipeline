package au.org.garvan.kccg.annotations.pipeline.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.utilities.Common;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APToken;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 12/9/17.
 */
public class LongFormMarker {
    private static final int OBSERVATION_WINDOW = 10;


    public static void main(String[] args) {

    }


    public static void markLongForms(APSentence sent) {

        List<APToken> shortFormTokens = sent.getTokens().stream().filter(x -> x.isShortForm()).collect(Collectors.toList());
        Map<APToken, APToken[]> sentSfLfLink = new HashMap<>();

        if (shortFormTokens.size() > 0) {
            List<APToken> tokens = sent.getSortedTokens();
            for (APToken shortToken : shortFormTokens) {
                int index = tokens.indexOf(shortToken);
                int start = index - OBSERVATION_WINDOW > 0 ? index - OBSERVATION_WINDOW : 0;
                List<APToken> potentialLongFormTokens = tokens.subList(start, index);
                List<APToken> longFormTokens = processRules(potentialLongFormTokens, shortToken);
                if (!longFormTokens.isEmpty()) {
                    sentSfLfLink.put(shortToken, longFormTokens.stream().toArray(APToken[]::new));
                } else {
                    sentSfLfLink.put(shortToken, new APToken[0]);
                }
            }
        }
        sent.setSfLfLink(sentSfLfLink);
    }

    private static List<APToken> processRules(List<APToken> potentialLongFormTokens, APToken shortToken) {
        String shortText = Common.getTrimmedText(shortToken);
        List<APToken> finalLongForm;

        //Process R1: FirstLetter for all tokens
        finalLongForm = R1_firstLetter(potentialLongFormTokens, shortText);
        if (finalLongForm.isEmpty())
            //Process R2: FirstLetter for all tokens except prepositions
            finalLongForm = R2_firstLetterWithSkippingPrepositions(potentialLongFormTokens, shortText);
        if (finalLongForm.isEmpty())
            finalLongForm = R3_lastWordIsPlural(potentialLongFormTokens, shortText);
        if (finalLongForm.isEmpty())
            finalLongForm = R4_firstLetterWithHyphensAndSkipPrepositions(potentialLongFormTokens, shortText);
        if (finalLongForm.isEmpty())
            finalLongForm = R5_allSFOrderedInOneToken(potentialLongFormTokens, shortText);
        if (finalLongForm.isEmpty())
            finalLongForm = R6_multipleSFOrderedInMultipleTokens(potentialLongFormTokens, shortText);

        return finalLongForm;
    }

    private static List<APToken> R1_firstLetter(List<APToken> potentialLongFormTokens, String strSF) {
        int sfLength = strSF.length();
        if (potentialLongFormTokens.size() >= sfLength) {
            List<String> initialsList = potentialLongFormTokens.subList(potentialLongFormTokens.size() - sfLength, potentialLongFormTokens.size())
                    .stream()
                    .map(x -> x.getOriginalText().substring(0, 1))
                    .collect(Collectors.toList());
            String initials = String.join("", initialsList);
            if (initials.toLowerCase().equals(strSF.toLowerCase())) {
                return potentialLongFormTokens.subList(potentialLongFormTokens.size() - sfLength, potentialLongFormTokens.size());
            }

        }
        return new ArrayList<>();

    }

    private static List<APToken> R2_firstLetterWithSkippingPrepositions(List<APToken> potentialLongFormTokens, String strSF) {
        List<APToken> finalLongForm = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        strSF.chars().mapToObj(c -> (char) c).collect(Collectors.toList()).stream().forEach(x -> stack.push(x));

        int LFindex = 0;
        //Reverse the collection for process and revert later
        Collections.reverse(potentialLongFormTokens);
        boolean violation = false;

        while (!stack.empty() && !violation && LFindex < potentialLongFormTokens.size()) {
            char charInAction = Character.toLowerCase(stack.pop());
            APToken tokenInAction = potentialLongFormTokens.get(LFindex);
            if (Character.toLowerCase(tokenInAction.getOriginalText().charAt(0)) == charInAction) {
                finalLongForm.add(tokenInAction);
                LFindex++;
            } else if (tokenInAction.getPartOfSpeech().equals("IN")) {
                LFindex++;
                finalLongForm.add(tokenInAction);
                stack.push(charInAction);
            } else {
                violation = true;
            }
        }

        Collections.reverse(potentialLongFormTokens);
        if (stack.empty() && !violation) {
            Collections.reverse(finalLongForm);
            return finalLongForm;
        } else {
            return new ArrayList<>();

        }

    }

    private static List<APToken> R3_lastWordIsPlural(List<APToken> potentialLongFormTokens, String strSF) {
        List<APToken> finalLongForm = new ArrayList<>();
        if (Character.toLowerCase(strSF.charAt(strSF.length() - 1)) == 's' && potentialLongFormTokens.get(potentialLongFormTokens.size() - 1).getPartOfSpeech().equals("NNS")) {
            String singularSF = strSF.substring(0, strSF.length() - 1);
            finalLongForm = R1_firstLetter(potentialLongFormTokens, singularSF);
            if (finalLongForm.isEmpty())
                finalLongForm = R2_firstLetterWithSkippingPrepositions(potentialLongFormTokens, singularSF);
        }

        return finalLongForm;

    }


    private static List<APToken> R4_firstLetterWithHyphensAndSkipPrepositions(List<APToken> potentialLongFormTokens, String strSF) {
        List<APToken> finalLongForm = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        strSF.chars().mapToObj(c -> (char) c).collect(Collectors.toList()).stream().forEach(x -> stack.push(x));

        int LFindex = 0;
        //Reverse the collection for process and revert later
        Collections.reverse(potentialLongFormTokens);
        boolean violation = false;

        while (!stack.empty() && !violation && LFindex < potentialLongFormTokens.size()) {
            char charInAction = Character.toLowerCase(stack.pop());
            APToken tokenInAction = potentialLongFormTokens.get(LFindex);

            boolean hyphonCase = false;
            if (tokenInAction.getOriginalText().indexOf('-') >= 0 && !stack.empty()) {

                List<String> subTexts = getSubtextsFromText(tokenInAction.getOriginalText(), '-');
                String subShortForm = "" + charInAction;
                int items = 1;
                while (!stack.isEmpty() && items < subTexts.size()) {
                    subShortForm = Character.toLowerCase(stack.pop()) + subShortForm;
                    items++;
                }
                if (subShortForm.length() == subTexts.size()) {

                    String constructedFirstLetter = subTexts.stream()
                            .map(s -> s.substring(0, 1).toLowerCase())
                            .collect(Collectors.joining());

                    if (constructedFirstLetter.equals(subShortForm)) {
                        finalLongForm.add(tokenInAction);
                        LFindex++;
                        hyphonCase = true;
                    }
                }


                if (!hyphonCase) {
                    while (subShortForm.length() > 1) {
                        stack.push(subShortForm.charAt(0));
                        subShortForm = subShortForm.substring(1, subShortForm.length());
                    }
                }

            }

            if (!hyphonCase) {
                if (Character.toLowerCase(tokenInAction.getOriginalText().charAt(0)) == charInAction) {
                    finalLongForm.add(tokenInAction);
                    LFindex++;
                } else if (tokenInAction.getPartOfSpeech().equals("IN")) {
                    LFindex++;
                    finalLongForm.add(tokenInAction);
                    stack.push(charInAction);
                } else {
                    violation = true;
                }
            }
        }

        Collections.reverse(potentialLongFormTokens);
        if (stack.empty() && !violation) {
            Collections.reverse(finalLongForm);
            return finalLongForm;
        } else {
            return new ArrayList<>();
        }

    }

    private static List<APToken> R5_allSFOrderedInOneToken(List<APToken> potentialLongFormTokens, String strSF) {
        List<APToken> finalLongForm = new ArrayList<>();
        APToken tokenInAction = potentialLongFormTokens.get(potentialLongFormTokens.size() - 1);

        boolean violation = false;
        int index = -1;
        for (char charInAction : strSF.toCharArray()) {
            int actionIndex = tokenInAction.getOriginalText().toLowerCase().indexOf(Character.toLowerCase(charInAction));
            if (index >= actionIndex || actionIndex < 0) {
                violation = true;
                break;
            } else {
                index = actionIndex;
            }
        }

        if (!violation) {
            finalLongForm.add(tokenInAction);
        }
        return finalLongForm;
    }

    private static List<APToken> R6_multipleSFOrderedInMultipleTokens(List<APToken> potentialLongFormTokens, String strSF) {
        List<APToken> finalLongForm = new ArrayList<>();
        int LFindex = potentialLongFormTokens.size() - 1;
        String runningSF = strSF.toLowerCase();
        boolean violation = false;

        while (LFindex > -1 && !violation && !runningSF.isEmpty()) {
            APToken tokenInAction = potentialLongFormTokens.get(LFindex);
            int cuttingIndex = 0;
            boolean  found = false;
            while (cuttingIndex < runningSF.length() && !found) {
                boolean caseResult = isOrderedNonConcurrentSubstring(tokenInAction.getOriginalText().toLowerCase(), runningSF.substring(cuttingIndex, runningSF.length()).toCharArray());
                if (caseResult) {
                    found = true;
                    finalLongForm.add(tokenInAction);
                    runningSF = runningSF.substring(0,cuttingIndex);
                }
                else
                    cuttingIndex ++;
            }// inner while

            if(!finalLongForm.isEmpty() && !found)
                violation = true;

            LFindex--;

        }
        if (!violation && runningSF.isEmpty()) {
            Collections.reverse(finalLongForm);
            return finalLongForm;
        } else {
            return new ArrayList<>();
        }

    }


    private static boolean isOrderedNonConcurrentSubstring(String target, char[] substring) {
        boolean violation = false;
        int index = -1;

        //in case only one SF is there then it should be the first character of the target string
        if (substring.length == 1)
            return Character.toLowerCase(target.charAt(0)) == Character.toLowerCase(substring[0]);

        for (char charInAction : substring) {
            int actionIndex = target.toLowerCase().indexOf(Character.toLowerCase(charInAction));
            if (index >= actionIndex || actionIndex < 0) {
                violation = true;
                break;
            } else {
                index = actionIndex;
            }
        }
        return !violation;
    }



    public static List<String> getSubtextsFromText(String strInput, char charDelim) {
        List<String> subTexts = new ArrayList<>();
        String strSliding = strInput;

        while (strSliding.indexOf(charDelim) > 0) {
            int index = strSliding.indexOf(charDelim);
            subTexts.add(strSliding.substring(0, index));
            strSliding = strSliding.substring(index + 1, strSliding.length());

        }
        if (!strInput.equals(strSliding) && !strSliding.isEmpty())
            subTexts.add(strSliding);
        return subTexts;
    }


}
