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
import nl.epicspray.AI.util.SystemController;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Gebruiker on 14-1-2017.
 */
public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Classifier");
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(25, 25, 25, 25));
        Scene scene = new Scene(pane, 600, 475);

        Text sceneTitle = new Text("Classify");
        sceneTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        pane.add(sceneTitle, 0, 0, 2, 1);

        Label maleFolder = new Label("Location of folder with male blogs :");
        pane.add(maleFolder, 0, 1);
        final TextField maleFolderLocation = new TextField();
        pane.add(maleFolderLocation, 1, 1);

        Label femaleFolder = new Label("Location of folder with female blogs:");
        pane.add(femaleFolder, 0, 2);
        final TextField femaleFolderLocation = new TextField();
        pane.add(femaleFolderLocation, 1, 2);

        final Label testFolder = new Label("Location of folder to test:");
        pane.add(testFolder, 0, 3);
        final TextField testFolderLocation = new TextField();
        pane.add(testFolderLocation, 1, 3);

        Button trainButton = new Button("Train");
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.BOTTOM_RIGHT);
        hbox.getChildren().add(trainButton);
        pane.add(hbox, 1, 4);

        final Text resultMessage = new Text();
        pane.add(resultMessage, 1, 6);

        trainButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                //TODO en meer dingen weergeven en CHECK

                File f = new File(femaleFolderLocation.getText()); // The path to the folder
                File m = new File(maleFolderLocation.getText()); // The path to the folder
                File train = new File(testFolderLocation.getText()); // The path to the folder
                List<String> classes = Protocol.genderClass;
                Map<Map<String, Integer>, String> tokenizedF = null;
                Map<Map<String, Integer>, String> tokenizedM = null;
                Map<Map<String, Integer>, String> tokenizedT = new HashMap<Map<String, Integer>, String>();
                Map<Map<String, Integer>, String> tokenizedTrain = null;
                Tokenizer tokenizer= new Tokenizer();
                try {
                    tokenizedF = tokenizer.tokenizeFolder(f, classes);
                    tokenizedM = tokenizer.tokenizeFolder(m, classes);
                    tokenizedTrain = tokenizer.tokenizeFolder(train, classes);
                    tokenizedT.putAll(tokenizedF);
                    tokenizedT.putAll(tokenizedM);
                } catch (CouldNotStartTokenizingException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                } catch (IllegalFileNameException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }

                Bayes bayes = new Bayes();
                bayes.train(classes, tokenizedT);
                String best = bayes.getHighestChiSquare();
                resultMessage.setText("Best ChiSquare: " + best + ", " + bayes.computeChiSquare(best));

            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}