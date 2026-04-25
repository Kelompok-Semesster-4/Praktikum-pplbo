package com.library.app.ui.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class KioskDashboardPanel {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"));

    public Node createContent(Runnable onVisitClick,
                              Runnable onSearchBookClick,
                              Runnable onFeedbackClick,
                              Runnable onProcurementClick) {
        VBox content = new VBox(14);
        content.getStyleClass().add("center-content");
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(760);

        Label heading = new Label("Layanan Perpustakaan");
        heading.getStyleClass().add("heading");

        Label subtitle = new Label("Pilih layanan yang Anda butuhkan");
        subtitle.getStyleClass().add("subtitle");

        GridPane serviceGrid = new GridPane();
        serviceGrid.getStyleClass().add("service-grid");
        serviceGrid.setHgap(24);
        serviceGrid.setVgap(22);
        serviceGrid.setAlignment(Pos.CENTER);

        serviceGrid.add(createServiceCard(
                "Absen Kunjungan",
                "Catat kehadiran Anda hari ini",
                "card-visit",
                KioskIconFactory.createVisitIcon(Color.web("#3B82F6")),
                onVisitClick), 0, 0);

        serviceGrid.add(createServiceCard(
                "Cari Buku",
                "Temukan koleksi buku perpustakaan",
                "card-search",
                KioskIconFactory.createSearchIcon(Color.web("#059669")),
                onSearchBookClick), 1, 0);

        serviceGrid.add(createServiceCard(
                "Beri Feedback",
                "Sampaikan saran dan masukan Anda",
                "card-feedback",
                KioskIconFactory.createFeedbackIcon(Color.web("#D97706")),
                onFeedbackClick), 0, 1);

        serviceGrid.add(createServiceCard(
                "Usul Buku",
                "Ajukan permintaan pengadaan buku",
                "card-request",
                KioskIconFactory.createRequestIcon(Color.web("#7C3AED")),
                onProcurementClick), 1, 1);

        Label dateLabel = new Label(LocalDate.now().format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("date-label");

        content.getChildren().addAll(
                KioskIconFactory.createLibraryLogo(42),
                heading,
                subtitle,
                serviceGrid,
                dateLabel);

        StackPane contentWrapper = new StackPane(content);
        contentWrapper.setAlignment(Pos.CENTER);
        contentWrapper.setPadding(new Insets(20, 16, 22, 16));
        return contentWrapper;
    }

    private StackPane createServiceCard(String titleText,
                                        String detailText,
                                        String cardClass,
                                        Node icon,
                                        Runnable onClick) {
        StackPane card = new StackPane();
        card.getStyleClass().addAll("service-card", cardClass);
        card.setPrefSize(320, 182);

        if (onClick != null) {
            card.setCursor(Cursor.HAND);
            card.setOnMouseClicked(event -> onClick.run());
        }

        StackPane iconHolder = new StackPane(icon);
        iconHolder.getStyleClass().add("service-icon-holder");
        iconHolder.setAlignment(Pos.CENTER);
        iconHolder.setMinSize(58, 58);
        iconHolder.setPrefSize(58, 58);
        iconHolder.setMaxSize(58, 58);
        StackPane.setAlignment(icon, Pos.CENTER);

        Label title = new Label(titleText);
        title.getStyleClass().add("service-title");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        Label detail = new Label(detailText);
        detail.getStyleClass().add("service-detail");
        detail.setTextAlignment(TextAlignment.CENTER);
        detail.setAlignment(Pos.CENTER);
        detail.setWrapText(true);
        detail.setMaxWidth(248);

        VBox cardContent = new VBox(12, iconHolder, title, detail);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setFillWidth(true);

        card.getChildren().add(cardContent);
        return card;
    }
}
