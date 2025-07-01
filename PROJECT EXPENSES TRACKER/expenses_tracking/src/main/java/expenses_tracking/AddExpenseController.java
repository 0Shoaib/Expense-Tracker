package expenses_tracking;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddExpenseController {

    @FXML private Label titleLabel; 
    @FXML private Label userInfoLabelExpense; 
    @FXML private TextField expenseTitleField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextArea descriptionField;
    @FXML private Button addExpenseButton;
    @FXML private Button cancelButton; 
    @FXML private Label messageLabel; 

    private User currentUser;
    private Category preSelectedCategory; 
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private List<Category> userCategories; 

    public void setUserAndCategory(User user, Category category) {
        this.currentUser = user;
        this.preSelectedCategory = category; 

        if (currentUser != null) {
            userInfoLabelExpense.setText("Adding expense for: " + currentUser.getUsername());
            loadCategoriesForUser(); 
        } else {
            userInfoLabelExpense.setText("User not logged in. Cannot add expense.");
            disableForm(true);
            messageLabel.setText("Error: No user logged in.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
   
    public void setCurrentUser(User user) {
        setUserAndCategory(user, null);
    }


    @FXML
    public void initialize() {
       
        paymentMethodComboBox.getItems().addAll("Cash", "Credit Card", "Debit Card", "Online Payment", "Bank Transfer", "Other");
       
        datePicker.setValue(LocalDate.now());
        messageLabel.setText(""); 
    }

    private void loadCategoriesForUser() {
        if (currentUser == null) {
            categoryComboBox.setDisable(true);
            categoryComboBox.setItems(FXCollections.observableArrayList("Login to load categories"));
            return;
        }
        try {
            userCategories = categoryDAO.getCategoriesByUserId(currentUser.getId());
            if (userCategories != null && !userCategories.isEmpty()) {
                categoryComboBox.setItems(FXCollections.observableArrayList(
                        userCategories.stream().map(Category::getName).collect(Collectors.toList())
                ));
                categoryComboBox.setDisable(false);

               
                if (preSelectedCategory != null) {
                    categoryComboBox.getSelectionModel().select(preSelectedCategory.getName());
                } else {
                    categoryComboBox.setPromptText("Select category");
                }

            } else {
                categoryComboBox.setItems(FXCollections.observableArrayList("No categories found. Add one first."));
                categoryComboBox.setDisable(true); 
            }
        } catch (SQLException e) {
            messageLabel.setText("Error loading categories: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
            categoryComboBox.setDisable(true);
        }
    }

    @FXML
    private void handleAddExpense() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "User Error", "No user is logged in. Cannot add expense.");
            return;
        }

        String title = expenseTitleField.getText().trim();
        String amountText = amountField.getText().trim();
        LocalDate date = datePicker.getValue();
        String selectedCategoryName = categoryComboBox.getValue();
        String selectedPaymentMethod = paymentMethodComboBox.getValue();
        String description = descriptionField.getText().trim(); 

    
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Expense title cannot be empty.");
            expenseTitleField.requestFocus(); return;
        }
        if (amountText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Amount cannot be empty.");
            amountField.requestFocus(); return;
        }
        if (date == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Date cannot be empty.");
            datePicker.requestFocus(); return;
        }
        if (selectedCategoryName == null || selectedCategoryName.startsWith("No categories found")) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a valid category.");
            categoryComboBox.requestFocus(); return;
        }
        if (selectedPaymentMethod == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a payment method.");
            paymentMethodComboBox.requestFocus(); return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) { 
                showAlert(Alert.AlertType.ERROR, "Input Error", "Amount must be a positive value.");
                amountField.requestFocus(); return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid amount format. Please enter a valid number (e.g., 1500.00).");
            amountField.requestFocus(); return;
        }
        
        Category selectedCategoryObject = userCategories.stream()
                .filter(cat -> cat.getName().equals(selectedCategoryName))
                .findFirst()
                .orElse(null);

        if (selectedCategoryObject == null) {
            showAlert(Alert.AlertType.ERROR, "Category Error", "Selected category not found. Please refresh or add categories.");
            return;
        }
        int categoryId = selectedCategoryObject.getId();

        Expense newExpense = new Expense(currentUser.getId(), categoryId, title, amount, date, description, selectedPaymentMethod);

        try {
            expenseDAO.addExpense(newExpense);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Expense '" + title + "' added successfully!");
            clearFields();
            
             handleCancel(); 
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add expense: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() { 
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/DashboardMain.fxml")));
            Parent mainDashboardRoot = loader.load();

            DashboardMainController controller = loader.getController();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) cancelButton.getScene().getWindow(); 
            stage.setScene(new Scene(mainDashboardRoot));
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading DashboardMain.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error loading main dashboard.");
        } catch (NullPointerException e) {
            System.err.println("Error: Cannot find /DashboardMain.fxml. Ensure it is in the root of your resources folder.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "File Not Found", "Error: Main dashboard FXML not found.");
        }
    }

    private void clearFields() {
        expenseTitleField.clear();
        amountField.clear();
        datePicker.setValue(LocalDate.now()); 
        categoryComboBox.getSelectionModel().clearSelection();
     
        if (preSelectedCategory != null && userCategories != null && userCategories.stream().anyMatch(c -> c.getName().equals(preSelectedCategory.getName()))) {
            categoryComboBox.getSelectionModel().select(preSelectedCategory.getName());
        } else {
            categoryComboBox.setPromptText("Select category");
        }
        paymentMethodComboBox.getSelectionModel().clearSelection();
        paymentMethodComboBox.setPromptText("Select payment method");
        descriptionField.clear();
        messageLabel.setText(""); 
    }
    
    private void disableForm(boolean disable) {
        expenseTitleField.setDisable(disable);
        amountField.setDisable(disable);
        datePicker.setDisable(disable);
        categoryComboBox.setDisable(disable);
        paymentMethodComboBox.setDisable(disable);
        descriptionField.setDisable(disable);
        addExpenseButton.setDisable(disable);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        messageLabel.setText(message);
        if (type == Alert.AlertType.ERROR) {
            messageLabel.setStyle("-fx-text-fill: red;");
        } else if (type == Alert.AlertType.INFORMATION || type == Alert.AlertType.CONFIRMATION) {
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
