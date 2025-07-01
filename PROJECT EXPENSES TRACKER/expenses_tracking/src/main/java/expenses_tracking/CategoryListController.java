package expenses_tracking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;

public class CategoryListController {

    @FXML private TableView<Category> categoryTableView;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TableColumn<Category, BigDecimal> budgetColumn;
    @FXML private TableColumn<Category, Void> actionsColumn; 

    @FXML private Label userInfoLabelList;
    @FXML private Button backButtonCategoryList;

    private User currentUser;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            userInfoLabelList.setText("Categories for: " + currentUser.getUsername());
            loadCategories();
        } else {
            userInfoLabelList.setText("User not logged in.");
            categoryTableView.setPlaceholder(new Label("Please log in to view categories."));
        }
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        budgetColumn.setCellValueFactory(new PropertyValueFactory<>("budget"));
        
        budgetColumn.setCellFactory(column -> new TableCell<Category, BigDecimal>() {
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

        setupActionsColumn();
        categoryTableView.setItems(categoryList);
        categoryTableView.setPlaceholder(new Label("No categories found. Add some!"));
    }

    public void loadCategories() {
        if (currentUser == null) return;
        categoryList.clear();
        try {
            List<Category> categoriesFromDB = categoryDAO.getCategoriesByUserId(currentUser.getId());
            categoryList.addAll(categoriesFromDB);
            if (categoriesFromDB.isEmpty()) {
                categoryTableView.setPlaceholder(new Label("No categories created yet. Click 'Add New Category'."));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load categories: " + e.getMessage());
            categoryTableView.setPlaceholder(new Label("Error loading categories."));
        }
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button viewDetailsButton = new Button("Details");
            private final HBox pane = new HBox(5, editButton, deleteButton, viewDetailsButton);

            {
                editButton.getStyleClass().add("table-button-edit");
                deleteButton.getStyleClass().add("table-button-delete");
                viewDetailsButton.getStyleClass().add("table-button-view");

                editButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleEditCategory(category);
                });
                deleteButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(category);
                });
                viewDetailsButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleViewCategoryDetails(category);
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

    private void handleEditCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/EditCategory.fxml")));
            Parent editCategoryRoot = loader.load();

            EditCategoryController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setCategoryToEdit(category);
            controller.setCategoryListController(this); 

            Stage stage = new Stage();
            stage.setTitle("Edit Category");
            stage.setScene(new Scene(editCategoryRoot));
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.showAndWait(); 

            loadCategories(); 

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load the edit category screen.");
        }
    }

    private void handleDeleteCategory(Category category) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Delete Category: " + category.getName());
        confirmationAlert.setContentText("Are you sure you want to delete this category? This action cannot be undone. \n(Note: Associated expenses will NOT be deleted but will become uncategorized if not handled otherwise).");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                
                List<Expense> expensesInCategory = new ExpenseDAO().getExpensesByCategoryId(currentUser.getId(), category.getId());
                if (!expensesInCategory.isEmpty()) {
                    Alert expenseWarning = new Alert(Alert.AlertType.WARNING);
                    expenseWarning.setTitle("Category Has Expenses");
                    expenseWarning.setHeaderText("Category '" + category.getName() + "' has " + expensesInCategory.size() + " expense(s) linked to it.");
                    expenseWarning.setContentText("Deleting this category will make these expenses uncategorized. Do you still want to proceed?");
                    Optional<ButtonType> warningResult = expenseWarning.showAndWait();
                    if (warningResult.isEmpty() || warningResult.get() != ButtonType.OK) {
                        return; 
                    }
                }
                
                categoryDAO.deleteCategory(category.getId(), currentUser.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category '" + category.getName() + "' deleted successfully.");
                loadCategories(); 
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete category: " + e.getMessage());
            }
        }
    }
    
    private void handleViewCategoryDetails(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/CategoryDetailDashboard.fxml")));
            Parent root = loader.load();

            CategoryDetailDashboardController controller = loader.getController();
            controller.setCurrentUserAndCategory(currentUser, category);

            Stage stage = (Stage) categoryTableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Category Details: " + category.getName());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load category details screen.");
        }
    }


    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/CategoriesDashboard.fxml")));
            Parent root = loader.load();
            CategoriesDashboardController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButtonCategoryList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manage Categories");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load categories dashboard.");
        }
    }
    
    @FXML
    private void handleRefreshList(ActionEvent event) {
        if (currentUser != null) {
            loadCategories();
            showAlert(Alert.AlertType.INFORMATION, "Refreshed", "Category list has been updated.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Refresh Failed", "No user logged in.");
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
