package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.prefs.Preferences;
import com.logistics.packinglist.service.AuthService;
import javafx.application.Platform;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.layout.StackPane;

public class LoginController extends VBox {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
    private final Stage stage;
    private final Runnable onLoginSuccess;

    public LoginController(Stage stage, Runnable onLoginSuccess) {
        this.stage = stage;
        this.onLoginSuccess = onLoginSuccess;
        construirUI();
    }

    private void construirUI() {
        getStyleClass().add("login-container");
        setAlignment(Pos.CENTER);
        setSpacing(14);
        setPadding(new Insets(30, 35, 30, 35));
        setStyle("-fx-background-color: #f5f7fb;");

        // Logo
        ImageView imgLogo = new ImageView();
        try {
            imgLogo.setImage(new Image(getClass().getResourceAsStream("/icon.png")));
            imgLogo.setFitHeight(95);
            imgLogo.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo del login: " + e.getMessage());
        }

        Label lblTitulo = new Label("Iniciar Sesión");
        lblTitulo.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E; -fx-padding: 5 0 15 0;");

        TextField txtEmail = new TextField();
        txtEmail.getStyleClass().add("login-field");
        txtEmail.setPromptText("Correo electrónico");
        txtEmail.setPrefHeight(42);
        txtEmail.setMinHeight(42);
        txtEmail.setStyle("-fx-font-size: 13.5px; -fx-padding: 9px 14px; -fx-min-height: 42px; -fx-pref-height: 42px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: white; -fx-text-fill: #1e293b;");

        StackPane passwordContainer = new StackPane();
        passwordContainer.setAlignment(Pos.CENTER_RIGHT);
        passwordContainer.setPrefHeight(42);
        passwordContainer.setMinHeight(42);

        PasswordField txtPassword = new PasswordField();
        txtPassword.getStyleClass().add("login-field-password");
        txtPassword.setPromptText("Contraseña");
        txtPassword.setPrefHeight(42);
        txtPassword.setMinHeight(42);
        txtPassword.setStyle("-fx-font-size: 13.5px; -fx-padding: 9px 42px 9px 14px; -fx-min-height: 42px; -fx-pref-height: 42px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: white; -fx-text-fill: #1e293b;");

        TextField txtPasswordVisible = new TextField();
        txtPasswordVisible.getStyleClass().add("login-field-password");
        txtPasswordVisible.setPromptText("Contraseña");
        txtPasswordVisible.setPrefHeight(42);
        txtPasswordVisible.setMinHeight(42);
        txtPasswordVisible.setStyle("-fx-font-size: 13.5px; -fx-padding: 9px 42px 9px 14px; -fx-min-height: 42px; -fx-pref-height: 42px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: white; -fx-text-fill: #1e293b;");
        txtPasswordVisible.setVisible(false);

        // Bind bidirectionally to sync text
        txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());

        Button btnTogglePassword = new Button();
        btnTogglePassword.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0 12 0 0;");
        
        FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE_SLASH);
        eyeIcon.setSize("16px");
        eyeIcon.setFill(javafx.scene.paint.Color.GRAY);
        btnTogglePassword.setGraphic(eyeIcon);

        btnTogglePassword.setOnAction(event -> {
            if (txtPassword.isVisible()) {
                eyeIcon.setIcon(FontAwesomeIcon.EYE);
                txtPassword.setVisible(false);
                txtPasswordVisible.setVisible(true);
            } else {
                eyeIcon.setIcon(FontAwesomeIcon.EYE_SLASH);
                txtPasswordVisible.setVisible(false);
                txtPassword.setVisible(true);
            }
        });

        passwordContainer.getChildren().addAll(txtPassword, txtPasswordVisible, btnTogglePassword);

        CheckBox chkRecordar = new CheckBox("Recordar credenciales");
        chkRecordar.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        // Cargar credenciales guardadas
        String savedEmail = prefs.get("login_email", "");
        if (!savedEmail.isEmpty()) {
            txtEmail.setText(savedEmail);
            try {
                String savedPassword = Keyring.create().getPassword("PackingListApp", savedEmail);
                if (savedPassword != null && !savedPassword.isEmpty()) {
                    txtPassword.setText(savedPassword);
                    chkRecordar.setSelected(true);
                    
                    // Opcional: Auto-login
                    Platform.runLater(() -> {
                        // Descomentar para auto-ingresar sin hacer click
                        // btnLogin.fire();
                    });
                }
            } catch (Exception e) {
                System.err.println("No se pudo recuperar la contraseña segura: " + e.getMessage());
            }
        }

        Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(42);
        btnLogin.setMinHeight(42);
        btnLogin.setStyle(
                "-fx-background-color: #f7722aff; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px; -fx-pref-height: 42px; -fx-min-height: 42px; -fx-background-radius: 6px; -fx-cursor: hand;");

        btnLogin.setOnAction(e -> {
            String email = txtEmail.getText();
            String password = txtPassword.getText();

            if (email.trim().isEmpty() || password.trim().isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Por favor, complete todos los campos.");
                a.setHeaderText(null);
                a.show();
                return;
            }

            // Iniciar flujo de autenticación API en hilo de fondo
            btnLogin.setDisable(true);
            txtEmail.setDisable(true);
            txtPassword.setDisable(true);
            txtPasswordVisible.setDisable(true);
            btnTogglePassword.setDisable(true);
            chkRecordar.setDisable(true);
            btnLogin.setText("Ingresando...");

            new Thread(() -> {
                try {
                    AuthService.getInstance().login(email, password);

                    Platform.runLater(() -> {
                        if (chkRecordar.isSelected()) {
                            prefs.put("login_email", email);
                            try {
                                Keyring.create().setPassword("PackingListApp", email, password);
                            } catch (Exception exK) {
                                System.err.println("Error guardando credencial segura: " + exK.getMessage());
                            }
                        } else {
                            prefs.remove("login_email");
                            try {
                                Keyring.create().deletePassword("PackingListApp", email);
                            } catch (Exception exK) {}
                        }
                        onLoginSuccess.run();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        // Re-habilitar controles
                        btnLogin.setDisable(false);
                        txtEmail.setDisable(false);
                        txtPassword.setDisable(false);
                        txtPasswordVisible.setDisable(false);
                        btnTogglePassword.setDisable(false);
                        chkRecordar.setDisable(false);
                        btnLogin.setText("Iniciar sesión");

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error de inicio de sesión");
                        alert.setHeaderText(null);
                        alert.setContentText(ex.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        });

        // Evento enter para el password field
        txtPassword.setOnAction(e -> btnLogin.fire());
        txtPasswordVisible.setOnAction(e -> btnLogin.fire());
 
        getChildren().addAll(imgLogo, lblTitulo, txtEmail, passwordContainer, chkRecordar, btnLogin);
    }
}
