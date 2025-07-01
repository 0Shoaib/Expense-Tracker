package expenses_tracking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class CategoryDetailDashboardController {

    @FXML private Label categoryNameLabel;
    @FXML private Label budgetLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label remainingBudgetLabel;
    @FXML private Label budgetPercentageLabel; 
    @FXML private ProgressBar budgetProgressBar;
    @FXML private TableView<Expense> recentExpensesTableView;
    @FXML private TableColumn<Expense, String> expenseTitleColumn;
    @FXML private TableColumn<Expense, String> expenseDescriptionColumn;
    @FXML private TableColumn<Expense, BigDecimal> expenseAmountColumn;
    @FXML private TableColumn<Expense, String> expensePaymentMethodColumn;
    @FXML private TableColumn<Expense, LocalDate> expenseDateColumn;
    @FXML private PieChart categoryPieChart;
    @FXML private Button backToCategoriesButton;

    private User currentUser;
    private Category currentCategory;
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Expense> recentExpensesList = FXCollections.observableArrayList();

    public void setCurrentUserAndCategory(User user, Category category) {
        this.currentUser = user;
        this.currentCategory = category;
        if (currentUser == null || currentCategory == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User or Category data is missing.");
            return;
        }
        refreshAllDetails();
    }

    @FXML
    public void initialize() {
        // Setup table columns
        expenseDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        expenseTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        expenseDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description")); 
        expenseAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        expensePaymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod")); 

        
        expenseAmountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("PKR %.2f", item));
                }
            }
        });
        recentExpensesTableView.setItems(recentExpensesList);
        recentExpensesTableView.setPlaceholder(new Label("No expenses recorded for this category yet."));
    }

    public void loadCategoryDetails() {
        if (currentCategory == null) return;
        categoryNameLabel.setText(currentCategory.getName() + " Details");
        budgetLabel.setText(String.format("Budget: PKR %.2f", currentCategory.getBudget()));
    }

    public void loadRecentExpenses() {
        if (currentUser == null || currentCategory == null) return;
        recentExpensesList.clear();
        try {
           
            List<Expense> expenses = expenseDAO.getExpensesByCategoryId(currentUser.getId(), currentCategory.getId());
            recentExpensesList.addAll(expenses);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load recent expenses: " + e.getMessage());
        }
    }

    public void calculateAndDisplayTotalSpent() {
        if (currentUser == null || currentCategory == null) return;
        try {
            BigDecimal totalSpent = expenseDAO.getTotalExpensesByCategoryId(currentUser.getId(), currentCategory.getId());
            totalSpentLabel.setText(String.format("Total Spent: PKR %.2f", totalSpent));

            BigDecimal budget = currentCategory.getBudget();
            double progress = 0.0;

            if (budget.compareTo(BigDecimal.ZERO) > 0) {
                progress = totalSpent.divide(budget, 4, BigDecimal.ROUND_HALF_UP).doubleValue();

               
                if (totalSpent.compareTo(budget) > 0) {
                    BigDecimal overBudget = totalSpent.subtract(budget);
                    remainingBudgetLabel.setText(String.format("Over Budget: PKR %.2f", overBudget));
                    remainingBudgetLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;"); 
                } else {
                    BigDecimal remaining = budget.subtract(totalSpent);
                    remainingBudgetLabel.setText(String.format("Remaining: PKR %.2f", remaining));
                   
                    if (progress > 0.8) {
                        remainingBudgetLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;"); 
                    } else {
                        remainingBudgetLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;"); 
                    }
                }
            } else {
                remainingBudgetLabel.setText("Budget not set.");
                remainingBudgetLabel.setStyle("-fx-text-fill: black;");
            }

            budgetProgressBar.setProgress(Math.min(progress, 1.0)); 
            double percentage = progress * 100;
            budgetPercentageLabel.setText(String.format("%.1f%% Used", percentage));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Calculation Error", "Failed to calculate total spending: " + e.getMessage());
        }
    }

    private void updatePieChart() {
        if (currentCategory == null || currentUser == null) return;

        try {
            BigDecimal totalSpent = expenseDAO.getTotalExpensesByCategoryId(currentUser.getId(), currentCategory.getId());
            BigDecimal budget = currentCategory.getBudget();
            BigDecimal remaining = budget.compareTo(totalSpent) >= 0 ? budget.subtract(totalSpent) : BigDecimal.ZERO;

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            if (totalSpent.compareTo(BigDecimal.ZERO) > 0) {
                pieChartData.add(new PieChart.Data("Spent (PKR " + totalSpent.setScale(2) + ")", totalSpent.doubleValue()));
            }
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                pieChartData.add(new PieChart.Data("Remaining (PKR " + remaining.setScale(2) + ")", remaining.doubleValue()));
            }

            
            if (pieChartData.isEmpty()) {
                if (budget.compareTo(BigDecimal.ZERO) > 0) {
                    pieChartData.add(new PieChart.Data("Budget (PKR " + budget.setScale(2) + ")", budget.doubleValue()));
                    categoryPieChart.setTitle("Spending for " + currentCategory.getName());
                } else {
                    pieChartData.add(new PieChart.Data("No Data", 1));
                    categoryPieChart.setTitle("No Budget or Spending Data for " + currentCategory.getName());
                }
            } else {
                categoryPieChart.setTitle("Spending for " + currentCategory.getName());
            }

            categoryPieChart.setData(pieChartData);
            categoryPieChart.setLegendVisible(true);
            
          
            for (PieChart.Data data : pieChartData) {
                if (data.getName().startsWith("Spent")) {
                    data.getNode().setStyle("-fx-pie-color: #d32f2f;"); // Red for spent
                } else if (data.getName().startsWith("Remaining")) {
                    data.getNode().setStyle("-fx-pie-color: #388e3c;"); // Green for remaining
                } else if (data.getName().startsWith("Budget")) {
                    data.getNode().setStyle("-fx-pie-color: #3f51b5;"); // Blue for budget
                } else {
                    data.getNode().setStyle("-fx-pie-color: #CCCCCC;"); // Grey for no data
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Pie Chart Error", "Failed to load data for pie chart: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddExpenseForThisCategory(ActionEvent event) {
        if (currentUser == null || currentCategory == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot add expense without user or category context.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/AddExpense.fxml")));
            Parent addExpenseRoot = loader.load();

            AddExpenseController controller = loader.getController();
            controller.setUserAndCategory(currentUser, currentCategory); // Pre-select the current category

            Stage stage = new Stage();
            stage.setScene(new Scene(addExpenseRoot));
            stage.setTitle("Add New Expense to " + currentCategory.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        
            refreshAllDetails();

        } catch (IOException e) {
            System.err.println("Error loading AddExpense.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load Add Expense screen.");
        }
    }

    @FXML
    private void handleRefreshDetails(ActionEvent event) {
        refreshAllDetails();
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "Category details and expenses have been updated.");
    }

    private void refreshAllDetails() {
        if (currentUser != null && currentCategory != null) {
            try {
        
                currentCategory = categoryDAO.getCategoryByIdAndUserId(currentCategory.getId(), currentUser.getId());
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Refresh Error", "Could not refresh category data.");
            }
            loadCategoryDetails();
            loadRecentExpenses();
            calculateAndDisplayTotalSpent();
            updatePieChart();
        }
    }

    @FXML
    private void handleBackToCategoryList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/CategoryList.fxml")));
            Parent root = loader.load();
            CategoryListController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backToCategoriesButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("View All Categories");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the category list screen.");
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