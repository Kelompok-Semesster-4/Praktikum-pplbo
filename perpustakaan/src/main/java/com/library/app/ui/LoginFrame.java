package com.library.app.ui;

import com.library.app.model.User;
import com.library.app.model.enums.Role;
import com.library.app.service.AuthService;
import com.library.app.session.UserSession;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Objects;

public class LoginFrame {
    private final AuthService authService = new AuthService();
    private Stage stage;

    public void showOn(Stage stage) {
        this.stage = stage;
        stage.setTitle("Login - Sistem Manajemen Perpustakaan");
        stage.setFullScreen(false);
        stage.setMaximized(false);
        stage.setFullScreenExitHint("");

        // Container utama (Background abu-abu kebiruan)
        StackPane root = new StackPane();
        root.getStyleClass().add("app-root");

        // Kartu Login (Kiri: Info, Kanan: Form)
        HBox mainCard = new HBox();
        mainCard.setMaxSize(850, 500);
        mainCard.getStyleClass().add("login-card");

        mainCard.getChildren().addAll(createLeftPanel(), createRightPanel());
        root.getChildren().add(mainCard);

        Scene scene = new Scene(root, 1024, 700);
        
        // Memuat CSS
        try {
            scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/styles/login.css")).toExternalForm());
        } catch (Exception e) {
            System.out.println("Gagal memuat login.css: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }

    // --- PANEL KIRI (Branding & Fitur) ---
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(20);
        leftPanel.getStyleClass().add("left-panel");
        leftPanel.setPrefWidth(400);
        leftPanel.setPadding(new Insets(40));

        // Header dengan LOGO BUKU KOTAK (Sinkron dengan KioskFrame)
        HBox brandBox = new HBox(12, createLibraryLogo(28), new Label("Sistem Manajemen\nPerpustakaan"));
        brandBox.getStyleClass().add("brand-box");
        brandBox.setAlignment(Pos.CENTER_LEFT);

        Label welcomeTitle = new Label("Selamat Datang di\nSistem Perpustakaan");
        welcomeTitle.getStyleClass().add("welcome-title");

        Label welcomeDesc = new Label("Platform manajemen perpustakaan terpadu untuk pengelolaan buku, anggota, peminjaman, dan laporan operasional.");
        welcomeDesc.getStyleClass().add("welcome-desc");
        welcomeDesc.setWrapText(true);

        // Daftar Fitur dengan Icon SVG
        VBox featureList = new VBox(15);
        featureList.getChildren().addAll(
                createFeatureItem("M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5", "Manajemen koleksi buku & eksemplar"),
                createFeatureItem("M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z", "Pengelolaan anggota & kunjungan"),
                createFeatureItem("M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15", "Peminjaman & pengembalian otomatis"),
                createFeatureItem("M18 20V10M12 20V4M6 20v-6", "Laporan & statistik lengkap")
        );
        VBox.setMargin(featureList, new Insets(20, 0, 0, 0));

        leftPanel.getChildren().addAll(brandBox, welcomeTitle, welcomeDesc, featureList);
        return leftPanel;
    }

    private HBox createFeatureItem(String svgPath, String text) {
        Label label = new Label(text);
        label.getStyleClass().add("feature-text");
        HBox box = new HBox(12, createIcon(svgPath, Color.web("#93C5FD")), label);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // --- PANEL KANAN (Form Input) ---
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setPrefWidth(450);
        rightPanel.setPadding(new Insets(50, 40, 50, 40));
        rightPanel.setAlignment(Pos.CENTER_LEFT);

        Label loginTitle = new Label("Masuk ke Sistem");
        loginTitle.getStyleClass().add("login-title");

        Label loginSubtitle = new Label("Masukkan kredensial akun Anda untuk melanjutkan.");
        loginSubtitle.getStyleClass().add("login-subtitle");
        VBox.setMargin(loginSubtitle, new Insets(0, 0, 15, 0));

        // Field Username
        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("input-label");
        TextField userField = new TextField();
        userField.setPromptText("Masukkan username");
        userField.getStyleClass().add("transparent-input");
        HBox.setHgrow(userField, Priority.ALWAYS);
        HBox userBox = new HBox(10, createIcon("M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z", Color.web("#9CA3AF")), userField);
        userBox.getStyleClass().add("input-group");
        userBox.setAlignment(Pos.CENTER_LEFT);

        // Field Password
        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("input-label");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Masukkan password");
        passField.getStyleClass().add("transparent-input");
        HBox.setHgrow(passField, Priority.ALWAYS);
        HBox passBox = new HBox(10, createIcon("M19 11H5a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2zm-7 6v-2M7 11V7a5 5 0 0 1 10 0v4", Color.web("#9CA3AF")), passField);
        passBox.getStyleClass().add("input-group");
        passBox.setAlignment(Pos.CENTER_LEFT);

        Button btnLogin = new Button("Masuk");
        btnLogin.getStyleClass().add("btn-login");
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("login-status");
        statusLabel.setWrapText(true);
        statusLabel.setManaged(false);
        statusLabel.setVisible(false);

        Runnable loginAction = () -> authenticateAndNavigate(userField, passField, statusLabel);
        btnLogin.setOnAction(event -> loginAction.run());
        userField.setOnAction(event -> loginAction.run());
        passField.setOnAction(event -> loginAction.run());

        rightPanel.getChildren().addAll(loginTitle, loginSubtitle, userLabel, userBox, passLabel, passBox, btnLogin,
            statusLabel);
        return rightPanel;
    }

    private void authenticateAndNavigate(TextField userField, PasswordField passField, Label statusLabel) {
        if (stage == null) {
            statusLabel.setText("Halaman login belum siap. Silakan coba lagi.");
            statusLabel.setManaged(true);
            statusLabel.setVisible(true);
            return;
        }

        statusLabel.setManaged(false);
        statusLabel.setVisible(false);

        try {
            User user = authService.login(userField.getText(), passField.getText());
            UserSession session = new UserSession(user);

            if (session.getRole() == Role.ADMIN) {
                StageTransition.switchScene(stage, () -> new AdminFrame().showDashboard(stage));
            } else if (session.getRole() == Role.KIOSK) {
                StageTransition.switchScene(stage, () -> new KioskFrame(session).showOn(stage));
            } else {
                throw new IllegalStateException("Role pengguna tidak dikenali.");
            }
        } catch (Exception exception) {
            statusLabel.setText(exception.getMessage());
            statusLabel.setManaged(true);
            statusLabel.setVisible(true);
            passField.clear();
        }
    }

    // --- HELPER UNTUK LOGO & ICON ---

    // Logo buku kotak yang disinkronkan dengan KioskFrame
    private Node createLibraryLogo(double size) {
        double bookWidth = size * 0.42;
        double bookHeight = size * 0.62;

        Rectangle leftBook = new Rectangle(bookWidth, bookHeight);
        leftBook.setArcWidth(8); leftBook.setArcHeight(8);
        leftBook.setFill(Color.web("#E2E8F0")); 
        leftBook.setTranslateX(-bookWidth * 0.34);

        Rectangle rightBook = new Rectangle(bookWidth, bookHeight);
        rightBook.setArcWidth(8); rightBook.setArcHeight(8);
        rightBook.setFill(Color.web("#60A5FA"));
        rightBook.setTranslateX(bookWidth * 0.34);

        Rectangle spine = new Rectangle(size * 0.12, bookHeight);
        spine.setArcWidth(6); spine.setArcHeight(6);
        spine.setFill(Color.web("#FFFFFF"));
        
        StackPane icon = new StackPane(leftBook, rightBook, spine);
        icon.setPrefSize(size, size * 0.8);
        return icon;
    }

    private Group createIcon(String pathData, Color color) {
        SVGPath path = new SVGPath();
        path.setContent(pathData);
        path.setStroke(color);
        path.setStrokeWidth(1.5);
        path.setFill(Color.TRANSPARENT);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return new Group(path);
    }
}
