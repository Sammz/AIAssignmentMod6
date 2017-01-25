package nl.epicspray.AI;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import nl.epicspray.AI.exceptions.CouldNotStartTokenizingException;
import nl.epicspray.AI.exceptions.IllegalFileNameException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Gebruiker on 14-1-2017.
 */
public class Main extends Application {

    private Map<List<String>, Bayes> bayesList = new HashMap<List<String>, Bayes>();
    private final Tokenizer tokenizer = new Tokenizer();
    private final String error = "Error: ";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Classifier");

        HBox rootBox = new HBox();
        Scene scene = new Scene(rootBox, 1300, 600);
        GridPane informationPane = new GridPane();
        GridPane trainPane = new GridPane();
        GridPane testPane = new GridPane();
        rootBox.getChildren().addAll(informationPane, trainPane, testPane);

        setInformationBox(informationPane);
        setTrainPane(trainPane);
        setTestPane(testPane);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setInformationBox(GridPane informationPane) {
        informationPane.setAlignment(Pos.TOP_LEFT);
        informationPane.setHgap(10);
        informationPane.setVgap(10);
        informationPane.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Information regarding input fields");
        sceneTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        informationPane.add(sceneTitle, 0, 0, 2, 1);

        Text info = new Text("Folder with train data:\nIn the folder should be a folder for each class \nwith the name of that class containing all \nfiles of that class to train with." +
                "\nSupports infinite classes.\nSeparate classes with spaces.\nTest folder:\n");
        informationPane.add(info, 0, 1, 1, 1);
    }

