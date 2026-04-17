package com.library.app.ui;

import com.library.app.dao.UserDAO;
import com.library.app.model.DashboardSummary;
import com.library.app.model.User;
import com.library.app.model.enums.Role;
import com.library.app.session.UserSession;
import com.library.app.ui.fx.ReportSectionView;
import com.library.app.ui.panel.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class AdminFrame extends BaseMainFrame {
   public AdminFrame(UserSession session) {
      super("Library Management System - Admin", session);
      tabbedPane.addTab("Dashboard", new DashboardPanel());
      tabbedPane.addTab("Buku", new BookManagementPanel());
      tabbedPane.addTab("Anggota", new MemberManagementPanel());
      tabbedPane.addTab("Kunjungan", new VisitPanel());
      tabbedPane.addTab("Peminjaman", new LoanPanel());
      tabbedPane.addTab("Pengembalian", new ReturnPanel());
      tabbedPane.addTab("Feedback", new FeedbackPanel(true));
      tabbedPane.addTab("Request", new ProcurementPanel(true));
   }
}

class AdminDashboardFxApp extends Application {
   private static final int MONTH_RANGE = 7;
   private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");

   private final com.library.app.service.DashboardService dashboardService = new com.library.app.service.DashboardService();
   private final UserDAO userDAO = new UserDAO();
   private final Map<String, Button> menuButtons = new LinkedHashMap<>();
   private Label topbarTitleLabel;
   private StackPane contentSwitcher;
   private ReportSectionView reportSectionView;

   @Override
   public void start(Stage stage) {
      DashboardSummary summary = safeLoad(dashboardService::getSummary, new DashboardSummary());
      LinkedHashMap<String, Integer> visitsPerMonth = safeLoad(
            () -> dashboardService.getMonthlyVisits(MONTH_RANGE),
            new LinkedHashMap<>());
      LinkedHashMap<String, int[]> loanTrend = safeLoad(
            () -> dashboardService.getMonthlyLoanReturnTrend(MONTH_RANGE),
            new LinkedHashMap<>());
      List<String[]> recentLoans = safeLoad(() -> dashboardService.getRecentLoans(6), Collections.emptyList());
      List<String[]> todayVisits = safeLoad(() -> dashboardService.getTodayVisits(6), Collections.emptyList());
      String[] adminIdentity = safeLoad(this::loadAdminIdentity,
            new String[] { "Admin Perpustakaan", "admin@perpus.ac.id", "A" });

      BorderPane root = new BorderPane();
      root.getStyleClass().add("app-root");
      root.setLeft(createSidebar());
      root.setCenter(createContent(summary, visitsPerMonth, loanTrend, recentLoans, todayVisits, adminIdentity));

      Scene scene = new Scene(root, 1366, 768);
      String stylesheet = getClass().getResource("/styles/dashboard.css") == null
            ? null
            : getClass().getResource("/styles/dashboard.css").toExternalForm();
      if (stylesheet != null) {
         scene.getStylesheets().add(stylesheet);
      }
      String reportStylesheet = getClass().getResource("/styles/report.css") == null
            ? null
            : getClass().getResource("/styles/report.css").toExternalForm();
      if (reportStylesheet != null) {
         scene.getStylesheets().add(reportStylesheet);
      }

      stage.initStyle(StageStyle.DECORATED);
      stage.setTitle("Sistem Manajemen Perpustakaan");
      stage.setWidth(1240);
      stage.setHeight(760);
      stage.setMinWidth(980);
      stage.setMinHeight(620);
      stage.setFullScreen(false);
      stage.setMaximized(true);
      stage.setScene(scene);
      stage.centerOnScreen();
      stage.show();
   }

   public void showDashboard(Stage stage) {
      try {
         start(stage);
      } catch (Exception exception) {
         throw new RuntimeException("Gagal menampilkan dashboard JavaFX.", exception);
      }
   }

