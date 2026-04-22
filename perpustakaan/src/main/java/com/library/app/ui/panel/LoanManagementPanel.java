package com.library.app.ui.panel;

import com.library.app.dao.LoanDAO;
import com.library.app.model.Loan;
import com.library.app.model.Visit;
import com.library.app.model.enums.VisitPresenceStatus;
import com.library.app.model.enums.VisitType;
import com.library.app.service.LoanService;
import com.library.app.service.VisitService;
import com.library.app.ui.util.FxFeedback;
import com.library.app.util.DateUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter UI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter MODAL_SOURCE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter MODAL_DB_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MODAL_RETURN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", ID_LOCALE);
    private static final DateTimeFormatter VISIT_TABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter VISIT_TABLE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
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
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
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
        VBox content = new VBox(16);
        content.getStyleClass().add("loan-management-content");
        content.setPadding(Insets.EMPTY);
        content.setFillWidth(true);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("loan-section-header");

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label("Peminjaman & Pengembalian");
        titleLabel.getStyleClass().add("section-title");
        subtitleLabel.setText("Kelola transaksi peminjaman, pengembalian, dan kunjungan");
        subtitleLabel.getStyleClass().add("section-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        primaryActionButton.getStyleClass().add("loan-primary-action");
        primaryActionButton.setOnAction(event -> openPrimaryModal());

        header.getChildren().addAll(titleBox, spacer, primaryActionButton);

        HBox tabsRow = new HBox(0);
        tabsRow.getStyleClass().add("loan-tab-row");
        activeTabButton.getStyleClass().add("loan-tab-button");
        returnedTabButton.getStyleClass().add("loan-tab-button");
        memberVisitTabButton.getStyleClass().add("loan-tab-button");
        guestVisitTabButton.getStyleClass().add("loan-tab-button");

        configureTabButtonGraphic(activeTabButton,
            "M3 4h7a3 3 0 0 1 2 2v10a2 2 0 0 0-2-2H3z M21 4h-7a3 3 0 0 0-2 2v10a2 2 0 0 1 2-2h7z");
        configureTabButtonGraphic(returnedTabButton,
            "M4 5v6h6 M4 11a8 8 0 1 0 2.4-5.7");
        configureTabButtonGraphic(memberVisitTabButton,
            "M12 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6 M6 20a6 6 0 0 1 12 0");
        configureTabButtonGraphic(guestVisitTabButton,
            "M11 12a3 3 0 1 0 0-6 3 3 0 0 0 0 6 M5 20a6 6 0 0 1 12 0 M18 8h4 M20 6v4");

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

        searchField.setPromptText("Cari nama, NIM, atau judul buku...");
        searchField.getStyleClass().add("loan-search-input");

        SVGPath searchIconSvg = new SVGPath();
        searchIconSvg.setContent("M11 19a8 8 0 1 1 5.293-2.707l4.207 4.207-1.414 1.414-4.207-4.207A7.963 7.963 0 0 1 11 19zm0-2a6 6 0 1 0 0-12 6 6 0 0 0 0 12z");
        searchIconSvg.getStyleClass().add("loan-search-icon-svg");
        StackPane iconWrapper = new StackPane(searchIconSvg);
        iconWrapper.setMinWidth(20);

        HBox searchBox = new HBox(8, iconWrapper, searchField);
        searchBox.getStyleClass().add("loan-search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        toolbar.getChildren().add(searchBox);

        VBox tableCard = new VBox(0);
        tableCard.getStyleClass().addAll("loan-table-card");
        tableCard.setPadding(new Insets(0));
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        mainTable.getStyleClass().add("loan-table");
        mainTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        mainTable.setFixedCellSize(56);
        mainTable.setFocusTraversable(false);
        VBox.setVgrow(mainTable, Priority.ALWAYS);

        Label emptyLabel = new Label("Belum ada data yang ditampilkan.");
        emptyLabel.getStyleClass().add("empty-list");
        mainTable.setPlaceholder(new StackPane(emptyLabel));

        tableCard.getChildren().add(mainTable);

        content.getChildren().addAll(header, tabsRow, toolbar, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        return content;
    }

    private void configureTabButtonGraphic(Button button, String iconPath) {
        SVGPath icon = new SVGPath();
        icon.setContent(iconPath);
        icon.getStyleClass().add("loan-tab-icon-svg");

        StackPane iconWrap = new StackPane(icon);
        iconWrap.getStyleClass().add("loan-tab-icon-wrap");

        button.setGraphic(iconWrap);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(6);
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
            primaryActionButton.setVisible(true);
            primaryActionButton.setManaged(true);
            primaryActionButton.setText("+ Catat Peminjaman");
            tableTitleLabel.setText("Daftar Peminjaman Aktif");
            rebuildLoanTableColumns();

        } else if (mode == LoanViewMode.RETURNED) {
            returnedTabButton.getStyleClass().add("active");
            primaryActionButton.setVisible(false);
            primaryActionButton.setManaged(false);
            tableTitleLabel.setText("Riwayat Pengembalian");
            rebuildLoanTableColumns();

        } else if (mode == LoanViewMode.MEMBER_VISIT) {
            memberVisitTabButton.getStyleClass().add("active");
            primaryActionButton.setVisible(false);
            primaryActionButton.setManaged(false);
            tableTitleLabel.setText("Riwayat Kunjungan Mahasiswa");
            rebuildVisitTableColumns();

        } else {
            guestVisitTabButton.getStyleClass().add("active");
            primaryActionButton.setVisible(true);
            primaryActionButton.setManaged(true);
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
            String fineText;
            if (currentMode == LoanViewMode.ACTIVE) {
                statusText = loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDate.now())
                        ? "Terlambat"
                        : "Aktif";

                if ("Terlambat".equals(statusText) && loan.getDueDate() != null) {
                    long daysLate = DateUtil.calculateDaysDifference(loan.getDueDate(), LocalDate.now());
                    long finePerDay = com.library.app.model.FineRule.getInstance().getFinePerDay();
                    fineText = formatCurrency(BigDecimal.valueOf(daysLate * finePerDay));
                } else {
                    fineText = "-";
                }
            } else {
                statusText = "Dikembalikan";
                fineText = formatCurrency(loan.getFineAmount());
            }

            rows.add(new LoanRow(
                    memberCode,
                    memberName,
                    copyCode,
                    bookTitle,
                    formatDate(loan.getLoanDate()),
                    formatDate(loan.getDueDate()),
                    formatDate(loan.getReturnDate()),
                    fineText,
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
                        formatVisitDate(visit.getVisitDate()),
                        visit.getVisitType() == VisitType.MEMBER ? "Mahasiswa" : "Tamu",
                        safeText(visit.getVisitorName()),
                        safeText(visit.getVisitorIdentifier()),
                        safeText(visit.getInstitution()),
                        safeText(visit.getPurpose()),
                        formatVisitTime(visit.getCheckInTime()),
                        formatVisitTime(visit.getCheckOutTime()),
                        resolveVisitStatusText(visit)
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
                        contains(row.purpose(), keyword) ||
                        contains(row.checkIn(), keyword) ||
                        contains(row.checkOut(), keyword))
                .toList();

        visibleVisitRows.setAll(filtered);
        mainTable.setItems(FXCollections.observableArrayList(new ArrayList<>(visibleVisitRows)));
    }

    private void updateSubtitle() {
        subtitleLabel.setText("Kelola transaksi peminjaman, pengembalian, dan kunjungan");
    }

    private void rebuildLoanTableColumns() {
        mainTable.getColumns().clear();

        TableColumn<Object, LoanRow> memberColumn = new TableColumn<>("PEMINJAM");
        memberColumn.getStyleClass().add("loan-col-member");
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
                Label code = new Label("NIM: " + item.memberCode());
                code.getStyleClass().add("loan-meta-text");
                VBox wrapper = new VBox(2, name, code);
                wrapper.setAlignment(Pos.CENTER_LEFT);
                wrapper.setPadding(new Insets(0, 0, 0, 8));
                setGraphic(wrapper);
                setText(null);
            }
        });
        memberColumn.setPrefWidth(200);

        TableColumn<Object, String> titleColumn = new TableColumn<>("BUKU");
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).bookTitle()));
        titleColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label label = new Label(item);
                label.getStyleClass().add("loan-book-text");
                label.setWrapText(true);
                setGraphic(label);
                setText(null);
            }
        });
        titleColumn.setPrefWidth(210);

        TableColumn<Object, String> loanDateColumn = new TableColumn<>("TGL PINJAM");
        loanDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).loanDate()));
        loanDateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
        loanDateColumn.setPrefWidth(105);

        if (currentMode == LoanViewMode.ACTIVE) {
            TableColumn<Object, String> dueDateColumn = new TableColumn<>("JATUH TEMPO");
            dueDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).dueDate()));
            dueDateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
            dueDateColumn.setPrefWidth(110);

            TableColumn<Object, String> statusColumn = createLoanStatusColumn();
            statusColumn.setPrefWidth(100);

            TableColumn<Object, String> fineColumn = new TableColumn<>("DENDA");
            fineColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).fineAmount()));
            fineColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    Label label;
                    if (item.equals("-") || item.equals("Rp0") || item.equals("Rp0.00")) {
                        label = new Label("-");
                        label.getStyleClass().add("loan-fine-dash");
                    } else {
                        label = new Label("Rp " + item.replace("Rp", "").trim());
                        label.getStyleClass().add("loan-fine-text");
                    }
                    HBox wrapper = new HBox(label);
                    wrapper.setAlignment(Pos.CENTER);
                    setGraphic(wrapper);
                    setText(null);
                }
            });
            fineColumn.setPrefWidth(100);

            TableColumn<Object, LoanRow> actionColumn = new TableColumn<>("");
            actionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>((LoanRow) cell.getValue()));
            actionColumn.setSortable(false);
            actionColumn.setReorderable(false);
            actionColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LoanRow item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    Button returnBtn = new Button("Proses Kembali");
                    returnBtn.getStyleClass().add("loan-return-button");
                    returnBtn.setOnAction(event -> openReturnModal(item));
                    HBox wrapper = new HBox(returnBtn);
                    wrapper.setAlignment(Pos.CENTER);
                    setGraphic(wrapper);
                    setText(null);
                }
            });
            actionColumn.setPrefWidth(135);

            mainTable.getColumns().setAll(
                    memberColumn, titleColumn, loanDateColumn, dueDateColumn, statusColumn, fineColumn, actionColumn
            );
        } else {
            TableColumn<Object, String> returnDateColumn = new TableColumn<>("TGL KEMBALI");
            returnDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).returnDate()));
            returnDateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
            returnDateColumn.setPrefWidth(110);

            TableColumn<Object, String> fineColumn = new TableColumn<>("DENDA");
            fineColumn.setCellValueFactory(cell -> new SimpleStringProperty(((LoanRow) cell.getValue()).fineAmount()));
            fineColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    Label label;
                    if (item.equals("-") || item.equals("Rp0") || item.equals("Rp0.00")) {
                        label = new Label("-");
                        label.getStyleClass().add("loan-fine-dash");
                    } else {
                        label = new Label("Rp " + item.replace("Rp", "").trim());
                        label.getStyleClass().add("loan-fine-text");
                    }
                    HBox wrapper = new HBox(label);
                    wrapper.setAlignment(Pos.CENTER);
                    setGraphic(wrapper);
                    setText(null);
                }
            });
            fineColumn.setPrefWidth(110);

            TableColumn<Object, String> statusColumn = createLoanStatusColumn();
            mainTable.getColumns().setAll(
                    memberColumn, titleColumn, loanDateColumn, returnDateColumn, fineColumn, statusColumn
            );
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

        if (currentMode == LoanViewMode.MEMBER_VISIT) {
            TableColumn<Object, String> visitorColumn = new TableColumn<>("PENGUNJUNG");
            visitorColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).name()));
            visitorColumn.setCellFactory(column -> visitTextCell("loan-title-text"));
            visitorColumn.setPrefWidth(220);

            TableColumn<Object, String> nimColumn = new TableColumn<>("NIM");
            nimColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).identifier()));
            nimColumn.setCellFactory(column -> centeredTextCell("loan-code-text"));
            nimColumn.setPrefWidth(150);

            TableColumn<Object, String> dateColumn = new TableColumn<>("TANGGAL");
            dateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).date()));
            dateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
            dateColumn.setPrefWidth(140);

            TableColumn<Object, String> inColumn = new TableColumn<>("MASUK");
            inColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).checkIn()));
            inColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
            inColumn.setPrefWidth(120);

            TableColumn<Object, String> outColumn = new TableColumn<>("KELUAR");
            outColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).checkOut()));
            outColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
            outColumn.setPrefWidth(120);

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
                    badge.getStyleClass().addAll("status-badge", resolveVisitStatusClass(item));

                    HBox wrapper = new HBox(badge);
                    wrapper.setAlignment(Pos.CENTER);

                    setGraphic(wrapper);
                    setText(null);
                }
            });
            statusColumn.setPrefWidth(130);

            mainTable.getColumns().setAll(
                    visitorColumn, nimColumn, dateColumn, inColumn, outColumn, statusColumn
            );

            mainTable.setItems(FXCollections.observableArrayList(new ArrayList<>(visibleVisitRows)));
            return;
        }

        TableColumn<Object, String> nameColumn = new TableColumn<>("NAMA");
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).name()));
        nameColumn.setCellFactory(column -> visitTextCell("loan-title-text"));
        nameColumn.setPrefWidth(190);

        TableColumn<Object, String> institutionColumn = new TableColumn<>("INSTANSI");
        institutionColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).institution()));
        institutionColumn.setCellFactory(column -> visitTextCell("loan-book-text"));
        institutionColumn.setPrefWidth(160);

        TableColumn<Object, String> purposeColumn = new TableColumn<>("KEPERLUAN");
        purposeColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).purpose()));
        purposeColumn.setCellFactory(column -> visitTextCell("loan-book-text"));
        purposeColumn.setPrefWidth(180);

        TableColumn<Object, String> dateColumn = new TableColumn<>("TANGGAL");
        dateColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).date()));
        dateColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
        dateColumn.setPrefWidth(120);

        TableColumn<Object, String> inColumn = new TableColumn<>("MASUK");
        inColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).checkIn()));
        inColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
        inColumn.setPrefWidth(110);

        TableColumn<Object, String> outColumn = new TableColumn<>("KELUAR");
        outColumn.setCellValueFactory(cell -> new SimpleStringProperty(((VisitRow) cell.getValue()).checkOut()));
        outColumn.setCellFactory(column -> centeredTextCell("loan-date-text"));
        outColumn.setPrefWidth(110);

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
                badge.getStyleClass().addAll("status-badge", resolveVisitStatusClass(item));

                HBox wrapper = new HBox(badge);
                wrapper.setAlignment(Pos.CENTER);

                setGraphic(wrapper);
                setText(null);
            }
        });
        statusColumn.setPrefWidth(110);

        mainTable.getColumns().setAll(
                nameColumn, institutionColumn, purposeColumn, dateColumn, inColumn, outColumn, statusColumn
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

    private String resolveVisitStatusText(Visit visit) {
        VisitPresenceStatus status = visit.getVisitStatus();
        if (status == VisitPresenceStatus.DI_DALAM) {
            return "Di dalam";
        }
        return "Selesai";
    }

    private String resolveVisitStatusClass(String statusText) {
        if ("Di dalam".equalsIgnoreCase(statusText)) {
            return "status-success";
        }
        return "status-muted";
    }

    private void openPrimaryModal() {
        if (modalOverlay != null) {
            return;
        }

        if (currentMode == LoanViewMode.RETURNED || currentMode == LoanViewMode.MEMBER_VISIT) {
            return;
        }

        StackPane host = resolveModalHost();
        if (host == null) {
            return;
        }

        if (currentMode == LoanViewMode.ACTIVE) {
            modalOverlay = buildBorrowModal();
        } else if (currentMode == LoanViewMode.GUEST_VISIT) {
            modalOverlay = buildGuestVisitModal();
        } else {
            return;
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

    private void openReturnModal(LoanRow loanRow) {
        if (modalOverlay != null) return;
        StackPane host = resolveModalHost();
        if (host == null) return;
        modalOverlay = buildReturnModal(loanRow);
        modalHost = host;
        modalHost.getChildren().add(modalOverlay);
    }

    private StackPane buildReturnModal(LoanRow loanRow) {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = parseUiDate(loanRow.dueDate());

        BigDecimal fine = dueDate != null
                ? com.library.app.util.FineCalculator.processFine(dueDate, today)
                : BigDecimal.ZERO;
        boolean hasFinePenalty = fine != null && fine.compareTo(BigDecimal.ZERO) > 0;
        long daysLate = (dueDate != null && hasFinePenalty)
                ? com.library.app.util.DateUtil.calculateDaysDifference(dueDate, today)
                : 0;
        long finePerDay = com.library.app.model.FineRule.getInstance().getFinePerDay();

        StackPane overlay = buildBaseModalOverlay();
        VBox card = new VBox();
        card.getStyleClass().add("loan-modal-card");
        card.getStyleClass().add("loan-return-modal-card");
        card.setPrefWidth(446);
        card.setMaxWidth(446);
        card.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        HBox header = new HBox();
        header.getStyleClass().add("loan-modal-header");
        Label titleLbl = new Label("Proses Pengembalian");
        titleLbl.getStyleClass().add("loan-return-modal-title");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("×");
        closeBtn.getStyleClass().add("loan-modal-close");
        closeBtn.setOnAction(e -> closeModal());
        header.getChildren().addAll(titleLbl, hSpacer, closeBtn);
        card.getChildren().addAll(header, buildReturnModalDivider());

        VBox body = new VBox(0);
        body.getStyleClass().add("loan-return-modal-body");

        VBox detailBox = new VBox(0);
        detailBox.getStyleClass().add("loan-return-detail-box");
        detailBox.getChildren().add(buildReturnDetailRow("Peminjam", loanRow.memberName(), true));
        detailBox.getChildren().add(buildReturnDetailRow("Buku", loanRow.bookTitle(), true));
        detailBox.getChildren().add(buildReturnDetailRow("Tgl Pinjam", toDbDateText(loanRow.loanDate()), false));
        detailBox.getChildren().add(buildReturnDetailRow("Jatuh Tempo", toDbDateText(loanRow.dueDate()), false));
        detailBox.getChildren().add(buildReturnDetailRow("Tgl Kembali", today.format(MODAL_RETURN_DATE_FORMAT), false));

        body.getChildren().add(detailBox);

        VBox infoBox;
        if (hasFinePenalty) {
            NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));
            String fineFormatted = "Rp " + nf.format(fine);
            String subText = "Terlambat " + daysLate + " hari × Rp " + nf.format(finePerDay) + "/hari";

            Label warningIcon = new Label("⚠");
            warningIcon.getStyleClass().add("loan-return-info-icon-warning");
            Label fineTitle = new Label("Denda Keterlambatan");
            fineTitle.getStyleClass().add("loan-return-fine-title");
            Region fineRowSpacer = new Region();
            HBox.setHgrow(fineRowSpacer, Priority.ALWAYS);
            Label fineAmount = new Label(fineFormatted);
            fineAmount.getStyleClass().add("loan-return-fine-amount");

            HBox fineRow = new HBox(6, warningIcon, fineTitle, fineRowSpacer, fineAmount);
            fineRow.setAlignment(Pos.CENTER_LEFT);

            Label subLabel = new Label(subText);
            subLabel.getStyleClass().add("loan-return-fine-sub");

            infoBox = new VBox(6, fineRow, subLabel);
            infoBox.getStyleClass().add("loan-return-info-warning");
            infoBox.setPadding(new Insets(14, 16, 14, 16));
        } else {
            Label okIcon = new Label("✓");
            okIcon.getStyleClass().add("loan-return-info-icon-ok");
            Label okLabel = new Label("Tidak ada denda. Dikembalikan tepat waktu.");
            okLabel.getStyleClass().add("loan-return-info-text");
            HBox okRow = new HBox(8, okIcon, okLabel);
            okRow.setAlignment(Pos.CENTER_LEFT);
            okRow.setPadding(new Insets(12, 14, 12, 14));
            infoBox = new VBox(okRow);
            infoBox.getStyleClass().add("loan-return-info-ok");
        }
        infoBox.setMaxWidth(Double.MAX_VALUE);
        VBox infoWrapper = new VBox(infoBox);
        infoWrapper.setPadding(new Insets(14, 0, 0, 0));
        body.getChildren().add(infoWrapper);
        card.getChildren().addAll(body, buildReturnModalDivider());

        HBox footer = new HBox(10);
        footer.getStyleClass().add("loan-modal-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button cancelBtn = new Button("Batal");
        cancelBtn.getStyleClass().addAll("loan-modal-button", "loan-modal-button-cancel");
        cancelBtn.setOnAction(e -> closeModal());
        Button confirmBtn = new Button("Konfirmasi Pengembalian");
        confirmBtn.getStyleClass().addAll("loan-modal-button", "loan-modal-button-save");
        confirmBtn.setOnAction(e -> {
            try {
                loanService.returnBook(loanRow.copyCode());
                refreshData();
                closeModal();
                showInfo("Buku berhasil dikembalikan.");
            } catch (Exception ex) {
                showError(resolveErrorMessage(ex));
            }
        });
        footer.getChildren().addAll(cancelBtn, confirmBtn);
        card.getChildren().add(footer);

        overlay.getChildren().add(card);
        return overlay;
    }

    private Region buildReturnModalDivider() {
        Region divider = new Region();
        divider.getStyleClass().add("loan-return-modal-divider");
        return divider;
    }

    private HBox buildReturnDetailRow(String key, String value, boolean valueBold) {
        Label keyLabel = new Label(key);
        keyLabel.getStyleClass().add("loan-return-detail-key");
        keyLabel.setMinWidth(100);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add(valueBold ? "loan-return-detail-value-bold" : "loan-return-detail-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(keyLabel, spacer, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));
        row.getStyleClass().add("loan-return-detail-row");
        return row;
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

    private StackPane buildGuestVisitModal() {
        TextField guestNameInput = createModalTextField("Masukkan nama tamu");
        TextField institutionInput = createModalTextField("Masukkan instansi / asal");
        TextField purposeInput = createModalTextField("Masukkan keperluan");

        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Absen Tamu");

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
                String message = visitService.recordGuestVisit(
                        guestNameInput.getText().trim(),
                        institutionInput.getText().trim(),
                        purposeInput.getText().trim()
                );
                refreshData();
                closeModal();
                showInfo(message);
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

    private String formatVisitDate(LocalDate date) {
        return date == null ? "-" : date.format(VISIT_TABLE_DATE_FORMAT);
    }

    private String formatVisitTime(LocalTime time) {
        return time == null ? "-" : time.format(VISIT_TABLE_TIME_FORMAT);
    }

    private LocalDate parseUiDate(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return null;
        }

        try {
            return LocalDate.parse(value, UI_DATE_FORMAT);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String toDbDateText(String uiDate) {
        LocalDate parsed = parseUiDate(uiDate);
        if (parsed == null) {
            return "-";
        }
        return parsed.format(MODAL_DB_DATE_FORMAT);
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
        FxFeedback.showSuccessToastCentered(FxFeedback.resolveHost(root), message);
    }

    private void showError(String message) {
        FxFeedback.showErrorToastCentered(FxFeedback.resolveHost(root), message);
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
            String checkIn,
            String checkOut,
            String status
    ) {
    }
}
