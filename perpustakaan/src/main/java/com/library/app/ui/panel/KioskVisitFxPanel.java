package com.library.app.ui.panel;

import com.library.app.service.VisitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class KioskVisitFxPanel {
    private final VisitService visitService = new VisitService();

    public Node createContent(Runnable onBack) {
        VBox content = new VBox(10);
        content.getStyleClass().add("visit-content");
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(460);

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

        Label title = new Label("Absen Kunjungan");
        title.getStyleClass().add("visit-title");

        Label subtitle = new Label("Masukkan NIM/NIS Anda untuk mencatat kehadiran");
        subtitle.getStyleClass().add("visit-subtitle");
        subtitle.setTextAlignment(TextAlignment.CENTER);

        Label memberCodeLabel = new Label("NIM / NIS");
        memberCodeLabel.getStyleClass().add("visit-label");

        TextField memberCodeField = new TextField();
        memberCodeField.getStyleClass().add("visit-input");
        memberCodeField.setPromptText("Contoh: 2021001001");
        memberCodeField.setMaxWidth(Double.MAX_VALUE);

        Button submitButton = new Button("Catat Kehadiran");
        submitButton.getStyleClass().add("visit-submit-button");
        submitButton.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("visit-status-label");
        statusLabel.setTextAlignment(TextAlignment.CENTER);
        statusLabel.setWrapText(true);
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        Runnable submitAction = () -> submitVisit(memberCodeField, statusLabel);
        submitButton.setOnAction(event -> submitAction.run());
        memberCodeField.setOnAction(event -> submitAction.run());

        VBox formBox = new VBox(8, memberCodeLabel, memberCodeField, submitButton);
        formBox.getStyleClass().add("visit-form-box");
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setFillWidth(true);
        formBox.setPrefWidth(320);
        formBox.setMaxWidth(320);

        Label backLabel = new Label("Kembali");
        backLabel.getStyleClass().add("visit-back-link");
        backLabel.setCursor(Cursor.HAND);
        backLabel.setOnMouseClicked(event -> onBack.run());

        VBox.setMargin(title, new Insets(8, 0, 0, 0));
        VBox.setMargin(subtitle, new Insets(2, 0, 16, 0));
        VBox.setMargin(backLabel, new Insets(18, 0, 0, 0));

        content.getChildren().addAll(visitIconShell, title, subtitle, formBox, statusLabel, backLabel);

        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(20, 16, 22, 16));
        return wrapper;
    }

    private void submitVisit(TextField memberCodeField, Label statusLabel) {
        statusLabel.getStyleClass().removeAll("visit-status-success", "visit-status-error");

        try {
            visitService.recordMemberVisit(memberCodeField.getText());
            statusLabel.getStyleClass().add("visit-status-success");
            statusLabel.setText("Kunjungan berhasil dicatat.");
            memberCodeField.clear();
        } catch (Exception exception) {
            statusLabel.getStyleClass().add("visit-status-error");
            statusLabel.setText(exception.getMessage());
        }

        statusLabel.setManaged(true);
        statusLabel.setVisible(true);
    }
}