   private Node createSidebar() {
      VBox sidebar = new VBox(18);
      sidebar.getStyleClass().add("sidebar");
      sidebar.setPadding(new Insets(18, 16, 18, 16));
      sidebar.setPrefWidth(300);

      VBox brandBox = new VBox(2);
      Label brandTitle = new Label("Sistem Manajemen");
      Label brandSubtitle = new Label("Perpustakaan");
      brandTitle.getStyleClass().add("brand-title");
      brandSubtitle.getStyleClass().add("brand-subtitle");
      brandBox.getChildren().addAll(brandTitle, brandSubtitle);

      Label roleBadge = new Label("Administrator");
      roleBadge.getStyleClass().addAll("sidebar-badge", "is-admin");

      VBox menuContainer = new VBox(6);
      menuContainer.getStyleClass().add("menu-container");
      menuContainer.getChildren().addAll(
            createMenuButton("Dashboard", "▦", true, () -> openFxSection("Dashboard")),
            createMenuButton("Manajemen Buku", "📚", false, () -> openFxSection("Manajemen Buku")),
            createMenuButton("Manajemen Anggota", "👥", false, () -> openFxSection("Manajemen Anggota")),
            createMenuButton("Peminjaman & Pengembalian", "⇄", false,
                  () -> openFxSection("Peminjaman & Pengembalian")),
            createMenuButton("Laporan", "📊", false,
                  () -> openFxSection("Laporan")),
            createMenuButton("Feedback & Permintaan", "💬", false,
                  () -> openFxSection("Feedback & Permintaan")));

      Region spacer = new Region();
      VBox.setVgrow(spacer, Priority.ALWAYS);

      VBox footerMenu = new VBox(6);
      footerMenu.getChildren().addAll(
            createMenuButton("Mode Kiosk", "🖥", false, () -> openFxSection("Mode Kiosk")),
            createMenuButton("Keluar", "⎋", false, Platform::exit));

      sidebar.getChildren().addAll(brandBox, roleBadge, menuContainer, spacer, footerMenu);
      return sidebar;
   }

   private Button createMenuButton(String text, String iconText, boolean active, Runnable onAction) {
      Button button = new Button(text);
      button.getStyleClass().add("menu-item");
      if (active) {
         button.getStyleClass().add("active");
      }
      HBox graphic = new HBox(10);
      graphic.setAlignment(Pos.CENTER_LEFT);
      Label icon = new Label(iconText);
      icon.getStyleClass().add("menu-icon");
      Label caption = new Label(text);
      caption.getStyleClass().add("menu-caption");
      graphic.getChildren().addAll(icon, caption);
      button.setGraphic(graphic);
      button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      button.setOnAction(event -> onAction.run());
      button.setMaxWidth(Double.MAX_VALUE);
      menuButtons.put(text, button);
      return button;
   }

   private Node createContent(
         DashboardSummary summary,
         LinkedHashMap<String, Integer> visitsPerMonth,
         LinkedHashMap<String, int[]> loanTrend,
         List<String[]> recentLoans,
         List<String[]> todayVisits,
         String[] adminIdentity) {
      VBox content = new VBox(18);
      content.getStyleClass().add("content-body");
      content.setPadding(new Insets(16, 24, 24, 24));

      contentSwitcher = new StackPane();
      contentSwitcher.getStyleClass().add("content-switcher");
      VBox.setVgrow(contentSwitcher, Priority.ALWAYS);
      content.getChildren().add(contentSwitcher);

      showDashboardSection(summary, visitsPerMonth, loanTrend, recentLoans, todayVisits);

      ScrollPane scrollPane = new ScrollPane(content);
      scrollPane.getStyleClass().add("content-scroll");
      scrollPane.setFitToWidth(true);
      scrollPane.setPannable(true);

      BorderPane wrapper = new BorderPane();
      wrapper.getStyleClass().add("content-wrapper");
      wrapper.setTop(createTopHeader(adminIdentity));
      wrapper.setCenter(scrollPane);
      return wrapper;
   }

