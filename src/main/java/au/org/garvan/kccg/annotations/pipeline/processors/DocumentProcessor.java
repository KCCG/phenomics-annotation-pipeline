package au.org.garvan.kccg.annotations.pipeline.processors;

import au.org.garvan.kccg.annotations.pipeline.Utils.APFileWriter;
import au.org.garvan.kccg.annotations.pipeline.connectors.SolrConnector;
import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APDocument;
import au.org.garvan.kccg.annotations.pipeline.linguisticentites.APSentence;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by ahmed on 7/7/17.
 */
public class DocumentProcessor {


    private static SolrConnector instSolrConnector = new SolrConnector();


    public static void main(String[] args) throws IOException {

        CoreNLPHanlder.init();

        APDocument doc = processDocument("28286899");

        List<String> writableLines = new ArrayList<>();
        writableLines.add("Abstract: " + doc.getOriginalText());

        for (APSentence sent: doc.getSentences())
        {
            sent.generateParseTree();
            sent.generateDependencies();
            writableLines.add( String.format(""));
            writableLines.add( String.format("Sent ID: %d Sent Text: %s\n", sent.getId(), sent.getOriginalText()));
            String pTree= sent.getAnnotatedTree().toString();
            pTree = pTree.replace('(','[');
            pTree = pTree.replace(')',']');
            writableLines.add( String.format("Parse Tree: %s\n", pTree));
            writableLines.add( String.format("Dependencies: %d\n", sent.getDependencyRelations().size()));
            writableLines.addAll(sent.getDependencyRelations().stream().map(x->x.toString()).collect(Collectors.toList()));

        }

        APFileWriter.writeSmallTextFile(writableLines, Integer.toString(doc.getId()));



    }


    public static APDocument processDocument(String PMID) throws IOException {
        CoreNLPHanlder.init();
        APDocument doc = instSolrConnector.getDocument(PMID);

        doc.hatch();
        return doc;


    }


    public static List<APDocument> processDocuments(LocalDate date) throws IOException {
        CoreNLPHanlder.init();
         return instSolrConnector.getDocuments(date);

    }


}
