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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.epicspray.AI.exceptions.CouldNotStartTokenizingException;
import nl.epicspray.AI.exceptions.IllegalFileNameException;
import nl.epicspray.AI.util.SystemController;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static Text trainedClasses;

    public static void main(String[] args) {
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Classifier");
        HBox rootBox = new HBox();
        Scene scene = new Scene(rootBox, 1450, 500);
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

        Text info = new Text("For both training and testing this classifier the input\nfolders must have a structure like this: " +
                "A folder with for\neach class a subfolder with the name of that class and\nall files of that class in it.");
        informationPane.add(info, 0, 1, 1, 4);

        Text trainedClassesHeader = new Text("Sets of classes trained:");
        trainedClassesHeader.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        ;
        informationPane.add(trainedClassesHeader, 0, 6);
        trainedClasses = new Text("No training has been done yet.");
        informationPane.add(trainedClasses, 0, 7);
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

//        Label trainClassesLabel = new Label("Classes of documents:");
//        trainPane.add(trainClassesLabel, 0, 2);
//        final TextField trainClasses = new TextField();
//        trainClasses.setPromptText("class class class class ...");
//        trainPane.add(trainClasses, 1, 2);

        Button trainButton = new Button("Train");
        HBox trainhbox = new HBox(10);
        trainhbox.setAlignment(Pos.BOTTOM_RIGHT);
        trainhbox.getChildren().add(trainButton);
        trainPane.add(trainhbox, 1, 3);

        final Text trainErrorMessage = new Text();
        trainPane.add(trainErrorMessage, 0, 4, 2, 1);

        final Text trainResultMessage = new Text();
        trainPane.add(trainResultMessage, 0, 5, 2, 1);


        trainButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                onTrainButtonClick(trainErrorMessage, trainResultMessage, trainFolderLocation);
            }
        });
    }

    private void onTrainButtonClick(Text trainErrorMessage, Text trainResultMessage, TextField trainFolderLocation) {
        String trainOption = "train";
        trainErrorMessage.setText("");
        trainResultMessage.setText("");
        File trainFolder = new File(trainFolderLocation.getText());
        if (trainFolder.exists() && trainFolder.isDirectory()) {
            Stage loadStage = makeLoadStage();
            loadStage.show();
            List<String> classes = new ArrayList<String>();
            for (File f : trainFolder.listFiles()) {
                classes.add(f.getName());
            }
            if (!bayesList.keySet().contains(classes)) {
                Bayes bayes = new Bayes();
                bayesList.put(classes, bayes);
            }
            setTrainedClassesText();
            try {
                Bayes b = bayesList.get(classes);
                Map<Map<String, Integer>, String> tokenized = tokenizer.tokenizeFolder(trainOption, trainFolder, classes);
                b.train(classes, tokenized);

                String chiSquares = "";
                for(String chi : b.getBestChiSquare(10)){
                    chiSquares += chi + "\n";
                }

                trainResultMessage.setText("Trained succesfully!\n\nBest 10 ChiSquares:\n" + chiSquares);
            } catch (IllegalFileNameException e) {
                e.printStackTrace();
                trainErrorMessage.setText(error + e.getMessage());
            } catch (CouldNotStartTokenizingException e) {
                e.printStackTrace();
                trainErrorMessage.setText(error + e.getMessage());
            }
            loadStage.close();
        } else {
            trainErrorMessage.setText(error + "Location has wrong input");
        }
    }

    private void setTrainedClassesText() {
        StringBuilder sb = new StringBuilder();
        for (List<String> classesSet : bayesList.keySet()) {
            for (String s : classesSet) {
                sb.append(s);
                sb.append(" ");
            }
            sb.append("\n");
        }
        trainedClasses.setText(sb.toString());
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

//        Label testClassesLabel = new Label("Classes of documents:");
//        testPane.add(testClassesLabel, 0, 2);
//        final TextField testClasses = new TextField();
//        testClasses.setPromptText("class class class class ...");
//        testPane.add(testClasses, 1, 2);

        Button testButton = new Button("Test");
        HBox testhbox = new HBox(10);
        testhbox.setAlignment(Pos.BOTTOM_RIGHT);
        testhbox.getChildren().add(testButton);
        testPane.add(testhbox, 1, 3);

        final Text testErrorMessage = new Text();
        testPane.add(testErrorMessage, 0, 4, 2, 1);

        final Text testResultMessage = new Text();
        testResultMessage.setFont(Font.font("Monospace", FontWeight.NORMAL, 12));
        testPane.add(testResultMessage, 0, 5, 2, 1);


        testButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                onTestButtonClick(testErrorMessage, testResultMessage, testFolderLocation);


            }
        });
    }

    private void onTestButtonClick(Text testErrorMessage, Text testResultMessage, TextField testFolderLocation) {
        String testOption = "train";
        testErrorMessage.setText("");
        testResultMessage.setText("");
        File testFolder = new File(testFolderLocation.getText());
        if (testFolder.exists() && testFolder.isDirectory()) {

            List<String> classes = new ArrayList<String>();

            for (File f : testFolder.listFiles()) {
                classes.add(f.getName());
            }
            if (bayesList.keySet().contains(classes)) {

                Stage loadStage = makeLoadStage();
                loadStage.show();

                try {
                    Bayes b = bayesList.get(classes);
                    Map<Map<String, Integer>, String> tokenized = tokenizer.tokenizeFolder(testOption, testFolder, classes);
                    Map<String, Map<String, Integer>> confusionMatrix = b.getConfusionMatrix(tokenized, false);
                    double accuracy = b.getAccuracy(tokenized, false);
                    SystemController.getLogger().debug("Accuracy: " + accuracy);
                    
                    String matrix = matrixToString(classes, confusionMatrix);

                    Map<String, Double> recall = b.getRecall(tokenized, false);
                    String recallString = easyStatsToString(recall);

                    Map<String, Double> precision = b.getPrecision(tokenized, false);
                    String precisionString = easyStatsToString(precision);


                    testResultMessage.setText("Accuracy: " + accuracy + "\n\n" + matrix + "\n" + "Recall:\n" + recallString + "\n" + "Precision:\n" + precisionString);
                } catch (IllegalFileNameException e) {
                    e.printStackTrace();
                    testErrorMessage.setText(error + e.getMessage());
                } catch (CouldNotStartTokenizingException e) {
                    e.printStackTrace();
                    testErrorMessage.setText(error + e.getMessage());
                }

                loadStage.close();

            } else {
                testErrorMessage.setText(error + "Not all classes have been trained.");
            }
        } else {
            testErrorMessage.setText(error + "Location has wrong input");
        }
    }

    private String easyStatsToString(Map<String, Double> stats) {
        String recallString = "";
        for (String classRecall : stats.keySet()) {
            recallString += classRecall + ": " + round(stats.get(classRecall),3) + "\n";
        }
        return recallString;
    }

    private String matrixToString(List<String> classes, Map<String, Map<String, Integer>> confusionMatrix) {
        String longestClass = "Class:";
        for (String matrixClass : confusionMatrix.keySet()) {
            if (matrixClass.length() > longestClass.length()) {
                longestClass = matrixClass;
            }
        }
        String matrix = "Class:";
        matrix = center(matrix, longestClass.length());
        matrix += " | ";
        for (String c : classes) {

            c = center(c, longestClass.length());

            matrix += c + " | ";
        }
        matrix += "\n";
        for (String c : classes) {
            matrix += center(c, longestClass.length()) + " | ";
            for (String c1 : classes) {
                String number = confusionMatrix.get(c).get(c1).toString();
                number = center(number, longestClass.length());
                matrix += number + " | ";
            }
            matrix += "\n";
        }
        return matrix;
    }

    private Stage makeLoadStage() {
        Image image = new Image("http://www.sgpj.nl/uploads/bestaand/decoratie/3809962_trump_jpegeff60af78d1ded9d62fcff68f84370ee.jpg");
        ImageView imageView = new ImageView(image);
        Stage loadStage = new Stage();
        loadStage.setScene(new Scene(new Group(imageView)));
        loadStage.sizeToScene();
        loadStage.setTitle("Loading...      Wait till this disappears.");
        return loadStage;
    }

    public static String center(String s, int size) {
        return center(s, size, ' ');
    }

    public static String center(String s, int size, char pad) {
        if (s == null || size <= s.length())
            return s;

        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < (size - s.length()) / 2; i++) {
            sb.append(pad);
        }
        sb.append(s);
        while (sb.length() < size) {
            sb.append(pad);
        }
        return sb.toString();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}
