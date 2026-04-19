package com.library.app.ui.panel;

import com.library.app.model.Member;
import com.library.app.service.MemberService;
import com.library.app.service.ProcurementService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KioskProcurementFxPanel {
    private final MemberService memberService = new MemberService();
    private final ProcurementService procurementService = new ProcurementService();

    private TextField memberCodeField;
    private Label memberNameHint;
    private TextField titleField;
    private TextField authorField;
    private TextField publisherField;
    private TextField publicationYearField;
    private TextField isbnField;
    private TextArea reasonArea;

    private Member selectedMember;

    public Node createContent(Runnable onBack) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(8, 12, 8, 12));

        ScrollPane scrollPane = new ScrollPane(buildFormCard(onBack));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scrollPane);
        return root;
    }

    private Node buildFormCard(Runnable onBack) {
        VBox wrapper = new VBox(18);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(10, 0, 24, 0));

        VBox card = new VBox(16);
        card.setMaxWidth(860);
        card.setPrefWidth(860);
        card.setPadding(new Insets(28));
        card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 26; " +
                "-fx-border-radius: 26; " +
                "-fx-border-color: #E5E7EB; " +
                "-fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.08), 22, 0, 0, 10);"
        );

        Label title = new Label("Usulan Pengadaan Buku");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Label subtitle = new Label(
                "Ajukan buku yang ingin tersedia di perpustakaan. Lengkapi data buku agar admin mudah meninjau permintaan."
        );
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(14);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        left.setHgrow(Priority.ALWAYS);

        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        right.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(left, right);

        memberCodeField = createField("Masukkan NIM / NIS / NIDN");
        memberCodeField.textProperty().addListener((obs, oldValue, newValue) -> {
            selectedMember = null;
            memberNameHint.setText("Cocokkan kode anggota untuk mengambil nama anggota.");
            memberNameHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        });

        memberNameHint = new Label("Cocokkan kode anggota untuk mengambil nama anggota.");
        memberNameHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Button checkMemberButton = new Button("Cek Anggota");
        checkMemberButton.setStyle(primaryButtonStyle());
        checkMemberButton.setOnAction(event -> lookupMember());

        HBox memberActionRow = new HBox(10, memberCodeField, checkMemberButton);
        HBox.setHgrow(memberCodeField, Priority.ALWAYS);

        VBox memberBlock = new VBox(8,
                createFieldLabel("NIM / NIS / NIDN"),
                memberActionRow,
                memberNameHint
        );

        titleField = createField("Contoh: Clean Code");
        authorField = createField("Contoh: Robert C. Martin");
        publisherField = createField("Opsional");
        publicationYearField = createField("Opsional");
        isbnField = createField("10 atau 13 digit, opsional");
        reasonArea = createArea("Jelaskan alasan buku ini dibutuhkan");

        grid.add(memberBlock, 0, 0);
        grid.add(buildFieldBox("Judul Buku", titleField), 1, 0);
        grid.add(buildFieldBox("Pengarang", authorField), 0, 1);
        grid.add(buildFieldBox("Penerbit", publisherField), 1, 1);
        grid.add(buildFieldBox("Tahun Terbit", publicationYearField), 0, 2);
        grid.add(buildFieldBox("ISBN", isbnField), 1, 2);

        VBox reasonBox = buildFieldBox("Alasan Permintaan", reasonArea);
        VBox.setVgrow(reasonArea, Priority.ALWAYS);

        Label helper = new Label(
                "Catatan: judul, pengarang, dan alasan permintaan sebaiknya diisi sejelas mungkin agar proses review lebih cepat."
        );
        helper.setWrapText(true);
        helper.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("Kembali");
        backButton.setStyle(secondaryButtonStyle());
        backButton.setOnAction(event -> onBack.run());

        Button submitButton = new Button("Kirim Usulan");
        submitButton.setStyle(primaryButtonStyle());
        submitButton.setOnAction(event -> submit());

        HBox actionRow = new HBox(12, backButton, spacer, submitButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, subtitle, grid, reasonBox, helper, actionRow);
        wrapper.getChildren().add(card);
        return wrapper;
    }

    private void lookupMember() {
        try {
            selectedMember = memberService.findByCode(memberCodeField.getText());
            memberNameHint.setText("Anggota ditemukan: " + selectedMember.getName());
            memberNameHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #0F766E; -fx-font-weight: 700;");
        } catch (IllegalArgumentException exception) {
            selectedMember = null;
            memberNameHint.setText(exception.getMessage());
            memberNameHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #DC2626; -fx-font-weight: 700;");
        }
    }

    private void submit() {
        try {
            if (selectedMember == null) {
                lookupMember();
            }

            if (selectedMember == null) {
                throw new IllegalArgumentException("Kode anggota harus valid dan terdaftar.");
            }

            Integer publicationYear = parseYear(publicationYearField.getText());

            procurementService.registerRequest(
                    selectedMember.getId(),
                    selectedMember.getName(),
                    titleField.getText(),
                    authorField.getText(),
                    publisherField.getText(),
                    publicationYear,
                    isbnField.getText(),
                    reasonArea.getText()
            );

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Usulan buku berhasil dikirim. Admin akan meninjau permintaan Anda.",
                    ButtonType.OK
            ).showAndWait();

            clearForm();
        } catch (IllegalArgumentException exception) {
            new Alert(Alert.AlertType.ERROR, exception.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private Integer parseYear(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Tahun terbit harus berupa angka.");
        }
    }

    private void clearForm() {
        memberCodeField.clear();
        titleField.clear();
        authorField.clear();
        publisherField.clear();
        publicationYearField.clear();
        isbnField.clear();
        reasonArea.clear();

        selectedMember = null;
        memberNameHint.setText("Cocokkan kode anggota untuk mengambil nama anggota.");
        memberNameHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
    }

    private VBox buildFieldBox(String labelText, Node input) {
        VBox box = new VBox(8, createFieldLabel(labelText), input);
        VBox.setVgrow(input, Priority.ALWAYS);
        return box;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        return label;
    }

    private TextField createField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setStyle(inputStyle());
        return field;
    }

    private TextArea createArea(String promptText) {
        TextArea area = new TextArea();
        area.setPromptText(promptText);
        area.setPrefRowCount(6);
        area.setWrapText(true);
        area.setStyle(inputStyle() + "-fx-padding: 14 16 14 16;");
        return area;
    }

    private String inputStyle() {
        return "-fx-background-color: #F9FAFB; " +
               "-fx-background-radius: 16; " +
               "-fx-border-color: #D1D5DB; " +
               "-fx-border-radius: 16; " +
               "-fx-padding: 12 14 12 14; " +
               "-fx-font-size: 13px; " +
               "-fx-text-fill: #111827;";
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: #2563EB; " +
               "-fx-text-fill: white; " +
               "-fx-font-weight: 700; " +
               "-fx-background-radius: 16; " +
               "-fx-padding: 12 20 12 20; " +
               "-fx-cursor: hand;";
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: #E5E7EB; " +
               "-fx-text-fill: #111827; " +
               "-fx-font-weight: 700; " +
               "-fx-background-radius: 16; " +
               "-fx-padding: 12 20 12 20; " +
               "-fx-cursor: hand;";
    }
}