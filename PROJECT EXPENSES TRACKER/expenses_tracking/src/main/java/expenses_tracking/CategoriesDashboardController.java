package expenses_tracking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CategoriesDashboardController {

    @FXML private Label categoriesTitleLabel;
    @FXML private Label userInfoLabelCategories;
    @FXML private Button addCategoryButton;
    @FXML private Button viewCategoriesButton;
    @FXML private Button backToMainDashboardButton;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            userInfoLabelCategories.setText("Managing categories for: " + currentUser.getUsername());
        } else {
            userInfoLabelCategories.setText("User not logged in.");
           
            addCategoryButton.setDisable(true);
            viewCategoriesButton.setDisable(true);
        }
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        navigateTo("/AddCategory.fxml", "Add New Category", event);
    }

    @FXML
    private void handleViewCategories(ActionEvent event) {
        navigateTo("/CategoryList.fxml", "View All Categories", event);
    }

    @FXML
    private void handleBackToMainDashboard(ActionEvent event) {
        navigateTo("/DashboardMain.fxml", "Main Dashboard", event);
    }

    private void navigateTo(String fxmlFile, String title, ActionEvent event) {
        if (currentUser == null) {
            showAlert("Navigation Error", "No user logged in. Cannot proceed.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlFile)));
            Parent root = loader.load();

          
            if (fxmlFile.equals("/AddCategory.fxml")) {
                AddCategoryController controller = loader.getController();
                controller.setCurrentUser(currentUser);
            } else if (fxmlFile.equals("/CategoryList.fxml")) {
                CategoryListController controller = loader.getController();
                controller.setCurrentUser(currentUser);
            } else if (fxmlFile.equals("/DashboardMain.fxml")) {
                DashboardMainController controller = loader.getController();
                controller.setCurrentUser(currentUser);
            }
     

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load the page: " + title);
        } catch (NullPointerException e) {
            System.err.println("Error: Cannot find " + fxmlFile + ". Ensure it is in the root of your resources folder.");
            e.printStackTrace();
            showAlert("File Not Found", "Error: FXML file " + fxmlFile + " not found.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