   private Node createTopHeader(String[] adminIdentity) {
      HBox topBar = new HBox();
      topBar.getStyleClass().add("topbar-fixed");
      topBar.setAlignment(Pos.CENTER_LEFT);
      topBar.setPadding(new Insets(16, 40, 16, 24));

      VBox left = new VBox(2);
      topbarTitleLabel = new Label("Dashboard");
      topbarTitleLabel.getStyleClass().add("topbar-title");
      Label date = new Label(
            capitalizeWords(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", ID_LOCALE))));
      date.getStyleClass().add("topbar-date");
      left.getChildren().addAll(topbarTitleLabel, date);

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      HBox right = new HBox(12);
      right.setAlignment(Pos.CENTER_RIGHT);
      Label notification = new Label("\uD83D\uDD14");
      notification.getStyleClass().add("topbar-icon");
      Label avatar = new Label(safeValue(adminIdentity[2]));
      avatar.getStyleClass().add("avatar");

      VBox identity = new VBox(0);
      identity.setAlignment(Pos.CENTER_LEFT);
      Label adminName = new Label(safeValue(adminIdentity[0]));
      Label adminEmail = new Label(safeValue(adminIdentity[1]));
      adminName.getStyleClass().add("admin-name");
      adminEmail.getStyleClass().add("admin-email");
      identity.getChildren().addAll(adminName, adminEmail);

      HBox userCluster = new HBox(10, avatar, identity);
      userCluster.setAlignment(Pos.CENTER_LEFT);

      right.getChildren().addAll(notification, userCluster);
      topBar.getChildren().addAll(left, spacer, right);
      return topBar;
   }

   private void showDashboardSection(DashboardSummary summary, LinkedHashMap<String, Integer> visitsPerMonth,
         LinkedHashMap<String, int[]> loanTrend, List<String[]> recentLoans, List<String[]> todayVisits) {
      setTopbarTitle("Dashboard");
      if (contentSwitcher != null) {
         VBox dashboardContent = new VBox(18);
         dashboardContent.getChildren().addAll(
               createSectionHeader(),
               createStatCards(summary),
               createChartRow(visitsPerMonth, loanTrend),
               createListRow(recentLoans, todayVisits));
         contentSwitcher.getChildren().setAll(dashboardContent);
      }
   }

   private void showReportSection() {
      setTopbarTitle("Laporan");
      if (contentSwitcher == null) {
         return;
      }
      if (reportSectionView == null) {
         reportSectionView = new ReportSectionView(dashboardService);
      }
      contentSwitcher.getChildren().setAll(reportSectionView.create());
   }

   private void setTopbarTitle(String title) {
      if (topbarTitleLabel != null) {
         topbarTitleLabel.setText(safeValue(title));
      }
   }

   private Node createSectionHeader() {
      VBox header = new VBox(4);
      Label title = new Label("Dashboard");
      title.getStyleClass().add("section-title");
      Label subtitle = new Label("Ringkasan operasional perpustakaan hari ini");
      subtitle.getStyleClass().add("section-subtitle");
      header.getChildren().addAll(title, subtitle);
      return header;
   }

   private Node createStatCards(DashboardSummary summary) {
      GridPane grid = new GridPane();
      grid.getStyleClass().add("stats-grid");
      grid.setHgap(14);
      grid.setVgap(14);

      for (int index = 0; index < 6; index++) {
         ColumnConstraints constraints = new ColumnConstraints();
         constraints.setPercentWidth(100.0 / 6.0);
         constraints.setFillWidth(true);
         constraints.setHgrow(Priority.ALWAYS);
         grid.getColumnConstraints().add(constraints);
      }

      grid.add(createStatCard("Total Buku", summary.getTotalBooks(), "Total judul katalog", "📘", "icon-blue"), 0, 0);
      grid.add(createStatCard("Total Anggota", summary.getTotalMembers(), "Anggota terdaftar", "👥", "icon-green"), 1,
            0);
      grid.add(createStatCard("Peminjaman Aktif", summary.getActiveLoans(), "Sedang dipinjam", "⇄", "icon-orange"), 2,
            0);
      grid.add(createStatCard("Kunjungan Hari Ini", summary.getVisitsToday(), "Tanggal " + LocalDate.now(), "🧍",
            "icon-purple"), 3, 0);
      grid.add(createStatCard("Buku Tersedia", summary.getAvailableCopies(),
            "Dari " + summary.getTotalCopies() + " eksemplar", "✅", "icon-teal"), 4, 0);
      grid.add(createStatCard("Permintaan Pending", summary.getPendingRequests(), "Perlu ditinjau", "📥", "icon-sand"),
            5, 0);
      return grid;
   }

   private Node createStatCard(String title, int value, String helperText, String iconText, String iconVariant) {
      VBox card = new VBox(8);
      card.getStyleClass().add("stat-card");
      card.setPadding(new Insets(14));
      card.setMinHeight(140);
      card.setPrefHeight(140);
      card.setMaxWidth(Double.MAX_VALUE);
      GridPane.setFillWidth(card, true);
      GridPane.setHgrow(card, Priority.ALWAYS);

      Label icon = new Label(iconText);
      icon.getStyleClass().addAll("stat-icon", iconVariant);

      Label titleLabel = new Label(title);
      titleLabel.getStyleClass().add("stat-title");

      Label valueLabel = new Label(formatNumber(value));
      valueLabel.getStyleClass().add("stat-value");

      Label helperLabel = new Label(helperText);
      helperLabel.getStyleClass().add("stat-helper");

      card.getChildren().addAll(icon, titleLabel, valueLabel, helperLabel);
      return card;
   }

   private Node createChartRow(Map<String, Integer> visitsPerMonth, Map<String, int[]> loanTrend) {
      HBox row = new HBox(16);
      row.getChildren().addAll(
            createChartCard("Kunjungan Bulanan", buildVisitsChart(visitsPerMonth)),
            createChartCard("Tren Peminjaman & Pengembalian", buildLoanTrendChart(loanTrend)));
      row.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
      return row;
   }

   private Node createChartCard(String title, Node chart) {
      VBox card = new VBox(14);
      card.getStyleClass().add("chart-card");
      card.setPadding(new Insets(16));

      HBox header = new HBox();
      header.setAlignment(Pos.CENTER_LEFT);
      Label titleLabel = new Label(title);
      titleLabel.getStyleClass().add("card-title");
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      if ("Tren Peminjaman & Pengembalian".equals(title)) {
         header.getChildren().addAll(titleLabel, spacer, createChartLegend());
      } else {
         header.getChildren().addAll(titleLabel, spacer);
      }

      StackPane chartWrapper = new StackPane();
      Pane labelLayer = new Pane();
      labelLayer.setMouseTransparent(true);
      chartWrapper.getChildren().addAll(chart, labelLayer);
      chartWrapper.getStyleClass().add("chart-wrapper");
      chartWrapper.setMinHeight(260);
      chartWrapper.setPrefHeight(260);

      card.getChildren().addAll(header, chartWrapper);
      VBox.setVgrow(chart, Priority.ALWAYS);
      if (chart instanceof BarChart<?, ?> barChart) {
         Platform.runLater(() -> installBarValueLabels(barChart, labelLayer));
      }
      return card;
   }

   private Node createChartLegend() {
      HBox legend = new HBox(16);
      legend.setAlignment(Pos.CENTER_RIGHT);
      legend.getChildren().addAll(
            createLegendChip("Pinjam", "legend-blue"),
            createLegendChip("Kembali", "legend-green"));
      return legend;
   }

   private Node createLegendChip(String text, String colorClass) {
      HBox chip = new HBox(6);
      chip.setAlignment(Pos.CENTER_LEFT);
      Label dot = new Label("●");
      dot.getStyleClass().addAll("legend-dot", colorClass);
      Label label = new Label(text);
      label.getStyleClass().add("legend-text");
      chip.getChildren().addAll(dot, label);
      return chip;
   }

   private Node buildVisitsChart(Map<String, Integer> visitsPerMonth) {
      CategoryAxis xAxis = new CategoryAxis();
      NumberAxis yAxis = new NumberAxis();
      yAxis.setForceZeroInRange(true);
      yAxis.setTickLabelsVisible(false);
      yAxis.setTickMarkVisible(false);
      yAxis.setMinorTickVisible(false);
      yAxis.setLabel(null);

      BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
      chart.getStyleClass().add("dashboard-chart");
      chart.setLegendVisible(false);
      chart.setAnimated(false);
      chart.setCategoryGap(12);
      chart.setBarGap(4);
      chart.setPrefHeight(260);
      chart.setPadding(new Insets(0, 0, 0, 0));

      XYChart.Series<String, Number> series = new XYChart.Series<>();
      for (Map.Entry<String, Integer> entry : visitsPerMonth.entrySet()) {
         series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
      }
      chart.getData().add(series);
      return chart;
   }

   private Node buildLoanTrendChart(Map<String, int[]> loanTrend) {
      CategoryAxis xAxis = new CategoryAxis();
      NumberAxis yAxis = new NumberAxis();
      yAxis.setForceZeroInRange(true);
      yAxis.setTickLabelsVisible(false);
      yAxis.setTickMarkVisible(false);
      yAxis.setMinorTickVisible(false);
      yAxis.setLabel(null);

      BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
      chart.getStyleClass().add("dashboard-chart");
      chart.setLegendVisible(false);
      chart.setAnimated(false);
      chart.setCategoryGap(12);
      chart.setBarGap(4);
      chart.setPrefHeight(260);
      chart.setPadding(new Insets(0, 0, 0, 0));

      XYChart.Series<String, Number> loanSeries = new XYChart.Series<>();
      loanSeries.setName("Pinjam");
      XYChart.Series<String, Number> returnSeries = new XYChart.Series<>();
      returnSeries.setName("Kembali");

      for (Map.Entry<String, int[]> entry : loanTrend.entrySet()) {
         int[] values = entry.getValue();
         loanSeries.getData().add(new XYChart.Data<>(entry.getKey(), values[0]));
         returnSeries.getData().add(new XYChart.Data<>(entry.getKey(), values[1]));
      }

      chart.getData().addAll(loanSeries, returnSeries);
      return chart;
   }

   private void installBarValueLabels(BarChart<?, ?> chart, Pane overlay) {
      overlay.getChildren().clear();

      for (XYChart.Series<?, ?> series : chart.getData()) {
         for (XYChart.Data<?, ?> data : series.getData()) {
            if (!isPositiveNumber(data.getYValue())) {
               continue;
            }

            data.nodeProperty().addListener((observable, oldNode, barNode) -> {
               if (barNode != null) {
                  Label valueLabel = new Label(String.valueOf(data.getYValue()));
                  valueLabel.getStyleClass().add("bar-value-label");
                  overlay.getChildren().add(valueLabel);
                  barNode.boundsInParentProperty().addListener(
                        (boundsObservable, oldBounds, newBounds) -> positionBarValueLabel(barNode, valueLabel,
                              overlay));
                  positionBarValueLabel(barNode, valueLabel, overlay);
               }
            });

            if (data.getNode() != null) {
               Label valueLabel = new Label(String.valueOf(data.getYValue()));
               valueLabel.getStyleClass().add("bar-value-label");
               overlay.getChildren().add(valueLabel);
               data.getNode().boundsInParentProperty().addListener(
                     (boundsObservable, oldBounds, newBounds) -> positionBarValueLabel(data.getNode(), valueLabel,
                           overlay));
               positionBarValueLabel(data.getNode(), valueLabel, overlay);
            }
         }
      }
   }

   private boolean isPositiveNumber(Object value) {
      if (!(value instanceof Number number)) {
         return false;
      }
      return number.doubleValue() > 0;
   }

   private void positionBarValueLabel(Node barNode, Label valueLabel, Pane overlay) {
      if (barNode == null || valueLabel == null || overlay == null || overlay.getScene() == null) {
         return;
      }

      Bounds sceneBounds = barNode.localToScene(barNode.getBoundsInLocal());
      Point2D topLeft = overlay.sceneToLocal(sceneBounds.getMinX(), sceneBounds.getMinY());
      valueLabel.applyCss();
      valueLabel.autosize();
      double x = topLeft.getX() + (sceneBounds.getWidth() - valueLabel.getWidth()) / 2.0;
      double y = topLeft.getY() - valueLabel.getHeight() - 4;
      valueLabel.relocate(x, Math.max(0, y));
   }

   private Node createListRow(List<String[]> recentLoans, List<String[]> todayVisits) {
      HBox row = new HBox(16);
      row.getChildren().addAll(
            createRecentLoanCard(recentLoans),
            createTodayVisitCard(todayVisits));
      row.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
      return row;
   }

   private Node createRecentLoanCard(List<String[]> rows) {
      VBox card = createListCard("Peminjaman Terkini", "Lihat semua",
            () -> openFxSection("Peminjaman & Pengembalian"));
      VBox items = new VBox(2);
      if (rows.isEmpty()) {
         items.getChildren().add(createEmptyItem("Belum ada data peminjaman."));
      } else {
         for (String[] row : rows) {
            LoanBadge loanBadge = resolveLoanBadge(row[4], row[3]);
            Label badge = createStatusBadge(loanBadge.label(), loanBadge.variant());
            String iconClass = switch (loanBadge.variant()) {
               case "success" -> "item-icon-loan-success";
               case "warning" -> "item-icon-loan-warning";
               default -> "item-icon-loan-active";
            };
            String iconText = switch (loanBadge.variant()) {
               case "success" -> "✓";
               case "warning" -> "!";
               default -> "↗";
            };
            String dueDate = formatDate(row[3]);
            Node item = createListItem(
                  iconText,
                  iconClass,
                  row[0],
                  row[1] + " - " + row[2],
                  "Jatuh tempo: " + dueDate,
                  badge);
            items.getChildren().add(item);
         }
      }
      card.getChildren().add(items);
      return card;
   }

   private Node createTodayVisitCard(List<String[]> rows) {
      VBox card = createListCard("Kunjungan Hari Ini", "Lihat semua",
            () -> openFxSection("Kunjungan Hari Ini"));
      VBox items = new VBox(2);
      if (rows.isEmpty()) {
         items.getChildren().add(createEmptyItem("Belum ada kunjungan hari ini."));
      } else {
         for (String[] row : rows) {
            String status = "MEMBER".equalsIgnoreCase(row[2]) ? "Di dalam" : "Selesai";
            Label badge = createStatusBadge(status, "Di dalam".equals(status) ? "success" : "muted");
            String iconClass = "MEMBER".equalsIgnoreCase(row[2]) ? "item-icon-member" : "item-icon-guest";
            Node item = createListItem(
                  "MEMBER".equalsIgnoreCase(row[2]) ? "🎓" : "👤",
                  iconClass,
                  row[0],
                  "ID: " + row[1],
                  "Jam: " + safeValue(row[3]),
                  badge);
            items.getChildren().add(item);
         }
      }
      card.getChildren().add(items);
      return card;
   }

   private VBox createListCard(String title, String actionText, Runnable actionHandler) {
      VBox card = new VBox(10);
      card.getStyleClass().add("list-card");
      card.setPadding(new Insets(16));

      HBox header = new HBox();
      header.setAlignment(Pos.CENTER_LEFT);
      Label titleLabel = new Label(title);
      titleLabel.getStyleClass().add("card-title");
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      Button action = new Button(actionText);
      action.getStyleClass().add("card-action-button");
      action.setOnAction(event -> actionHandler.run());
      header.getChildren().addAll(titleLabel, spacer, action);

      Separator separator = new Separator();
      separator.getStyleClass().add("card-separator");
      card.getChildren().addAll(header, separator);
      return card;
   }

   private Node createListItem(String iconText, String iconClass, String title, String subtitle, String meta,
         Label badge) {
      HBox row = new HBox(12);
      row.getStyleClass().add("list-item");
      row.setAlignment(Pos.CENTER_LEFT);
      row.setPadding(new Insets(8, 0, 8, 0));

      Label icon = new Label(iconText);
      icon.getStyleClass().addAll("list-item-icon", iconClass);

      VBox left = new VBox(2);
      Label titleLabel = new Label(safeValue(title));
      titleLabel.getStyleClass().add("list-item-title");
      Label subtitleLabel = new Label(safeValue(subtitle));
      subtitleLabel.getStyleClass().add("list-item-subtitle");
      left.getChildren().addAll(titleLabel, subtitleLabel);

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      VBox right = new VBox(2);
      right.setAlignment(Pos.CENTER_RIGHT);
      Label metaLabel = new Label(safeValue(meta));
      metaLabel.getStyleClass().add("list-item-meta");
      right.getChildren().addAll(badge, metaLabel);

      row.getChildren().addAll(icon, left, spacer, right);
      return row;
   }

   private Node createEmptyItem(String message) {
      Label label = new Label(message);
      label.getStyleClass().add("empty-list");
      VBox wrapper = new VBox(label);
      wrapper.setPadding(new Insets(10, 0, 8, 0));
      return wrapper;
   }

   private Label createStatusBadge(String value, String variant) {
      Label badge = new Label(value);
      badge.getStyleClass().addAll("status-badge", "status-" + variant);
      return badge;
   }

   private LoanBadge resolveLoanBadge(String rawStatus, String dueDateValue) {
      if (rawStatus == null) {
         return new LoanBadge("-", "muted");
      }

      String normalizedStatus = rawStatus.toUpperCase(Locale.ROOT);
      if (isReturnedStatus(normalizedStatus)) {
         return new LoanBadge("Selesai", "success");
      }

      if (isOverdueStatus(normalizedStatus) || isOverdue(dueDateValue)) {
         return new LoanBadge("Terlambat", "warning");
      }

      if (isActiveStatus(normalizedStatus)) {
         return new LoanBadge("Aktif", "active");
      }

      return new LoanBadge("Aktif", "active");
   }

   private boolean isActiveStatus(String normalizedStatus) {
      return "ACTIVE".equals(normalizedStatus) || "BORROWED".equals(normalizedStatus);
   }

   private boolean isOverdueStatus(String normalizedStatus) {
      return "OVERDUE".equals(normalizedStatus) || "LATE".equals(normalizedStatus);
   }

   private boolean isReturnedStatus(String normalizedStatus) {
      return "RETURNED".equals(normalizedStatus) || "SELESAI".equals(normalizedStatus);
   }

   private boolean isOverdue(String dueDateValue) {
      try {
         return LocalDate.parse(dueDateValue).isBefore(LocalDate.now());
      } catch (Exception exception) {
         return false;
      }
   }

   private String formatDate(String value) {
      try {
         return LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd MMM yyyy", ID_LOCALE));
      } catch (Exception ignored) {
         return safeValue(value);
      }
   }

   private String capitalizeWords(String value) {
      if (value == null || value.isBlank()) {
         return "-";
      }
      String[] parts = value.split(" ");
      StringBuilder builder = new StringBuilder();
      for (String part : parts) {
         if (part.isBlank()) {
            continue;
         }
         String normalized = part.toLowerCase(ID_LOCALE);
         String capitalized = normalized.substring(0, 1).toUpperCase(ID_LOCALE) + normalized.substring(1);
         if (!builder.isEmpty()) {
            builder.append(' ');
         }
         builder.append(capitalized);
      }
      return builder.toString();
   }

   private String formatNumber(int value) {
      return String.format(ID_LOCALE, "%,d", value);
   }

   private String safeValue(String value) {
      return value == null || value.isBlank() ? "-" : value;
   }

   private String[] loadAdminIdentity() {
      List<User> users = userDAO.findAll();
      User admin = users.stream()
            .filter(user -> user.getRole() == Role.ADMIN)
            .findFirst()
            .orElse(users.isEmpty() ? null : users.get(0));

      if (admin == null || admin.getUsername() == null || admin.getUsername().isBlank()) {
         return new String[] { "Admin Perpustakaan", "admin@perpus.ac.id", "A" };
      }

      String username = admin.getUsername().trim();
      String displayName = "Admin " + capitalizeWords(username.replace('.', ' ').replace('_', ' '));
      String email = username.toLowerCase(ID_LOCALE) + "@perpus.ac.id";
      String avatarText = username.substring(0, 1).toUpperCase(ID_LOCALE);
      return new String[] { displayName, email, avatarText };
   }

   private void openFxSection(String menuName) {
      setActiveMenu(menuName);
      if ("Dashboard".equals(menuName)) {
         showDashboardSection(
               safeLoad(dashboardService::getSummary, new DashboardSummary()),
               safeLoad(() -> dashboardService.getMonthlyVisits(MONTH_RANGE), new LinkedHashMap<>()),
               safeLoad(() -> dashboardService.getMonthlyLoanReturnTrend(MONTH_RANGE), new LinkedHashMap<>()),
               safeLoad(() -> dashboardService.getRecentLoans(6), Collections.emptyList()),
               safeLoad(() -> dashboardService.getTodayVisits(6), Collections.emptyList()));
         return;
      }
      if ("Laporan".equals(menuName)) {
         showReportSection();
         return;
      }
      if ("Mode Kiosk".equals(menuName)) {
         new KioskFrame().setVisible(true);
         return;
      }
      showInfo("Menu " + menuName + " sedang dimigrasi ke JavaFX.");
   }

   private void setActiveMenu(String menuName) {
      for (Map.Entry<String, Button> entry : menuButtons.entrySet()) {
         ObservableList<String> classes = entry.getValue().getStyleClass();
         classes.remove("active");
         if (entry.getKey().equals(menuName)) {
            classes.add("active");
         }
      }
   }

   private void showInfo(String message) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
   }

   private <T> T safeLoad(Supplier<T> supplier, T fallback) {
      try {
         return supplier.get();
      } catch (RuntimeException exception) {
         return fallback;
      }
   }

   private record LoanBadge(String label, String variant) {
   }
}
