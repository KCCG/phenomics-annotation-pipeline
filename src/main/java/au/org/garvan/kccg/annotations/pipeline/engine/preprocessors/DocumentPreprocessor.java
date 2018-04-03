package au.org.garvan.kccg.annotations.pipeline.engine.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.DiseaseHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.PhenotypeHandler;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APPhrase;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.profiles.ProcessingProfile;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.GenesHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.CoreNLPManager;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ahmed on 1/8/17.
 */
public class DocumentPreprocessor {
    private static final Logger slf4jLogger = LoggerFactory.getLogger(DocumentPreprocessor.class);
    private static List<Character> spaceRiders = Arrays.asList('(',')',';',':','_','[',']','{','}','/' , '"', '-');


    @Getter
    private static  GenesHandler  HGNCGeneHandler;

    @Getter
    private static PhenotypeHandler phenotypeHandler;

    @Getter
    private static DiseaseHandler mondoHandler;



    //    private static NormalizationHandler LVGNormalizationHandler;

    static {

        HGNCGeneHandler = new GenesHandler("genes.txt");
        HGNCGeneHandler.loadGenes();

        slf4jLogger.info(String.format("Phenotype Handler init() called."));
        phenotypeHandler = new PhenotypeHandler();



        mondoHandler = new DiseaseHandler("mondo.json");
        mondoHandler.readFile();


        if(!CoreNLPManager.isInitialized())
            CoreNLPManager.init();

    }

    public static void main(String[] args) {

    }

    public static void init(){

    }

    public static void preprocessDocument(APDocument doc, Integer articleId) {
        ProcessingProfile docProfile = doc.getProcessingProfile();
        doc.setCleanedText(addSpaceAfterFullStop(doc.getOriginalText()));

        String textToBeProcessed = doc.getCleanedText().replace("-"," ");
        textToBeProcessed = textToBeProcessed.replace("‑"," ");

        List<Integer> hyphenIndex= getIndexOfHyphen(doc.getCleanedText());

        //Using cleaned text after prepossessing is done
        Annotation docAnnotation = CoreNLPManager.annotateDocText(textToBeProcessed);
        List<CoreMap> sentencesMap = docAnnotation.get(CoreAnnotations.SentencesAnnotation.class);

        slf4jLogger.info(String.format("Hatching Article ID: %d Sentence:%d ", articleId, sentencesMap.size()));
        int sentID=1;
        for (CoreMap sentence : sentencesMap) {

            APSentence sent = new APSentence(sentID,sentence.toString());
            sent.setDocOffset(new Point(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)));
            doc.getSentences().add(sent);
            sentID++;
        }

        for (APSentence sent : doc.getSentences())
        {
            List<Character> punctuations = Arrays.asList(',','.',':',';','\'');
            Annotation sentAnnotation = CoreNLPManager.annotateSentText(sent.getOriginalText());
            List<CoreMap> localSentenceMap = sentAnnotation.get(CoreAnnotations.SentencesAnnotation.class);

            if (localSentenceMap.size()>1)
                System.out.println(String.format("More than one sentence splits. Sent entityId:%d",sent.getId()));
            CoreMap aSent = localSentenceMap.get(0);

            sent.setAnnotatedTree(aSent.get(TreeCoreAnnotations.TreeAnnotation.class));
            sent.setSemanticGraph(aSent.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class));

