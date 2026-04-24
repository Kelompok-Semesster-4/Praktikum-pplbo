package com.library.app.ui.panel;

import java.util.ArrayList;
import java.util.List;

import com.library.app.model.BookCatalogItem;
import com.library.app.service.BookService;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class KioskSearchBookPanel {
    private final BookService bookService = new BookService();
    private static final double SEARCH_CARD_WIDTH = 620;
    private static final double SEARCH_FIELD_WIDTH = 500;
    private static final double THUMBNAIL_WIDTH = 60;
    private static final double THUMBNAIL_HEIGHT = 80;
    private static final double PREVIEW_WIDTH = 240;
    private static final double PREVIEW_HEIGHT = 340;

    private StackPane coverPreviewOverlay;
    private StackPane coverPreviewImageShell;
    private Label coverPreviewTitleLabel;
    private Label coverPreviewSubtitleLabel;

    public Node createContent(Runnable onBack) {
        VBox content = new VBox(10);
        content.getStyleClass().add("visit-content");
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(700);

        Node topIcon = KioskIconFactory.createSearchIcon(Color.web("#059669"));
        StackPane topIconShell = new StackPane(topIcon);
        topIconShell.getStyleClass().addAll("visit-icon-shell", "search-feature-icon-shell");
        topIconShell.setAlignment(Pos.CENTER);
        topIconShell.setMinSize(46, 46);
        topIconShell.setPrefSize(46, 46);
        topIconShell.setMaxSize(46, 46);

        Label title = new Label("Cari Buku");
        title.getStyleClass().addAll("visit-title", "search-feature-title");
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Cari berdasarkan judul, pengarang, atau kategori buku.");
        subtitle.getStyleClass().addAll("visit-subtitle", "search-feature-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(520);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        VBox headerBox = new VBox(8, topIconShell, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        Node searchIcon = KioskIconFactory.createSearchIcon(Color.web("#64748b"));
        searchIcon.setScaleX(1.0);
        searchIcon.setScaleY(1.0);

        TextField searchField = new TextField();
        searchField.setPromptText("Ketik judul atau pengarang...");
        searchField.getStyleClass().addAll("visit-input", "search-feature-input");
        searchField.setPrefWidth(SEARCH_FIELD_WIDTH);
        searchField.setMaxWidth(SEARCH_FIELD_WIDTH);

        HBox searchBox = new HBox(10);
        searchBox.getStyleClass().add("search-feature-search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(8, 15, 8, 15));
        searchBox.setMaxWidth(SEARCH_FIELD_WIDTH + 46);
        searchBox.getChildren().addAll(searchIcon, searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (isFocused) {
                if (!searchBox.getStyleClass().contains("search-feature-search-box-focused")) {
                    searchBox.getStyleClass().add("search-feature-search-box-focused");
                }
            } else {
                searchBox.getStyleClass().remove("search-feature-search-box-focused");
            }
        });

        VBox emptyBox = buildSearchStateBox(
                "Ketik minimal 2 karakter untuk mencari",
                Color.web("#059669"));
        VBox noResultBox = buildSearchStateBox(
                "Buku tidak ditemukan.",
                Color.web("#059669"));

        VBox resultsContainer = new VBox(15);
        resultsContainer.getStyleClass().add("search-feature-results");
        resultsContainer.setPadding(new Insets(10, 5, 20, 5));
        resultsContainer.setFillWidth(true);

        ScrollPane scroller = new ScrollPane(resultsContainer);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().addAll("search-book-scroller", "app-scroll");
        scroller.setFitToHeight(true);
        scroller.setPrefHeight(340);
        scroller.setMaxHeight(Double.MAX_VALUE);

        VBox bookLists = new VBox(emptyBox);
        bookLists.setAlignment(Pos.CENTER);
        bookLists.setPrefHeight(340);
        bookLists.setPrefWidth(SEARCH_FIELD_WIDTH + 46);
        bookLists.setMaxWidth(SEARCH_FIELD_WIDTH + 46);
        bookLists.setFillWidth(true);
        VBox.setVgrow(bookLists, Priority.ALWAYS);
        VBox.setVgrow(scroller, Priority.ALWAYS);

        PauseTransition pause = new PauseTransition(Duration.millis(500));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.stop();
            pause.setOnFinished(event -> {
                String query = newValue == null ? "" : newValue.trim();
                if (query.length() < 2) {
                    resultsContainer.getChildren().clear();
                    bookLists.setAlignment(Pos.CENTER);
                    bookLists.getChildren().setAll(emptyBox);
                    closeCoverPreview();
                    return;
                }

                Task<List<BookCatalogItem>> searchTask = new Task<>() {
                    @Override
                    protected List<BookCatalogItem> call() {
                        return bookService.searchCatalog(query);
                    }
                };

                searchTask.setOnSucceeded(taskEvent -> {
                    String currentQuery = searchField.getText() == null ? "" : searchField.getText().trim();
                    if (!query.equals(currentQuery)) {
                        return;
                    }
                    renderSearchResults(searchTask.getValue(), resultsContainer, bookLists, scroller, noResultBox);
                });

                searchTask.setOnFailed(taskEvent -> {
                    String currentQuery = searchField.getText() == null ? "" : searchField.getText().trim();
                    if (!query.equals(currentQuery)) {
                        return;
                    }
                    resultsContainer.getChildren().clear();
                    bookLists.setAlignment(Pos.CENTER);
                    bookLists.getChildren().setAll(noResultBox);
                });

                Thread searchThread = new Thread(searchTask, "kiosk-search-book");
                searchThread.setDaemon(true);
                searchThread.start();
            });
            pause.playFromStart();
        });

        Label back = new Label("Kembali");
        back.getStyleClass().add("visit-back-link");
        back.setCursor(Cursor.HAND);
        back.setOnMouseClicked(event -> onBack.run());

        HBox backRow = new HBox(back);
        backRow.setAlignment(Pos.CENTER);

        VBox card = new VBox(10, headerBox, searchBox, bookLists, backRow);
        card.getStyleClass().add("search-feature-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setFillWidth(false);
        card.setPrefWidth(SEARCH_CARD_WIDTH);
        card.setMaxWidth(SEARCH_CARD_WIDTH);

        VBox.setMargin(headerBox, new Insets(2, 0, 8, 0));
        VBox.setMargin(backRow, new Insets(8, 0, 0, 0));

        content.getChildren().add(card);

        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(20, 16, 22, 16));

        coverPreviewOverlay = buildCoverPreviewOverlay();
        wrapper.getChildren().add(coverPreviewOverlay);

        return wrapper;
    }

    private VBox buildSearchStateBox(String message, Color iconColor) {
        Node bigSearchIcon = KioskIconFactory.createSearchIcon(iconColor);
        bigSearchIcon.setScaleX(1.5);
        bigSearchIcon.setScaleY(1.5);

        StackPane bigSearchIconShell = new StackPane(bigSearchIcon);
        bigSearchIconShell.getStyleClass().add("search-feature-empty-icon-shell");
        bigSearchIconShell.setAlignment(Pos.CENTER);
        bigSearchIconShell.setMinSize(50, 50);
        bigSearchIconShell.setPrefSize(50, 50);
        bigSearchIconShell.setMaxSize(50, 50);
        StackPane.setAlignment(bigSearchIconShell, Pos.CENTER);

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("search-feature-hint");
        messageLabel.setTextAlignment(TextAlignment.CENTER);

        VBox stateBox = new VBox(15, bigSearchIconShell, messageLabel);
        stateBox.setAlignment(Pos.CENTER);
        stateBox.setPadding(new Insets(28, 0, 28, 0));
        return stateBox;
    }

    private void renderSearchResults(List<BookCatalogItem> items,
                                     VBox resultsContainer,
                                     VBox bookLists,
                                     ScrollPane scroller,
                                     Node noResultBox) {
        resultsContainer.getChildren().clear();

        for (BookCatalogItem item : items) {
            resultsContainer.getChildren().add(createBookCard(
                    item.getCoverUrl(),
                    item.getIsbn(),
                    item.getTitle(),
                    item.getAuthor(),
                    item.getPublisher(),
                    String.valueOf(item.getPublicationYear()),
                    item.getCategory(),
                    item.getShelfCode(),
                    item.getAvailableCopies()));
        }

        if (items.isEmpty()) {
            bookLists.setAlignment(Pos.CENTER);
            bookLists.getChildren().setAll(noResultBox);
        } else {
            bookLists.setAlignment(Pos.TOP_CENTER);
            bookLists.getChildren().setAll(scroller);
        }
    }

    private HBox createBookCard(String coverUrl,
                                String isbn,
                                String title,
                                String author,
                                String publisher,
                                String year,
                                String category,
                                String shelf,
                                int availableCopies) {
        StackPane cover = createCover(coverUrl, isbn);

        Label titleLabel = new Label(safeText(title, "Judul buku tidak tersedia"));
        titleLabel.getStyleClass().add("book-card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        String subtitleText = buildBookMeta(author, publisher, year);
        Label subtitleLabel = new Label(subtitleText);
        subtitleLabel.getStyleClass().add("book-card-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);
        subtitleLabel.setManaged(!subtitleText.isBlank());
        subtitleLabel.setVisible(!subtitleText.isBlank());

        VBox details = new VBox(8);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label categoryPill = new Label(safeText(category, "Kategori"));
        categoryPill.getStyleClass().add("book-card-category");

        Label shelfLabel = new Label(safeText(shelf, "Lokasi rak belum diatur"));
        shelfLabel.getStyleClass().add("book-card-shelf");

        Label availableCopiesLabel = new Label(availableCopies + " tersedia");
        availableCopiesLabel.getStyleClass().add("book-card-copies");
        String color = availableCopies > 0 ? "#10b981" : "#ef4444";
        availableCopiesLabel.setStyle("-fx-text-fill: " + color);

        HBox infoRow = new HBox(15);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setMaxWidth(Double.MAX_VALUE);
        infoRow.getChildren().addAll(categoryPill, shelfLabel, availableCopiesLabel);

        details.getChildren().addAll(titleLabel, subtitleLabel, infoRow);

        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("search-book-card");
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(110);
        card.getChildren().addAll(cover, details);
        card.setOnMouseClicked(event -> openCoverPreview(coverUrl, isbn, title, author, publisher, year));
        return card;
    }

    private StackPane createCover(String coverUrl, String isbn) {
        StackPane coverArea = new StackPane();
        coverArea.setMinSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        coverArea.setPrefSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        coverArea.setMaxSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        coverArea.getStyleClass().add("book-card-cover");
        updateCoverDisplay(coverArea, resolveCoverUrl(coverUrl, isbn), THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, false);
        return coverArea;
    }

    private StackPane buildCoverPreviewOverlay() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("cover-preview-overlay");
        overlay.setVisible(false);
        overlay.setManaged(false);
        overlay.setOnMouseClicked(event -> closeCoverPreview());

        Label headingLabel = new Label("Preview Cover");
        headingLabel.getStyleClass().add("cover-preview-heading");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("x");
        closeButton.getStyleClass().add("cover-preview-close");
        closeButton.setOnAction(event -> closeCoverPreview());

        HBox header = new HBox(12, headingLabel, spacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);

        coverPreviewImageShell = new StackPane();
        coverPreviewImageShell.getStyleClass().add("cover-preview-image-shell");
        coverPreviewImageShell.setMinSize(0, 0);
        coverPreviewImageShell.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        coverPreviewTitleLabel = new Label();
        coverPreviewTitleLabel.getStyleClass().add("cover-preview-title");
        coverPreviewTitleLabel.setWrapText(true);
        coverPreviewTitleLabel.setTextAlignment(TextAlignment.CENTER);
        coverPreviewTitleLabel.setAlignment(Pos.CENTER);
        coverPreviewTitleLabel.setMaxWidth(Double.MAX_VALUE);

        coverPreviewSubtitleLabel = new Label();
        coverPreviewSubtitleLabel.getStyleClass().add("cover-preview-subtitle");
        coverPreviewSubtitleLabel.setWrapText(true);
        coverPreviewSubtitleLabel.setTextAlignment(TextAlignment.CENTER);
        coverPreviewSubtitleLabel.setAlignment(Pos.CENTER);
        coverPreviewSubtitleLabel.setMaxWidth(Double.MAX_VALUE);

        Label helperLabel = new Label("Klik area luar untuk menutup.");
        helperLabel.getStyleClass().add("cover-preview-hint");

        VBox card = new VBox(18, header, coverPreviewImageShell, coverPreviewTitleLabel, coverPreviewSubtitleLabel, helperLabel);
        card.getStyleClass().add("cover-preview-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(360);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

        overlay.getChildren().add(card);
        updateCoverDisplay(coverPreviewImageShell, "", PREVIEW_WIDTH, PREVIEW_HEIGHT, true);
        return overlay;
    }

    private void openCoverPreview(String coverUrl,
                                  String isbn,
                                  String title,
                                  String author,
                                  String publisher,
                                  String year) {
        if (coverPreviewOverlay == null || coverPreviewImageShell == null) {
            return;
        }

        coverPreviewTitleLabel.setText(safeText(title, "Judul buku tidak tersedia"));

        String subtitleText = buildBookMeta(author, publisher, year);
        coverPreviewSubtitleLabel.setText(subtitleText);
        coverPreviewSubtitleLabel.setManaged(!subtitleText.isBlank());
        coverPreviewSubtitleLabel.setVisible(!subtitleText.isBlank());

        updateCoverDisplay(coverPreviewImageShell, resolveCoverUrl(coverUrl, isbn), PREVIEW_WIDTH, PREVIEW_HEIGHT, true);

        coverPreviewOverlay.setManaged(true);
        coverPreviewOverlay.setVisible(true);
        coverPreviewOverlay.toFront();
    }

    private void closeCoverPreview() {
        if (coverPreviewOverlay == null) {
            return;
        }
        coverPreviewOverlay.setVisible(false);
        coverPreviewOverlay.setManaged(false);
    }

    private void updateCoverDisplay(StackPane container,
                                    String imageUrl,
                                    double fitWidth,
                                    double fitHeight,
                                    boolean previewMode) {
        String resolvedUrl = imageUrl == null ? "" : imageUrl.trim();
        container.getProperties().put("coverImageUrl", resolvedUrl);
        container.getChildren().setAll(previewMode ? createPreviewFallback() : createThumbnailFallback());

        if (resolvedUrl.isBlank()) {
            return;
        }

        Image coverImage = new Image(resolvedUrl, true);
        if (coverImage.getProgress() >= 1.0 && !coverImage.isError()) {
            setLoadedImage(container, coverImage, fitWidth, fitHeight, resolvedUrl);
            return;
        }

        coverImage.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 1.0 && !coverImage.isError()) {
                Platform.runLater(() -> setLoadedImage(container, coverImage, fitWidth, fitHeight, resolvedUrl));
            }
        });

        coverImage.errorProperty().addListener((observable, wasError, isError) -> {
            if (Boolean.TRUE.equals(isError)
                    && resolvedUrl.equals(container.getProperties().get("coverImageUrl"))) {
                Platform.runLater(() -> container.getChildren().setAll(
                        previewMode ? createPreviewFallback() : createThumbnailFallback()));
            }
        });
    }

    private void setLoadedImage(StackPane container,
                                Image image,
                                double fitWidth,
                                double fitHeight,
                                String resolvedUrl) {
        if (!resolvedUrl.equals(container.getProperties().get("coverImageUrl"))) {
            return;
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        container.getChildren().setAll(imageView);
    }

    private Node createThumbnailFallback() {
        StackPane fallback = new StackPane(KioskIconFactory.createBookIcon(Color.web("#3b82f6")));
        fallback.setAlignment(Pos.CENTER);
        return fallback;
    }

    private Node createPreviewFallback() {
        Node fallbackIcon = KioskIconFactory.createBookIcon(Color.web("#3b82f6"));
        fallbackIcon.setScaleX(2.8);
        fallbackIcon.setScaleY(2.8);

        Label placeholderLabel = new Label("Cover tidak tersedia");
        placeholderLabel.getStyleClass().add("cover-preview-placeholder-text");

        VBox placeholder = new VBox(12, fallbackIcon, placeholderLabel);
        placeholder.getStyleClass().add("cover-preview-placeholder");
        placeholder.setAlignment(Pos.CENTER);
        return placeholder;
    }

    private String resolveCoverUrl(String coverUrl, String isbn) {
        if (coverUrl != null && !coverUrl.trim().isEmpty()) {
            return coverUrl.trim();
        }
        if (isbn != null && !isbn.trim().isEmpty()) {
            return "https://covers.openlibrary.org/b/isbn/" + isbn.trim() + "-L.jpg?default=false";
        }
        return "";
    }

    private String buildBookMeta(String author, String publisher, String year) {
        List<String> parts = new ArrayList<>();

        if (author != null && !author.trim().isEmpty()) {
            parts.add(author.trim());
        }
        if (publisher != null && !publisher.trim().isEmpty()) {
            parts.add(publisher.trim());
        }
        if (year != null) {
            String cleanedYear = year.trim();
            if (!cleanedYear.isEmpty() && !"0".equals(cleanedYear)) {
                parts.add(cleanedYear);
            }
        }

        return String.join(" | ", parts);
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
