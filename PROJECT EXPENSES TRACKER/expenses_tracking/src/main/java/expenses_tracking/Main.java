package expenses_tracking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image; 
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stg) {
        try {
          
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Login.fxml")));
            Scene scene = new Scene(root);

          
            try {
                String iconPath = "/exp.png";
                Image icon = new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toExternalForm());
                stg.getIcons().add(icon);
  }
            catch (NullPointerException | IllegalArgumentException e) {
                          System.err.println("Warning: Application icon not found or failed to load from path: " + (e.getMessage().contains("InputStream is null") || e.getMessage().contains("null URL") ? "[Likely incorrect path or file missing]" : e.getMessage()));
                  }
           

            stg.setTitle("Expense Tracking App ");
            stg.setScene(scene);

          
            stg.setWidth(800); 
            stg.setHeight(600); 
            stg.setResizable(true); 

            stg.show();
        } catch (IOException e) {
            System.err.println("Error loading Login.fxml: " + e.getMessage());
            e.printStackTrace();
         
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to load application interface.");
            alert.setContentText("The Login.fxml file could not be loaded. Please ensure it is in the correct resources path.");
            alert.showAndWait();
        } catch (NullPointerException e) {
        
            System.err.println("Error: Cannot find /Login.fxml. Ensure it is in the root of your resources folder. " + e.getMessage());
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("File Not Found");
            alert.setContentText("The Login.fxml file was not found. The application cannot start.");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}



