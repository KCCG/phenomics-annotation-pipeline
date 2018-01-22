package au.org.garvan.kccg.annotations.pipeline.engine.preprocessors;

import au.org.garvan.kccg.annotations.pipeline.engine.lexicons.PhenotypeHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Common;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.lexicons.GenesHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.lexicons.NormalizationHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.CoreNLPManager;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import lombok.Getter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ahmed on 1/8/17.
 */
public class DocumentPreprocessor {

    @Getter
    private static  GenesHandler  HGNCGeneHandler;

    @Getter
    private static PhenotypeHandler TempPhenotypeHandler;
//    private static NormalizationHandler LVGNormalizationHandler;
    static {

        HGNCGeneHandler = new GenesHandler("genes.txt");
        HGNCGeneHandler.loadGenes();

        TempPhenotypeHandler = new PhenotypeHandler("hpo.txt");
        TempPhenotypeHandler.loadPhenotypes();

//        LVGNormalizationHandler = new NormalizationHandler("lvg_normalizations.txt");
//        LVGNormalizationHandler.loadLVGNormalizedList();


        if(!CoreNLPManager.isInitialized())
            CoreNLPManager.init();

    }

    public static void main(String[] args) {

    }

    public static void init(){

    }

    public static void preprocessDocument(APDocument doc) {
        doc.setCleanedText(addSpaceAfterFullStop(doc.getOriginalText()));

        //Using cleaned text after prepossessing is done
        Annotation docAnnotation = CoreNLPManager.annotateDocText(doc.getCleanedText());
        List<CoreMap> sentencesMap = docAnnotation.get(CoreAnnotations.SentencesAnnotation.class);


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
                APGene geneCheck = HGNCGeneHandler.getGene(Common.getTrimmedText(tok));
                if(geneCheck!=null)
                    tok.getLexicalEntityList().add(geneCheck);

//                String normalizedText = LVGNormalizationHandler.getNormalizedText(Common.getTrimmedText(tok));
//                if (normalizedText!=null)
//                    tok.setNormalizedText(normalizedText);

                sent.getTokens().add(tok);
                id++;

            }
            ShortFormExtractor.markShortForms(sent.getTokens());
            LongFormMarker.markLongForms(sent);
            sent.generateDependencies();
            sent.generateParseTree();

        }

    }


    private static String addSpaceAfterFullStop(String input){

        String modified;
        Pattern p = Pattern.compile("\\.([A-Z])");
        Matcher m = p.matcher(input);
        modified = m.replaceAll(". $1");
        return modified;
    }


}
