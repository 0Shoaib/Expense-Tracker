package expenses_tracking;

import javafx.application.Platform; 
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane; 
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors; 

public class ExpenseHistoryController {

    @FXML private Label titleLabel;
    
    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, LocalDate> dateColumn;
    @FXML private TableColumn<Expense, BigDecimal> amountColumn;
    @FXML private TableColumn<Expense, String> descriptionColumn;
    @FXML private TableColumn<Expense, String> paymentMethodColumn;
    @FXML private TableColumn<Expense, Void> actionsColumn; 
    @FXML private Button backButton;

    private User currentUser;
    private Category currentCategory; 
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    
    public void setHistoryDetails(User user, Category category) {
        this.currentUser = user;
        this.currentCategory = category;
        loadExpenses(); 
    }

    @FXML
    public void initialize() {
        
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

     
        dateColumn.setCellFactory(column -> new TableCell<Expense, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

      
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3;");

                editButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    handleEditExpense(expense);
                });

                deleteButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    handleDeleteExpense(expense);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadExpenses() {
        if (currentUser == null || currentCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "User or Category not set. Cannot load expenses.");
            return;
        }
        try {
            List<Expense> expenses = expenseDAO.getExpensesByCategoryId(currentUser.getId(), currentCategory.getId());
            expenseTable.getItems().setAll(expenses);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load expenses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditExpense(Expense expense) {
        if (expense == null) return;

        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Edit Expense");
        dialog.setHeaderText("Editing Expense ID: " + expense.getId());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField amountField = new TextField(expense.getAmount().toPlainString());
        ComboBox<String> categoryComboBox = new ComboBox<>();
        DatePicker datePicker = new DatePicker(expense.getDate());
        TextArea descriptionArea = new TextArea(expense.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(3);
        ComboBox<String> paymentMethodComboBox = new ComboBox<>();

      
        try {
            List<Category> categories = categoryDAO.getCategoriesByUserId(currentUser.getId());
            List<String> categoryNames = categories.stream()
                                                 .map(Category::getName)
                                                 .collect(Collectors.toList());
            categoryComboBox.setItems(FXCollections.observableArrayList(categoryNames));

            
            Category currentExpenseCategory = categories.stream()
                    .filter(c -> c.getId() == expense.getCategoryId())
                    .findFirst()
                    .orElse(null);
            if (currentExpenseCategory != null) {
                categoryComboBox.getSelectionModel().select(currentExpenseCategory.getName());
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load categories for editing: " + e.getMessage());
            e.printStackTrace();
            dialog.close();
            return;
        }

      
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
                "Cash", "Credit Card", "Debit Card", "Bank Transfer", "Online Payment", "Other"
        ));
        paymentMethodComboBox.getSelectionModel().select(expense.getPaymentMethod());

        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryComboBox, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionArea, 1, 3);
        grid.add(new Label("Payment Method:"), 0, 4);
        grid.add(paymentMethodComboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

      
        Platform.runLater(amountField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    
                    String amountText = amountField.getText();
                    if (amountText.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Input Error", "Amount cannot be empty.");
                        return null;
                    }
                    BigDecimal newAmount = new BigDecimal(amountText);
                    if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Input Error", "Amount must be positive.");
                        return null; 
                    }

                    String newCategoryName = categoryComboBox.getSelectionModel().getSelectedItem();
                    if (newCategoryName == null || newCategoryName.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a category.");
                        return null; 
                    }
                    Category newCategory = categoryDAO.getCategoryByNameAndUserId(newCategoryName, currentUser.getId());
                    if (newCategory == null) {
                        showAlert(Alert.AlertType.ERROR, "Input Error", "Selected category is invalid.");
                        return null; 
                    }
                    int newCategoryId = newCategory.getId();

                    LocalDate newDate = datePicker.getValue();
                    if (newDate == null) {
                        showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a date.");
                        return null; 
                    }

                    String newPaymentMethod = paymentMethodComboBox.getSelectionModel().getSelectedItem();
                    if (newPaymentMethod == null || newPaymentMethod.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a payment method.");
                        return null; 
                    }

                    expense.setAmount(newAmount);
                    expense.setCategoryId(newCategoryId);
                    expense.setDate(newDate);
                    expense.setDescription(descriptionArea.getText());
                    expense.setPaymentMethod(newPaymentMethod);

                    return expense;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid amount format. Please enter a valid number.");
                    return null; 
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Error retrieving category: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        Optional<Expense> result = dialog.showAndWait();
        result.ifPresent(updatedExpense -> {
            try {
                expenseDAO.updateExpense(updatedExpense);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Expense updated successfully!");
                loadExpenses(); 
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update expense: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    private void handleDeleteExpense(Expense expense) {
        if (expense == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Expense?");
        confirmAlert.setContentText("Are you sure you want to delete this expense of PKR " + expense.getAmount() + "? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                expenseDAO.deleteExpense(expense.getId(), currentUser.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Expense deleted successfully!");
                loadExpenses(); 
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete expense: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/CategoryDetailDashboard.fxml")));
            Parent categoryDetailRoot = loader.load();

            CategoryDetailDashboardController controller = loader.getController();
          
            controller.setCurrentUserAndCategory(currentUser, currentCategory);
         

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(categoryDetailRoot));
            stage.setTitle(currentCategory != null ? currentCategory.getName() + " Dashboard" : "Category Dashboard"); // Adjust title
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading CategoryDetailDashboard.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load Category Detail Dashboard.");
        } catch (NullPointerException e) {
            System.err.println("Error: Cannot find /CategoryDetailDashboard.fxml. Ensure it is in the root of your resources folder.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Resource Error", "CategoryDetailDashboard.fxml not found.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}