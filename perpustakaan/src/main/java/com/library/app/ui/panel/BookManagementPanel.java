package com.library.app.ui.panel;

import com.library.app.model.BookCatalogItem;
import com.library.app.service.BookService;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
import javafx.scene.shape.SVGPath;
import javafx.stage.Window;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class BookManagementPanel {
    private static final String ALL_CATEGORIES = "Semua Kategori";
    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
    private static final double ADD_BOOK_MODAL_WIDTH = 980;
    private static final double ADD_BOOK_MODAL_HEIGHT = 620;
    private static final double ADD_BOOK_MODAL_OFFSET_X = 72;
    private static final double BOOK_TABLE_HEADER_HEIGHT = 44;

    private final BookService bookService = new BookService();
    private final ObservableList<BookCatalogItem> catalogItems = FXCollections.observableArrayList();
    private final FilteredList<BookCatalogItem> filteredItems = new FilteredList<>(catalogItems, item -> true);

    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final Label subtitleLabel = new Label();
    private final TableView<BookCatalogItem> bookTable = new TableView<>();

    private StackPane root;
    private StackPane addBookModalOverlay;
    private StackPane modalHost;

    public Node create() {
        if (root == null) {
            VBox content = buildContent();
            root = new StackPane(content);
            root.getStyleClass().add("book-management-root");
            StackPane.setAlignment(content, Pos.TOP_LEFT);

            bindFilterEvents();
            configureTable();
        }

        refreshData();
        return root;
    }

   public void refreshData() {
    try {
        List<BookCatalogItem> items = bookService.searchCatalog("");
        catalogItems.setAll(items);
        rebuildCategoryOptions(items);
        applyFilters();
    } catch (Exception exception) {
        catalogItems.clear();
        filteredItems.setPredicate(item -> true);
        updateSubtitle();
        showError(resolveErrorMessage(exception));
    }
}

    private VBox buildContent() {
        VBox content = new VBox(18);
        content.getStyleClass().add("book-management-content");
        content.setPadding(Insets.EMPTY);

        HBox header = new HBox();
        header.getStyleClass().add("book-section-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label("Manajemen Buku");
        titleLabel.getStyleClass().add("section-title");
        subtitleLabel.getStyleClass().add("section-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+ Tambah Buku");
        addButton.getStyleClass().add("book-add-button");
        addButton.setOnAction(event -> openAddBookDialog());

        header.getChildren().addAll(titleBox, spacer, addButton);

        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().addAll("list-card", "book-toolbar-card");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(14, 16, 14, 16));

        searchField.setPromptText("Cari judul, pengarang, penerbit, atau ISBN...");
        searchField.getStyleClass().add("book-search-input");

        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent("M11 19a8 8 0 1 1 5.293-2.707l4.207 4.207-1.414 1.414-4.207-4.207A7.963 7.963 0 0 1 11 19zm0-2a6 6 0 1 0 0-12 6 6 0 0 0 0 12z");
        searchIcon.getStyleClass().add("book-search-icon-svg");

        StackPane iconWrapper = new StackPane(searchIcon);
        iconWrapper.getStyleClass().add("book-search-icon-wrapper");
        iconWrapper.setMinWidth(20);

        HBox searchBox = new HBox(8, iconWrapper, searchField);
        searchBox.getStyleClass().add("book-search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        categoryFilter.getStyleClass().add("book-category-filter");
        categoryFilter.setPrefWidth(200);
        categoryFilter.getItems().setAll(ALL_CATEGORIES);
        categoryFilter.setValue(ALL_CATEGORIES);

        toolbar.getChildren().addAll(searchBox, categoryFilter);

        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().addAll("list-card", "book-table-card");
        tableCard.setPadding(new Insets(0));

        bookTable.getStyleClass().add("book-table");
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bookTable.setFixedCellSize(56);
        bookTable.setFocusTraversable(false);

        Label emptyLabel = new Label("Belum ada data buku yang dapat ditampilkan.");
        emptyLabel.getStyleClass().add("empty-list");
        bookTable.setPlaceholder(new StackPane(emptyLabel));

        tableCard.getChildren().add(bookTable);

        content.getChildren().addAll(header, toolbar, tableCard);
        return content;
    }

    private void bindFilterEvents() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filteredItems.addListener((ListChangeListener<BookCatalogItem>) change -> {
            updateSubtitle();
            updateTableHeight();
        });
    }

    private void configureTable() {
        SortedList<BookCatalogItem> sortedItems = new SortedList<>(filteredItems);
        sortedItems.comparatorProperty().bind(bookTable.comparatorProperty());
        bookTable.setItems(sortedItems);

        TableColumn<BookCatalogItem, BookCatalogItem> bookColumn = new TableColumn<>("BUKU");
        bookColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        bookColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        bookColumn.getStyleClass().add("book-column-title");
        bookColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BookCatalogItem item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER_LEFT);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label title = new Label(safe(item.getTitle(), "Tanpa Judul"));
                title.getStyleClass().add("book-title-text");

                Label meta = new Label(buildMetaText(item));
                meta.getStyleClass().add("book-meta-text");

                VBox wrapper = new VBox(2, title, meta);
                wrapper.setAlignment(Pos.CENTER_LEFT);
                wrapper.setPadding(new Insets(0, 0, 0, 8));
                setText(null);
                setGraphic(wrapper);
            }
        });
        bookColumn.setPrefWidth(190);

        TableColumn<BookCatalogItem, String> isbnColumn = new TableColumn<>("ISBN");
        isbnColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(cell.getValue().getIsbn(), "-")));
        isbnColumn.setStyle("-fx-alignment: CENTER;");
        isbnColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item);
                value.getStyleClass().add("book-isbn-text");
                value.setTooltip(new Tooltip(item));
                setText(null);
                setGraphic(value);
            }
        });
        isbnColumn.setPrefWidth(160);

        TableColumn<BookCatalogItem, String> categoryColumn = new TableColumn<>("KATEGORI");
        categoryColumn.setCellValueFactory(cell -> new SimpleStringProperty(normalizedCategory(cell.getValue())));
        categoryColumn.setStyle("-fx-alignment: CENTER;");
        categoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label chip = new Label(item);
                chip.getStyleClass().add("book-category-chip");
                chip.setAlignment(Pos.CENTER);
                chip.setTooltip(new Tooltip(item));
                HBox wrapper = new HBox(chip);
                wrapper.setAlignment(Pos.CENTER);
                setText(null);
                setGraphic(wrapper);
            }
        });
        categoryColumn.setPrefWidth(130);

        TableColumn<BookCatalogItem, Integer> yearColumn = new TableColumn<>("TAHUN");
        yearColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPublicationYear()));
        yearColumn.setStyle("-fx-alignment: CENTER;");
        yearColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item <= 0 ? "-" : String.valueOf(item));
                value.getStyleClass().add("book-year-text");
                setText(null);
                setGraphic(value);
            }
        });
        yearColumn.setPrefWidth(90);

        TableColumn<BookCatalogItem, BookCatalogItem> copiesColumn = new TableColumn<>("EKSEMPLAR");
        copiesColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        copiesColumn.setStyle("-fx-alignment: CENTER;");
        copiesColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BookCatalogItem item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item.getAvailableCopies() + " / " + item.getTotalCopies());
                value.getStyleClass().add("book-copies-text");
                setText(null);
                setGraphic(value);
            }
        });
        copiesColumn.setPrefWidth(100);

        TableColumn<BookCatalogItem, BookCatalogItem> statusColumn = new TableColumn<>("STATUS");
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        statusColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BookCatalogItem item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                boolean available = item.getAvailableCopies() > 0;
                Label badge = new Label(available ? "Tersedia" : "Habis");
                badge.getStyleClass().addAll("status-badge", available ? "status-success" : "status-danger");

                HBox wrapper = new HBox(badge);
                wrapper.setAlignment(Pos.CENTER);

                setText(null);
                setGraphic(wrapper);
            }
        });
        statusColumn.setPrefWidth(110);

        TableColumn<BookCatalogItem, String> shelfColumn = new TableColumn<>("LOKASI");
        shelfColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(cell.getValue().getShelfCode(), "-")));
        shelfColumn.setStyle("-fx-alignment: CENTER;");
        shelfColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item);
                value.getStyleClass().add("book-shelf-text");
                setText(null);
                setGraphic(value);
            }
        });
        shelfColumn.setPrefWidth(90);

        TableColumn<BookCatalogItem, BookCatalogItem> actionColumn = new TableColumn<>("AKSI");
        actionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        actionColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BookCatalogItem item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Button editButton = createIconActionButton(createEditIcon(), "book-action-edit", "Ubah buku");
                editButton.setOnAction(event -> openEditBookDialog(item));

                Button deleteButton = createIconActionButton(createDeleteIcon(), "book-action-delete", "Hapus buku");
                deleteButton.setOnAction(event -> openDeleteBookDialog(item));

                HBox actions = new HBox(8, editButton, deleteButton);
                actions.setAlignment(Pos.CENTER);
                actions.getStyleClass().add("book-actions-wrapper");

                setText(null);
                setGraphic(actions);
            }
        });
        actionColumn.setSortable(false);
        actionColumn.setReorderable(false);
        actionColumn.setPrefWidth(105);

        bookTable.getColumns().setAll(
                bookColumn,
                isbnColumn,
                categoryColumn,
                yearColumn,
                copiesColumn,
                statusColumn,
                shelfColumn,
                actionColumn);

        updateTableHeight();
    }

    private void updateTableHeight() {
        double rowHeight = bookTable.getFixedCellSize() > 0 ? bookTable.getFixedCellSize() : 56;
        int rowCount = Math.max(filteredItems.size(), 1);
        double tableHeight = BOOK_TABLE_HEADER_HEIGHT + (rowCount * rowHeight) + 2;

        bookTable.setMinHeight(tableHeight);
        bookTable.setPrefHeight(tableHeight);
        bookTable.setMaxHeight(tableHeight);
    }

    private Button createIconActionButton(Node icon, String variantClass, String tooltipText) {
        Button button = new Button();
        button.getStyleClass().addAll("book-action-button", variantClass);
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
        icon.setContent(
                "M12.854.146a.5.5 0 0 1 .707 0l2.293 2.293a.5.5 0 0 1 0 .707L5.207 13.793 2 14.5l.707-3.207L12.854.146z");
        icon.getStyleClass().addAll("book-action-icon", "book-action-icon-edit");
        return icon;
    }

    private Node createDeleteIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(
                "M2.5 3a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h2.5a1 1 0 0 1 1 1V4H2.5V3zm1 2h9l-.8 9.2a1.5 1.5 0 0 1-1.5 1.3H5.8a1.5 1.5 0 0 1-1.5-1.3L3.5 5z");
        icon.getStyleClass().addAll("book-action-icon", "book-action-icon-delete");
        return icon;
    }

    private void rebuildCategoryOptions(List<BookCatalogItem> items) {
        String previousValue = categoryFilter.getValue();

        Set<String> categories = items.stream()
                .map(BookCatalogItem::getCategory)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> sortedCategories = categories.stream()
                .sorted(Comparator.comparing(value -> value.toLowerCase(ID_LOCALE)))
                .collect(Collectors.toList());

        categoryFilter.getItems().setAll(ALL_CATEGORIES);
        categoryFilter.getItems().addAll(sortedCategories);

        if (previousValue != null && categoryFilter.getItems().contains(previousValue)) {
            categoryFilter.setValue(previousValue);
        } else {
            categoryFilter.setValue(ALL_CATEGORIES);
        }
    }

    private void applyFilters() {
        String keyword = normalize(searchField.getText());
        String selectedCategory = normalize(categoryFilter.getValue());

        filteredItems.setPredicate(item ->
                matchesKeyword(item, keyword) && matchesCategory(item, selectedCategory)
        );

        updateSubtitle();
    }

    private boolean matchesKeyword(BookCatalogItem item, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        return contains(item.getTitle(), keyword)
                || contains(item.getAuthor(), keyword)
                || contains(item.getPublisher(), keyword)
                || contains(item.getIsbn(), keyword);
    }

    private boolean matchesCategory(BookCatalogItem item, String selectedCategory) {
        if (selectedCategory.isBlank() || ALL_CATEGORIES.equalsIgnoreCase(selectedCategory)) {
            return true;
        }
        return normalize(item.getCategory()).equals(selectedCategory);
    }

    private void updateSubtitle() {
        int total = catalogItems.size();
        int filtered = filteredItems.size();

        if (total == filtered) {
            subtitleLabel.setText(total + " buku terdaftar");
        } else {
            subtitleLabel.setText(total + " buku terdaftar • " + filtered + " hasil ditampilkan");
        }
    }

    private String normalizedCategory(BookCatalogItem item) {
        String value = item.getCategory();
        if (value == null || value.isBlank()) {
            return "Umum";
        }
        return value;
    }

    private String buildMetaText(BookCatalogItem item) {
        String author = safe(item.getAuthor(), "Penulis tidak tersedia");
        String publisher = item.getPublisher() == null ? "" : item.getPublisher().trim();
        if (publisher.isBlank()) {
            return author;
        }
        return author + " • " + publisher;
    }

    private void openAddBookDialog() {
        if (addBookModalOverlay != null) {
            return;
        }

        StackPane host = resolveModalHost();
        if (host == null) {
            return;
        }

        addBookModalOverlay = buildAddBookModalOverlay();
        modalHost = host;
        modalHost.getChildren().add(addBookModalOverlay);
    }

    private void openEditBookDialog(BookCatalogItem selectedBook) {
        if (selectedBook == null || selectedBook.getBookId() == null) {
            showError("Data buku tidak valid.");
            return;
        }

        if (addBookModalOverlay != null) {
            return;
        }

        StackPane host = resolveModalHost();
        if (host == null) {
            return;
        }

        addBookModalOverlay = buildEditBookModalOverlay(selectedBook);
        modalHost = host;
        modalHost.getChildren().add(addBookModalOverlay);
    }

    private void openDeleteBookDialog(BookCatalogItem selectedBook) {
        if (selectedBook == null || selectedBook.getBookId() == null) {
            showError("Data buku tidak valid.");
            return;
        }

        String displayTitle = safe(selectedBook.getTitle(), "Tanpa Judul");
        boolean confirmed = showDeleteConfirmation(displayTitle);
        if (!confirmed) {
            return;
        }

        try {
            bookService.deleteBook(selectedBook.getBookId());
            refreshData();
            showInfo("Buku berhasil dihapus.");
        } catch (Exception exception) {
            showError(resolveErrorMessage(exception));
        }
    }

    private void closeAddBookDialog() {
        if (addBookModalOverlay == null) {
            return;
        }

        StackPane host = modalHost != null ? modalHost : resolveModalHost();
        if (host != null) {
            host.getChildren().remove(addBookModalOverlay);
        }

        addBookModalOverlay = null;
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

    private StackPane buildAddBookModalOverlay() {
        TextField titleInput = createModalTextField("");
        TextField authorInput = createModalTextField("");
        TextField publisherInput = createModalTextField("");
        TextField isbnInput = createModalTextField("");
        TextField yearInput = createModalTextField("");
        TextField copiesInput = createModalTextField("");
        copiesInput.setText("1");
        TextField shelfInput = createModalTextField("");
        ComboBox<String> categoryInput = createModalCategoryField();

        StackPane overlay = buildBaseModalOverlay();

        VBox card = buildBaseModalCard("Tambah Buku Baru");

        VBox body = new VBox(18);
        body.getStyleClass().add("book-modal-body");
        body.getChildren().add(buildModalField("Judul Buku", titleInput));
        body.getChildren().add(buildModalTwoColumnRow("Pengarang", authorInput, "Penerbit", publisherInput));
        body.getChildren().add(buildModalTwoColumnRow("ISBN", isbnInput, "Tahun Terbit", yearInput));
        body.getChildren().add(buildModalTwoColumnRow("Jumlah Eksemplar", copiesInput, "Lokasi Rak", shelfInput));
        body.getChildren().add(buildModalHalfRow("Kategori", categoryInput));

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();
        Button saveButton = new Button("Simpan");
        saveButton.getStyleClass().addAll("book-modal-button", "book-modal-button-save");
        saveButton.setDefaultButton(true);

        saveButton.setOnAction(event -> {
            try {
                int publicationYear = parseInt(yearInput.getText(), "Tahun terbit harus berupa angka.");
                int totalCopies = parseInt(copiesInput.getText(), "Jumlah eksemplar harus berupa angka.");
                String categoryValue = normalizeInput(categoryInput.getValue());

                if (categoryValue.isBlank()) {
                    throw new IllegalArgumentException("Kategori wajib dipilih.");
                }

                bookService.addBook(
                        normalizeInput(isbnInput.getText()),
                        normalizeInput(titleInput.getText()),
                        normalizeInput(authorInput.getText()),
                        normalizeInput(publisherInput.getText()),
                        publicationYear,
                        categoryValue,
                        normalizeInput(shelfInput.getText()),
                        totalCopies
                );

                refreshData();
                closeAddBookDialog();
                showInfo("Buku berhasil disimpan.");
            } catch (Exception exception) {
                showError(resolveErrorMessage(exception));
            }
        });

        footer.getChildren().addAll(cancelButton, saveButton);
        card.getChildren().addAll(body, footer);
        overlay.getChildren().add(card);

        return overlay;
    }

    private StackPane buildEditBookModalOverlay(BookCatalogItem selectedBook) {
        TextField titleInput = createModalTextField("", selectedBook.getTitle());
        TextField authorInput = createModalTextField("", selectedBook.getAuthor());
        TextField publisherInput = createModalTextField("", selectedBook.getPublisher());
        TextField isbnInput = createModalTextField("", selectedBook.getIsbn());
        TextField yearInput = createModalTextField("", selectedBook.getPublicationYear() <= 0
                ? ""
                : String.valueOf(selectedBook.getPublicationYear()));
        TextField shelfInput = createModalTextField("", selectedBook.getShelfCode());
        ComboBox<String> categoryInput = createModalCategoryField();

        String currentCategory = normalizedCategory(selectedBook);
        if (!categoryInput.getItems().contains(currentCategory)) {
            categoryInput.getItems().add(currentCategory);
        }
        categoryInput.setValue(currentCategory);

        StackPane overlay = buildBaseModalOverlay();
        VBox card = buildBaseModalCard("Ubah Buku");

        VBox body = new VBox(18);
        body.getStyleClass().add("book-modal-body");
        body.getChildren().add(buildModalField("Judul Buku", titleInput));
        body.getChildren().add(buildModalTwoColumnRow("Pengarang", authorInput, "Penerbit", publisherInput));
        body.getChildren().add(buildModalTwoColumnRow("ISBN", isbnInput, "Tahun Terbit", yearInput));
        body.getChildren().add(buildModalHalfRow("Kategori", categoryInput));
        body.getChildren().add(buildModalHalfRow("Lokasi Rak", shelfInput));

        HBox footer = buildModalFooter();

        Button cancelButton = createCancelModalButton();
        Button saveButton = new Button("Perbarui");
        saveButton.getStyleClass().addAll("book-modal-button", "book-modal-button-save");
        saveButton.setDefaultButton(true);

        saveButton.setOnAction(event -> {
            try {
                int publicationYear = parseInt(yearInput.getText(), "Tahun terbit harus berupa angka.");
                String categoryValue = normalizeInput(categoryInput.getValue());

                if (categoryValue.isBlank()) {
                    throw new IllegalArgumentException("Kategori wajib dipilih.");
                }

                bookService.updateBook(
                        selectedBook.getBookId(),
                        normalizeInput(isbnInput.getText()),
                        normalizeInput(titleInput.getText()),
                        normalizeInput(authorInput.getText()),
                        normalizeInput(publisherInput.getText()),
                        publicationYear,
                        categoryValue,
                        normalizeInput(shelfInput.getText())
                );

                refreshData();
                closeAddBookDialog();
                showInfo("Buku berhasil diperbarui.");
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
        overlay.getStyleClass().add("book-modal-overlay");
        overlay.setAlignment(Pos.CENTER);

        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                closeAddBookDialog();
            }
        });

        return overlay;
    }

    private VBox buildBaseModalCard(String titleText) {
        VBox card = new VBox();
        card.getStyleClass().add("book-modal-card");
        card.setPrefWidth(ADD_BOOK_MODAL_WIDTH);
        card.setMaxWidth(ADD_BOOK_MODAL_WIDTH);
        card.setPrefHeight(ADD_BOOK_MODAL_HEIGHT);
        card.setMaxHeight(ADD_BOOK_MODAL_HEIGHT);

        HBox header = new HBox();
        header.getStyleClass().add("book-modal-header");

        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("book-modal-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button closeButton = new Button("x");
        closeButton.getStyleClass().add("book-modal-close");
        closeButton.setOnAction(event -> closeAddBookDialog());

        header.getChildren().addAll(titleLabel, headerSpacer, closeButton);
        card.getChildren().add(header);

        return card;
    }

    private HBox buildModalFooter() {
        HBox footer = new HBox(14);
        footer.getStyleClass().add("book-modal-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        return footer;
    }

    private Button createCancelModalButton() {
        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().addAll("book-modal-button", "book-modal-button-cancel");
        cancelButton.setOnAction(event -> closeAddBookDialog());
        cancelButton.setCancelButton(true);
        return cancelButton;
    }

    private TextField createModalTextField(String promptText) {
        TextField field = new TextField();
        field.getStyleClass().add("book-modal-input");
        field.setPromptText(promptText);
        field.setMinHeight(44);
        field.setPrefHeight(44);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private TextField createModalTextField(String promptText, String value) {
        TextField field = createModalTextField(promptText);
        field.setText(safe(value, ""));
        return field;
    }

    private ComboBox<String> createModalCategoryField() {
        ComboBox<String> field = new ComboBox<>();
        field.getStyleClass().add("book-modal-select");
        field.getItems().setAll(resolveModalCategoryOptions());
        field.setPromptText("Pilih Kategori");
        field.setMinHeight(44);
        field.setPrefHeight(44);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private List<String> resolveModalCategoryOptions() {
        Set<String> options = new LinkedHashSet<>();
        options.add("Teknologi");
        options.add("Sains");
        options.add("Fiksi");
        options.add("Sejarah");
        options.add("Pendidikan");
        options.add("Referensi");
        options.add("Umum");

        for (String category : categoryFilter.getItems()) {
            if (category == null || category.isBlank() || ALL_CATEGORIES.equalsIgnoreCase(category)) {
                continue;
            }
            options.add(category);
        }

        return options.stream().collect(Collectors.toList());
    }

    private VBox buildModalField(String labelText, Node inputControl) {
        Label label = new Label(labelText);
        label.getStyleClass().add("book-modal-label");

        if (inputControl instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        VBox field = new VBox(8, label, inputControl);
        field.getStyleClass().add("book-modal-field");
        HBox.setHgrow(field, Priority.ALWAYS);
        return field;
    }

    private HBox buildModalTwoColumnRow(String leftLabel, Node leftInput, String rightLabel, Node rightInput) {
        VBox leftField = buildModalField(leftLabel, leftInput);
        VBox rightField = buildModalField(rightLabel, rightInput);

        HBox row = new HBox(22, leftField, rightField);
        row.getStyleClass().add("book-modal-row");
        HBox.setHgrow(leftField, Priority.ALWAYS);
        HBox.setHgrow(rightField, Priority.ALWAYS);
        return row;
    }

    private HBox buildModalHalfRow(String labelText, Node inputControl) {
        VBox leftField = buildModalField(labelText, inputControl);
        Region spacer = new Region();

        HBox row = new HBox(22, leftField, spacer);
        row.getStyleClass().add("book-modal-row");
        HBox.setHgrow(leftField, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return row;
    }

    private int parseInt(String rawValue, String errorMessage) {
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private String resolveErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Terjadi kesalahan saat memproses data buku.";
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
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(ID_LOCALE);
    }

    private boolean contains(String source, String keyword) {
        return normalize(source).contains(keyword);
    }

    private void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, message);
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, message);
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

    private boolean showDeleteConfirmation(String bookTitle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus Buku");
        alert.setHeaderText("Hapus buku \"" + bookTitle + "\"?");

        Label contentLabel = new Label("Aksi ini akan menghapus data buku dan seluruh eksemplarnya.");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(420);

        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setPrefWidth(480);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        ButtonType cancelButton = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType deleteButton = new ButtonType("Hapus", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(cancelButton, deleteButton);

        Window owner = root == null || root.getScene() == null ? null : root.getScene().getWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }

        ButtonType chosen = alert.showAndWait().orElse(cancelButton);
        return chosen == deleteButton;
    }
}