package nl.epicspray.AI;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

    final Bayes bayes = new Bayes();
    final Tokenizer tokenizer = new Tokenizer();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Classifier");

        HBox rootBox = new HBox();
        Scene scene = new Scene(rootBox, 1100, 500);
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
        trainPane.add(trainFolderLocation, 1, 1);

        Label trainClassesLabel = new Label("Classes of documents:");
        trainPane.add(trainClassesLabel, 0, 2);
        final TextField trainClasses = new TextField();
        trainPane.add(trainClasses, 1, 2);

        Button trainButton = new Button("Train");
        HBox trainhbox = new HBox(10);
        trainhbox.setAlignment(Pos.BOTTOM_RIGHT);
        trainhbox.getChildren().add(trainButton);
        trainPane.add(trainhbox, 1, 4);

        final Text trainResultMessage = new Text();
        trainPane.add(trainResultMessage, 1, 6);

        final Text errorMessage = new Text();
        trainPane.add(errorMessage, 1, 8);

        trainButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                String option = "train";
                File trainFolder = new File(trainFolderLocation.getText());
                String[] classesArray = trainClasses.getText().split(" ");
                List<String> classes = new ArrayList<String>();
                for(String clas : classesArray){
                    classes.add(clas);
                }
                Map<Map<String, Integer>, String> tokenized = new HashMap<Map<String, Integer>, String>();
                try {
                    tokenized = tokenizer.tokenizeFolder(option, trainFolder, classes);
                } catch (IllegalFileNameException e) {
                    e.printStackTrace();
                    errorMessage.setText("Error: " + e.getMessage());
                } catch (CouldNotStartTokenizingException e) {
                    e.printStackTrace();
                    errorMessage.setText("Error: " + e.getMessage());
                }
                bayes.train(classes, tokenized);
                trainResultMessage.setText("Best ChiSquare: " + bayes.getHighestChiSquare());

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
        testPane.add(testFolderLocation, 1, 1);

        Label testClassesLabel = new Label("Classes of documents:");
        testPane.add(testClassesLabel, 0, 2);
        final TextField testClasses = new TextField();
        testPane.add(testClasses, 1, 2);

        Button testButton = new Button("Test");
        HBox testhbox = new HBox(10);
        testhbox.setAlignment(Pos.BOTTOM_RIGHT);
        testhbox.getChildren().add(testButton);
        testPane.add(testhbox, 1, 3);

        final Text testResultMessage = new Text();
        testPane.add(testResultMessage, 1, 6);

        testButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {


                testResultMessage.setText("Best ChiSquare: ");

            }
        });
    }


}
