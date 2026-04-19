package com.library.app.ui.panel;

import com.library.app.service.FeedbackService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class KioskFeedbackFxPanel {
    private final FeedbackService feedbackService = new FeedbackService();

    public Node createContent(Runnable onBack) {
        VBox content = new VBox(10);
        content.getStyleClass().add("visit-content");
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(470);

        Node icon = KioskIconFactory.createFeedbackIcon(Color.web("#D97706"));
        StackPane iconShell = new StackPane(icon);
        iconShell.getStyleClass().add("visit-icon-shell");
        iconShell.setAlignment(Pos.CENTER);
        iconShell.setMinSize(46, 46);
        iconShell.setPrefSize(46, 46);
        iconShell.setMaxSize(46, 46);

        Label title = new Label("Beri Feedback");
        title.getStyleClass().add("visit-title");

        Label subtitle = new Label("Sampaikan saran, masukan, atau apresiasi Anda untuk layanan perpustakaan");
        subtitle.getStyleClass().add("visit-subtitle");
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(360);

        Label senderLabel = new Label("Nama");
        senderLabel.getStyleClass().add("visit-label");

        TextField senderField = new TextField();
        senderField.getStyleClass().add("visit-input");
        senderField.setPromptText("Masukkan nama Anda");

        Label subjectLabel = new Label("Subjek");
        subjectLabel.getStyleClass().add("visit-label");

        TextField subjectField = new TextField();
        subjectField.getStyleClass().add("visit-input");
        subjectField.setPromptText("Contoh: Saran koleksi / Pelayanan");

        Label ratingLabel = new Label("Rating");
        ratingLabel.getStyleClass().add("visit-label");

        ComboBox<Integer> ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(0, 1, 2, 3, 4, 5);
        ratingBox.setValue(0);
        ratingBox.getStyleClass().add("visit-input");
        ratingBox.setMaxWidth(Double.MAX_VALUE);

        Label messageLabel = new Label("Pesan");
        messageLabel.getStyleClass().add("visit-label");

        TextArea messageArea = new TextArea();
        messageArea.getStyleClass().add("visit-input");
        messageArea.setPromptText("Tuliskan feedback Anda di sini...");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(5);
        messageArea.setMaxWidth(Double.MAX_VALUE);

        Button submitButton = new Button("Kirim Feedback");
        submitButton.getStyleClass().add("visit-submit-button");
        submitButton.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("visit-status-label");
        statusLabel.setTextAlignment(TextAlignment.CENTER);
        statusLabel.setWrapText(true);
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        Runnable submitAction = () -> submitFeedback(
                senderField,
                subjectField,
                ratingBox,
                messageArea,
                statusLabel
        );

        submitButton.setOnAction(event -> submitAction.run());

        VBox formBox = new VBox(
                8,
                senderLabel, senderField,
                subjectLabel, subjectField,
                ratingLabel, ratingBox,
                messageLabel, messageArea,
                submitButton
        );
        formBox.getStyleClass().add("visit-form-box");
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setFillWidth(true);
        formBox.setPrefWidth(340);
        formBox.setMaxWidth(340);

        Label backLabel = new Label("Kembali");
        backLabel.getStyleClass().add("visit-back-link");
        backLabel.setCursor(Cursor.HAND);
        backLabel.setOnMouseClicked(event -> onBack.run());

        VBox.setMargin(title, new Insets(8, 0, 0, 0));
        VBox.setMargin(subtitle, new Insets(2, 0, 16, 0));
        VBox.setMargin(backLabel, new Insets(18, 0, 0, 0));

        content.getChildren().addAll(
                iconShell,
                title,
                subtitle,
                formBox,
                statusLabel,
                backLabel
        );

        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(20, 16, 22, 16));
        return wrapper;
    }

    private void submitFeedback(TextField senderField,
                                TextField subjectField,
                                ComboBox<Integer> ratingBox,
                                TextArea messageArea,
                                Label statusLabel) {
        statusLabel.getStyleClass().removeAll("visit-status-success", "visit-status-error");

        try {
            int rating = ratingBox.getValue() == null ? 0 : ratingBox.getValue();

            feedbackService.registerFeedback(
                    senderField.getText(),
                    subjectField.getText(),
                    rating,
                    messageArea.getText()
            );

            statusLabel.getStyleClass().add("visit-status-success");
            statusLabel.setText("Feedback berhasil dikirim. Terima kasih atas masukan Anda.");

            senderField.clear();
            subjectField.clear();
            ratingBox.setValue(0);
            messageArea.clear();
        } catch (Exception exception) {
            statusLabel.getStyleClass().add("visit-status-error");
            statusLabel.setText(exception.getMessage());
        }

        statusLabel.setManaged(true);
        statusLabel.setVisible(true);
    }
}