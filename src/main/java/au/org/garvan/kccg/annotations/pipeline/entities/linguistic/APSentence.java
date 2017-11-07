package au.org.garvan.kccg.annotations.pipeline.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.enums.EntityType;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
import jdk.nashorn.internal.objects.annotations.Property;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 7/7/17.
 */
public class APSentence extends LinguisticEntity {


    @Getter
    @Setter
    private List<APToken> tokens = new ArrayList<>();


    @Getter
    @Setter
    private Point docOffset = new Point();


    @Getter
    @Setter
    private List<APParseTreeRow> parseTree;


    @Getter
    @Setter
    private List<APDependencyRelation> dependencyRelations;


    @Getter
    @Setter
    private Tree annotatedTree;


    @Getter
    @Setter
    private SemanticGraph semanticGraph;


    @Getter
    @Setter
    private Map<APToken, APToken[]> SfLfLink;


    public APSentence(int id, String text) {
        super(id, text);
    }

    public APSentence(String text) {
        super(text);
    }


    public void generateParseTree() {
        parseTree = new ArrayList<>();
        BuildParseTree(annotatedTree, 0, new AtomicInteger(0), parseTree);  // Initially parentID = 0 and rootID = 0

    }


    public void generateDependencies() {

        dependencyRelations = new ArrayList<>();
        IndexedWord root = semanticGraph.getFirstRoot();
        APDependencyRelation rootRelation = new APDependencyRelation();
        rootRelation.setRelation("root");
        rootRelation.setGovernor(new APToken(0, "ROOT", "", ""));


        rootRelation.setDependent(tokens.stream().filter(t -> t.beginPosition() == root.beginPosition()).findFirst().get());
        dependencyRelations.add(rootRelation);

        for (SemanticGraphEdge sge : semanticGraph.edgeListSorted()) {
            APDependencyRelation depRelation = new APDependencyRelation();

            depRelation.setRelation(sge.getRelation().getShortName());
            depRelation.setGovernor(tokens.stream().filter(t -> t.beginPosition() == sge.getGovernor().beginPosition()).findFirst().get());
            depRelation.setDependent(tokens.stream().filter(t -> t.beginPosition() == sge.getDependent().beginPosition()).findFirst().get());
            dependencyRelations.add(depRelation);

        }

    }


    // Parse Tree generation
    // A sample parse tree visualization is given below for reference. Leaf
    // nodes contain originalText and non-terminal nodes contain POS tags.
    //
    //						Root
    //						  |
    //						  S
    //					    /   \
    //					  VP     NP
    //					 / \
    //				   DT  VBZ
    //				 /    /
    //			   this  is
    //
    // Recursively build a parse tree with pre-order traversal (root-left-right)

    private void BuildParseTree(Tree node, int parentId, AtomicInteger incrementalNodeId, List<APParseTreeRow> parseTree) {
        if (node == null)
            return;

        if (node.isPreTerminal())  // recursive base case
        {
            // Match the parse tree row to a token. Dictionary<string, offsetBegin> can be used here.
            int offset = ((CoreLabel) node.getChild(0).label()).beginPosition();
            String leafText = node.getChild(0).value() + "/" + node.value();
            parseTree.add(new APParseTreeRow(incrementalNodeId.incrementAndGet(), parentId, leafText, true, offset));
            return;
        }

        int currentNodeId = incrementalNodeId.incrementAndGet();
        parseTree.add(new APParseTreeRow(currentNodeId, parentId, node.value(), false, -1));

        for (Tree child : node.children()) {
            BuildParseTree(child, currentNodeId, incrementalNodeId, parseTree);
        }
    }

    public List<APToken> getSortedTokens() {

        return tokens.stream().sorted(Comparator.comparing(t -> t.getSentOffset().x)).collect(Collectors.toList());
    }

    public APSentence(DynamoDBObject dbObject){
        if(dbObject.getEntityType().equals(EntityType.APSentence))
        {

        }
        else{

        }

    }

    @Override
    public JSONObject constructJson(){
        JSONObject returnObject = super.constructJson();

        JSONArray jsonTokens = new JSONArray();
        tokens.forEach(t-> jsonTokens.add(t.constructJson()));
        returnObject.put("tokens",jsonTokens);


        JSONObject jsonPoint = new JSONObject();
        jsonPoint.put("x",docOffset.getX());
        jsonPoint.put("y",docOffset.getY());
        returnObject.put("docOffset",jsonPoint);


        JSONArray jsonAPParseTreeRows = new JSONArray();
        parseTree.forEach(pt-> jsonAPParseTreeRows.add(pt.constructJson()));
        returnObject.put("parseTree",jsonAPParseTreeRows);


        JSONArray jsonAPDependencyRelations = new JSONArray();
        dependencyRelations.forEach(dr-> jsonAPDependencyRelations.add(dr.constructJson()));
        returnObject.put("dependencyRelations",jsonAPDependencyRelations);


        JSONArray jsonSfLfLinks = new JSONArray();
        SfLfLink.forEach((key,value) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key.getId(), tokenIDArrayFromTokenArray(value));
            jsonSfLfLinks.add(jsonObject);
        });
        returnObject.put("SfLfLink",jsonSfLfLinks);
        return returnObject;
    }



    private JSONArray tokenIDArrayFromTokenArray(APToken[] tokens){
        JSONArray jsonArray = new JSONArray();
        for(int x = 0; x<tokens.length; x++){
            jsonArray.add(x,tokens[x].getId());
        }
        return jsonArray;
    }



}


