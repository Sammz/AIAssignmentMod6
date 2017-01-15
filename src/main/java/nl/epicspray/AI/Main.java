package nl.epicspray.AI;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;



/**
 * Created by Gebruiker on 14-1-2017.
 */
public class Main extends Application implements EventHandler<ActionEvent> {

    Stage window;
    Button trainButton;
    ButtonBar bar;
    Stage dataWindow;
    TextArea inputLocation;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Wèka 2.0");
        trainButton = new Button("Train");
        trainButton.setOnAction(this);

        inputLocation = new TextArea();
        inputLocation.setText("Location of data");


        Label labelLocation = new Label("Location:");
        TextField textField = new TextField();
        HBox hb = new HBox();
        hb.getChildren().addAll(labelLocation, textField);
        hb.setSpacing(40);


        StackPane layout = new StackPane();
        layout.getChildren().addAll(inputLocation, textField, hb, trainButton);

        Scene scene = new Scene(layout, 600, 500);

        window.setScene(scene);
        window.show();
    }


    public void handle(ActionEvent event) {
        if (event.getSource() == trainButton){
            dataWindow = new Stage();
            dataWindow.setTitle("Trained");

            StackPane layout = new StackPane();
            layout.getChildren().add(trainButton);

            Scene scene = new Scene(layout, 600, 500);

            dataWindow.setScene(scene);
            dataWindow.show();
        }
    }
}