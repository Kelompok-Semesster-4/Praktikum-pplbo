package com.library.app.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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
        showToast(host, message, true);
    }

    public static void showErrorToast(StackPane host, String message) {
        showToast(host, message, false);
    }

    private static void showToast(StackPane host, String message, boolean success) {
        if (host == null) {
            return;
        }

        host.getChildren().removeIf(node -> Boolean.TRUE.equals(node.getProperties().get("fx-toast")));

        HBox toast = new HBox(12);
        toast.getProperties().put("fx-toast", true);
        toast.getStyleClass().addAll("fx-toast", success ? "fx-toast-success" : "fx-toast-error");
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setMaxWidth(380);
        toast.setOpacity(0);

        Label icon = new Label(success ? "✓" : "✕");
        icon.getStyleClass().add("fx-toast-icon");

        Label text = new Label(message == null ? "" : message);
        text.getStyleClass().add("fx-toast-text");
        text.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("fx-toast-close");
        closeButton.setOnAction(event -> host.getChildren().remove(toast));

        toast.getChildren().addAll(icon, text, spacer, closeButton);

        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        StackPane.setMargin(toast, new Insets(22, 22, 0, 0));
        host.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> host.getChildren().remove(toast));

        new SequentialTransition(fadeIn, pause, fadeOut).play();
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