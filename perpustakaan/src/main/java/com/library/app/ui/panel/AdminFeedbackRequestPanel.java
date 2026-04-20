package com.library.app.ui.panel;

import com.library.app.model.Feedback;
import com.library.app.model.ProcurementRequest;
import com.library.app.model.enums.FeedbackStatus;
import com.library.app.model.enums.RequestStatus;
import com.library.app.service.FeedbackService;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AdminFeedbackRequestPanel {
    private enum TabType {
        FEEDBACK, PROCUREMENT
    }

    private final FeedbackService feedbackService = new FeedbackService();
    private final ProcurementService procurementService = new ProcurementService();
    private final DateTimeFormatter listFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));
    private final DateTimeFormatter detailFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy • HH:mm",
            new Locale("id", "ID"));

    private final BorderPane root = new BorderPane();
    private final VBox listContainer = new VBox(10);
    private final VBox detailContainer = new VBox();
    private final Button feedbackTabButton = new Button();
    private final Button procurementTabButton = new Button();
    private final Label sectionTitle = new Label();
    private final Label sectionSubtitle = new Label();

    private boolean initialized = false;
    private TabType activeTab = TabType.FEEDBACK;
    private List<Feedback> feedbacks = List.of();
    private List<ProcurementRequest> requests = List.of();
    private Feedback selectedFeedback;
    private ProcurementRequest selectedRequest;

    public Node create() {
        if (!initialized) {
            root.setPadding(Insets.EMPTY);
            root.setCenter(buildContent());
            initialized = true;
        }
        refreshData();
        return root;
    }

    public void refreshData() {
        feedbacks = feedbackService.findAll();
        requests = procurementService.findAll();

        if (activeTab == TabType.FEEDBACK) {
            if (selectedFeedback != null) {
                selectedFeedback = feedbacks.stream()
                        .filter(item -> item.getId().equals(selectedFeedback.getId()))
                        .findFirst()
                        .orElse(feedbacks.isEmpty() ? null : feedbacks.get(0));
            } else if (!feedbacks.isEmpty()) {
                selectedFeedback = feedbacks.get(0);
            }
        } else {
            if (selectedRequest != null) {
                selectedRequest = requests.stream()
                        .filter(item -> item.getId().equals(selectedRequest.getId()))
                        .findFirst()
                        .orElse(requests.isEmpty() ? null : requests.get(0));
            } else if (!requests.isEmpty()) {
                selectedRequest = requests.get(0);
            }
        }

        renderTabButtons();
        renderBody();
    }

    private Node buildContent() {
        VBox wrapper = new VBox(16);
        wrapper.setPadding(Insets.EMPTY);
        wrapper.getChildren().addAll(buildHeader(), buildBodyArea());
        return wrapper;
    }

    private Node buildHeader() {
        VBox header = new VBox(8);
        header.setPadding(Insets.EMPTY);

        Label title = new Label("Feedback & Permintaan");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        Label subtitle = new Label("Kelola masukan pengguna kiosk dan tinjau usulan pengadaan buku dari satu halaman.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        HBox tabRow = new HBox(12, feedbackTabButton, procurementTabButton);
        tabRow.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(title, subtitle, tabRow);
        return header;
    }

    private Node buildBodyArea() {
        HBox content = new HBox(18);
        content.setFillHeight(true);

        VBox leftPane = new VBox(14);
        leftPane.setPrefWidth(380);
        leftPane.setMinWidth(340);
        leftPane.setPadding(new Insets(22));
        leftPane.setStyle(panelStyle());

        sectionTitle.setStyle("-fx-font-size: 19px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");
        sectionSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        sectionSubtitle.setWrapText(true);

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.getStyleClass().add("app-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        leftPane.getChildren().addAll(sectionTitle, sectionSubtitle, scrollPane);

        detailContainer.setPadding(new Insets(24));
        detailContainer.setSpacing(16);
        detailContainer.setStyle(panelStyle());
        HBox.setHgrow(detailContainer, Priority.ALWAYS);

        content.getChildren().addAll(leftPane, detailContainer);
        return content;
    }

    private void renderTabButtons() {
        feedbackTabButton.setGraphic(buildTabGraphic(
                "Feedback Kiosk",
                countFeedbackNeedingAttention()));
        feedbackTabButton.setStyle(tabButtonStyle(activeTab == TabType.FEEDBACK));
        feedbackTabButton.setOnAction(event -> {
            activeTab = TabType.FEEDBACK;
            if (selectedFeedback == null && !feedbacks.isEmpty()) {
                selectedFeedback = feedbacks.get(0);
            }
            renderTabButtons();
            renderBody();
        });

        procurementTabButton.setGraphic(buildTabGraphic(
                "Permintaan Buku",
                countPendingRequests()));
        procurementTabButton.setStyle(tabButtonStyle(activeTab == TabType.PROCUREMENT));
        procurementTabButton.setOnAction(event -> {
            activeTab = TabType.PROCUREMENT;
            if (selectedRequest == null && !requests.isEmpty()) {
                selectedRequest = requests.get(0);
            }
            renderTabButtons();
            renderBody();
        });
    }

    private Node buildTabGraphic(String title, int badgeCount) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(titleLabel);

        if (badgeCount > 0) {
            Label badge = new Label(String.valueOf(badgeCount));
            badge.setStyle(
                    "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 800;" +
                            "-fx-background-radius: 999; -fx-padding: 4 8 4 8;");
            box.getChildren().add(badge);
        }

        return box;
    }

    private void renderBody() {
        if (activeTab == TabType.FEEDBACK) {
            sectionTitle.setText("Daftar Feedback");
            sectionSubtitle.setText(feedbacks.isEmpty()
                    ? "Belum ada feedback dari pengguna kiosk."
                    : feedbacks.size() + " feedback masuk. Pilih salah satu untuk melihat detail dan merespons.");
            renderFeedbackList();
            renderFeedbackDetail();
            return;
        }

        sectionTitle.setText("Daftar Permintaan Buku");
        sectionSubtitle.setText(requests.isEmpty()
                ? "Belum ada usulan pengadaan buku."
                : requests.size() + " usulan tercatat. Tinjau detail lalu setujui atau tolak.");
        renderRequestList();
        renderRequestDetail();
    }

    private void renderFeedbackList() {
        listContainer.getChildren().clear();

        if (feedbacks.isEmpty()) {
            listContainer.getChildren().add(createEmptyState("Belum ada feedback yang masuk."));
            return;
        }

        for (Feedback feedback : feedbacks) {
            Button itemButton = new Button();
            itemButton.setMaxWidth(Double.MAX_VALUE);
            itemButton.setAlignment(Pos.TOP_LEFT);
            itemButton.setWrapText(true);
            itemButton.setGraphic(buildFeedbackListItem(feedback));
            itemButton.setStyle(
                    listItemStyle(selectedFeedback != null && feedback.getId().equals(selectedFeedback.getId())));
            itemButton.setOnAction(event -> {
                selectedFeedback = feedback;
                renderFeedbackList();
                renderFeedbackDetail();
            });
            listContainer.getChildren().add(itemButton);
        }
    }

    private Node buildFeedbackListItem(Feedback feedback) {
        VBox box = new VBox(8);
        box.setFillWidth(true);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label subjectLabel = new Label(safeText(feedback.getSubject(), "Feedback Tanpa Judul"));
        subjectLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        subjectLabel.setWrapText(true);
        HBox.setHgrow(subjectLabel, Priority.ALWAYS);

        Label statusChip = buildStatusChip(feedback.getStatus());
        topRow.getChildren().addAll(subjectLabel, statusChip);

        Label ratingLabel = new Label(renderStars(feedback.getRating()));
        ratingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #F59E0B; -fx-font-weight: 700;");

        Label metaLabel = new Label(
                safeText(feedback.getSenderName(), "Pengguna kiosk") + " • " + formatDate(feedback.getCreatedAt()));
        metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Label previewLabel = new Label(trimText(feedback.getMessage(), 88));
        previewLabel.setWrapText(true);
        previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        box.getChildren().addAll(topRow, ratingLabel, metaLabel, previewLabel);
        return box;
    }

    private void renderFeedbackDetail() {
        detailContainer.getChildren().clear();

        if (selectedFeedback == null) {
            detailContainer.getChildren().add(createEmptyState("Pilih feedback untuk melihat detailnya."));
            return;
        }

        Label heading = new Label(safeText(selectedFeedback.getSubject(), "Feedback Tanpa Judul"));
        heading.setWrapText(true);
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        FlowPane metaRow = new FlowPane();
        metaRow.setHgap(10);
        metaRow.setVgap(10);
        metaRow.getChildren().addAll(
                buildSoftChip(toStatusLabel(selectedFeedback.getStatus())),
                buildSoftChip("Pengirim: " + safeText(selectedFeedback.getSenderName(), "Pengguna kiosk")),
                buildSoftChip("Rating: " + renderStars(selectedFeedback.getRating())));

        Label timeLabel = new Label("Dikirim pada " + formatDateTime(selectedFeedback.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        VBox messageCard = new VBox(10);
        messageCard.setPadding(new Insets(18));
        messageCard.setStyle(innerCardStyle());

        Label messageTitle = new Label("Isi Feedback");
        messageTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Label messageValue = new Label(safeText(selectedFeedback.getMessage(), "-"));
        messageValue.setWrapText(true);
        messageValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");

        messageCard.getChildren().addAll(messageTitle, messageValue);

        VBox responseCard = new VBox(10);
        responseCard.setPadding(new Insets(18));
        responseCard.setStyle(innerCardStyle());

        Label responseTitle = new Label("Respons / Tindak Lanjut Admin");
        responseTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        TextArea responseArea = new TextArea(safeText(selectedFeedback.getResponseNote(), ""));
        responseArea.setPromptText("Tulis respons atau catatan tindak lanjut untuk feedback ini");
        responseArea.setWrapText(true);
        responseArea.setPrefRowCount(6);
        responseArea.setStyle(textAreaStyle());

        if (selectedFeedback.getRespondedAt() != null) {
            Label respondedAt = new Label("Terakhir diperbarui: " + formatDateTime(selectedFeedback.getRespondedAt()));
            respondedAt.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            responseCard.getChildren().add(respondedAt);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button readButton = new Button(
                selectedFeedback.getStatus() == FeedbackStatus.NEW ? "Tandai Dibaca" : "Sudah Dibaca");
        readButton.setDisable(selectedFeedback.getStatus() != FeedbackStatus.NEW);
        readButton.setStyle(secondaryButtonStyle());
        readButton.setOnAction(event -> {
            feedbackService.markAsRead(selectedFeedback.getId());
            refreshData();
        });

        Button respondButton = new Button("Simpan Respons");
        respondButton.setStyle(primaryButtonStyle());
        respondButton.setOnAction(event -> {
            try {
                feedbackService.respond(selectedFeedback.getId(), responseArea.getText());
                refreshData();
                showInfo("Respons feedback berhasil disimpan.");
            } catch (IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        });

        HBox actionRow = new HBox(12, readButton, spacer, respondButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        responseCard.getChildren().addAll(responseTitle, responseArea, actionRow);

        detailContainer.getChildren().addAll(heading, metaRow, timeLabel, messageCard, responseCard);
    }

    private void renderRequestList() {
        listContainer.getChildren().clear();

        if (requests.isEmpty()) {
            listContainer.getChildren().add(createEmptyState("Belum ada permintaan pengadaan buku."));
            return;
        }

        for (ProcurementRequest request : requests) {
            Button itemButton = new Button();
            itemButton.setMaxWidth(Double.MAX_VALUE);
            itemButton.setAlignment(Pos.TOP_LEFT);
            itemButton.setGraphic(buildRequestListItem(request));
            itemButton.setStyle(
                    listItemStyle(selectedRequest != null && request.getId().equals(selectedRequest.getId())));
            itemButton.setOnAction(event -> {
                selectedRequest = request;
                renderRequestList();
                renderRequestDetail();
            });
            listContainer.getChildren().add(itemButton);
        }
    }

    private Node buildRequestListItem(ProcurementRequest request) {
        VBox box = new VBox(8);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(safeText(request.getTitle(), "Tanpa Judul"));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label statusChip = buildRequestStatusChip(request.getStatus());
        topRow.getChildren().addAll(titleLabel, statusChip);

        Label meta = new Label(joinMeta(
                safeText(request.getAuthor(), "Pengarang belum diisi"),
                safeText(request.getRequesterName(), "Pemohon tidak diketahui"),
                formatDate(request.getCreatedAt())));
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Label preview = new Label(trimText(request.getNote(), 90));
        preview.setWrapText(true);
        preview.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        box.getChildren().addAll(topRow, meta, preview);
        return box;
    }

    private void renderRequestDetail() {
        detailContainer.getChildren().clear();

        if (selectedRequest == null) {
            detailContainer.getChildren().add(createEmptyState("Pilih permintaan buku untuk melihat detailnya."));
            return;
        }

        Label heading = new Label(safeText(selectedRequest.getTitle(), "Tanpa Judul"));
        heading.setWrapText(true);
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        FlowPane metaRow = new FlowPane();
        metaRow.setHgap(10);
        metaRow.setVgap(10);
        metaRow.getChildren().addAll(
                buildSoftChip(toRequestStatusLabel(selectedRequest.getStatus())),
                buildSoftChip("Pemohon: " + safeText(selectedRequest.getRequesterName(), "-")),
                buildSoftChip(formatDate(selectedRequest.getCreatedAt())));

        GridPane detailGrid = new GridPane();
        detailGrid.setHgap(14);
        detailGrid.setVgap(12);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        left.setHgrow(Priority.ALWAYS);

        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        right.setHgrow(Priority.ALWAYS);

        detailGrid.getColumnConstraints().addAll(left, right);

        detailGrid.add(buildInfoCard("Pengarang", safeText(selectedRequest.getAuthor(), "-")), 0, 0);
        detailGrid.add(buildInfoCard("Penerbit", safeText(selectedRequest.getPublisher(), "-")), 1, 0);
        detailGrid.add(buildInfoCard(
                "Tahun Terbit",
                selectedRequest.getPublicationYear() == null ? "-"
                        : String.valueOf(selectedRequest.getPublicationYear())),
                0, 1);
        detailGrid.add(buildInfoCard("ISBN", safeText(selectedRequest.getIsbn(), "-")), 1, 1);
        detailGrid.add(buildInfoCard("Alasan Permintaan", safeText(selectedRequest.getNote(), "-")), 0, 2, 2, 1);

        VBox reviewCard = new VBox(10);
        reviewCard.setPadding(new Insets(18));
        reviewCard.setStyle(innerCardStyle());

        Label reviewTitle = new Label("Catatan Review Admin");
        reviewTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        TextArea noteArea = new TextArea(safeText(selectedRequest.getResponseNote(), ""));
        noteArea.setPromptText("Tulis catatan persetujuan atau alasan penolakan");
        noteArea.setWrapText(true);
        noteArea.setPrefRowCount(5);
        noteArea.setStyle(textAreaStyle());

        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button rejectButton = new Button("Tolak");
        rejectButton.setStyle(dangerButtonStyle());
        rejectButton.setOnAction(event -> reviewRequest(RequestStatus.REJECTED, noteArea.getText()));

        Button approveButton = new Button("Setujui");
        approveButton.setStyle(primaryButtonStyle());
        approveButton.setOnAction(event -> reviewRequest(RequestStatus.APPROVED, noteArea.getText()));

        buttonRow.getChildren().addAll(rejectButton, approveButton);

        if (selectedRequest.getRespondedAt() != null) {
            Label respondedAt = new Label("Terakhir ditinjau: " + formatDateTime(selectedRequest.getRespondedAt()));
            respondedAt.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            reviewCard.getChildren().add(respondedAt);
        }

        reviewCard.getChildren().addAll(reviewTitle, noteArea, buttonRow);

        detailContainer.getChildren().addAll(heading, metaRow, detailGrid, reviewCard);
    }

    private void reviewRequest(RequestStatus status, String responseNote) {
        if (selectedRequest == null) {
            return;
        }
        procurementService.reviewRequest(selectedRequest.getId(), status, responseNote);
        refreshData();
        showInfo(status == RequestStatus.APPROVED
                ? "Permintaan buku disetujui."
                : "Permintaan buku ditolak.");
    }

    private VBox buildInfoCard(String labelText, String valueText) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle(innerCardStyle());

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #64748B;");

        Label value = new Label(valueText);
        value.setWrapText(true);
        value.setStyle("-fx-font-size: 13px; -fx-text-fill: #0F172A;");

        card.getChildren().addAll(label, value);
        return card;
    }

    private Node createEmptyState(String message) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(36));
        box.setStyle(innerCardStyle());

        Label title = new Label("Belum Ada Data");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Label text = new Label(message);
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        box.getChildren().addAll(title, text);
        return box;
    }

    private Label buildStatusChip(FeedbackStatus status) {
        Label chip = new Label(toStatusLabel(status));
        chip.setStyle(statusChipStyle(
                status == FeedbackStatus.NEW ? "#FEE2E2" : status == FeedbackStatus.RESPONDED ? "#DCFCE7" : "#E0E7FF",
                status == FeedbackStatus.NEW ? "#B91C1C" : status == FeedbackStatus.RESPONDED ? "#166534" : "#1D4ED8"));
        return chip;
    }

    private Label buildRequestStatusChip(RequestStatus status) {
        Label chip = new Label(toRequestStatusLabel(status));
        String background = status == RequestStatus.PENDING ? "#FEF3C7"
                : status == RequestStatus.APPROVED ? "#DCFCE7"
                        : "#FEE2E2";
        String foreground = status == RequestStatus.PENDING ? "#92400E"
                : status == RequestStatus.APPROVED ? "#166534"
                        : "#B91C1C";
        chip.setStyle(statusChipStyle(background, foreground));
        return chip;
    }

    private Label buildSoftChip(String text) {
        Label chip = new Label(text);
        chip.setStyle(
                "-fx-background-color: #F1F5F9; -fx-text-fill: #334155; -fx-font-size: 12px;" +
                        "-fx-font-weight: 700; -fx-background-radius: 999; -fx-padding: 6 12 6 12;");
        return chip;
    }

    private String renderStars(Integer rating) {
        int safeRating = rating == null ? 0 : Math.max(0, Math.min(rating, 5));
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < 5; index++) {
            builder.append(index < safeRating ? '★' : '☆');
        }
        return builder.toString();
    }

    private int countFeedbackNeedingAttention() {
        return (int) feedbacks.stream()
                .filter(item -> item.getStatus() == FeedbackStatus.NEW)
                .count();
    }

    private int countPendingRequests() {
        return (int) requests.stream()
                .filter(item -> item.getStatus() == RequestStatus.PENDING)
                .count();
    }

    private String panelStyle() {
        return "-fx-background-color: white; -fx-background-radius: 22; -fx-border-radius: 22;" +
                "-fx-border-color: #E2E8F0; -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.06), 18, 0, 0, 8);";
    }

    private String innerCardStyle() {
        return "-fx-background-color: #F8FAFC; -fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: #E2E8F0;";
    }

    private String listItemStyle(boolean active) {
        return "-fx-background-color: " + (active ? "#EFF6FF" : "white") + ";" +
                "-fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: " +
                (active ? "#60A5FA" : "#E2E8F0") + "; -fx-padding: 16; -fx-cursor: hand;";
    }

    private String tabButtonStyle(boolean active) {
        return "-fx-background-color: " + (active ? "#DBEAFE" : "white") + ";" +
                "-fx-text-fill: #0F172A; -fx-font-weight: 700; -fx-background-radius: 999; -fx-border-radius: 999;" +
                "-fx-border-color: " + (active ? "#93C5FD" : "#E2E8F0")
                + "; -fx-padding: 10 16 10 16; -fx-cursor: hand;";
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700;" +
                "-fx-background-radius: 14; -fx-padding: 12 18 12 18; -fx-cursor: hand;";
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: #E2E8F0; -fx-text-fill: #0F172A; -fx-font-weight: 700;" +
                "-fx-background-radius: 14; -fx-padding: 12 18 12 18; -fx-cursor: hand;";
    }

    private String dangerButtonStyle() {
        return "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 700;" +
                "-fx-background-radius: 14; -fx-padding: 12 18 12 18; -fx-cursor: hand;";
    }

    private String textAreaStyle() {
        return "-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16;" +
                "-fx-border-color: #CBD5E1; -fx-padding: 12; -fx-font-size: 13px; -fx-text-fill: #0F172A;";
    }

    private String statusChipStyle(String background, String foreground) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + foreground + ";" +
                "-fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999; -fx-padding: 5 10 5 10;";
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "-" : value.format(listFormatter);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(detailFormatter);
    }

    private String trimText(String value, int maxLength) {
        String safe = safeText(value, "-");
        if (safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, maxLength).trim() + "...";
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String joinMeta(String first, String second, String third) {
        return first + " • " + second + " • " + third;
    }

    private String toStatusLabel(FeedbackStatus status) {
        if (status == null) {
            return "Belum Dibaca";
        }
        return switch (status) {
            case READ -> "Dibaca";
            case RESPONDED -> "Direspons";
            default -> "Belum Dibaca";
        };
    }

    private String toRequestStatusLabel(RequestStatus status) {
        if (status == null) {
            return "Menunggu";
        }
        return switch (status) {
            case APPROVED -> "Disetujui";
            case REJECTED -> "Ditolak";
            default -> "Menunggu";
        };
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}