package expenses_tracking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReportController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button generateReportButton;
    @FXML private TableView<Expense> reportTableView;
    @FXML private TableColumn<Expense, String> reportTitleColumn;
    @FXML private TableColumn<Expense, String> reportCategoryColumn;
    @FXML private TableColumn<Expense, BigDecimal> reportAmountColumn;
    @FXML private TableColumn<Expense, LocalDate> reportDateColumn;
    @FXML private Label totalExpensesLabel;
    @FXML private BarChart<String, Number> expensesByCategoryChart;
    @FXML private CategoryAxis xAxis; 
    @FXML private NumberAxis yAxis;   
    @FXML private Label userInfoLabelReport;
    @FXML private Button backButtonReport;


    private User currentUser;
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private ObservableList<Expense> reportExpensesList = FXCollections.observableArrayList();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            userInfoLabelReport.setText("Reports for: " + currentUser.getUsername());
            
            endDatePicker.setValue(LocalDate.now());
            startDatePicker.setValue(LocalDate.now().withDayOfMonth(1)); 
            handleGenerateReport(null); 
        } else {
            userInfoLabelReport.setText("User not logged in.");
            reportTableView.setPlaceholder(new Label("Please log in to generate reports."));
            generateReportButton.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        reportTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        reportCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName")); // Uses the joined category name
        reportAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        reportDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

      
        reportAmountColumn.setCellFactory(column -> new TableCell<>() {
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

        reportTableView.setItems(reportExpensesList);
        reportTableView.setPlaceholder(new Label("No expenses found for the selected period."));
        
        xAxis.setLabel("Category");
        yAxis.setLabel("Total Amount (PKR)");
        expensesByCategoryChart.setTitle("Expenses by Category");
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "User Error", "No user logged in.");
            return;
        }
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select both start and end dates.");
            return;
        }
        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Start date cannot be after end date.");
            return;
        }

        reportExpensesList.clear();
        try {
            List<Expense> expenses = expenseDAO.getExpensesByUserIdAndDateRange(currentUser.getId(), startDate, endDate);
            reportExpensesList.addAll(expenses);

            BigDecimal total = expenses.stream()
                                       .map(Expense::getAmount)
                                       .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalExpensesLabel.setText(String.format("Total Expenses for Period: PKR %.2f", total));

            updateBarChart(expenses);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to generate report: " + e.getMessage());
        }
    }

    private void updateBarChart(List<Expense> expenses) {
        expensesByCategoryChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Spending");

        
        Map<String, BigDecimal> expensesPerCategory = expenses.stream()
            .collect(Collectors.groupingBy(
                Expense::getCategoryName, 
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ));

        if (expensesPerCategory.isEmpty()) {
            expensesByCategoryChart.setTitle("No Expense Data for Period");
        } else {
            expensesByCategoryChart.setTitle("Expenses by Category");
        }

        expensesPerCategory.forEach((categoryName, totalAmount) -> {
            series.getData().add(new XYChart.Data<>(categoryName, totalAmount));
        });
        
        expensesByCategoryChart.getData().add(series);
        
        for(XYChart.Data<String, Number> data : series.getData()){
            data.getNode().setStyle("-fx-bar-fill: #1E88E5;"); // Example color
        }
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
         try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/DashboardMain.fxml")));
            Parent root = loader.load();
            DashboardMainController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButtonReport.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the main dashboard.");
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
