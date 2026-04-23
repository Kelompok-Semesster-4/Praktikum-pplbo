package com.library.app.ui.panel;

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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class KioskSearchBookPanel {
    private final BookService bookService = new BookService();
    private static final double SEARCH_CARD_WIDTH = 620;
    private static final double SEARCH_FIELD_WIDTH = 500;

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

        Node bigSearchIcon = KioskIconFactory.createSearchIcon(Color.web("#059669"));
        bigSearchIcon.setScaleX(1.5);
        bigSearchIcon.setScaleY(1.5);

        StackPane bigSearchIconShell = new StackPane(bigSearchIcon);
        bigSearchIconShell.getStyleClass().add("search-feature-empty-icon-shell");
        bigSearchIconShell.setAlignment(Pos.CENTER);
        bigSearchIconShell.setMinSize(50, 50);
        bigSearchIconShell.setPrefSize(50, 50);
        bigSearchIconShell.setMaxSize(50, 50);
        StackPane.setAlignment(bigSearchIconShell, Pos.CENTER);

        Label hint = new Label("Ketik minimal 2 karakter untuk mencari");
        hint.getStyleClass().add("search-feature-hint");
        hint.setTextAlignment(TextAlignment.CENTER);

        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(28, 0, 28, 0));
        emptyBox.getChildren().addAll(bigSearchIconShell, hint);

        Label noResultLabel = new Label("Buku tidak ditemukan.");
        noResultLabel.getStyleClass().add("search-feature-hint");

        VBox noResultBox = new VBox(15, bigSearchIconShell, noResultLabel);
        noResultBox.setAlignment(Pos.CENTER);
        noResultBox.setPadding(new Insets(28, 0, 28, 0));

        VBox resultsContainer = new VBox(15);
        resultsContainer.getStyleClass().add("search-feature-results");
        resultsContainer.setPadding(new Insets(10, 5, 20, 5));
        resultsContainer.setFillWidth(true);

        ScrollPane scroller = new ScrollPane(resultsContainer);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().addAll("search-book-scroller", "app-scroll");

        VBox bookLists = new VBox(emptyBox);
        bookLists.setAlignment(Pos.CENTER);
        bookLists.setPrefHeight(340);
        bookLists.setPrefWidth(SEARCH_FIELD_WIDTH + 46);
        bookLists.setMaxWidth(SEARCH_FIELD_WIDTH + 46);
        VBox.setVgrow(bookLists, Priority.ALWAYS);

        PauseTransition pause = new PauseTransition(Duration.millis(500));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(event -> {
                if (newValue == null || newValue.trim().length() < 2) {
                    bookLists.getChildren().setAll(emptyBox);
                    resultsContainer.getChildren().clear();
                    return;
                }

                Task<Void> search = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        List<BookCatalogItem> items = bookService.searchCatalog(newValue);

                        Platform.runLater(() -> {
                            resultsContainer.getChildren().clear();

                            for (BookCatalogItem item : items) {
                                HBox card = createBookCard(
                                    item.getCoverUrl(),
                                    item.getIsbn(), 
                                    item.getTitle(), 
                                    item.getAuthor(), 
                                    item.getPublisher(), 
                                    String.valueOf(item.getPublicationYear()), 
                                    item.getCategory(), 
                                    item.getShelfCode(), 
                                    item.getAvailableCopies());
                                
                                resultsContainer.getChildren().add(card);
                            }
                            if (items.isEmpty()) {
                                bookLists.getChildren().setAll(noResultBox);
                            } else {
                                bookLists.getChildren().setAll(scroller);
                            }
                        });

                        return null;
                    }
                };
                new Thread(search).start();
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
        return wrapper;
    }

    private HBox createBookCard(String coverUrl,
                                String isbn,
                                String title,
                                String author,
                                String publisher,
                                String year,
                                String category,
                                String shelf,
                                int availableCopies
                                ) {
                    StackPane cover = createCover(coverUrl, isbn);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("book-card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label subtitleLabel = new Label(author + " • " + publisher + " • " + year);
        subtitleLabel.getStyleClass().add("book-card-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);

        VBox details = new VBox(8);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label categoryPill = new Label(category);
        categoryPill.getStyleClass().add("book-card-category");

        Label shelfLabel = new Label(shelf);
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
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(110);
        card.getChildren().addAll(cover, details);
        return card;
    }

    private StackPane createCover(String coverUrl, String isbn) {
        StackPane coverArea = new StackPane();
        coverArea.setMinSize(60, 80);
        coverArea.setPrefSize(60, 80);
        coverArea.getStyleClass().add("book-card-cover");

        Node fallbackIcon = KioskIconFactory.createBookIcon(Color.web("#3b82f6"));
        coverArea.getChildren().add(fallbackIcon);

        String url = null;
        if (coverUrl != null && !coverUrl.trim().isEmpty()) {
            url = coverUrl.trim();
        } else if (isbn != null && !isbn.trim().isEmpty()) {
            url = "https://covers.openlibrary.org/b/isbn/" + isbn + "-M.jpg?default=false";
        }

        if (url != null && !url.isBlank()) {
            Image coverImage = new Image(url, true);

            coverImage.progressProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 1.0 && !coverImage.isError()) {
                    Platform.runLater(() -> {
                        ImageView img = new ImageView(coverImage);
                        img.setFitWidth(60);
                        img.setFitHeight(80);
                        img.setPreserveRatio(true);

                        coverArea.getChildren().clear();
                        coverArea.getChildren().add(img);
                    });
                }
            });
        }

        return coverArea;
    }
}
