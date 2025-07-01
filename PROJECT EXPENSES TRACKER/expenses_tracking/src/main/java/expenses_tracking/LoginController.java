package expenses_tracking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        messageLabel.setText(""); 
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText(); 

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Username and password cannot be empty.");
            return;
        }

        try {
            User user = userDAO.validateUser(username, password);
            if (user != null) {
               
                messageLabel.setText("Login successful!");
                messageLabel.setStyle("-fx-text-fill: green;");
                navigateToDashboard(user, event);
            } else {
                // Login failed
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
                passwordField.clear(); 
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while trying to log in: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(User user, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/DashboardMain.fxml")));
            Parent dashboardRoot = loader.load();

           
            DashboardMainController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot));
            stage.setTitle("Expense Tracker Dashboard - " + user.getUsername());
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading DashboardMain.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the main dashboard.");
        } catch (NullPointerException e) {
            System.err.println("Error: Cannot find /DashboardMain.fxml. Ensure it is in the root of your resources folder.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "File Not Found", "Error: Main dashboard FXML not found.");
        }
    }

    @FXML
    private void handleGoToSignup(ActionEvent event) {
        try {
            Parent signupRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Signup.fxml")));
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(signupRoot));
            stage.setTitle("Expense Tracker - Sign Up");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading Signup.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the signup page.");
        } catch (NullPointerException e) {
            System.err.println("Error: Cannot find /Signup.fxml. Ensure it is in the root of your resources folder.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "File Not Found", "Error: Signup FXML not found.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        messageLabel.setText(message);
        if (type == Alert.AlertType.ERROR) {
            messageLabel.setStyle("-fx-text-fill: red;");
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







