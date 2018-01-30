package au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.database.DynamoDBObject;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.EntityType;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.PhraseType;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
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


    public APSentence(DynamoDBObject dbObject) {
        super(Integer.parseInt(dbObject.getJsonObject().get("id").toString()), dbObject.getJsonObject().get("originalText").toString());
        if (dbObject.getEntityType().equals(EntityType.APSentence)) {
            JSONArray jsonTokens = (JSONArray) dbObject.getJsonObject().get("tokens");
            jsonTokens.forEach(t -> tokens.add(new APToken(new DynamoDBObject((JSONObject) t, EntityType.APToken))));
            docOffset.setLocation(Integer.parseInt(((JSONObject) dbObject.getJsonObject().get("docOffset")).get("x").toString()),
                    Integer.parseInt(((JSONObject) dbObject.getJsonObject().get("docOffset")).get("y").toString()));

            JSONArray jsonAPParseTreeRows = (JSONArray) dbObject.getJsonObject().get("parseTree");
            parseTree = new ArrayList<>();
            jsonAPParseTreeRows.forEach(p -> parseTree.add(new APParseTreeRow(new DynamoDBObject((JSONObject) p, EntityType.APParseTreeRow))));


            JSONArray jsonAPDependencyRelations = (JSONArray) dbObject.getJsonObject().get("dependencyRelations");
            dependencyRelations = new ArrayList<>();
            jsonAPDependencyRelations.forEach(d -> dependencyRelations.add(new APDependencyRelation(new DynamoDBObject((JSONObject) d, EntityType.APDependencyRelation), tokens)));


            JSONArray jsonSfLfLinks = (JSONArray) dbObject.getJsonObject().get("SfLfLink");
            if (jsonSfLfLinks.size() > 0) {
                SfLfLink = new HashMap<>();
                for (Object obj : jsonSfLfLinks) {
                    ((JSONObject) obj).keySet().forEach(k ->
                            {
                                APToken SFToken = tokens.stream().filter(t -> t.getId() == Integer.parseInt(k.toString())).collect(Collectors.toList()).get(0);
                                APToken[] LFTokens = tokenArrayFromTokenIDArray((JSONArray) ((JSONObject) obj).get(k));
                                SfLfLink.put(SFToken, LFTokens);
                            }

                    );

                }
            }


        } else {

        }

    }


    /***
     * This function is called during the fist time hatching process.
     * There is a possibility that whole Linguistic structure is brought back from JSON dump. In that case annotatedTree would be null.
     */
    public void generateParseTree() {
        parseTree = new ArrayList<>();
        BuildParseTree(annotatedTree, 0, new AtomicInteger(0), parseTree);  // Initially parentID = 0 and rootID = 0

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

    @Override
    public JSONObject constructJson() {
        JSONObject returnObject = super.constructJson();

        JSONArray jsonTokens = new JSONArray();
        tokens.forEach(t -> jsonTokens.add(t.constructJson()));
        returnObject.put("tokens", jsonTokens);


        JSONObject jsonPoint = new JSONObject();
        jsonPoint.put("x", docOffset.getX());
        jsonPoint.put("y", docOffset.getY());
        returnObject.put("docOffset", jsonPoint);


        JSONArray jsonAPParseTreeRows = new JSONArray();
        parseTree.forEach(pt -> jsonAPParseTreeRows.add(pt.constructJson()));
        returnObject.put("parseTree", jsonAPParseTreeRows);


        JSONArray jsonAPDependencyRelations = new JSONArray();
        dependencyRelations.forEach(dr -> jsonAPDependencyRelations.add(dr.constructJson()));
        returnObject.put("dependencyRelations", jsonAPDependencyRelations);


        JSONArray jsonSfLfLinks = new JSONArray();
        SfLfLink.forEach((key, value) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key.getId(), tokenIDArrayFromTokenArray(value));
            jsonSfLfLinks.add(jsonObject);
        });
        returnObject.put("SfLfLink", jsonSfLfLinks);
        return returnObject;
    }


    private JSONArray tokenIDArrayFromTokenArray(APToken[] tokens) {
        JSONArray jsonArray = new JSONArray();
        for (int x = 0; x < tokens.length; x++) {
            jsonArray.add(x, tokens[x].getId());
        }
        return jsonArray;
    }

    private APToken[] tokenArrayFromTokenIDArray(JSONArray jsonArray) {

        APToken[] longForm = new APToken[jsonArray.size()];
        int index = 0;
        for (Object obj : jsonArray) {
            longForm[index] = tokens.stream().filter(x -> x.getId() == Integer.parseInt(obj.toString())).collect(Collectors.toList()).get(0);
            index++;
        }
        return longForm;

    }

    /***
     * This function is written to consume parse tree nodes. It performs two operations.
     * 1: It finds all phrases/chunks using stored list of parse tree nodes. This can be done after resurrecting document from S3.
     * 2. When embedding is required (Information and links about embedded phrases) a true flag must be passed in second param.
     * @param phraseType
     * @param checkEmbedding
     * @return
     */
    public List<APPhrase> getPhrases(PhraseType phraseType , boolean checkEmbedding) {
        String phraseIdentifier;

        switch (phraseType) {
            case NOUN:
                phraseIdentifier = "NP";
                break;
            case VERB:
                phraseIdentifier = "VP";
                break;
            default:
                phraseIdentifier = "S";

        }
        List<APPhrase> phrases = new ArrayList<>();
        Stack<Integer> childStack = new Stack();
        //Collect all rows/nodes are root of phrase subtree
        List<APParseTreeRow> phraseNodes = parseTree.stream().
                filter(x -> x.getOriginalText().equals(phraseIdentifier))
                .collect(Collectors.toList());

        for (APParseTreeRow NPNode : phraseNodes) {
            childStack.push(NPNode.getId());
            List<Integer> lstOffsets = new ArrayList<>();
            getChildrenOffsets(childStack, lstOffsets);
            if (lstOffsets.size() > 0) {
                APPhrase apPhrase = new APPhrase();
                apPhrase.setTokens(tokens.stream().filter(t -> lstOffsets.contains(t.getSentOffset().x)).collect(Collectors.toList()));
                apPhrase.getTokens().sort(Comparator.comparing(APToken::beginPosition));
                apPhrase.setPhraseType(phraseType);
                phrases.add(apPhrase);
            }
        }
        if(checkEmbedding)
            inspectPhraseEmbedding(phrases);
        return phrases;

    }

    /***
     * This is a n^2 function to iterate over a list of phrases to find and mark embedded/nested ones.
     * @param phrases
     */
    private void inspectPhraseEmbedding(List<APPhrase> phrases) {
        for (APPhrase aPhrase : phrases) {
            for (APPhrase bPhrase : phrases) {
                if (aPhrase.getId() != bPhrase.getId() &&
                        aPhrase.isSubSetOff(bPhrase)) {
                    aPhrase.setEmbedded(true);
                    aPhrase.getParentPhraseIds().add(bPhrase.getId());
                }
            }
        }
    }

    private void getChildrenOffsets(Stack childStack, List<Integer> lstOffsets) {
        if (childStack.isEmpty())
            return;
        Integer currentId = (Integer) childStack.pop();
        List<APParseTreeRow> children = parseTree.stream().filter(x -> x.getParentID() == currentId).collect(Collectors.toList());
        for (APParseTreeRow childNode : children) {
            if (childNode.isLeafNode()) {
                lstOffsets.add(childNode.getOffsetBegin());
            } else {
                childStack.push(childNode.getId());
            }
        }
        getChildrenOffsets(childStack, lstOffsets);

    }

    @Override
    public String toString()
    {
        return String.format("[%d:%d] %s", docOffset.x, docOffset.y, getOriginalText());
    }



    ////////////////////////////////////////////////////////////////////////////////
    // CR: Support Properties and methods. Can be moved later
    ///////////////////////////////////////////////////////////////////////////////

    @Getter
    private Map<Integer, APToken> indexedTokens;
    @Getter
    private Map<String, List<Integer>> inverseTokenPositions;
    @Getter
    private Map<Integer, APToken> verbPositions;
    @Getter
    private Map<Integer, APToken> punctuation;
    @Getter
    private Map<Integer, APToken> conjunctions;
    @Getter
    private Map<Integer, List<Integer>> subSentences;
    @Getter
    private List<Integer> startBracketPositions;
    @Getter
    private List<Integer> endBracketPositions;

    public void conceptRecognizerHatch(){
        this.indexedTokens = new LinkedHashMap<>();
        this.inverseTokenPositions = new LinkedHashMap<>();
        this.verbPositions = new LinkedHashMap<>();
        this.punctuation = new LinkedHashMap<>();
        this.conjunctions = new LinkedHashMap<>();
        this.subSentences = new LinkedHashMap<>();
        this.startBracketPositions = new ArrayList<>();
        this.endBracketPositions = new ArrayList<>();


        tokens.sort(Comparator.comparing(t->t.getSentOffset().x));
        for (int index = 0; index<tokens.size(); index ++){
            conceptEntitiesHatch(index, tokens.get(index));
        }



    }

    private void conceptEntitiesHatch(int index, APToken positionedToken) {
        this.indexedTokens.put(index, positionedToken);
        if (positionedToken.getPartOfSpeech() != null) {
            if (positionedToken.getPartOfSpeech().startsWith(TAConstants.POS_VB)) {
                if (!positionedToken.getPartOfSpeech().equalsIgnoreCase(TAConstants.POS_VBN)
                        || positionedToken.getPartOfSpeech().equalsIgnoreCase(TAConstants.POS_VBG)) {
                    verbPositions.put(index, positionedToken);
                }
            }
            if (positionedToken.getPartOfSpeech().startsWith(TAConstants.POS_CC)) {
                conjunctions.put(index, positionedToken);
            }
            if (positionedToken.getPartOfSpeech().equalsIgnoreCase(TAConstants.POS_LBR)) {
                startBracketPositions.add(index);
            }
            if (positionedToken.getPartOfSpeech().equalsIgnoreCase(TAConstants.POS_RBR)) {
                endBracketPositions.add(index);
            }
            if (positionedToken.getPartOfSpeech().length() == 1) {
                punctuation.put(index, positionedToken);
            }
        }

        //CR: Merged functions from TASentence. This is the making of sentence and will be done only when CR is initialized.
        List<Integer> list = this.inverseTokenPositions.containsKey(positionedToken.getOriginalText()) ? this.inverseTokenPositions.get(positionedToken.getOriginalText()) : new ArrayList<>();
        list.add(index);
        this.inverseTokenPositions.put(positionedToken.getOriginalText(), list);
    }




}