    private void setTrainPane(GridPane trainPane) {
        trainPane.setAlignment(Pos.TOP_CENTER);
        trainPane.setHgap(10);
        trainPane.setVgap(10);
        trainPane.setPadding(new Insets(25, 25, 25, 25));


        Text sceneTitle = new Text("Train classifier");
        sceneTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        trainPane.add(sceneTitle, 0, 0, 2, 1);

        Label trainFolderLocationLabel = new Label("Location of folder with train data:");
        trainPane.add(trainFolderLocationLabel, 0, 1);
        final TextField trainFolderLocation = new TextField();
        trainFolderLocation.setPromptText("Enter path to folder");
        trainPane.add(trainFolderLocation, 1, 1);

        Label trainClassesLabel = new Label("Classes of documents:");
        trainPane.add(trainClassesLabel, 0, 2);
        final TextField trainClasses = new TextField();
        trainClasses.setPromptText("class class class class ...");
        trainPane.add(trainClasses, 1, 2);

        Button trainButton = new Button("Train");
        HBox trainhbox = new HBox(10);
        trainhbox.setAlignment(Pos.BOTTOM_RIGHT);
        trainhbox.getChildren().add(trainButton);
        trainPane.add(trainhbox, 1, 4);

        final Text trainResultMessage = new Text();
        trainPane.add(trainResultMessage, 0, 6,2,1);

        final Text trainErrorMessage = new Text();
        trainPane.add(trainErrorMessage, 0, 8,2,1);

        trainButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                String trainOption = "train";
                trainErrorMessage.setText("");
                trainResultMessage.setText("");
                File trainFolder = new File(trainFolderLocation.getText());
                if (!trainClasses.getText().equals("") &&!trainClasses.getText().equals(" ") && trainClasses.getText().contains(" ") && trainFolder.exists() && trainFolder.isDirectory()) {
                    Stage loadStage = makeLoadStage();
                    loadStage.show();

                    String[] classesArray = trainClasses.getText().split(" ");
                    List<String> classes = new ArrayList<String>();
                    for (String clas : classesArray) {
                        classes.add(clas);
                    }

                    if(!bayesList.keySet().contains(classes)){
                        Bayes bayes = new Bayes();
                        bayesList.put(classes, bayes);
                    }

                    try {

                        Bayes b =bayesList.get(classes);
                        Map<Map<String, Integer>, String> tokenized = tokenizer.tokenizeFolder(trainOption, trainFolder, classes);
                        b.train(classes, tokenized);
                        trainResultMessage.setText("Trained succesfully!\nBest ChiSquare: " + b.getHighestChiSquare());

                        //TODO show more info maybe

                    } catch (IllegalFileNameException e) {
                        e.printStackTrace();
                        trainErrorMessage.setText(error + e.getMessage());
                    } catch (CouldNotStartTokenizingException e) {
                        e.printStackTrace();
                        trainErrorMessage.setText(error + e.getMessage());
                    }


                    loadStage.close();

                } else {
                    trainErrorMessage.setText(error + "Location and or classes have wrong input");
                }
            }
        });
    }

    private void setTestPane(GridPane testPane) {
        testPane.setAlignment(Pos.TOP_RIGHT);
        testPane.setHgap(10);
        testPane.setVgap(10);
        testPane.setPadding(new Insets(25, 25, 25, 25));

        Text testSceneTitle = new Text("Test");
        testSceneTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        testPane.add(testSceneTitle, 0, 0, 2, 1);

        final Label testFolder = new Label("Location of folder to test:");
        testPane.add(testFolder, 0, 1);
        final TextField testFolderLocation = new TextField();
        testFolderLocation.setPromptText("Enter path to folder");
        testPane.add(testFolderLocation, 1, 1);

        Label testClassesLabel = new Label("Classes of documents:");
        testPane.add(testClassesLabel, 0, 2);
        final TextField testClasses = new TextField();
        testClasses.setPromptText("class class class class ...");
        testPane.add(testClasses, 1, 2);

        Button testButton = new Button("Test");
        HBox testhbox = new HBox(10);
        testhbox.setAlignment(Pos.BOTTOM_RIGHT);
        testhbox.getChildren().add(testButton);
        testPane.add(testhbox, 1, 3);

        final Text testResultMessage = new Text();
        testPane.add(testResultMessage, 0, 6,2,1);

        final Text testErrorMessage = new Text();
        testPane.add(testErrorMessage, 0,8,2,1);

        testButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                String testOption = "train";

                testErrorMessage.setText("");
                testResultMessage.setText("");
                File testFolder = new File(testFolderLocation.getText());
                if (!testClasses.getText().equals("") &&!testClasses.getText().equals(" ") && testClasses.getText().contains(" ") && testFolder.exists() && testFolder.isDirectory()) {

                    String[] classesArray = testClasses.getText().split(" ");
                    List<String> classes = new ArrayList<String>();

                    for (String clas : classesArray) {
                        classes.add(clas);
                    }
                    if (bayesList.keySet().contains(classes)) {

                        Stage loadStage = makeLoadStage();
                        loadStage.show();

                        int correct = 0;
                        int incorrect = 0;

                        try {
                            Bayes b = bayesList.get(classes);
                            Map<Map<String, Integer>, String> tokenized = tokenizer.tokenizeFolder(testOption, testFolder, classes);
                            for(Map<String, Integer> doc : tokenized.keySet()){
                                String docClass = tokenized.get(doc);
                                String predictedDocClass = b.classify(doc);
                                //System.out.println("class: " + docClass + "     predicted class: " + predictedDocClass);
                                if(docClass.equals(predictedDocClass)){
                                    correct ++;
                                } else {
                                    incorrect ++;
                                }

                            }

                            testResultMessage.setText("Correct: " + correct + "\nIncorrect: " + incorrect);

                            //TODO show more information

                        } catch (IllegalFileNameException e) {
                            e.printStackTrace();
                            testErrorMessage.setText(error + e.getMessage());
                        } catch (CouldNotStartTokenizingException e) {
                            e.printStackTrace();
                            testErrorMessage.setText(error + e.getMessage());
                        }

                        loadStage.close();

                    } else {
                        testErrorMessage.setText(error +"Not all classes have been trained.");
                    }
                } else {
                    testErrorMessage.setText(error + "Location and or classes have wrong input");
                }


            }
        });
    }

    public Stage makeLoadStage(){

        Group root = new Group();
        Scene scene = new Scene(root, 600, 500, Color.WHITE);

        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);

        ImageView imv = new ImageView();

        Image i = new Image(Main.class.getResourceAsStream("/images/spin.gif"));
        //TODO vaag image werkt niet
        imv.setImage(i);

        HBox pictureRegion = new HBox();

        pictureRegion.getChildren().add(imv);
        gridpane.add(pictureRegion, 1, 1);


        root.getChildren().add(gridpane);

        Stage loadStage = new Stage();
        loadStage.setTitle("Loading...");

        loadStage.setScene(scene);
        return loadStage;
    }


}
