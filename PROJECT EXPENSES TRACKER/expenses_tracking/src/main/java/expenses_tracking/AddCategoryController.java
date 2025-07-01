package expenses_tracking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; 
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal; 
import java.sql.SQLException;
import java.util.Objects;

public class AddCategoryController {

    @FXML private Label userInfoLabel;
    @FXML private TextField categoryNameField;
    @FXML private TextField budgetField;
    @FXML private Label messageLabel; 
    @FXML private Button backButton; 

    private User currentUser;
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            userInfoLabel.setText("Adding category for: " + currentUser.getUsername());
        } else {
            userInfoLabel.setText("User not logged in. Cannot add category.");
         
            categoryNameField.setDisable(true);
            budgetField.setDisable(true);
            ((Button)backButton.getParent().lookup("#addCategoryButtonActual")).setDisable(true); 
        }
    }

    @FXML
    public void initialize() {
        messageLabel.setText(""); 
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "User Error", "No user is logged in. Cannot add category.");
            return;
        }

        String categoryName = categoryNameField.getText().trim();
        String budgetText = budgetField.getText().trim();

        if (categoryName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Category name cannot be empty.");
            categoryNameField.requestFocus();
            return;
        }
        if (budgetText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Budget cannot be empty.");
            budgetField.requestFocus();
            return;
        }

        BigDecimal budgetAmount;
        try {
            budgetAmount = new BigDecimal(budgetText);
            if (budgetAmount.compareTo(BigDecimal.ZERO) < 0) { 
                showAlert(Alert.AlertType.ERROR, "Input Error", "Budget cannot be negative.");
                budgetField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid budget format. Please enter a valid number (example. 500.00).");
            budgetField.requestFocus();
            return;
        }

        
        try {
            Category existingCategory = categoryDAO.getCategoryByNameAndUserId(categoryName, currentUser.getId());
            if (existingCategory != null) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "A category with this name already exists for you.");
                categoryNameField.requestFocus();
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error checking for existing category: " + e.getMessage());
            e.printStackTrace();
            return; 
        }

        Category newCategory = new Category(currentUser.getId(), categoryName, budgetAmount);

        try {
            categoryDAO.addCategory(newCategory);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category '" + categoryName + "' added successfully!");
            clearFields();
            handleBack(event); 
        }
        catch (SQLException e) {
           showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/CategoriesDashboard.fxml")));
            Parent categoriesDashboardRoot = loader.load();

            CategoriesDashboardController controller = loader.getController();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(categoriesDashboardRoot));
            stage.setTitle("Categories Dashboard");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading CategoriesDashboard.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load categories dashboard.");
        } catch (NullPointerException e) {
            System.err.println("Error: Cannot find /CategoriesDashboard.fxml. Ensure it's in the root of resources.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "File Not Found", "Error: Categories dashboard FXML not found.");
        }
    }

    private void clearFields() {
        categoryNameField.clear();
        budgetField.clear();
        messageLabel.setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        messageLabel.setText(message);
        if (type == Alert.AlertType.ERROR) {
            messageLabel.setStyle("-fx-text-fill: red;");
        } else if (type == Alert.AlertType.INFORMATION) {
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setStyle("-fx-text-fill: black;");
        }
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
         alert.showAndWait();
    }
}
