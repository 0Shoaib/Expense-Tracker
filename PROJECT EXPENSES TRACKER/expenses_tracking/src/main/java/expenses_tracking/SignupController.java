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
import java.util.regex.Pattern;

public class SignupController{

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Button loginButton; 
    @FXML private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");


    @FXML
    public void initialize() {
        messageLabel.setText("");
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText(); 
        String confirmPassword = confirmPasswordField.getText();

       
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
            return;
        }
        if (username.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Username must be at least 3 characters long.");
            usernameField.requestFocus();
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid email format.");
            emailField.requestFocus();
            return;
        }
        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Password must be at least 6 characters long.");
            passwordField.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Passwords do not match.");
            confirmPasswordField.clear();
            confirmPasswordField.requestFocus();
            return;
        }
       


        try {
           
            if (userDAO.usernameExists(username)) {
                showAlert(Alert.AlertType.ERROR, "Signup Error", "Username already taken. Please choose another.");
                usernameField.requestFocus();
                return;
            }
            if (userDAO.emailExists(email)) {
                showAlert(Alert.AlertType.ERROR, "Signup Error", "Email address already registered.");
                emailField.requestFocus();
                return;
            }

          
            User newUser = new User(username, password, email); 
            userDAO.addUser(newUser);

            showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "Account created successfully! You can now log in.");
            clearFields();
            
            handleGoToLogin(event);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred during signup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Login.fxml")));
            Stage stage = (Stage) loginButton.getScene().getWindow(); 
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Expense Tracker ");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading Login.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the login page.");
        } catch (NullPointerException e) {
            System.err.println("Error: Cannot find /Login.fxml. Ensure it is in the root of your resources folder.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "File Not Found", "Error: Login FXML not found.");
        }
    }
    
    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
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


















