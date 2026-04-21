package com.library.app.ui.panel;

import com.library.app.ui.util.FxFeedback;
import com.library.app.dao.LoanDAO;
import com.library.app.model.Loan;
import com.library.app.model.Visit;
import com.library.app.model.enums.VisitType;
import com.library.app.service.LoanService;
import com.library.app.service.VisitService;
import com.library.app.util.DateUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoanManagementPanel {
    private enum LoanViewMode {
        ACTIVE,
        RETURNED,
        MEMBER_VISIT,
        GUEST_VISIT
    }

    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
    private static final double MODAL_WIDTH = 560;
    private static final double MODAL_HEIGHT = 360;

    private final LoanService loanService = new LoanService();
    private final LoanDAO loanDAO = new LoanDAO();
    private final VisitService visitService = new VisitService();

    private final ObservableList<LoanRow> allRows = FXCollections.observableArrayList();
    private final ObservableList<LoanRow> visibleRows = FXCollections.observableArrayList();

    private final ObservableList<VisitRow> allVisitRows = FXCollections.observableArrayList();
    private final ObservableList<VisitRow> visibleVisitRows = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final Label subtitleLabel = new Label();
    private final Label tableTitleLabel = new Label();

    private final Button primaryActionButton = new Button();
    private final Button activeTabButton = new Button("Peminjaman Aktif");
    private final Button returnedTabButton = new Button("Pengembalian");
    private final Button memberVisitTabButton = new Button("Kunjungan Mahasiswa");
    private final Button guestVisitTabButton = new Button("Absen Tamu");

    private final TableView<Object> mainTable = new TableView<>();

    private LoanViewMode currentMode = LoanViewMode.ACTIVE;

    private StackPane root;
    private StackPane modalOverlay;
    private StackPane modalHost;

    public Node create() {
        if (root == null) {
            VBox content = buildContent();
            root = new StackPane(content);
            root.getStyleClass().add("loan-management-root");
            StackPane.setAlignment(content, Pos.TOP_LEFT);

            configureTabButtons();
            bindSearch();
            switchMode(LoanViewMode.ACTIVE);
        }

        refreshData();
        return root;
    }

    public void refreshData() {
        if (currentMode == LoanViewMode.ACTIVE || currentMode == LoanViewMode.RETURNED) {
            loadLoanRows();
            applyLoanSearchFilter();
        } else {
            loadVisitRows();
            applyVisitSearchFilter();
        }
        updateSubtitle();
    }

    private VBox buildContent() {
        VBox content = new VBox(18);
        content.getStyleClass().add("loan-management-content");
        content.setPadding(Insets.EMPTY);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("loan-section-header");

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label("Peminjaman & Pengembalian");
        titleLabel.getStyleClass().add("section-title");
        subtitleLabel.getStyleClass().add("section-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        primaryActionButton.getStyleClass().add("loan-primary-action");
        primaryActionButton.setOnAction(event -> openPrimaryModal());

        header.getChildren().addAll(titleBox, spacer, primaryActionButton);

        HBox tabsRow = new HBox(10);
        tabsRow.getStyleClass().add("loan-tab-row");
        activeTabButton.getStyleClass().add("loan-tab-button");
        returnedTabButton.getStyleClass().add("loan-tab-button");
        memberVisitTabButton.getStyleClass().add("loan-tab-button");
        guestVisitTabButton.getStyleClass().add("loan-tab-button");
        tabsRow.getChildren().addAll(
                activeTabButton,
                returnedTabButton,
                memberVisitTabButton,
                guestVisitTabButton
        );

        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().addAll("list-card", "loan-toolbar-card");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(14, 16, 14, 16));

        searchField.setPromptText("Cari data...");
        searchField.getStyleClass().add("loan-search-input");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        toolbar.getChildren().add(searchField);

        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().addAll("list-card", "loan-table-card");
        tableCard.setPadding(new Insets(0));
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setPadding(new Insets(16, 16, 0, 16));
        tableTitleLabel.getStyleClass().add("card-title");
        tableHeader.getChildren().add(tableTitleLabel);

        mainTable.getStyleClass().add("loan-table");
        mainTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        mainTable.setFixedCellSize(56);
        mainTable.setFocusTraversable(false);
        VBox.setVgrow(mainTable, Priority.ALWAYS);

        Label emptyLabel = new Label("Belum ada data yang ditampilkan.");
        emptyLabel.getStyleClass().add("empty-list");
        mainTable.setPlaceholder(new StackPane(emptyLabel));

        tableCard.getChildren().addAll(tableHeader, mainTable);

        content.getChildren().addAll(header, tabsRow, toolbar, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        return content;
    }

    private void configureTabButtons() {
        activeTabButton.setOnAction(event -> switchMode(LoanViewMode.ACTIVE));
        returnedTabButton.setOnAction(event -> switchMode(LoanViewMode.RETURNED));
        memberVisitTabButton.setOnAction(event -> switchMode(LoanViewMode.MEMBER_VISIT));
        guestVisitTabButton.setOnAction(event -> switchMode(LoanViewMode.GUEST_VISIT));
    }

    private void bindSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (currentMode == LoanViewMode.ACTIVE || currentMode == LoanViewMode.RETURNED) {
                applyLoanSearchFilter();
            } else {
                applyVisitSearchFilter();
            }
            updateSubtitle();
        });
    }

    private void switchMode(LoanViewMode mode) {
        currentMode = mode;

        activeTabButton.getStyleClass().remove("active");
        returnedTabButton.getStyleClass().remove("active");
        memberVisitTabButton.getStyleClass().remove("active");
        guestVisitTabButton.getStyleClass().remove("active");

        if (mode == LoanViewMode.ACTIVE) {
            activeTabButton.getStyleClass().add("active");
            primaryActionButton.setText("+ Catat Peminjaman");
            tableTitleLabel.setText("Daftar Peminjaman Aktif");
            rebuildLoanTableColumns();
        } else if (mode == LoanViewMode.RETURNED) {
            returnedTabButton.getStyleClass().add("active");
            primaryActionButton.setText("Catat Pengembalian");
            tableTitleLabel.setText("Riwayat Pengembalian");
            rebuildLoanTableColumns();
        } else if (mode == LoanViewMode.MEMBER_VISIT) {
            memberVisitTabButton.getStyleClass().add("active");
            primaryActionButton.setText("Catat Kunjungan");
            tableTitleLabel.setText("Riwayat Kunjungan Mahasiswa");
            rebuildVisitTableColumns();
        } else {
            guestVisitTabButton.getStyleClass().add("active");
            primaryActionButton.setText("Absen Tamu Masuk");
            tableTitleLabel.setText("Riwayat Absen Tamu");
            rebuildVisitTableColumns();
        }

        refreshData();
    }

    private void loadLoanRows() {
        List<Loan> loans = currentMode == LoanViewMode.ACTIVE
                ? loanService.getActiveLoans()
                : loanService.getReturnedLoans();

        List<LoanRow> rows = new ArrayList<>();
        for (Loan loan : loans) {
            Object[] identity = loanDAO.getLoanIdentity(loan.getId());

            String memberCode = identity[0] == null ? "-" : identity[0].toString();
            String memberName = identity[1] == null ? "-" : identity[1].toString();
            String copyCode = identity[2] == null ? "-" : identity[2].toString();
            String bookTitle = identity[3] == null ? "-" : identity[3].toString();

            String statusText;
            if (currentMode == LoanViewMode.ACTIVE) {
                statusText = loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDate.now())
                        ? "Terlambat"
                        : "Aktif";
            } else {
                statusText = "Dikembalikan";
            }

            rows.add(new LoanRow(
                    memberCode,
                    memberName,
                    copyCode,
                    bookTitle,
                    formatDate(loan.getLoanDate()),
                    formatDate(loan.getDueDate()),
                    formatDate(loan.getReturnDate()),
                    formatCurrency(loan.getFineAmount()),
                    statusText
            ));
        }

        allRows.setAll(rows);
    }

    private void applyLoanSearchFilter() {
        String keyword = normalize(searchField.getText());

        if (keyword.isBlank()) {
            visibleRows.setAll(allRows);
            mainTable.setItems(FXCollections.observableArrayList(new ArrayList<>(visibleRows)));
            return;
        }

        List<LoanRow> filtered = allRows.stream()
                .filter(row ->
                        contains(row.memberCode(), keyword) ||
                        contains(row.memberName(), keyword) ||
                        contains(row.copyCode(), keyword) ||
                        contains(row.bookTitle(), keyword))
                .toList();

        visibleRows.setAll(filtered);
        mainTable.setItems(FXCollections.observableArrayList(new ArrayList<>(visibleRows)));
    }

    private void loadVisitRows() {
        List<Visit> visits = visitService.getRecentVisits();

        List<VisitRow> rows = visits.stream()
                .filter(visit -> {
                    if (currentMode == LoanViewMode.MEMBER_VISIT) {
                        return visit.getVisitType() == VisitType.MEMBER;
                    }
                    return visit.getVisitType() == VisitType.GUEST;
                })
                .map(visit -> new VisitRow(
                        formatDate(visit.getVisitDate()),
                        visit.getVisitType() == VisitType.MEMBER ? "Mahasiswa" : "Tamu",
                        safeText(visit.getVisitorName()),
                        safeText(visit.getVisitorIdentifier()),
                        safeText(visit.getInstitution()),
                        safeText(visit.getPurpose()),
                        visit.getVisitType() == VisitType.MEMBER ? "Di dalam" : "Tamu"
                ))
                .toList();

        allVisitRows.setAll(rows);
    }

    private void applyVisitSearchFilter() {
        String keyword = normalize(searchField.getText());

        if (keyword.isBlank()) {
            visibleVisitRows.setAll(allVisitRows);
            mainTable.setItems(FXCollections.observableArrayList(new ArrayList<>(visibleVisitRows)));
            return;
        }

        List<VisitRow> filtered = allVisitRows.stream()
                .filter(row ->
                        contains(row.name(), keyword) ||
                        contains(row.identifier(), keyword) ||
                        contains(row.institution(), keyword) ||
                        contains(row.purpose(), keyword))
                .toList();

        visibleVisitRows.setAll(filtered);
        mainTable.setItems(FXCollections.observableArrayList(new ArrayList<>(visibleVisitRows)));
    }

    private void updateSubtitle() {
        if (currentMode == LoanViewMode.ACTIVE) {
            subtitleLabel.setText(visibleRows.size() + " transaksi peminjaman aktif");
        } else if (currentMode == LoanViewMode.RETURNED) {
            subtitleLabel.setText(visibleRows.size() + " riwayat pengembalian");
        } else if (currentMode == LoanViewMode.MEMBER_VISIT) {
            subtitleLabel.setText(visibleVisitRows.size() + " kunjungan mahasiswa");
        } else {
            subtitleLabel.setText(visibleVisitRows.size() + " data tamu");
        }
    }

    private void rebuildLoanTableColumns() {
        mainTable.getColumns().clear();

        TableColumn<Object, LoanRow> memberColumn = new TableColumn<>("ANGGOTA");
        memberColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>((LoanRow) cell.getValue()));
        memberColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LoanRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label name = new Label(item.memberName());
                name.getStyleClass().add("loan-title-text");

                Label code = new Label(item.memberCode());
                code.getStyleClass().add("loan-meta-text");

                VBox wrapper = new VBox(2, name, code);
                wrapper.setAlignment(Pos.CENTER_LEFT);
                wrapper.setPadding(new Insets(0, 0, 0, 8));

                setGraphic(wrapper);
                setText(null);
            }
        });
        memberColumn.setPrefWidth(180);

        TableColumn<Object, String> copyColumn = new TableColumn<>("KODE EKSEMPLAR");
        copyColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).copyCode()));
        copyColumn.setCellFactory(column -> centeredTextCell("loan-code-text"));
        copyColumn.setPrefWidth(120);

        TableColumn<Object, String> titleColumn = new TableColumn<>("JUDUL");
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).bookTitle()));
        titleColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label label = new Label(item);
                label.getStyleClass().add("loan-book-text");
                label.setWrapText(true);
                setGraphic(label);
                setText(null);
            }
        });
        titleColumn.setPrefWidth(220);

        if (currentMode == LoanViewMode.ACTIVE) {
            TableColumn<Object, String> loanDateColumn = new TableColumn<>("PINJAM");
            loanDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).loanDate()));
            loanDateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
            loanDateColumn.setPrefWidth(110);

            TableColumn<Object, String> dueDateColumn = new TableColumn<>("JATUH TEMPO");
            dueDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).dueDate()));
            dueDateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
            dueDateColumn.setPrefWidth(120);

            TableColumn<Object, String> statusColumn = createLoanStatusColumn();
            mainTable.getColumns().setAll(memberColumn, copyColumn, titleColumn, loanDateColumn, dueDateColumn, statusColumn);
        } else {
            TableColumn<Object, String> returnDateColumn = new TableColumn<>("DIKEMBALIKAN");
            returnDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).returnDate()));
            returnDateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
            returnDateColumn.setPrefWidth(120);

            TableColumn<Object, String> fineColumn = new TableColumn<>("DENDA");
            fineColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).fineAmount()));
            fineColumn.setCellFactory(column -> centeredTextCell("loan-fine-text"));
            fineColumn.setPrefWidth(120);

            TableColumn<Object, String> statusColumn = createLoanStatusColumn();
            mainTable.getColumns().setAll(memberColumn, copyColumn, titleColumn, returnDateColumn, fineColumn, statusColumn);
        }

        mainTable.setItems(FXCollections.observableArrayList(new ArrayList<>(visibleRows)));
    }

    private TableColumn<Object, String> createLoanStatusColumn() {
        TableColumn<Object, String> statusColumn = new TableColumn<>("STATUS");
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).status()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(item);
                badge.getStyleClass().addAll("status-badge", resolveStatusClass(item));

                HBox wrapper = new HBox(badge);
                wrapper.setAlignment(Pos.CENTER);

                setGraphic(wrapper);
                setText(null);
            }
        });
        statusColumn.setPrefWidth(110);
        return statusColumn;
    }

    private void rebuildVisitTableColumns() {
        mainTable.getColumns().clear();

        TableColumn<Object, String> dateColumn = new TableColumn<>("TANGGAL");
        dateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).date()));
        dateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
        dateColumn.setPrefWidth(110);

        TableColumn<Object, String> typeColumn = new TableColumn<>("TIPE");
        typeColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).type()));
        typeColumn.setCellFactory(column -> centeredTextCell("loan-code-text"));
        typeColumn.setPrefWidth(110);

        TableColumn<Object, String> nameColumn = new TableColumn<>("NAMA");
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).name()));
        nameColumn.setCellFactory(column -> visitTextCell("loan-title-text"));
        nameColumn.setPrefWidth(180);

        TableColumn<Object, String> idColumn = new TableColumn<>("IDENTITAS");
        idColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).identifier()));
        idColumn.setCellFactory(column -> centeredTextCell("loan-code-text"));
        idColumn.setPrefWidth(130);

        TableColumn<Object, String> institutionColumn = new TableColumn<>("INSTANSI");
        institutionColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).institution()));
        institutionColumn.setCellFactory(column -> visitTextCell("loan-book-text"));
        institutionColumn.setPrefWidth(160);

        TableColumn<Object, String> purposeColumn = new TableColumn<>("KEPERLUAN");
        purposeColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).purpose()));
        purposeColumn.setCellFactory(column -> visitTextCell("loan-book-text"));
        purposeColumn.setPrefWidth(180);

        TableColumn<Object, String> statusColumn = new TableColumn<>("STATUS");
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).status()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(item);
                badge.getStyleClass().addAll("status-badge", "status-success");

                HBox wrapper = new HBox(badge);
                wrapper.setAlignment(Pos.CENTER);

                setGraphic(wrapper);
                setText(null);
            }
        });
        statusColumn.setPrefWidth(110);

        mainTable.getColumns().setAll(
                dateColumn, typeColumn, nameColumn, idColumn, institutionColumn, purposeColumn, statusColumn
        );

        mainTable.setItems(FXCollections.observableArrayList(new ArrayList<>(visibleVisitRows)));
    }

    private TableCell<Object, String> centeredTextCell(String styleClass) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label label = new Label(item);
                label.getStyleClass().add(styleClass);

                HBox wrapper = new HBox(label);
                wrapper.setAlignment(Pos.CENTER);

                setGraphic(wrapper);
                setText(null);
            }
        };
    }

    private TableCell<Object, String> visitTextCell(String styleClass) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label label = new Label(item);
                label.getStyleClass().add(styleClass);
                label.setWrapText(true);

                setGraphic(label);
                setText(null);
            }
        };
    }

    private String resolveStatusClass(String status) {
        if ("Terlambat".equalsIgnoreCase(status)) {
            return "status-warning";
        }
        if ("Dikembalikan".equalsIgnoreCase(status)) {
            return "status-success";
        }
        return "status-info";
    }

    private void openPrimaryModal() {
        if (modalOverlay != null) {
            return;
        }

        StackPane host = resolveModalHost();
        if (host == null) {
            return;
        }

        if (currentMode == LoanViewMode.ACTIVE) {
            modalOverlay = buildBorrowModal();
        } else if (currentMode == LoanViewMode.RETURNED) {
            modalOverlay = buildReturnModal();
        } else if (currentMode == LoanViewMode.MEMBER_VISIT) {
            modalOverlay = buildMemberVisitModal();
        } else {
            modalOverlay = buildGuestVisitModal();
        }

        modalHost = host;
        modalHost.getChildren().add(modalOverlay);
    }

    private void closeModal() {
        if (modalOverlay == null) {
            return;
        }

        StackPane host = modalHost != null ? modalHost : resolveModalHost();
        if (host != null) {
            host.getChildren().remove(modalOverlay);
        }

        modalOverlay = null;
        modalHost = null;
    }

    private StackPane resolveModalHost() {
        if (root == null) {
            return null;
        }

        if (root.getScene() != null) {
            Parent sceneRoot = root.getScene().getRoot();
            if (sceneRoot instanceof StackPane sceneStack) {
                return sceneStack;
            }
        }

        return root;
    }

    private StackPane buildBorrowModal() {
        TextField memberCodeInput = createModalTextField("Masukkan kode anggota");
        TextField copyCodeInput = createModalTextField("Masukkan kode eksemplar");

        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Catat Peminjaman");

        VBox body = new VBox(16);
        body.getStyleClass().add("loan-modal-body");
        body.getChildren().add(buildModalField("Kode Anggota", memberCodeInput));
        body.getChildren().add(buildModalField("Kode Eksemplar", copyCodeInput));

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();
        Button saveButton = new Button("Simpan");
        saveButton.getStyleClass().addAll("loan-modal-button", "loan-modal-button-save");
        saveButton.setOnAction(event -> {
            try {
                loanService.borrowBook(memberCodeInput.getText().trim(), copyCodeInput.getText().trim());
                refreshData();
                closeModal();
                showInfo("Transaksi peminjaman berhasil.");
            } catch (Exception exception) {
                showError(resolveErrorMessage(exception));
            }
        });

        footer.getChildren().addAll(cancelButton, saveButton);
        card.getChildren().addAll(body, footer);
        overlay.getChildren().add(card);
        return overlay;
    }

    private StackPane buildReturnModal() {
        TextField copyCodeInput = createModalTextField("Masukkan kode eksemplar");

        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Catat Pengembalian");

        VBox body = new VBox(16);
        body.getStyleClass().add("loan-modal-body");
        body.getChildren().add(buildModalField("Kode Eksemplar", copyCodeInput));

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();
        Button saveButton = new Button("Proses");
        saveButton.getStyleClass().addAll("loan-modal-button", "loan-modal-button-save");
        saveButton.setOnAction(event -> {
            try {
                Loan loan = loanService.returnBook(copyCodeInput.getText().trim());
                refreshData();
                closeModal();
                showInfo("Buku berhasil dikembalikan. Denda: " + formatCurrency(loan.getFineAmount()));
            } catch (Exception exception) {
                showError(resolveErrorMessage(exception));
            }
        });

        footer.getChildren().addAll(cancelButton, saveButton);
        card.getChildren().addAll(body, footer);
        overlay.getChildren().add(card);
        return overlay;
    }

    private StackPane buildMemberVisitModal() {
        TextField memberCodeInput = createModalTextField("Masukkan NIM / NIS / NIDN");

        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Catat Kunjungan Mahasiswa");

        VBox body = new VBox(16);
        body.getStyleClass().add("loan-modal-body");
        body.getChildren().add(buildModalField("Kode Anggota", memberCodeInput));

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();
        Button saveButton = new Button("Simpan");
        saveButton.getStyleClass().addAll("loan-modal-button", "loan-modal-button-save");
        saveButton.setOnAction(event -> {
            try {
                visitService.recordMemberVisit(memberCodeInput.getText().trim());
                refreshData();
                closeModal();
                showInfo("Kunjungan berhasil dicatat.");
            } catch (Exception exception) {
                showError(resolveErrorMessage(exception));
            }
        });

        footer.getChildren().addAll(cancelButton, saveButton);
        card.getChildren().addAll(body, footer);
        overlay.getChildren().add(card);
        return overlay;
    }

    private StackPane buildGuestVisitModal() {
        TextField guestNameInput = createModalTextField("Masukkan nama tamu");
        TextField institutionInput = createModalTextField("Masukkan instansi / asal");
        TextField purposeInput = createModalTextField("Masukkan keperluan");

        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Absen Tamu Masuk");

        VBox body = new VBox(16);
        body.getStyleClass().add("loan-modal-body");
        body.getChildren().add(buildModalField("Nama Tamu", guestNameInput));
        body.getChildren().add(buildModalField("Instansi", institutionInput));
        body.getChildren().add(buildModalField("Keperluan", purposeInput));

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();
        Button saveButton = new Button("Simpan");
        saveButton.getStyleClass().addAll("loan-modal-button", "loan-modal-button-save");
        saveButton.setOnAction(event -> {
            try {
                visitService.recordGuestVisit(
                        guestNameInput.getText().trim(),
                        institutionInput.getText().trim(),
                        purposeInput.getText().trim()
                );
                refreshData();
                closeModal();
                showInfo("Data tamu berhasil dicatat.");
            } catch (Exception exception) {
                showError(resolveErrorMessage(exception));
            }
        });

        footer.getChildren().addAll(cancelButton, saveButton);
        card.getChildren().addAll(body, footer);
        overlay.getChildren().add(card);
        return overlay;
    }

    private StackPane buildBaseModalOverlay() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("loan-modal-overlay");
        overlay.setAlignment(Pos.CENTER);
        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                closeModal();
            }
        });
        return overlay;
    }

    private VBox buildBaseModalCard(String titleText) {
        VBox card = new VBox();
        card.getStyleClass().add("loan-modal-card");
        card.setPrefWidth(MODAL_WIDTH);
        card.setMaxWidth(MODAL_WIDTH);
        card.setPrefHeight(MODAL_HEIGHT);
        card.setMaxHeight(MODAL_HEIGHT);

        HBox header = new HBox();
        header.getStyleClass().add("loan-modal-header");

        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("loan-modal-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("loan-modal-close");
        closeButton.setOnAction(event -> closeModal());

        header.getChildren().addAll(titleLabel, spacer, closeButton);
        card.getChildren().add(header);
        return card;
    }

    private HBox buildModalFooter() {
        HBox footer = new HBox(14);
        footer.getStyleClass().add("loan-modal-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        return footer;
    }

    private Button createCancelModalButton() {
        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().addAll("loan-modal-button", "loan-modal-button-cancel");
        cancelButton.setOnAction(event -> closeModal());
        return cancelButton;
    }

    private TextField createModalTextField(String prompt) {
        TextField field = new TextField();
        field.getStyleClass().add("loan-modal-input");
        field.setPromptText(prompt);
        field.setMinHeight(44);
        field.setPrefHeight(44);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private VBox buildModalField(String labelText, Node inputControl) {
        Label label = new Label(labelText);
        label.getStyleClass().add("loan-modal-label");

        if (inputControl instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        VBox field = new VBox(8, label, inputControl);
        field.getStyleClass().add("loan-modal-field");
        HBox.setHgrow(field, Priority.ALWAYS);
        return field;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DateUtil.format(date);
    }

    private String formatCurrency(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return "Rp" + safeValue.toPlainString();
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(ID_LOCALE);
    }

    private boolean contains(String source, String keyword) {
        return normalize(source).contains(keyword);
    }

    private String resolveErrorMessage(Exception exception) {
        String message = exception.getMessage();
        return (message == null || message.isBlank())
                ? "Terjadi kesalahan saat memproses transaksi."
                : message;
    }

   private void showInfo(String message) {
    FxFeedback.showSuccessToast(FxFeedback.resolveHost(root), message);
}

private void showError(String message) {
    FxFeedback.showErrorToast(FxFeedback.resolveHost(root), message);
}
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Window owner = root == null || root.getScene() == null ? null : root.getScene().getWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }

        alert.showAndWait();
    }

    private record LoanRow(
            String memberCode,
            String memberName,
            String copyCode,
            String bookTitle,
            String loanDate,
            String dueDate,
            String returnDate,
            String fineAmount,
            String status
    ) {
    }

    private record VisitRow(
            String date,
            String type,
            String name,
            String identifier,
            String institution,
            String purpose,
            String status
    ) {
    }
}