            int id = 1;
            for (CoreLabel token : aSent.get(CoreAnnotations.TokensAnnotation.class)) {
                String text = token.originalText();
                APToken tok = new APToken(id, text, token.get(CoreAnnotations.PartOfSpeechAnnotation.class).toString(), token.lemma());
                tok.setSentOffset(new Point(token.beginPosition(), token.endPosition()));
                tok.setPunctuation(punctuations.contains(text.charAt(text.length()-1)));
                //Hyphen is replace with space and this is a marker for such tokens
                tok.setBeforeHyphen(hyphenIndex.contains(sent.getDocOffset().x+tok.getSentOffset().y));
                tok.setAfterHyphen(hyphenIndex.contains(sent.getDocOffset().x+tok.getSentOffset().x-1));

                if(docProfile.getAnnotationRequests().contains(AnnotationType.GENE)) {
                    APGene geneCheck = HGNCGeneHandler.getGene(tok.getOriginalText());
                    if (geneCheck != null)
                        tok.getLexicalEntityList().add(geneCheck);
                }
                sent.getTokens().add(tok);
                id++;
            }
//            if(docProfile.isProcessDependencies())
//                sent.generateDependencies();
//            if(docProfile.isProcessParseTree())
//                sent.generateParseTree();

        }
        slf4jLogger.info(String.format("Hatching Article ID: %s Linguistic is done. Starting Annotations. ", articleId));

        if(docProfile.getAnnotationRequests().contains(AnnotationType.PHENOTYPE))
        {
            phenotypeHandler.processAndUpdateDocument(doc);
        }
        if(docProfile.getAnnotationRequests().contains(AnnotationType.DISEASE))
        {
            mondoHandler.processAndUpdateDocument(doc, articleId);
        }
        slf4jLogger.info(String.format("Hatching Article ID: %s Phenotype Annotation is done. ", articleId));


    }




    private static List<Integer> getIndexOfHyphen(String input){
        List<Integer> indices = new ArrayList<>();
        for(int index=0; index<input.length(); index++){
            if(input.charAt(index)=='-' || input.charAt(index)=='‑')
                indices.add(index);
        }
        return indices;
    }

    /***
     * Wrote to eliminate dependancy of two pipelines but it messes up with offsets and no improvement in speed.
     * @param doc
     * @param articleId
     */

    public static void preprocessDocumentFast(APDocument doc, Integer articleId) {

        ProcessingProfile docProfile = doc.getProcessingProfile();
        cleanDocumentManual(doc);
        //Using cleaned text after prepossessing is done

        slf4jLogger.info(String.format("Starting NLP Pipeline for Article ID: %d ", articleId));
        Annotation smartDocAnnotation = CoreNLPManager.annotateSentText(doc.getCleanedText());
        List<CoreMap> smartSentencesMap = smartDocAnnotation.get(CoreAnnotations.SentencesAnnotation.class);
        List<Character> punctuations = Arrays.asList(',','.',':',';','\'');

        slf4jLogger.info(String.format("Hatching Article ID: %d Sentence:%d ", articleId, smartSentencesMap.size()));
        int sentID=1;

        for (CoreMap sentence : smartSentencesMap) {

            APSentence apSentence = new APSentence(sentID,sentence.toString());
            apSentence.setDocOffset(new Point(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)));

            apSentence.setAnnotatedTree(sentence.get(TreeCoreAnnotations.TreeAnnotation.class));
            apSentence.setSemanticGraph(sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class));

            int tokenId = 1;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String text = token.originalText();
                APToken tok = new APToken(tokenId, text, token.get(CoreAnnotations.PartOfSpeechAnnotation.class).toString(), token.lemma());
                tok.setSentOffset(new Point(token.beginPosition(), token.endPosition()));
                tok.setPunctuation(punctuations.contains(text.charAt(text.length()-1)));
                if(docProfile.getAnnotationRequests().contains(AnnotationType.GENE)) {
                    APGene geneCheck = HGNCGeneHandler.getGene(tok.getOriginalText());
                    if (geneCheck != null)
                        tok.getLexicalEntityList().add(geneCheck);
                }
//               String normalizedText = LVGNormalizationHandler.getNormalizedText(Common.getPunctuationLessText(tok));
//                if (normalizedText!=null)
//                    tok.setNormalizedText(normalizedText);

                apSentence.getTokens().add(tok);
                tokenId++;

            }

