package com.library.app.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public final class FxFeedback {
    private FxFeedback() {
    }

    public static StackPane resolveHost(Node node) {
        if (node == null) {
            return null;
        }

        if (node.getScene() != null && node.getScene().getRoot() instanceof StackPane stackPane) {
            return stackPane;
        }

        if (node instanceof StackPane stackPane) {
            return stackPane;
        }

        return null;
    }

    public static void showSuccessToast(StackPane host, String message) {
        showToast(host, message, true, Pos.TOP_RIGHT, new Insets(22, 22, 0, 0), false);
    }

    public static void showSuccessToast(StackPane host, String message, Insets margin) {
        showToast(host, message, true, Pos.TOP_RIGHT, margin, false);
    }

    public static void showErrorToast(StackPane host, String message) {
        showToast(host, message, false, Pos.TOP_RIGHT, new Insets(22, 22, 0, 0), false);
    }

    public static void showErrorToast(StackPane host, String message, Pos alignment, Insets margin) {
        showToast(host, message, false, alignment, margin, false);
    }

    public static void showSuccessToastCentered(StackPane host, String message) {
        showToast(host, message, true, Pos.CENTER, Insets.EMPTY, true);
    }

    public static void showErrorToastCentered(StackPane host, String message) {
        showToast(host, message, false, Pos.CENTER, Insets.EMPTY, true);
    }

    private static void showToast(StackPane host, String message, boolean success, Pos alignment, Insets margin, boolean compact) {
    if (host == null) {
        return;
    }

    host.getChildren().removeIf(node -> Boolean.TRUE.equals(node.getProperties().get("fx-toast")));

    // Container toast
    HBox toast = new HBox(compact ? 10 : 12);
    toast.getProperties().put("fx-toast", true);
    toast.setAlignment(Pos.CENTER_LEFT);
    toast.setMaxWidth(Region.USE_PREF_SIZE);
    toast.setMaxHeight(Region.USE_PREF_SIZE);
    toast.setOpacity(0);

    // Style mengikuti contoh kedua
    String bgColor = success ? "#ecfdf5" : "#fef2f2";
    String borderColor = success ? "#a7f3d0" : "#fecaca";
    String iconColor = success ? "#10b981" : "#ef4444";
    String textColor = success ? "#065f46" : "#991b1b";

    toast.setStyle(
        "-fx-background-color: " + bgColor + "; " +
        "-fx-border-color: " + borderColor + "; " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: 6; " +
        "-fx-background-radius: 6; " +
        "-fx-padding: " + (compact ? "10 12;" : "12 16;")
    );

    // Icon
    Label icon = new Label(success ? "✓" : "✕");
    icon.setStyle(
        "-fx-text-fill: " + iconColor + "; " +
        "-fx-font-size: " + (compact ? "14px;" : "15px;") +
        "-fx-font-weight: bold;"
    );

    // Text
    Label text = new Label(message == null ? "" : message);
    text.setWrapText(true);
    text.setMaxWidth(compact ? 220 : 260);
    text.setStyle(
        "-fx-text-fill: " + textColor + "; " +
        "-fx-font-size: " + (compact ? "13px;" : "14px;") +
        "-fx-font-weight: 500;"
    );

    // Spacer
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    spacer.setMinWidth(compact ? 24 : 40);

    // Close button
    Label closeButton = new Label("✕");
    closeButton.setStyle(
        "-fx-text-fill: " + iconColor + "; " +
        "-fx-font-size: " + (compact ? "13px;" : "14px;") +
        "-fx-cursor: hand;"
    );

    toast.getChildren().addAll(icon, text, spacer, closeButton);

    // Posisi default mengikuti fungsi kedua
    StackPane.setAlignment(toast, alignment == null ? Pos.TOP_RIGHT : alignment);
    StackPane.setMargin(toast, margin == null ? new Insets(20, 24, 0, 0) : margin);

    host.getChildren().add(toast);

    // Animasi masuk
    TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
    slideIn.setFromY(-40);
    slideIn.setToY(0);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);

    ParallelTransition ptIn = new ParallelTransition(slideIn, fadeIn);

    // Animasi keluar
    TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toast);
    slideOut.setByY(-40);

    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
    fadeOut.setToValue(0);

    ParallelTransition ptOut = new ParallelTransition(slideOut, fadeOut);
    ptOut.setOnFinished(e -> host.getChildren().remove(toast));

    PauseTransition delay = new PauseTransition(Duration.millis(3500));
    delay.setOnFinished(e -> ptOut.play());

    closeButton.setOnMouseClicked(e -> {
        delay.stop();
        ptOut.play();
    });

    ptIn.setOnFinished(e -> delay.play());
    ptIn.play();
}

    public static Label createFieldErrorLabel() {
        Label label = new Label();
        label.getStyleClass().add("fx-field-error");
        label.setManaged(false);
        label.setVisible(false);
        label.setWrapText(true);
        return label;
    }

    public static void clearFieldError(TextInputControl input, Label errorLabel) {
        if (input != null) {
            input.getStyleClass().remove("input-error");
        }

        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setManaged(false);
            errorLabel.setVisible(false);
        }
    }

    public static void showFieldError(TextInputControl input, Label errorLabel, String message) {
        if (input != null && !input.getStyleClass().contains("input-error")) {
            input.getStyleClass().add("input-error");
        }

        if (errorLabel != null) {
            errorLabel.setText(message == null ? "" : message);
            errorLabel.setManaged(true);
            errorLabel.setVisible(true);
        }
    }

    public static void showDetailDialog(StackPane host, String title, List<String[]> rows) {
        if (host == null) {
            return;
        }

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("fx-detail-overlay");
        overlay.setAlignment(Pos.CENTER);

        VBox card = new VBox(18);
        card.getStyleClass().add("fx-detail-card");
        card.setMaxWidth(430);

        Label titleLabel = new Label(title == null ? "" : title);
        titleLabel.getStyleClass().add("fx-detail-title");

        VBox body = new VBox(10);
        if (rows != null) {
            for (String[] row : rows) {
                String keyText = row != null && row.length > 0 ? row[0] : "";
                String valueText = row != null && row.length > 1 ? row[1] : "";

                HBox item = new HBox(10);
                item.setAlignment(Pos.TOP_LEFT);

                Label key = new Label(keyText);
                key.getStyleClass().add("fx-detail-key");
                key.setMinWidth(90);

                Label value = new Label(valueText);
                value.getStyleClass().add("fx-detail-value");
                value.setWrapText(true);

                item.getChildren().addAll(key, value);
                body.getChildren().add(item);
            }
        }

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("fx-detail-button");
        okButton.setOnAction(event -> host.getChildren().remove(overlay));

        footer.getChildren().add(okButton);
        card.getChildren().addAll(titleLabel, body, footer);
        overlay.getChildren().add(card);

        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                host.getChildren().remove(overlay);
            }
        });

        host.getChildren().add(overlay);
    }
}
