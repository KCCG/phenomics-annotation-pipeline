package au.org.garvan.kccg.annotations.pipeline.userinterfaces;

/**
 * Created by ahmed on 26/7/17.
 */

import au.org.garvan.kccg.annotations.pipeline.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.processors.DocumentProcessor;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BaseUI extends Application {

    private final int SENTENCES_INDEX = 0;
    private final int TOKENS_INDEX = 1;
    private final int PARSE_TREE_INDEX = 2;
    private final int DEPENDENCIES_INDEX = 3;
    private APDocument currentDoc = null;
    private GridPane root;
    private GridPane GPSentences;
    private List<APDocument> allDocs;
    private int currentDocIndex;
    private int currentSentId;
    private int totalDocs;
    private double height;
    private double width;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        width = Screen.getPrimary().getVisualBounds().getWidth();
        height = Screen.getPrimary().getVisualBounds().getHeight();


        root = new GridPane();
        GPSentences = new GridPane();

        root.setAlignment(Pos.TOP_LEFT);
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(25, 25, 25, 25));
        Button btnProcess = new Button("Fetch Articles");
        btnProcess.setOnAction(e -> {
            try {
                fetchDocuments();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Label lblDate = new Label("Date:");
        TextField txtDate = new TextField("01/10/2017");
        txtDate.setId("txtDate");

        Label lblTotalDocs = new Label("Total Articles:");
        Text txtTotalDocs = new Text("0");
        txtTotalDocs.setId("txtTotalDocs");

        Label lblCurrentDocIndex = new Label("Current Article:");
        Text txtCurrentDocIndex = new Text("0");
        txtCurrentDocIndex.setId("txtCurrentDocsIndex");


        Button btnPrevious = new Button("<");
        btnPrevious.setOnAction(e -> prevDocument());

        Button btnNext = new Button(">");
        btnNext.setOnAction(e -> nextDocument());


        Button btnGoToIndex = new Button("Go To:");
        btnGoToIndex.setOnAction(e -> goToDocument());
        TextField txtGoToIndex = new TextField("0");
        txtGoToIndex.setId("txtGoToIndex");


        Button btnProcessPMID = new Button("Fetch Article");
        btnProcessPMID.setOnAction(e -> {
            try {
                fetchDocumentBySearch();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Label lblPMID = new Label("Search by PMID/Query:");
        TextField txtPMID = new TextField("");
        txtPMID.setId("txtPMID");

        root.add(lblDate, 1, 1);
        root.add(txtDate, 2, 1);
        root.add(btnProcess, 3, 1);


        root.add(lblTotalDocs, 4, 1);
        root.add(txtTotalDocs, 5, 1);
        root.add(lblCurrentDocIndex, 6, 1);
        root.add(txtCurrentDocIndex, 7, 1);
        root.add(btnPrevious, 8, 1);
        root.add(btnNext, 9, 1);
        root.add(btnGoToIndex, 10, 1);
        root.add(txtGoToIndex, 11, 1);

        root.add(lblPMID, 1, 2);
        root.add(txtPMID, 2, 2);
        root.add(btnProcessPMID, 3, 2);


        VBox verticleBox = new VBox();
        verticleBox.getChildren().addAll(root, GPSentences);

        Scene scene = new Scene(verticleBox, width, height);
        primaryStage.setTitle("Document Analyzer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void goToDocument() {

        int goToID = Integer.parseInt(((TextField) root.lookup("#txtGoToIndex")).getText());
        if (goToID >= 0 && goToID < totalDocs) {
            currentDocIndex = goToID;
            try {
                processDocument();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void nextDocument() {
        if (currentDocIndex < totalDocs - 1) {
            currentDocIndex++;
            try {
                processDocument();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void prevDocument() {

        if (currentDocIndex > 0) {
            currentDocIndex--;
            try {
                processDocument();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void fetchDocuments() throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
        String strDate = ((TextField) root.lookup("#txtDate")).getText();

        LocalDate localDate = LocalDate.parse(strDate, formatter);
        allDocs = DocumentProcessor.processDocuments(localDate);
        allDocs.sort(Comparator.comparing(APDocument::getId));
        currentDocIndex = 0;
        totalDocs = allDocs.size();
        processDocument();


    }

    private void fetchDocumentBySearch() throws IOException {

        allDocs = new ArrayList<>();
        String strSearch = ((TextField) root.lookup("#txtPMID")).getText();

        try {
            Integer PMID = Integer.parseInt(strSearch);
            allDocs.add(DocumentProcessor.processDocument(strSearch));
        } catch (NumberFormatException e) {
            allDocs = DocumentProcessor.processDocuments(strSearch);
        }

        currentDocIndex = 0;
        totalDocs = allDocs.size();
        processDocument();


    }

    private void processDocument() throws IOException {
        if (totalDocs > 0) {
            currentDoc = allDocs.get(currentDocIndex);
            textUpdate("txtTotalDocs", Integer.toString(totalDocs));
            textUpdate("txtCurrentDocsIndex", Integer.toString(currentDocIndex));

            clearGPSentences(SENTENCES_INDEX, true);

            List<LinguisticCellContent> list = new ArrayList<>();
            if (currentDoc != null) {

                if (currentDoc.getSentences().size() == 0)
                    currentDoc.hatch();
                for (APSentence sent : currentDoc.getSentences()) {
                    list.add(new LinguisticCellContent(sent.getId(), sent.getOriginalText()));
                }

                ObservableList<LinguisticCellContent> ObList = FXCollections.observableList(list);
                ListView<LinguisticCellContent> lv = new ListView<>(ObList);
                lv.setCellFactory(new Callback<ListView<LinguisticCellContent>, ListCell<LinguisticCellContent>>() {
                    @Override
                    public ListCell<LinguisticCellContent> call(ListView<LinguisticCellContent> param) {
                        return new XCell();
                    }
                });
                lv.setMinWidth(width);
                lv.setMaxHeight(height * .4);
                GPSentences.addRow(SENTENCES_INDEX, lv);
            }
        }


    }


    private void fillCurrentDocument() throws IOException {

        String PMID = ((TextField) root.lookup("#txtDate")).getText();
        currentDoc = DocumentProcessor.processDocument(PMID);

    }

    private void processSentence(int id) {

        APSentence activeSent = currentDoc.getSentenceWithID(id);
        FlowPane tokenPane = new FlowPane();


        //Flatten map structure to collect IDs of long forms; This is not meant to link anything but just for highlighting
        List<Integer> longFormTokenIndices = activeSent.getSfLfLink().values().stream().filter(x -> x.length > 0).flatMap(Arrays::stream).map(x -> x.getId()).collect(Collectors.toList());

        activeSent.getTokens().stream().forEach(tok ->
                {
                    Button btnToken = new Button();
                    btnToken.setId(Integer.toString(tok.getId()));

                    btnToken.setText(tok.getOriginalText());

                    String buttonStyle = "";

                    if (tok.isShortForm()) {
                        buttonStyle = buttonStyle + "-fx-base: #b6e7c9;";

                    } else if (longFormTokenIndices.contains(tok.getId())) {
                        buttonStyle = buttonStyle + "-fx-base: #e6d7f2;";
                    }

                    if (!tok.getLexicalEntityList().isEmpty()) {
                        buttonStyle = buttonStyle + "-fx-text-fill: #800080;";

                    }

                    if (!tok.getNormalizedText().isEmpty()) {
                        buttonStyle = buttonStyle + "-fx-underline: true;";
                        btnToken.setTooltip(new Tooltip(String.format("%s:%s -> %s", tok.getLemma(), tok.getPartOfSpeech(), tok.getNormalizedText() )));
                    }
                    else
                    {                    btnToken.setTooltip(new Tooltip(String.format("%s:%s", tok.getLemma(), tok.getPartOfSpeech())));


                    }
                        btnToken.setStyle(buttonStyle);


                    btnToken.setOnAction(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent e) {
                            updateDependencies(((Button) e.getSource()).getId());
                        }
                    });
                    tokenPane.getChildren().add(btnToken);

                }
        );

        activeSent.generateDependencies();
        activeSent.generateParseTree();

        ListView<String> dependencyList = new ListView<String>();
        ObservableList<String> items = FXCollections.observableArrayList(activeSent.getDependencyRelations().stream().map(d -> d.toString()).collect(Collectors.toList()));
        dependencyList.setItems(items);
        dependencyList.setPrefWidth(300);

        Text parseTree = new Text();
        parseTree.setText(activeSent.getAnnotatedTree().toString());
        parseTree.setWrappingWidth(width * .98);

        clearGPSentences(TOKENS_INDEX, true);
        GPSentences.addRow(TOKENS_INDEX, tokenPane);
        GPSentences.addRow(PARSE_TREE_INDEX, parseTree);
        GPSentences.addRow(DEPENDENCIES_INDEX, dependencyList);
        GPSentences.setVgap(5);
        GPSentences.setMargin(tokenPane, new Insets(10, 5, 10, 5));

    }


    private void clearGPSentences(int index, boolean isAll) {
        if (isAll) {
            while (GPSentences.getChildren().size() > index) {
                GPSentences.getChildren().remove(index);
            }
        } else {
            GPSentences.getChildren().remove(index);
        }

    }

    private void updateDependencies(String textId) {

        System.out.println("Update dependencies called with token text: " + textId);
        int id = Integer.parseInt(textId);
        APSentence sent = currentDoc.getSentenceWithID(currentSentId);
        ListView<String> dependencyList = new ListView<String>();
        ObservableList<String> items = FXCollections.observableArrayList(sent.getDependencyRelations()
                .stream()
                .filter(g -> (g.getDependent().getId() == id || g.getGovernor().getId() == id))
                .map(d -> d.toString()).collect(Collectors.toList()));
        dependencyList.setItems(items);


        //Point: Get token and see if Lexical Entities are there
        APToken tok = sent.getTokens().stream().filter(x -> x.getId() == id).collect(Collectors.toList()).get(0);

        GPSentences.getChildren().remove(DEPENDENCIES_INDEX);

        if (!tok.getLexicalEntityList().isEmpty()) {
            ListView<String> entityList = new ListView<String>();
            entityList.setItems(FXCollections.observableArrayList(((APGene) tok.getLexicalEntityList().get(0)).stringList()));
            HBox depLexHolder = new HBox();
            dependencyList.setPrefWidth(450);
            entityList.setPrefWidth(450);
            depLexHolder.getChildren().addAll(dependencyList, entityList);
            GPSentences.addRow(DEPENDENCIES_INDEX, depLexHolder);
        } else {
            GPSentences.addRow(DEPENDENCIES_INDEX, dependencyList);
        }

    }


    public void textUpdate(String titleId, String value) {
        Object obj = root.lookup("#" + titleId);


        if (obj instanceof Label) {
            ((Label) obj).setText(value);

        } else if (obj instanceof Text) {
            ((Text) obj).setText(value);
        } else if (obj instanceof TextField) {
            ((TextField) obj).setText(value);
        }

    }


    class XCell extends ListCell<LinguisticCellContent> {
        HBox hbox = new HBox();
        Label label = new Label("(empty)");
        Pane pane = new Pane();
        Button button = new Button("(>)");
        LinguisticCellContent content;

        public XCell() {
            super();
            label.setWrapText(true);
            label.setMaxWidth(width * .8);
            hbox.getChildren().addAll(label, pane, button);
            HBox.setHgrow(pane, Priority.ALWAYS);

            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println(content.getId() + " : " + currentDoc.getSentenceWithID(content.getId()).getTokens().size() + ":" + event);
                    currentSentId = content.getId();
                    processSentence(content.getId());
                }
            });
        }

        @Override
        protected void updateItem(LinguisticCellContent item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);  // No originalText in label of super class
            if (empty) {
                content = null;
                setGraphic(null);
            } else {
                content = item;
                label.setText(item != null ? item.getOriginalText() : "<null>");
                setGraphic(hbox);
            }
        }
    }
}