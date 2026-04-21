package com.library.app.ui.panel;

import com.library.app.model.Member;
import com.library.app.model.enums.MemberType;
import com.library.app.service.MemberService;
import com.library.app.ui.util.FxFeedback;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MemberManagementPanel {
    private static final String ALL_TYPES = "Semua Tipe";
    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
    private static final double ADD_MEMBER_MODAL_WIDTH = 760;
    private static final double ADD_MEMBER_MODAL_HEIGHT = 520;

    private final MemberService memberService = new MemberService();
    private final ObservableList<Member> members = FXCollections.observableArrayList();
    private final FilteredList<Member> filteredMembers = new FilteredList<>(members, item -> true);

    private final TextField searchField = new TextField();
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private final Label subtitleLabel = new Label();
    private final TableView<Member> memberTable = new TableView<>();

    private StackPane root;
    private StackPane memberModalOverlay;
    private StackPane modalHost;

    public Node create() {
        if (root == null) {
            VBox content = buildContent();
            root = new StackPane(content);
            root.getStyleClass().add("member-management-root");
            StackPane.setAlignment(content, Pos.TOP_LEFT);

            bindFilterEvents();
            configureTable();
        }

        refreshData();
        return root;
    }

    public void refreshData() {
        try {
            List<Member> result = memberService.search("");
            members.setAll(result);
            rebuildTypeOptions();
            applyFilters();
            memberTable.refresh();
        } catch (Exception exception) {
            members.clear();
            filteredMembers.setPredicate(item -> true);
            updateSubtitle();
            showError(resolveErrorMessage(exception));
        }
    }

    private VBox buildContent() {
        VBox content = new VBox(18);
        content.getStyleClass().add("member-management-content");
        content.setPadding(Insets.EMPTY);

        HBox header = new HBox();
        header.getStyleClass().add("member-section-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label("Manajemen Anggota");
        titleLabel.getStyleClass().add("section-title");
        subtitleLabel.getStyleClass().add("section-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+ Tambah Anggota");
        addButton.getStyleClass().add("member-add-button");
        addButton.setOnAction(event -> openAddMemberDialog());

        header.getChildren().addAll(titleBox, spacer, addButton);

        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().addAll("list-card", "member-toolbar-card");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(14, 16, 14, 16));

        searchField.setPromptText("Cari nama, NIM/NIS, atau telepon...");
        searchField.getStyleClass().add("member-search-input");

        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent(
                "M11 19a8 8 0 1 1 5.293-2.707l4.207 4.207-1.414 1.414-4.207-4.207A7.963 7.963 0 0 1 11 19zm0-2a6 6 0 1 0 0-12 6 6 0 0 0 0 12z");
        searchIcon.getStyleClass().add("member-search-icon-svg");

        StackPane iconWrapper = new StackPane(searchIcon);
        iconWrapper.getStyleClass().add("member-search-icon-wrapper");
        iconWrapper.setMinWidth(20);

        HBox searchBox = new HBox(8, iconWrapper, searchField);
        searchBox.getStyleClass().add("member-search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        typeFilter.getStyleClass().add("member-type-filter");
        typeFilter.setPrefWidth(180);
        typeFilter.getItems().setAll(ALL_TYPES);
        typeFilter.setValue(ALL_TYPES);

        toolbar.getChildren().addAll(searchBox, typeFilter);

        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().addAll("list-card", "member-table-card");
        tableCard.setPadding(new Insets(0));
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        memberTable.getStyleClass().add("member-table");
        memberTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        memberTable.setFixedCellSize(62);
        memberTable.setMinHeight(260);
        memberTable.setFocusTraversable(false);
        VBox.setVgrow(memberTable, Priority.ALWAYS);

        Label emptyLabel = new Label("Belum ada data anggota yang dapat ditampilkan.");
        emptyLabel.getStyleClass().add("empty-list");
        memberTable.setPlaceholder(new StackPane(emptyLabel));

        tableCard.getChildren().add(memberTable);

        content.getChildren().addAll(header, toolbar, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        return content;
    }

    private void bindFilterEvents() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        typeFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filteredMembers.addListener((ListChangeListener<Member>) change -> updateSubtitle());
    }

    private void configureTable() {
        SortedList<Member> sortedItems = new SortedList<>(filteredMembers);
        sortedItems.comparatorProperty().bind(memberTable.comparatorProperty());
        memberTable.setItems(sortedItems);

        TableColumn<Member, Member> memberColumn = new TableColumn<>("ANGGOTA");
        memberColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        memberColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        memberColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Member item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Circle avatarCircle = new Circle(16);
                avatarCircle.getStyleClass().add(resolveAvatarClass(item));

                Label avatarText = new Label(resolveAvatarText(item));
                avatarText.getStyleClass().add("member-avatar-text");

                StackPane avatar = new StackPane(avatarCircle, avatarText);
                avatar.getStyleClass().add("member-avatar-wrap");

                Label title = new Label(safe(item.getName(), "Tanpa Nama"));
                title.getStyleClass().add("member-title-text");

                VBox wrapper = new VBox(2, title);
                wrapper.setAlignment(Pos.CENTER_LEFT);

                HBox row = new HBox(12, avatar, wrapper);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(0, 0, 0, 8));

                setText(null);
                setGraphic(row);
            }
        });
        memberColumn.setPrefWidth(220);

        TableColumn<Member, String> codeColumn = new TableColumn<>("NIM/NIS");
        codeColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(cell.getValue().getMemberCode(), "-")));
        codeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        codeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item);
                value.getStyleClass().add("member-code-text");
                value.setTooltip(new Tooltip(item));
                setText(null);
                setGraphic(value);
            }
        });
        codeColumn.setPrefWidth(120);

        TableColumn<Member, Member> contactColumn = new TableColumn<>("KONTAK");
        contactColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        contactColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        contactColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Member item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label line1 = new Label("-");
                line1.getStyleClass().add("member-contact-primary");

                Label line2 = new Label(safe(item.getPhone(), "-"));
                line2.getStyleClass().add("member-contact-secondary");

                VBox wrapper = new VBox(2, line1, line2);
                wrapper.setAlignment(Pos.CENTER_LEFT);

                setText(null);
                setGraphic(wrapper);
            }
        });
        contactColumn.setPrefWidth(180);

        TableColumn<Member, String> majorColumn = new TableColumn<>("FAKULTAS");
        majorColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(itemMajor(cell.getValue()), "-")));
        majorColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        majorColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item);
                value.getStyleClass().add("member-major-text");
                value.setTooltip(new Tooltip(item));
                setText(null);
                setGraphic(value);
            }
        });
        majorColumn.setPrefWidth(170);

        TableColumn<Member, String> typeColumn = new TableColumn<>("TIPE");
        typeColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatMemberType(cell.getValue().getMemberType())));
        typeColumn.setStyle("-fx-alignment: CENTER;");
        typeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label chip = new Label(item);
                chip.getStyleClass().addAll("member-type-chip", resolveTypeClass(item));
                chip.setAlignment(Pos.CENTER);

                HBox wrapper = new HBox(chip);
                wrapper.setAlignment(Pos.CENTER);

                setText(null);
                setGraphic(wrapper);
                setAlignment(Pos.CENTER);
            }
        });
        typeColumn.setPrefWidth(120);

        TableColumn<Member, String> statusColumn = new TableColumn<>("STATUS");
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(resolveStatusText(cell.getValue())));
        statusColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label badge = new Label(item);
                badge.getStyleClass().addAll("status-badge", resolveStatusClass(item));

                HBox wrapper = new HBox(badge);
                wrapper.setAlignment(Pos.CENTER);

                setText(null);
                setGraphic(wrapper);
                setAlignment(Pos.CENTER);
            }
        });
        statusColumn.setPrefWidth(110);

        TableColumn<Member, Member> actionColumn = new TableColumn<>("");
        actionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        actionColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Member item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Button editButton = createIconActionButton(createEditIcon(), "member-action-edit", "Edit anggota");
                editButton.setOnAction(event -> openEditMemberDialog(item));

                Button deleteButton = createIconActionButton(createDeleteIcon(), "member-action-delete", "Hapus anggota");
                deleteButton.setOnAction(event -> openDeleteMemberDialog(item));

                HBox actions = new HBox(8, editButton, deleteButton);
                actions.setAlignment(Pos.CENTER);
                actions.getStyleClass().add("member-actions-wrapper");

                setText(null);
                setGraphic(actions);
                setAlignment(Pos.CENTER);
            }
        });
        actionColumn.setSortable(false);
        actionColumn.setReorderable(false);
        actionColumn.setPrefWidth(120);

        memberTable.getColumns().setAll(
                memberColumn,
                codeColumn,
                contactColumn,
                majorColumn,
                typeColumn,
                statusColumn,
                actionColumn
        );
    }

    private Button createIconActionButton(Node icon, String variantClass, String tooltipText) {
        Button button = new Button();
        button.getStyleClass().addAll("member-action-button", variantClass);
        button.setGraphic(icon);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setTooltip(new Tooltip(tooltipText));
        button.setMinWidth(28);
        button.setPrefWidth(28);
        button.setMaxWidth(28);
        button.setMinHeight(28);
        button.setPrefHeight(28);
        button.setMaxHeight(28);
        button.setFocusTraversable(false);
        button.setMnemonicParsing(false);
        return button;
    }

    private Node createEditIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M12.854.146a.5.5 0 0 1 .707 0l2.293 2.293a.5.5 0 0 1 0 .707L5.207 13.793 2 14.5l.707-3.207L12.854.146z");
        icon.getStyleClass().addAll("member-action-icon", "member-action-icon-edit");
        return icon;
    }

    private Node createDeleteIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M2.5 3a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h2.5a1 1 0 0 1 1 1V4H2.5V3zm1 2h9l-.8 9.2a1.5 1.5 0 0 1-1.5 1.3H5.8a1.5 1.5 0 0 1-1.5-1.3L3.5 5z");
        icon.getStyleClass().addAll("member-action-icon", "member-action-icon-delete");
        return icon;
    }

    private void rebuildTypeOptions() {
        String previousValue = typeFilter.getValue();

        List<String> typeOptions = Arrays.stream(MemberType.values())
                .map(this::formatMemberType)
                .distinct()
                .sorted(Comparator.comparing(value -> value.toLowerCase(ID_LOCALE)))
                .collect(Collectors.toList());

        typeFilter.getItems().setAll(ALL_TYPES);
        typeFilter.getItems().addAll(typeOptions);

        if (previousValue != null && typeFilter.getItems().contains(previousValue)) {
            typeFilter.setValue(previousValue);
        } else {
            typeFilter.setValue(ALL_TYPES);
        }
    }

    private void applyFilters() {
        String keyword = normalize(searchField.getText());
        String selectedType = normalize(typeFilter.getValue());

        filteredMembers.setPredicate(member -> matchesKeyword(member, keyword) && matchesType(member, selectedType));
        updateSubtitle();
    }

    private boolean matchesKeyword(Member member, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        return contains(member.getMemberCode(), keyword)
                || contains(member.getName(), keyword)
                || contains(itemMajor(member), keyword)
                || contains(member.getPhone(), keyword);
    }

    private boolean matchesType(Member member, String selectedType) {
        if (selectedType.isBlank() || ALL_TYPES.equalsIgnoreCase(selectedType)) {
            return true;
        }
        return normalize(formatMemberType(member.getMemberType())).equals(selectedType);
    }

    private void updateSubtitle() {
        subtitleLabel.setText(filteredMembers.size() + " anggota terdaftar");
    }

    private String formatMemberType(MemberType type) {
        if (type == null) {
            return "Umum";
        }

        String raw = type.name().toUpperCase(ID_LOCALE);

        if (raw.contains("STUDENT") || raw.contains("MAHASISWA")) {
            return "Mahasiswa";
        }

        if (raw.contains("GUEST") || raw.contains("TAMU")) {
            return "Tamu";
        }

        if (raw.contains("LECTURER") || raw.contains("DOSEN")) {
            return "Tamu";
        }

        return type.name();
    }

    private String formatModalMemberType(MemberType type) {
        if (type == null) {
            return "";
        }

        String raw = type.name().toUpperCase(ID_LOCALE);

        if (raw.contains("STUDENT") || raw.contains("MAHASISWA")) {
            return "Mahasiswa";
        }

        if (raw.contains("GUEST") || raw.contains("TAMU")) {
            return "Tamu (Guest)";
        }

        if (raw.contains("LECTURER") || raw.contains("DOSEN")) {
            return "Tamu (Guest)";
        }

        return formatMemberType(type);
    }

    private String resolveStatusText(Member member) {
        String code = safe(member.getMemberCode(), "");
        if (code.startsWith("2019")) {
            return "Nonaktif";
        }
        return "Aktif";
    }

    private String resolveStatusClass(String status) {
        return "Nonaktif".equalsIgnoreCase(status) ? "status-muted" : "status-success";
    }

    private String resolveTypeClass(String type) {
        return "Tamu".equalsIgnoreCase(type) ? "member-type-guest" : "member-type-student";
    }

    private String resolveAvatarClass(Member member) {
        String type = formatMemberType(member.getMemberType());
        return "Tamu".equalsIgnoreCase(type) ? "member-avatar-guest" : "member-avatar-student";
    }

    private String resolveAvatarText(Member member) {
        String name = safe(member.getName(), "?");
        return name.substring(0, 1).toUpperCase(ID_LOCALE);
    }

    private void openAddMemberDialog() {
        if (memberModalOverlay != null) {
            return;
        }

        StackPane host = resolveModalHost();
        if (host == null) {
            return;
        }

        memberModalOverlay = buildAddMemberModalOverlay();
        modalHost = host;
        modalHost.getChildren().add(memberModalOverlay);
    }

    private void openEditMemberDialog(Member member) {
        if (memberModalOverlay != null) {
            return;
        }

        StackPane host = resolveModalHost();
        if (host == null) {
            return;
        }

        memberModalOverlay = buildEditMemberModalOverlay(member);
        modalHost = host;
        modalHost.getChildren().add(memberModalOverlay);
    }

    private void openDeleteMemberDialog(Member member) {
        if (memberModalOverlay != null) {
            return;
        }

        StackPane host = resolveModalHost();
        if (host == null) {
            return;
        }

        memberModalOverlay = buildDeleteMemberModalOverlay(member);
        modalHost = host;
        modalHost.getChildren().add(memberModalOverlay);
    }

    private void closeMemberDialog() {
        if (memberModalOverlay == null) {
            return;
        }

        StackPane host = modalHost != null ? modalHost : resolveModalHost();
        if (host != null) {
            host.getChildren().remove(memberModalOverlay);
        }

        memberModalOverlay = null;
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

    private StackPane buildAddMemberModalOverlay() {
        ComboBox<MemberType> typeInput = createTypeComboBox();
        ComboBox<String> statusInput = createStatusComboBox();

        TextField codeInput = createModalTextField("Hanya angka");
        TextField nameInput = createModalTextField("");
        TextField emailInput = createModalTextField("");
        TextField phoneInput = createModalTextField("");
        TextField majorInput = createModalTextField("");

        Label codeErrorLabel = FxFeedback.createFieldErrorLabel();
        Label nameErrorLabel = FxFeedback.createFieldErrorLabel();

        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Tambah Anggota Baru");

        VBox body = new VBox(16);
        body.getStyleClass().add("member-modal-body");

        body.getChildren().add(buildModalField("Tipe Anggota", typeInput));
        body.getChildren().add(buildModalFieldWithError("NIM / NIS", codeInput, codeErrorLabel));
        body.getChildren().add(buildModalFieldWithError("Nama Lengkap", nameInput, nameErrorLabel));
        body.getChildren().add(buildModalField("Email", emailInput));
        body.getChildren().add(buildModalField("No. HP", phoneInput));
        body.getChildren().add(buildModalField("Fakultas / Jurusan", majorInput));
        body.getChildren().add(buildModalField("Status", statusInput));

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();
        Button saveButton = new Button("Simpan");
        saveButton.getStyleClass().addAll("member-modal-button", "member-modal-button-save");
        saveButton.setDefaultButton(true);

        saveButton.setOnAction(event -> {
            FxFeedback.clearFieldError(codeInput, codeErrorLabel);
            FxFeedback.clearFieldError(nameInput, nameErrorLabel);

            String memberCode = normalizeInput(codeInput.getText());
            String memberName = normalizeInput(nameInput.getText());

            MemberType selectedType = typeInput.getValue();
            if (selectedType == null) {
                showError("Tipe anggota wajib dipilih.");
                return;
            }

            if (memberCode.isBlank()) {
                FxFeedback.showFieldError(codeInput, codeErrorLabel, "NIM / NIS wajib diisi.");
                return;
            }

            if (!memberCode.matches("\\d+")) {
                FxFeedback.showFieldError(codeInput, codeErrorLabel, "NIM/NIS harus berupa angka.");
                return;
            }

            if (memberName.isBlank()) {
                FxFeedback.showFieldError(nameInput, nameErrorLabel, "Nama lengkap wajib diisi.");
                return;
            }

            try {
                memberService.registerMember(
                        memberCode,
                        memberName,
                        selectedType,
                        normalizeInput(majorInput.getText()),
                        normalizeInput(phoneInput.getText())
                );

                refreshData();
                closeMemberDialog();
                showInfo("Anggota berhasil disimpan.");
            } catch (Exception exception) {
                String errorMessage = resolveErrorMessage(exception);

                if (errorMessage != null && errorMessage.toLowerCase().contains("nim")) {
                    FxFeedback.showFieldError(codeInput, codeErrorLabel, errorMessage);
                    return;
                }

                if (errorMessage != null && errorMessage.toLowerCase().contains("nama")) {
                    FxFeedback.showFieldError(nameInput, nameErrorLabel, errorMessage);
                    return;
                }

                showError(errorMessage);
            }
        });

        footer.getChildren().addAll(cancelButton, saveButton);
        card.getChildren().addAll(body, footer);
        overlay.getChildren().add(card);

        return overlay;
    }

    private StackPane buildEditMemberModalOverlay(Member member) {
        ComboBox<MemberType> typeInput = createTypeComboBox();
        ComboBox<String> statusInput = createStatusComboBox();

        TextField codeInput = createModalTextField("Hanya angka");
        TextField nameInput = createModalTextField("");
        TextField emailInput = createModalTextField("");
        TextField phoneInput = createModalTextField("");
        TextField majorInput = createModalTextField("");

        codeInput.setText(safe(member.getMemberCode(), ""));
        nameInput.setText(safe(member.getName(), ""));
        phoneInput.setText(safe(member.getPhone(), ""));
        majorInput.setText(safe(itemMajor(member), ""));
        typeInput.setValue(member.getMemberType());
        statusInput.setValue(resolveStatusText(member));

        Label codeErrorLabel = FxFeedback.createFieldErrorLabel();
        Label nameErrorLabel = FxFeedback.createFieldErrorLabel();

        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Edit Anggota");

        VBox body = new VBox(16);
        body.getStyleClass().add("member-modal-body");

        body.getChildren().add(buildModalField("Tipe Anggota", typeInput));
        body.getChildren().add(buildModalFieldWithError("NIM / NIS", codeInput, codeErrorLabel));
        body.getChildren().add(buildModalFieldWithError("Nama Lengkap", nameInput, nameErrorLabel));
        body.getChildren().add(buildModalField("Email", emailInput));
        body.getChildren().add(buildModalField("No. HP", phoneInput));
        body.getChildren().add(buildModalField("Fakultas / Jurusan", majorInput));
        body.getChildren().add(buildModalField("Status", statusInput));

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();
        Button saveButton = new Button("Perbarui");
        saveButton.getStyleClass().addAll("member-modal-button", "member-modal-button-save");
        saveButton.setDefaultButton(true);

        saveButton.setOnAction(event -> {
            FxFeedback.clearFieldError(codeInput, codeErrorLabel);
            FxFeedback.clearFieldError(nameInput, nameErrorLabel);

            String memberCode = normalizeInput(codeInput.getText());
            String memberName = normalizeInput(nameInput.getText());

            MemberType selectedType = typeInput.getValue();
            if (selectedType == null) {
                showError("Tipe anggota wajib dipilih.");
                return;
            }

            if (memberCode.isBlank()) {
                FxFeedback.showFieldError(codeInput, codeErrorLabel, "NIM / NIS wajib diisi.");
                return;
            }

            if (!memberCode.matches("\\d+")) {
                FxFeedback.showFieldError(codeInput, codeErrorLabel, "NIM/NIS harus berupa angka.");
                return;
            }

            if (memberName.isBlank()) {
                FxFeedback.showFieldError(nameInput, nameErrorLabel, "Nama lengkap wajib diisi.");
                return;
            }

            try {
                memberService.updateMember(
                        member.getId(),
                        memberCode,
                        memberName,
                        selectedType,
                        normalizeInput(majorInput.getText()),
                        normalizeInput(phoneInput.getText())
                );

                refreshData();
                memberTable.refresh();
                closeMemberDialog();
                showInfo("Anggota berhasil diperbarui.");
            } catch (Exception exception) {
                String errorMessage = resolveErrorMessage(exception);

                if (errorMessage != null && errorMessage.toLowerCase().contains("nim")) {
                    FxFeedback.showFieldError(codeInput, codeErrorLabel, errorMessage);
                    return;
                }

                if (errorMessage != null && errorMessage.toLowerCase().contains("nama")) {
                    FxFeedback.showFieldError(nameInput, nameErrorLabel, errorMessage);
                    return;
                }

                showError(errorMessage);
            }
        });

        footer.getChildren().addAll(cancelButton, saveButton);
        card.getChildren().addAll(body, footer);
        overlay.getChildren().add(card);

        return overlay;
    }

    private StackPane buildDeleteMemberModalOverlay(Member member) {
        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Hapus Anggota");

        VBox body = new VBox(12);
        body.getStyleClass().add("member-modal-body");

        Label messageLabel = new Label("Apakah kamu yakin ingin menghapus anggota \"" + safe(member.getName(), "-") + "\"?");
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("member-delete-info");

        Label subLabel = new Label("Tindakan ini tidak dapat dibatalkan.");
        subLabel.setWrapText(true);
        subLabel.getStyleClass().add("member-delete-subinfo");

        body.getChildren().addAll(messageLabel, subLabel);

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();

        Button deleteButton = new Button("Hapus");
        deleteButton.getStyleClass().addAll("member-modal-button", "member-modal-button-danger");
        deleteButton.setOnAction(event -> {
            try {
                memberService.deleteMember(member.getId());
                refreshData();
                memberTable.refresh();
                closeMemberDialog();
                showInfo("Anggota berhasil dihapus.");
            } catch (Exception exception) {
                showError(resolveErrorMessage(exception));
            }
        });

        footer.getChildren().addAll(cancelButton, deleteButton);
        card.getChildren().addAll(body, footer);
        overlay.getChildren().add(card);

        return overlay;
    }

    private ComboBox<MemberType> createTypeComboBox() {
        ComboBox<MemberType> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(MemberType.values());
        comboBox.getStyleClass().add("member-modal-select");
        comboBox.setMinHeight(44);
        comboBox.setPrefHeight(44);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setValue(MemberType.values().length > 0 ? MemberType.values()[0] : null);

        comboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(MemberType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatModalMemberType(item));
            }
        });

        comboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(MemberType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatModalMemberType(item));
            }
        });

        return comboBox;
    }

    private ComboBox<String> createStatusComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().setAll("Aktif", "Nonaktif");
        comboBox.getStyleClass().add("member-modal-select");
        comboBox.setMinHeight(44);
        comboBox.setPrefHeight(44);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setValue("Aktif");
        return comboBox;
    }

    private StackPane buildBaseModalOverlay() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("member-modal-overlay");
        overlay.setAlignment(Pos.CENTER);

        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                closeMemberDialog();
            }
        });

        return overlay;
    }

    private VBox buildBaseModalCard(String titleText) {
        VBox card = new VBox();
        card.getStyleClass().add("member-modal-card");
        card.setPrefWidth(ADD_MEMBER_MODAL_WIDTH);
        card.setMaxWidth(ADD_MEMBER_MODAL_WIDTH);
        card.setPrefHeight(ADD_MEMBER_MODAL_HEIGHT);
        card.setMaxHeight(ADD_MEMBER_MODAL_HEIGHT);

        HBox header = new HBox();
        header.getStyleClass().add("member-modal-header");

        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("member-modal-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("member-modal-close");
        closeButton.setOnAction(event -> closeMemberDialog());

        header.getChildren().addAll(titleLabel, headerSpacer, closeButton);
        card.getChildren().add(header);

        return card;
    }

    private HBox buildModalFooter() {
        HBox footer = new HBox(14);
        footer.getStyleClass().add("member-modal-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        return footer;
    }

    private Button createCancelModalButton() {
        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().addAll("member-modal-button", "member-modal-button-cancel");
        cancelButton.setOnAction(event -> closeMemberDialog());
        cancelButton.setCancelButton(true);
        return cancelButton;
    }

    private TextField createModalTextField(String promptText) {
        TextField field = new TextField();
        field.getStyleClass().add("member-modal-input");
        field.setPromptText(promptText);
        field.setMinHeight(44);
        field.setPrefHeight(44);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private VBox buildModalField(String labelText, Node inputControl) {
        Label label = new Label(labelText);
        label.getStyleClass().add("member-modal-label");

        if (inputControl instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        VBox field = new VBox(8, label, inputControl);
        field.getStyleClass().add("member-modal-field");
        HBox.setHgrow(field, Priority.ALWAYS);
        return field;
    }

    private VBox buildModalFieldWithError(String labelText, Node inputControl, Label errorLabel) {
        VBox field = buildModalField(labelText, inputControl);
        field.getChildren().add(errorLabel);
        return field;
    }

    private String itemMajor(Member member) {
        return member == null ? "" : safe(member.getMajor(), "");
    }

    private String resolveErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Terjadi kesalahan saat memproses data anggota.";
        }
        return message;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String normalizeInput(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(ID_LOCALE);
    }

    private boolean contains(String source, String keyword) {
        return normalize(source).contains(keyword);
    }

    private void showInfo(String message) {
        FxFeedback.showSuccessToast(FxFeedback.resolveHost(root), message);
    }

    private void showError(String message) {
        FxFeedback.showErrorToast(FxFeedback.resolveHost(root), message);
    }
}