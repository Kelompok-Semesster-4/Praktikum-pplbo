package com.library.app.ui.panel;

import com.library.app.service.VisitService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class KioskVisitPanel {
    private static final double VISIT_CARD_WIDTH = 560;
    private static final double VISIT_FORM_WIDTH = 400;
    private final VisitService visitService = new VisitService();

    public Node createContent(Runnable onBack) {
        VBox content = new VBox(10);
        content.getStyleClass().add("visit-content");
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(640);

        Node visitIcon = KioskIconFactory.createVisitIcon(Color.web("#3B82F6"));
        visitIcon.setScaleX(1.0);
        visitIcon.setScaleY(1.0);

        StackPane visitIconShell = new StackPane(visitIcon);
        visitIconShell.getStyleClass().add("visit-icon-shell");
        visitIconShell.setAlignment(Pos.CENTER);
        visitIconShell.setMinSize(46, 46);
        visitIconShell.setPrefSize(46, 46);
        visitIconShell.setMaxSize(46, 46);
        StackPane.setAlignment(visitIcon, Pos.CENTER);

        Label title = new Label("Absen Masuk / Keluar");
        title.getStyleClass().add("visit-title");

        Label subtitle = new Label("Scan pertama untuk masuk, scan kedua untuk keluar (selesai).\nMasukkan NIM/NIS/NIDN Anda.");
        subtitle.getStyleClass().add("visit-subtitle");
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(500);
        subtitle.setAlignment(Pos.CENTER);

        Label memberCodeLabel = new Label("NIM / NIS / NIDN");
        memberCodeLabel.getStyleClass().add("visit-label");

        TextField memberCodeField = new TextField();
        memberCodeField.getStyleClass().add("visit-input");
        memberCodeField.setPromptText("Contoh: 09000000000001");
        memberCodeField.setPrefWidth(VISIT_FORM_WIDTH);
        memberCodeField.setMaxWidth(VISIT_FORM_WIDTH);

        Button submitButton = new Button("Scan Masuk / Keluar");
        submitButton.getStyleClass().add("visit-submit-button");
        submitButton.setPrefWidth(VISIT_FORM_WIDTH);
        submitButton.setMaxWidth(VISIT_FORM_WIDTH);

        Label toastIcon = new Label("✓");
        toastIcon.getStyleClass().add("visit-inline-toast-icon");

        Label toastMessage = new Label();
        toastMessage.getStyleClass().add("visit-inline-toast-message");
        toastMessage.setWrapText(true);

        Region toastSpacer = new Region();
        HBox.setHgrow(toastSpacer, Priority.ALWAYS);

        Label toastClose = new Label("✕");
        toastClose.getStyleClass().add("visit-inline-toast-close");

        HBox inlineToast = new HBox(10, toastIcon, toastMessage, toastSpacer, toastClose);
        inlineToast.getStyleClass().addAll("visit-inline-toast", "visit-inline-toast-success");
        inlineToast.setAlignment(Pos.CENTER_LEFT);
        inlineToast.setVisible(false);
        inlineToast.setManaged(false);
        inlineToast.setOpacity(0);
        inlineToast.setPrefWidth(VISIT_FORM_WIDTH);
        inlineToast.setMaxWidth(VISIT_FORM_WIDTH);

        toastClose.setOnMouseClicked(event -> hideInlineToast(inlineToast));

        Runnable submitAction = () -> submitVisit(memberCodeField, inlineToast, toastIcon, toastMessage);
        submitButton.setOnAction(event -> submitAction.run());
        memberCodeField.setOnAction(event -> submitAction.run());

        VBox headerBox = new VBox(8, visitIconShell, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        VBox formFields = new VBox(8, memberCodeLabel, memberCodeField, submitButton, inlineToast);
        formFields.setAlignment(Pos.CENTER_LEFT);
        formFields.setFillWidth(true);
        formFields.setPrefWidth(VISIT_FORM_WIDTH);
        formFields.setMaxWidth(VISIT_FORM_WIDTH);

        VBox formBox = new VBox(8, headerBox, formFields);
        formBox.getStyleClass().addAll("visit-form-box", "visit-feature-card");
        formBox.setAlignment(Pos.CENTER);
        formBox.setFillWidth(false);
        formBox.setPrefWidth(VISIT_CARD_WIDTH);
        formBox.setMaxWidth(VISIT_CARD_WIDTH);

        Label backLabel = new Label("Kembali");
        backLabel.getStyleClass().add("visit-back-link");
        backLabel.setCursor(Cursor.HAND);
        backLabel.setOnMouseClicked(event -> onBack.run());

        HBox backRow = new HBox(backLabel);
        backRow.setAlignment(Pos.CENTER);
        formBox.getChildren().add(backRow);

        VBox.setMargin(headerBox, new Insets(2, 0, 8, 0));
        VBox.setMargin(backRow, new Insets(8, 0, 0, 0));

        content.getChildren().add(formBox);

        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(20, 16, 22, 16));
        return wrapper;
    }

    private void submitVisit(TextField memberCodeField, HBox inlineToast, Label toastIcon, Label toastMessage) {
        try {
            String message = visitService.recordMemberVisit(memberCodeField.getText());
            showInlineToast(inlineToast, toastIcon, toastMessage, message, true);
            memberCodeField.clear();
        } catch (Exception exception) {
            showInlineToast(inlineToast, toastIcon, toastMessage, exception.getMessage(), false);
        }
    }

    private void showInlineToast(HBox toast, Label iconLabel, Label messageLabel, String message, boolean success) {
        toast.getStyleClass().removeAll("visit-inline-toast-success", "visit-inline-toast-error");
        toast.getStyleClass().add(success ? "visit-inline-toast-success" : "visit-inline-toast-error");

        iconLabel.setText(success ? "✓" : "✕");
        messageLabel.setText(message == null ? "" : message);

        toast.setManaged(true);
        toast.setVisible(true);
        toast.setOpacity(0);
        toast.setTranslateY(8);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(220), toast);
        slideIn.setFromY(8);
        slideIn.setToY(0);

        ParallelTransition in = new ParallelTransition(fadeIn, slideIn);
        in.play();

        if (success) {
            PauseTransition delay = new PauseTransition(Duration.millis(3000));
            delay.setOnFinished(event -> hideInlineToast(toast));
            delay.play();
        }
    }

    private void hideInlineToast(HBox toast) {
        if (!toast.isVisible()) {
            return;
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), toast);
        fadeOut.setFromValue(toast.getOpacity());
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(180), toast);
        slideOut.setFromY(toast.getTranslateY());
        slideOut.setToY(6);

        ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
        out.setOnFinished(event -> {
            toast.setVisible(false);
            toast.setManaged(false);
            toast.setTranslateY(0);
        });
        out.play();
    }
}
