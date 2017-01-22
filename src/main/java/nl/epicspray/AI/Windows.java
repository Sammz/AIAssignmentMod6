package nl.epicspray.AI;

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Created by Sam on 22-1-2017.
 */
public class Windows {

    public Stage makeLoadStage(){
        HBox l = new HBox();
        Stage loadStage = new Stage();
        Scene scene = new Scene(l, 250, 70);
        loadStage.setScene(scene);
        Text loadText = new Text("Loading...");
        l.getChildren().add(loadText);

        return loadStage;
    }
}