//            ShortFormExtractor.markShortForms(sent.getTokens());
//            LongFormMarker.markLongForms(sent);

            if(docProfile.isProcessDependencies())
                apSentence.generateDependencies();
            if(docProfile.isProcessParseTree())
                apSentence.generateParseTree();



            doc.getSentences().add(apSentence);
            sentID++;
        }

        slf4jLogger.info(String.format("Hatching Article ID: %s Linguistic is done. Starting Annotations. ", articleId));

        if(docProfile.getAnnotationRequests().contains(AnnotationType.PHENOTYPE))
        {
            phenotypeHandler.processAndUpdateDocument(doc);
            slf4jLogger.info(String.format("Hatching Article ID: %s Phenotype Annotation is done. ", articleId));
        }


    }


    public static APPhrase preprocessPhrase(String text){
        APPhrase tempPhrase = new APPhrase();
        List<APToken> lstTokens = new ArrayList<>();
        Annotation phraseAnnotation = CoreNLPManager.annotatePhraseText(addSpaceAfterFullStop(text));


        List<CoreMap> localSentenceMap = phraseAnnotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (localSentenceMap.size()>0) {
            CoreMap aSent = localSentenceMap.get(0);
            int id = 1;
            for (CoreLabel token : aSent.get(CoreAnnotations.TokensAnnotation.class)) {
                String tokenText = token.originalText();
                APToken tok = new APToken(id, tokenText, token.get(CoreAnnotations.PartOfSpeechAnnotation.class).toString(), token.lemma());
                tok.setSentOffset(new Point(token.beginPosition(), token.endPosition()));
                lstTokens.add(tok);
            }
        }
        tempPhrase.setTokens(lstTokens);
        return tempPhrase;

    }
    private static String addSpaceAfterFullStop(String input){

        String modified;
        Pattern p = Pattern.compile("\\.([A-Z])");
        Matcher m = p.matcher(input);
        modified = m.replaceAll(". $1");

//
//        String modified2;
//        Pattern p2 = Pattern.compile("\\. ");
//        Matcher m2= p2.matcher(modified);
//        modified2 = m2.replaceAll(" . ");


        return modified;
    }



    public static void cleanDocumentManual(APDocument doc){
        Map<Integer, Integer> offSetMap = new HashMap<>();
        String text =  doc.getOriginalText().replace("-"," ");
        char spaceChar = ' ';
        char dotChar = '.';
        char commaChar = ',';
        char [] inPut= text.toCharArray();
        char [] outPut = new char[text.length()*2];

        Integer oIndex = 0;
        Integer oPrevIndex= 0;
        Integer eod= inPut.length-1;
        for(int x = 0; x<inPut.length; x++){

            //First goes as it is
            if(x<1){
                outPut[oIndex] = inPut[x];
                offSetMap.put(oIndex,x);
                oPrevIndex = oIndex;
                oIndex ++;
            }

            else {
                boolean eos = false;
                //Check if is is end of sentence.
                if (inPut[x] == dotChar) {
                    if (x < eod) {
                        eos = inPut[x + 1] == spaceChar || Character.isUpperCase(inPut[x + 1]);
                    }
                    if (x == eod)
                        eos = true;
                }

                boolean processComma= false;
                if(inPut[x]==commaChar)
                {
                    if (x < eod) {
                        processComma = !Character.isDigit(inPut[x + 1]);
                    }
                    if (x == eod)
                        processComma = true;

                }

                if (spaceRiders.contains(inPut[x]) || eos || processComma) {
                    if (outPut[oPrevIndex] != spaceChar) {
                        outPut[oIndex] = spaceChar;
                        offSetMap.put(oIndex, x);
                        oPrevIndex = oIndex;
                        oIndex++;
                    }
                    outPut[oIndex] = inPut[x];
                    offSetMap.put(oIndex, x);
                    oPrevIndex = oIndex;
                    oIndex++;

                    if (x < eod && inPut[x + 1] != spaceChar) {
                        outPut[oIndex] = spaceChar;
                        offSetMap.put(oIndex, x);
                        oPrevIndex = oIndex;
                        oIndex++;
                    }

                } else {
                    outPut[oIndex] = inPut[x];
                    offSetMap.put(oIndex, x);
                    oPrevIndex = oIndex;
                    oIndex++;
                }


            }//Not first char
        }//Loops




        StringBuilder stringBuilder = new StringBuilder();
        for(int y = 0; y<oIndex; y++){
            stringBuilder.append(outPut[y]);
        }

        doc.setCleanedText(stringBuilder.toString());
        //doc.setOffSetMap(offSetMap);


    }


}
