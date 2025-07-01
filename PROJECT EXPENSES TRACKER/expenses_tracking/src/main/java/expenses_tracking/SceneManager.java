package expenses_tracking;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SceneManager {

    private Stage stage;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void switchScene(String fxmlFile, String title, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlFile)));
            if (controller != null) {
                loader.setController(controller);  
            }
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Error: Cannot find " + fxmlFile + ". Ensure it is in the root of your resources folder.");
            e.printStackTrace();
        }
    }
}