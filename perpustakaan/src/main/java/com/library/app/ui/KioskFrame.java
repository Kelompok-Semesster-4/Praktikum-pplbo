package com.library.app.ui;

import com.library.app.service.VisitService;
import com.library.app.session.UserSession;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class KioskFrame {
   private static final DateTimeFormatter DATE_FORMATTER =
         DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"));
   private static final AtomicBoolean FX_RUNTIME_STARTED = new AtomicBoolean(false);

      private final VisitService visitService = new VisitService();
   private final UserSession session;
      private BorderPane root;
   private Stage stage;

   public KioskFrame() {
      this(null);
   }

   public KioskFrame(UserSession session) {
      this.session = session;
   }

   public void showOn(Stage hostStage) {
      this.stage = hostStage;
      hostStage.setTitle("Kiosk Layanan Perpustakaan");
      hostStage.setMinWidth(1024);
      hostStage.setMinHeight(700);
      hostStage.setScene(createScene(hostStage));
      hostStage.show();
   }

   // Adapter untuk menjaga kompatibilitas pemanggilan lama dari LoginFrame Swing.
   public void setVisible(boolean visible) {
      if (!visible) {
         if (stage != null) {
            Platform.runLater(stage::hide);
         }
         return;
      }

      ensureFxRuntime();
      Platform.runLater(() -> {
         if (stage == null) {
            stage = new Stage();
            stage.setOnHidden(event -> stage = null);
            showOn(stage);
            return;
         }
         stage.show();
      });
   }

   private Scene createScene(Stage hostStage) {
      root = new BorderPane();
      root.getStyleClass().add("app-root");
      root.setTop(createHeader(hostStage));
      root.setCenter(createDashboardContent());
      root.setBottom(createFooter());

      Scene scene = new Scene(root, 1366, 768);
      scene.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource("/styles/kiosk.css"), "File CSS kiosk tidak ditemukan")
            .toExternalForm());
      return scene;
   }

   private static void ensureFxRuntime() {
      if (Platform.isFxApplicationThread()) {
         FX_RUNTIME_STARTED.set(true);
         return;
      }

      if (FX_RUNTIME_STARTED.get()) {
         return;
      }

      synchronized (FX_RUNTIME_STARTED) {
         if (FX_RUNTIME_STARTED.get()) {
            return;
         }

         try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
         } catch (IllegalStateException ignored) {
            // Runtime JavaFX sudah aktif dari launcher lain.
         } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gagal menyiapkan runtime JavaFX.", exception);
         }

         FX_RUNTIME_STARTED.set(true);
      }
   }

   private Node createHeader(Stage hostStage) {
      HBox topBar = new HBox(12);
      topBar.getStyleClass().add("top-bar");

      HBox brandArea = new HBox(8);
      brandArea.setAlignment(Pos.CENTER_LEFT);
      brandArea.getChildren().addAll(createLibraryLogo(20), createBrandText());

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      Label statusChip = new Label("Sesi Aktif");
      statusChip.getStyleClass().add("status-chip");

      Button logoutButton = new Button("Keluar");
      logoutButton.getStyleClass().add("logout-button");
      logoutButton.setOnAction(event -> hostStage.close());

      HBox actions = new HBox(10, statusChip, logoutButton);
      actions.setAlignment(Pos.CENTER_RIGHT);

      topBar.getChildren().addAll(brandArea, spacer, actions);
      return topBar;
   }

   private VBox createBrandText() {
      Label brandTitle = new Label("Kiosk Layanan Perpustakaan");
      brandTitle.getStyleClass().add("brand-title");

      Label brandSubtitle = new Label("Mode Layanan Mandiri");
      brandSubtitle.getStyleClass().add("brand-subtitle");

      VBox brandText = new VBox(2, brandTitle, brandSubtitle);
      brandText.setAlignment(Pos.CENTER_LEFT);
      return brandText;
   }

   private Node createDashboardContent() {
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
         createVisitIcon(Color.web("#3B82F6")),
         this::showVisitContent), 0, 0);

      serviceGrid.add(createServiceCard(
            "Cari Buku",
            "Temukan koleksi buku perpustakaan",
            "card-search",
         createSearchIcon(Color.web("#059669")),
         null), 1, 0);

      serviceGrid.add(createServiceCard(
            "Beri Feedback",
            "Sampaikan saran dan masukan Anda",
            "card-feedback",
         createFeedbackIcon(Color.web("#D97706")),
         null), 0, 1);

      serviceGrid.add(createServiceCard(
            "Usul Buku",
            "Ajukan permintaan pengadaan buku",
            "card-request",
         createRequestIcon(Color.web("#7C3AED")),
         null), 1, 1);

      Label dateLabel = new Label(LocalDate.now().format(DATE_FORMATTER));
      dateLabel.getStyleClass().add("date-label");

      content.getChildren().addAll(createLibraryLogo(42), heading, subtitle, serviceGrid, dateLabel);

      StackPane contentWrapper = new StackPane(content);
      contentWrapper.setPadding(new Insets(20, 16, 22, 16));
      return contentWrapper;
   }

   private void showVisitContent() {
      if (root != null) {
         root.setCenter(createVisitContent());
      }
   }

   private void showDashboardContent() {
      if (root != null) {
         root.setCenter(createDashboardContent());
      }
   }

   private Node createVisitContent() {
      VBox content = new VBox(10);
      content.getStyleClass().add("visit-content");
      content.setAlignment(Pos.CENTER);
      content.setMaxWidth(460);

      Node visitIcon = createVisitIcon(Color.web("#3B82F6"));
      visitIcon.setScaleX(1.0);
      visitIcon.setScaleY(1.0);

      StackPane visitIconShell = new StackPane(visitIcon);
      visitIconShell.getStyleClass().add("visit-icon-shell");

      Label title = new Label("Absen Kunjungan");
      title.getStyleClass().add("visit-title");

      Label subtitle = new Label("Masukkan NIM/NIS Anda untuk mencatat kehadiran");
      subtitle.getStyleClass().add("visit-subtitle");
      subtitle.setTextAlignment(TextAlignment.CENTER);

      Label memberCodeLabel = new Label("NIM / NIS");
      memberCodeLabel.getStyleClass().add("visit-label");

      TextField memberCodeField = new TextField();
      memberCodeField.getStyleClass().add("visit-input");
      memberCodeField.setPromptText("Contoh: 2021001001");
      memberCodeField.setMaxWidth(Double.MAX_VALUE);

      Button submitButton = new Button("Catat Kehadiran");
      submitButton.getStyleClass().add("visit-submit-button");
      submitButton.setMaxWidth(Double.MAX_VALUE);

      Label statusLabel = new Label();
      statusLabel.getStyleClass().add("visit-status-label");
      statusLabel.setTextAlignment(TextAlignment.CENTER);
      statusLabel.setWrapText(true);
      statusLabel.setVisible(false);
      statusLabel.setManaged(false);

      Runnable submitAction = () -> submitVisit(memberCodeField, statusLabel);
      submitButton.setOnAction(event -> submitAction.run());
      memberCodeField.setOnAction(event -> submitAction.run());

      VBox formBox = new VBox(8, memberCodeLabel, memberCodeField, submitButton);
      formBox.getStyleClass().add("visit-form-box");
      formBox.setAlignment(Pos.CENTER_LEFT);
      formBox.setFillWidth(true);
      formBox.setPrefWidth(320);
      formBox.setMaxWidth(320);

      Label backLabel = new Label("Kembali");
      backLabel.getStyleClass().add("visit-back-link");
      backLabel.setCursor(Cursor.HAND);
      backLabel.setOnMouseClicked(event -> showDashboardContent());

      VBox.setMargin(title, new Insets(8, 0, 0, 0));
      VBox.setMargin(subtitle, new Insets(2, 0, 16, 0));
      VBox.setMargin(backLabel, new Insets(18, 0, 0, 0));

      content.getChildren().addAll(visitIconShell, title, subtitle, formBox, statusLabel, backLabel);

      StackPane wrapper = new StackPane(content);
      wrapper.setPadding(new Insets(20, 16, 22, 16));
      return wrapper;
   }

   private void submitVisit(TextField memberCodeField, Label statusLabel) {
      statusLabel.getStyleClass().removeAll("visit-status-success", "visit-status-error");

      try {
         visitService.recordMemberVisit(memberCodeField.getText());
         statusLabel.getStyleClass().add("visit-status-success");
         statusLabel.setText("Kunjungan berhasil dicatat.");
         memberCodeField.clear();
      } catch (Exception exception) {
         statusLabel.getStyleClass().add("visit-status-error");
         statusLabel.setText(exception.getMessage());
      }

      statusLabel.setManaged(true);
      statusLabel.setVisible(true);
   }

   private Node createFooter() {
      Label footerText = new Label("Sistem Manajemen Perpustakaan - Kiosk Layanan Mandiri");
      footerText.getStyleClass().add("footer-text");

      StackPane footer = new StackPane(footerText);
      footer.getStyleClass().add("footer-bar");
      footer.setPadding(new Insets(8, 12, 8, 12));
      return footer;
   }

   private StackPane createServiceCard(String titleText, String detailText, String cardClass, Node icon, Runnable onClick) {
      StackPane card = new StackPane();
      card.getStyleClass().addAll("service-card", cardClass);
      card.setPrefSize(320, 182);

      if (onClick != null) {
         card.setCursor(Cursor.HAND);
         card.setOnMouseClicked(event -> onClick.run());
      }

      StackPane iconHolder = new StackPane(icon);
      iconHolder.getStyleClass().add("service-icon-holder");

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

   private Node createLibraryLogo(double size) {
      double bookWidth = size * 0.42;
      double bookHeight = size * 0.62;

      Rectangle leftBook = new Rectangle(bookWidth, bookHeight);
      leftBook.setArcWidth(8);
      leftBook.setArcHeight(8);
      leftBook.setFill(Color.web("#374151"));
      leftBook.setTranslateX(-bookWidth * 0.34);

      Rectangle rightBook = new Rectangle(bookWidth, bookHeight);
      rightBook.setArcWidth(8);
      rightBook.setArcHeight(8);
      rightBook.setFill(Color.web("#2FAAC6"));
      rightBook.setTranslateX(bookWidth * 0.34);

      Rectangle spine = new Rectangle(size * 0.12, bookHeight);
      spine.setArcWidth(6);
      spine.setArcHeight(6);
      spine.setFill(Color.web("#1F2937"));

      StackPane icon = new StackPane(leftBook, rightBook, spine);
      icon.setPrefSize(size, size * 0.8);
      return icon;
   }

   private Node createVisitIcon(Color color) {
      Pane pane = new Pane();
      pane.setPrefSize(24, 24);
      pane.getChildren().addAll(
            iconPath("M9 8A3 3 0 1 1 15 8A3 3 0 0 1 9 8", color, 2.0),
            iconPath("M4.8 19A5.2 5.2 0 0 1 12.2 14.4", color, 2.0),
            iconPath("M14.2 15.8L16.3 17.9L20 14", color, 2.0)
      );
      return pane;
   }

   private Node createSearchIcon(Color color) {
      Pane pane = new Pane();
      pane.setPrefSize(24, 24);
      pane.getChildren().addAll(
            iconPath("M11 4A7 7 0 1 1 11 18A7 7 0 0 1 11 4", color, 2.0),
            iconPath("M16 16L20 20", color, 2.0)
      );
      return pane;
   }

   private Node createFeedbackIcon(Color color) {
      Pane pane = new Pane();
      pane.setPrefSize(24, 24);
      pane.getChildren().addAll(
            iconPath("M6 5.5H18A2 2 0 0 1 20 7.5V14A2 2 0 0 1 18 16H11L7.8 18.8V16H6A2 2 0 0 1 4 14V7.5A2 2 0 0 1 6 5.5", color, 2.0),
            iconPath("M12 8.8V12.5", color, 2.0),
            iconPath("M12 14.8H12.01", color, 2.3)
      );
      return pane;
   }

   private Node createRequestIcon(Color color) {
      Pane pane = new Pane();
      pane.setPrefSize(24, 24);
      pane.getChildren().addAll(
            iconPath("M5.5 6H18.5A1.5 1.5 0 0 1 20 7.5V17.5A1.5 1.5 0 0 1 18.5 19H5.5A1.5 1.5 0 0 1 4 17.5V7.5A1.5 1.5 0 0 1 5.5 6", color, 2.0),
            iconPath("M4 10.5H20", color, 2.0),
            iconPath("M12 11.5V15", color, 2.0),
            iconPath("M9.9 13.7L12 15.8L14.1 13.7", color, 2.0)
      );
      return pane;
   }

   private SVGPath iconPath(String pathData, Color color, double strokeWidth) {
      SVGPath path = strokeShape(new SVGPath(), color, strokeWidth);
      path.setContent(pathData);
      path.setFill(Color.TRANSPARENT);
      return path;
   }

   private <T extends Shape> T strokeShape(T shape, Color color, double strokeWidth) {
      shape.setStroke(color);
      shape.setStrokeWidth(strokeWidth);
      shape.setStrokeLineCap(StrokeLineCap.ROUND);
      shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
      return shape;
   }
}
