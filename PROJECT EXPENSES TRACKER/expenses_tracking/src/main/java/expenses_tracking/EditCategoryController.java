package expenses_tracking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;

public class EditCategoryController {

    @FXML private TextField categoryNameField;
    @FXML private TextField budgetField;
    @FXML private Label messageLabel;
    @FXML private Label currentCategoryInfoLabel;

    private User currentUser;
    private Category categoryToEdit;
 
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO(); 
    private CategoryListController categoryListController; 

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setCategoryToEdit(Category category) {
        this.categoryToEdit = category;
        if (category != null) {
            categoryNameField.setText(category.getName());
            budgetField.setText(category.getBudget() != null ? category.getBudget().toPlainString() : "0.00");
            currentCategoryInfoLabel.setText("Editing: " + category.getName());
        } else {
            messageLabel.setText("Error: No category selected for editing.");
            categoryNameField.setDisable(true);
            budgetField.setDisable(true);
         
            if (categoryNameField.getScene() != null) {
                Button updateButton = (Button) categoryNameField.getScene().lookup("#updateCategoryButton");
                if (updateButton != null) {
                    updateButton.setDisable(true);
                }
            }
        }
    }

    public void setCategoryListController(CategoryListController controller) {
        this.categoryListController = controller;
    }

    @FXML
    private void handleUpdateCategory(ActionEvent event) {
        if (categoryToEdit == null || currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot update category. Essential data missing.");
            return;
        }

        String newName = categoryNameField.getText().trim();
        String budgetText = budgetField.getText().trim();

        if (newName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Category name cannot be empty.");
            categoryNameField.requestFocus();
            return;
        }
        if (budgetText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Budget cannot be empty.");
            budgetField.requestFocus();
            return;
        }

        BigDecimal newBudgetAmount;
        try {
            newBudgetAmount = new BigDecimal(budgetText);
            if (newBudgetAmount.compareTo(BigDecimal.ZERO) < 0) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Budget cannot be negative.");
                budgetField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid budget format. Please enter a valid number (e.g., 1000.50).");
            budgetField.requestFocus();
            return;
        }


        try {
            BigDecimal totalSpent = expenseDAO.getTotalExpensesByCategoryId(currentUser.getId(), categoryToEdit.getId());
            if (newBudgetAmount.compareTo(totalSpent) < 0) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        String.format("New budget (PKR %.2f) cannot be less than the amount already spent (PKR %.2f).",
                                newBudgetAmount, totalSpent));
                budgetField.requestFocus();
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not verify spent amount: " + e.getMessage());
            e.printStackTrace();
            return;
        }
     


        if (!newName.equalsIgnoreCase(categoryToEdit.getName())) {
            try {
                Category existingCategory = categoryDAO.getCategoryByNameAndUserId(newName, currentUser.getId());
                if (existingCategory != null) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Another category with this name already exists.");
                    categoryNameField.requestFocus();
                    return;
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error checking for existing category: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }


        categoryToEdit.setName(newName);
        categoryToEdit.setBudget(newBudgetAmount);

        // Persist changes to the database
        try {
            categoryDAO.updateCategory(categoryToEdit);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");

            if (categoryListController != null) {
                categoryListController.loadCategories(); 
            }

       
            Stage stage = (Stage) categoryNameField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
    
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

     
        messageLabel.setText(message);
        if (type == Alert.AlertType.ERROR) {
            messageLabel.setStyle("-fx-text-fill: red;");
        } else if (type == Alert.AlertType.INFORMATION) {
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setStyle("-fx-text-fill: black;");
        }
    }
}