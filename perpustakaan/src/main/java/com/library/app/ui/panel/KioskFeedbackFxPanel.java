package com.library.app.ui.panel;

import com.library.app.service.FeedbackService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class KioskFeedbackFxPanel {
    private final FeedbackService feedbackService = new FeedbackService();
    private static final double FEEDBACK_CARD_WIDTH = 560;
    private static final double FEEDBACK_FORM_WIDTH = 400;
    private int selectedRating = 0;
    private Label messageLabelRef;

    public Node createContent(Runnable onBack) {
        VBox content = new VBox(10);
        content.getStyleClass().add("visit-content");
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(640);

        Node icon = KioskIconFactory.createFeedbackIcon(Color.web("#D97706"));
        StackPane iconShell = new StackPane(icon);
        iconShell.getStyleClass().add("visit-icon-shell");
        iconShell.setAlignment(Pos.CENTER);
        iconShell.setMinSize(46, 46);
        iconShell.setPrefSize(46, 46);
        iconShell.setMaxSize(46, 46);

        Label title = new Label("Beri Feedback");
        title.getStyleClass().add("visit-title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Sampaikan saran dan masukan Anda");
        subtitle.getStyleClass().add("visit-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(500);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        Label toastIcon = new Label("\u2713");
        toastIcon.getStyleClass().add("visit-inline-toast-icon");

        Label toastMessage = new Label();
        toastMessage.getStyleClass().add("visit-inline-toast-message");
        toastMessage.setWrapText(true);

        Region toastSpacer = new Region();
        HBox.setHgrow(toastSpacer, Priority.ALWAYS);

        Label toastClose = new Label("\u2715");
        toastClose.getStyleClass().add("visit-inline-toast-close");

        HBox inlineToast = new HBox(10, toastIcon, toastMessage, toastSpacer, toastClose);
        inlineToast.getStyleClass().addAll("visit-inline-toast", "visit-inline-toast-success");
        inlineToast.setAlignment(Pos.CENTER_LEFT);
        inlineToast.setVisible(false);
        inlineToast.setManaged(false);
        inlineToast.setOpacity(0);
        inlineToast.setPrefWidth(FEEDBACK_FORM_WIDTH);
        inlineToast.setMaxWidth(FEEDBACK_FORM_WIDTH);

        VBox headerBox = new VBox(8, iconShell, title, subtitle, inlineToast);
        headerBox.setAlignment(Pos.CENTER);

        Label senderNameLabel = new Label("Nama Pengirim");
        senderNameLabel.getStyleClass().add("visit-label");

        TextField senderNameField = new TextField();
        senderNameField.getStyleClass().add("visit-input");
        senderNameField.setPromptText("Masukkan nama Anda");

        Label ratingLabel = new Label("Rating");
        ratingLabel.getStyleClass().add("visit-label");

        HBox ratingStars = new HBox(8);
        ratingStars.setAlignment(Pos.CENTER_LEFT);

        StackPane[] starBoxes = new StackPane[5];
        Label[] stars = new Label[5];

        for (int i = 0; i < 5; i++) {
            final int ratingValue = i + 1;

            Label star = new Label("★");
            star.setFont(Font.font(16));
            stars[i] = star;

            StackPane starBox = new StackPane(star);
            starBox.setMinSize(36, 36);
            starBox.setPrefSize(36, 36);
            starBox.setMaxSize(36, 36);
            starBox.setAlignment(Pos.CENTER);
            starBox.setCursor(Cursor.HAND);

            starBox.setOnMouseClicked(event -> {
                selectedRating = ratingValue;
                updateStars(starBoxes, stars, selectedRating);
            });

            starBoxes[i] = starBox;
        }

        ratingStars.getChildren().addAll(starBoxes);
        updateStars(starBoxes, stars, selectedRating);

        Label subjectLabel = new Label("Subjek");
        subjectLabel.getStyleClass().add("visit-label");

        TextField subjectField = new TextField();
        subjectField.getStyleClass().add("visit-input");
        subjectField.setPromptText("Topik feedback Anda");

        Label messageLabel = new Label("Pesan (0/500)");
        messageLabel.getStyleClass().add("visit-label");
        this.messageLabelRef = messageLabel;

        TextArea messageArea = new TextArea();
        messageArea.getStyleClass().addAll("visit-input", "visit-message-area");
        messageArea.setPromptText("Tuliskan masukan Anda...");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(5);
        messageArea.setMaxWidth(Double.MAX_VALUE);

        messageArea.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 500) {
                messageArea.setText(oldText);
            } else {
                messageLabelRef.setText("Pesan (" + newText.length() + "/500)");
            }
        });

        Button submitButton = new Button("Kirim Feedback");
        submitButton.getStyleClass().addAll("visit-submit-button", "feedback-submit-button");
        senderNameField.setPrefWidth(FEEDBACK_FORM_WIDTH);
        senderNameField.setMaxWidth(FEEDBACK_FORM_WIDTH);

        subjectField.setPrefWidth(FEEDBACK_FORM_WIDTH);
        subjectField.setMaxWidth(FEEDBACK_FORM_WIDTH);

        messageArea.setPrefWidth(FEEDBACK_FORM_WIDTH);
        messageArea.setMaxWidth(FEEDBACK_FORM_WIDTH);

        submitButton.setPrefWidth(FEEDBACK_FORM_WIDTH);
        submitButton.setMaxWidth(FEEDBACK_FORM_WIDTH);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("app-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        toastClose.setOnMouseClicked(event -> hideInlineToast(inlineToast));

        submitButton.setOnAction(event ->
                submitFeedback(
                        senderNameField,
                        subjectField,
                        messageArea,
                        inlineToast,
                        toastIcon,
                        toastMessage,
                        starBoxes,
                        stars,
                        scrollPane)
        );

        VBox formFields = new VBox(
                8,
                senderNameLabel, senderNameField,
                ratingLabel, ratingStars,
                subjectLabel, subjectField,
                messageLabel, messageArea,
                submitButton
        );
        formFields.setAlignment(Pos.CENTER_LEFT);
        formFields.setFillWidth(true);
        formFields.setPrefWidth(FEEDBACK_FORM_WIDTH);
        formFields.setMaxWidth(FEEDBACK_FORM_WIDTH);

        VBox formBox = new VBox(8, headerBox, formFields);
        formBox.getStyleClass().addAll("visit-form-box", "feedback-feature-card");
        formBox.setAlignment(Pos.CENTER);
        formBox.setFillWidth(false);
        formBox.setPrefWidth(FEEDBACK_CARD_WIDTH);
        formBox.setMaxWidth(FEEDBACK_CARD_WIDTH);

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
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(20, 16, 22, 16));
        scrollPane.viewportBoundsProperty()
                .addListener((observable, oldBounds, bounds) -> wrapper.setMinHeight(bounds.getHeight()));
        scrollPane.setContent(wrapper);
        return scrollPane;
    }

    private void updateStars(StackPane[] starBoxes, Label[] stars, int selectedRating) {
        for (int i = 0; i < stars.length; i++) {
            boolean active = i < selectedRating;

            if (active) {
                starBoxes[i].setStyle(
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #F2B233;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1.2;"
                );

                stars[i].setStyle(
                        "-fx-text-fill: #F4A300;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
                );
            } else {
                starBoxes[i].setStyle(
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
                );

                stars[i].setStyle(
                        "-fx-text-fill: #C9CED6;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
                );
            }
        }
    }

    private void submitFeedback(TextField senderNameField,
                                TextField subjectField,
                                TextArea messageArea,
                                HBox inlineToast,
                                Label toastIcon,
                                Label toastMessage,
                                StackPane[] starBoxes,
                                Label[] stars,
                                ScrollPane scrollPane) {
        try {
            feedbackService.registerFeedback(
                    senderNameField.getText(),
                    subjectField.getText(),
                    selectedRating,
                    messageArea.getText()
            );

            showInlineToast(
                    inlineToast,
                    toastIcon,
                    toastMessage,
                    "Feedback berhasil dikirim. Terima kasih atas masukan Anda.",
                    true,
                    scrollPane
            );

            senderNameField.clear();
            subjectField.clear();
            messageArea.clear();
            selectedRating = 0;
            messageLabelRef.setText("Pesan (0/500)");
            updateStars(starBoxes, stars, selectedRating);
        } catch (Exception exception) {
            showInlineToast(inlineToast, toastIcon, toastMessage, exception.getMessage(), false, scrollPane);
        }
    }

    private void showInlineToast(HBox toast,
                                 Label iconLabel,
                                 Label messageLabel,
                                 String message,
                                 boolean success,
                                 ScrollPane scrollPane) {
        toast.getStyleClass().removeAll("visit-inline-toast-success", "visit-inline-toast-error");
        toast.getStyleClass().add(success ? "visit-inline-toast-success" : "visit-inline-toast-error");

        iconLabel.setText(success ? "\u2713" : "!");
        messageLabel.setText(message == null ? "" : message);

        toast.setManaged(true);
        toast.setVisible(true);
        toast.setOpacity(0);
        toast.setTranslateY(8);
        scrollToTop(scrollPane);

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

    private void scrollToTop(ScrollPane scrollPane) {
        if (scrollPane == null) {
            return;
        }
        Platform.runLater(() -> scrollPane.setVvalue(0));
    }
}
