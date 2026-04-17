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
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
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

    private final BookService bookService = new BookService();
    private final ObservableList<BookCatalogItem> catalogItems = FXCollections.observableArrayList();
    private final FilteredList<BookCatalogItem> filteredItems = new FilteredList<>(catalogItems, item -> true);

    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final Label subtitleLabel = new Label();
    private final TableView<BookCatalogItem> bookTable = new TableView<>();

    private VBox root;

    public Node create() {
        if (root == null) {
            root = buildContent();
            bindFilterEvents();
            configureTable();
        }
        refreshData();
        return root;
    }

    public void refreshData() {
        List<BookCatalogItem> items = bookService.searchCatalog("");
        catalogItems.setAll(items);
        rebuildCategoryOptions(items);
        applyFilters();
    }

    private VBox buildContent() {
        VBox content = new VBox(16);
        content.getStyleClass().add("book-management-content");
        content.setPadding(new Insets(16, 24, 24, 24));

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

        searchField.setPromptText("Cari judul, pengarang, atau ISBN...");
        searchField.getStyleClass().add("book-search-input");

        Label searchIcon = new Label("\u2315");
        searchIcon.getStyleClass().add("book-search-icon");

        HBox searchBox = new HBox(8, searchIcon, searchField);
        searchBox.getStyleClass().add("book-search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        categoryFilter.getStyleClass().add("book-category-filter");
        categoryFilter.setPrefWidth(190);
        categoryFilter.getItems().setAll(ALL_CATEGORIES);
        categoryFilter.setValue(ALL_CATEGORIES);

        toolbar.getChildren().addAll(searchBox, categoryFilter);

        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().addAll("list-card", "book-table-card");
        tableCard.setPadding(new Insets(0));
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        bookTable.getStyleClass().add("book-table");
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bookTable.setFixedCellSize(56);
        bookTable.setMinHeight(260);
        bookTable.setFocusTraversable(false);
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        Label emptyLabel = new Label("Belum ada buku yang sesuai filter.");
        emptyLabel.getStyleClass().add("empty-list");
        bookTable.setPlaceholder(new StackPane(emptyLabel));

        tableCard.getChildren().add(bookTable);

        content.getChildren().addAll(header, toolbar, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        return content;
    }

    private void bindFilterEvents() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filteredItems.addListener((ListChangeListener<BookCatalogItem>) change -> updateSubtitle());
    }

    private void configureTable() {
        SortedList<BookCatalogItem> sortedItems = new SortedList<>(filteredItems);
        sortedItems.comparatorProperty().bind(bookTable.comparatorProperty());
        bookTable.setItems(sortedItems);

        TableColumn<BookCatalogItem, BookCatalogItem> bookColumn = new TableColumn<>("BUKU");
        bookColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        bookColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BookCatalogItem item, boolean empty) {
                super.updateItem(item, empty);
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
                setText(null);
                setGraphic(wrapper);
            }
        });
        bookColumn.setPrefWidth(280);

        TableColumn<BookCatalogItem, String> isbnColumn = new TableColumn<>("ISBN");
        isbnColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(cell.getValue().getIsbn(), "-")));
        isbnColumn.setStyle("-fx-alignment: CENTER;");
        isbnColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item);
                value.getStyleClass().addAll("book-isbn-text", "book-isbn-chip");
                value.setAlignment(Pos.CENTER);
                value.setTooltip(new Tooltip(item));
                HBox wrapper = new HBox(value);
                wrapper.setAlignment(Pos.CENTER);
                setText(null);
                setGraphic(wrapper);
                setAlignment(Pos.CENTER);
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
                setAlignment(Pos.CENTER);
            }
        });
        categoryColumn.setPrefWidth(128);

        TableColumn<BookCatalogItem, Integer> yearColumn = new TableColumn<>("TAHUN");
        yearColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPublicationYear()));
        yearColumn.setStyle("-fx-alignment: CENTER;");
        yearColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item <= 0 ? "-" : String.valueOf(item));
                value.getStyleClass().add("book-year-text");
                setText(null);
                setGraphic(value);
                setAlignment(Pos.CENTER);
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
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item.getAvailableCopies() + " / " + item.getTotalCopies());
                value.getStyleClass().add("book-copies-text");
                setText(null);
                setGraphic(value);
                setAlignment(Pos.CENTER);
            }
        });
        copiesColumn.setPrefWidth(90);

        TableColumn<BookCatalogItem, BookCatalogItem> statusColumn = new TableColumn<>("STATUS");
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        statusColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BookCatalogItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                boolean available = item.getAvailableCopies() > 0;
                Label badge = new Label(available ? "Tersedia" : "Habis");
                badge.getStyleClass().addAll("status-badge", available ? "status-success" : "status-warning");
                HBox wrapper = new HBox(badge);
                wrapper.setAlignment(Pos.CENTER);
                setText(null);
                setGraphic(wrapper);
                setAlignment(Pos.CENTER);
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
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label value = new Label(item);
                value.getStyleClass().add("book-shelf-text");
                setText(null);
                setGraphic(value);
                setAlignment(Pos.CENTER);
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
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Button editButton = createIconActionButton(createEditIcon(), "book-action-edit", "Ubah buku");
                editButton.setOnAction(event -> showInfo("Fitur ubah buku masih dalam pengembangan."));

                Button deleteButton = createIconActionButton(createDeleteIcon(), "book-action-delete", "Hapus buku");
                deleteButton.setOnAction(event -> showInfo("Fitur hapus buku masih dalam pengembangan."));

                HBox actions = new HBox(8, editButton, deleteButton);
                actions.setAlignment(Pos.CENTER);
                actions.getStyleClass().add("book-actions-wrapper");
                setText(null);
                setGraphic(actions);
                setAlignment(Pos.CENTER);
            }
        });
        actionColumn.setSortable(false);
        actionColumn.setReorderable(false);
        actionColumn.setPrefWidth(102);

        bookTable.getColumns().setAll(
                bookColumn,
                isbnColumn,
                categoryColumn,
                yearColumn,
                copiesColumn,
                statusColumn,
                shelfColumn,
                actionColumn);
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
        icon.setContent("M12.854.146a.5.5 0 0 1 .707 0l2.293 2.293a.5.5 0 0 1 0 .707L5.207 13.793 2 14.5l.707-3.207L12.854.146z");
        icon.getStyleClass().addAll("book-action-icon", "book-action-icon-edit");
        return icon;
    }

    private Node createDeleteIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M2.5 3a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h2.5a1 1 0 0 1 1 1V4H2.5V3zm1 2h9l-.8 9.2a1.5 1.5 0 0 1-1.5 1.3H5.8a1.5 1.5 0 0 1-1.5-1.3L3.5 5z");
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
            return;
        }
        categoryFilter.setValue(ALL_CATEGORIES);
    }

    private void applyFilters() {
        String keyword = normalize(searchField.getText());
        String selectedCategory = normalize(categoryFilter.getValue());
        filteredItems.setPredicate(item -> matchesKeyword(item, keyword) && matchesCategory(item, selectedCategory));
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
        subtitleLabel.setText(filteredItems.size() + " buku terdaftar");
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
        return author + " - " + publisher;
    }

    private void openAddBookDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Buku");
        dialog.setHeaderText("Masukkan data buku baru");
        dialog.getDialogPane().getStyleClass().add("book-dialog-pane");

        ButtonType saveType = new ButtonType("Simpan Buku", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, saveType);

        TextField isbnInput = new TextField();
        TextField titleInput = new TextField();
        TextField authorInput = new TextField();
        TextField publisherInput = new TextField();
        TextField yearInput = new TextField();
        TextField categoryInput = new TextField();
        TextField shelfInput = new TextField();
        TextField copiesInput = new TextField("1");

        isbnInput.setPromptText("978-602-123-001");
        titleInput.setPromptText("Judul buku");
        authorInput.setPromptText("Nama penulis");
        publisherInput.setPromptText("Penerbit");
        yearInput.setPromptText("2026");
        categoryInput.setPromptText("Teknologi");
        shelfInput.setPromptText("Rak A-01");
        copiesInput.setPromptText("1");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(12, 2, 4, 2));

        addFormRow(form, 0, "ISBN", isbnInput);
        addFormRow(form, 1, "Judul", titleInput);
        addFormRow(form, 2, "Penulis", authorInput);
        addFormRow(form, 3, "Penerbit", publisherInput);
        addFormRow(form, 4, "Tahun", yearInput);
        addFormRow(form, 5, "Kategori", categoryInput);
        addFormRow(form, 6, "Lokasi Rak", shelfInput);
        addFormRow(form, 7, "Jumlah Eksemplar", copiesInput);

        dialog.getDialogPane().setContent(form);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                int publicationYear = parseInt(yearInput.getText(), "Tahun publikasi harus berupa angka.");
                int totalCopies = parseInt(copiesInput.getText(), "Jumlah eksemplar harus berupa angka.");

                bookService.addBook(
                        isbnInput.getText(),
                        titleInput.getText(),
                        authorInput.getText(),
                        publisherInput.getText(),
                        publicationYear,
                        categoryInput.getText(),
                        shelfInput.getText(),
                        totalCopies);

                refreshData();
                showInfo("Buku berhasil disimpan.");
            } catch (Exception exception) {
                showError(resolveErrorMessage(exception));
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void addFormRow(GridPane form, int row, String labelText, TextField field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("book-form-label");
        form.add(label, 0, row);
        form.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
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
            return "Terjadi kesalahan saat menyimpan buku.";
        }
        return message;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
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
}
