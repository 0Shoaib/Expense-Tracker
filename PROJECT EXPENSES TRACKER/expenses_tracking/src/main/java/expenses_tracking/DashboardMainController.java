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

public class DashboardMainController {

    @FXML private Label welcomeLabel;
    @FXML private Button addExpenseButton;
    @FXML private Button manageCategoriesButton;
    @FXML private Button viewReportsButton;
    @FXML private Button logoutButton;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        } else {
            welcomeLabel.setText("Welcome, Guest!"); 
            addExpenseButton.setDisable(true);
            manageCategoriesButton.setDisable(true);
            viewReportsButton.setDisable(true);
        }
    }

    @FXML
    private void handleAddExpense(ActionEvent event) {
        navigateTo("/AddExpense.fxml", "Add New Expense", event, true);
    }

    @FXML
    private void handleManageCategories(ActionEvent event) {
        navigateTo("/CategoriesDashboard.fxml", "Manage Categories", event, true);
    }

    @FXML
    private void handleViewReports(ActionEvent event) {
        navigateTo("/ReportDashboard.fxml", "View Reports", event, true);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        currentUser = null; 
        try {
            Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Login.fxml")));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Expense Tracker - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Logout Error", "Error loading login screen.");
        }
    }

    private void navigateTo(String fxmlFile, String title, ActionEvent event, boolean passUser) {
        if (currentUser == null && passUser) {
            showAlert("Navigation Error", "No user logged in. Cannot proceed.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlFile)));
            Parent root = loader.load();

            if (passUser) { 
                if (fxmlFile.equals("/AddExpense.fxml")) {
                    AddExpenseController controller = loader.getController();
                    controller.setCurrentUser(currentUser); 
                } else if (fxmlFile.equals("/CategoriesDashboard.fxml")) {
                    CategoriesDashboardController controller = loader.getController();
                    controller.setCurrentUser(currentUser);
                } else if (fxmlFile.equals("/ReportDashboard.fxml")) {
                    ReportController controller = loader.getController();
                    controller.setCurrentUser(currentUser);
                }
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
