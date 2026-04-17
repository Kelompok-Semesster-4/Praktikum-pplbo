package com.library.app.ui;

import com.library.app.session.UserSession;
import com.library.app.ui.panel.KioskDashboardPanel;
import com.library.app.ui.panel.KioskIconFactory;
import com.library.app.ui.panel.KioskVisitFxPanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class KioskFrame {
   private static final AtomicBoolean FX_RUNTIME_STARTED = new AtomicBoolean(false);

   private final KioskDashboardPanel dashboardPanel = new KioskDashboardPanel();
   private final KioskVisitFxPanel visitPanel = new KioskVisitFxPanel();
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
      showDashboardContent();
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
      brandArea.getChildren().addAll(KioskIconFactory.createLibraryLogo(20), createBrandText());

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

   private void showVisitContent() {
      if (root != null) {
         root.setCenter(visitPanel.createContent(this::showDashboardContent));
      }
   }

   private void showDashboardContent() {
      if (root != null) {
         root.setCenter(dashboardPanel.createContent(this::showVisitContent));
      }
   }

   private Node createFooter() {
      Label footerText = new Label("Sistem Manajemen Perpustakaan - Kiosk Layanan Mandiri");
      footerText.getStyleClass().add("footer-text");

      StackPane footer = new StackPane(footerText);
      footer.getStyleClass().add("footer-bar");
      footer.setPadding(new Insets(8, 12, 8, 12));
      return footer;
   }
}
