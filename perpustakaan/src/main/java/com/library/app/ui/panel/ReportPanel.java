package com.library.app.ui.panel;

import com.library.app.model.OverdueLoanReportItem;
import com.library.app.model.ReportSummary;
import com.library.app.service.DashboardService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class ReportPanel {
   private static final int MONTH_RANGE = 7;
   private static final int OVERDUE_LIMIT = 12;
   private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
   private static final double TABLE_ROW_HEIGHT = 60.0;
   private static final double TABLE_EMPTY_HEIGHT = 118.0;

   private final DashboardService dashboardService;
   private final ObservableList<OverdueLoanReportItem> overdueItems = FXCollections.observableArrayList();

   private Label totalLoansLabel;
   private Label returnedLoansLabel;
   private Label overdueLoansLabel;
   private Label totalFineLabel;
   private StackPane monthlyVisitsChartHost;
   private StackPane loanTrendChartHost;
   private TableView<OverdueLoanReportItem> overdueTable;
   private Button exportButton;

   public ReportPanel(DashboardService dashboardService) {
      this.dashboardService = dashboardService;
   }

   public Node create() {
      VBox content = new VBox(16);
      content.getStyleClass().add("report-content");
      content.setPadding(new Insets(16, 24, 24, 24));
      content.setFillWidth(true);

      HBox header = new HBox();
      header.setAlignment(Pos.CENTER_LEFT);
      header.setFillHeight(true);

      VBox titleBox = new VBox(3);
      Label title = new Label("Laporan");
      title.getStyleClass().add("section-title");
      Label subtitle = new Label("Statistik dan laporan operasional perpustakaan");
      subtitle.getStyleClass().add("section-subtitle");
      titleBox.getChildren().addAll(title, subtitle);

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      exportButton = new Button("\u2193 Ekspor Laporan");
      exportButton.getStyleClass().add("report-export-button");
      exportButton.setOnAction(event -> onExportReport());

      header.getChildren().addAll(titleBox, spacer, exportButton);

      VBox body = new VBox(16);
      body.getChildren().addAll(
            createStatCards(),
            createChartRow(),
            createTableCard());
      body.setPadding(new Insets(0, 0, 0, 0));

      content.getChildren().addAll(header, body);

      loadData();
      return content;
   }

   public void loadData() {
      ReportSummary summary = safeLoad(dashboardService::getReportSummary, new ReportSummary());
      LinkedHashMap<String, Integer> visitsPerMonth = safeLoad(
            () -> dashboardService.getMonthlyVisits(MONTH_RANGE),
            new LinkedHashMap<>());
      LinkedHashMap<String, int[]> loanTrend = safeLoad(
            () -> dashboardService.getMonthlyLoanReturnTrend(MONTH_RANGE),
            new LinkedHashMap<>());
      List<OverdueLoanReportItem> overdueRows = safeLoad(
            () -> dashboardService.getOverdueLoans(OVERDUE_LIMIT),
            List.of());

      totalLoansLabel.setText(formatNumber(summary.getTotalLoans()));
      returnedLoansLabel.setText(formatNumber(summary.getReturnedLoans()));
      overdueLoansLabel.setText(formatNumber(summary.getOverdueLoans()));
      totalFineLabel.setText(formatCurrency(summary.getTotalFineAmount()));

      BarChart<String, Number> monthlyVisitsChart = buildVisitsChart(visitsPerMonth);
      Pane visitsLabelLayer = new Pane();
      visitsLabelLayer.setMouseTransparent(true);
      monthlyVisitsChartHost.getChildren().setAll(monthlyVisitsChart, visitsLabelLayer);
      Platform.runLater(() -> installBarValueLabels(monthlyVisitsChart, visitsLabelLayer));

      BarChart<String, Number> loanTrendChart = buildLoanTrendChart(loanTrend);
      Pane loanTrendLabelLayer = new Pane();
      loanTrendLabelLayer.setMouseTransparent(true);
      loanTrendChartHost.getChildren().setAll(loanTrendChart, loanTrendLabelLayer);
      Platform.runLater(() -> installBarValueLabels(loanTrendChart, loanTrendLabelLayer));
      overdueItems.setAll(overdueRows);
      adjustTableHeight(overdueRows.size());
   }

   private Node createStatCards() {
      HBox cards = new HBox(14);
      cards.setFillHeight(true);

      cards.getChildren().addAll(
            createStatCard("\uD83D\uDCD6", "icon-blue", "Total Peminjaman"),
            createStatCard("\u2713", "icon-green", "Dikembalikan"),
            createStatCard("\u23F0", "icon-orange", "Terlambat"),
            createStatCard("\u00A4", "icon-sand", "Total Denda"));

      for (Node node : cards.getChildren()) {
         HBox.setHgrow(node, Priority.ALWAYS);
      }
      return cards;
   }

   private VBox createStatCard(String iconText, String iconClass, String helperText) {
      VBox card = new VBox(4);
      card.getStyleClass().add("stat-card");
      card.setPadding(new Insets(12, 14, 12, 14));

      Label icon = new Label(iconText);
      icon.getStyleClass().addAll("stat-icon", iconClass);
      card.setMaxWidth(Double.MAX_VALUE);

      Label value = new Label("0");
      value.getStyleClass().add("stat-value");

      if ("Total Peminjaman".equals(helperText)) {
         totalLoansLabel = value;
      } else if ("Dikembalikan".equals(helperText)) {
         returnedLoansLabel = value;
      } else if ("Terlambat".equals(helperText)) {
         overdueLoansLabel = value;
      } else {
         totalFineLabel = value;
         totalFineLabel.setText("Rp 0");
      }

      Label helper = new Label(helperText);
      helper.getStyleClass().add("stat-helper");

      card.getChildren().addAll(icon, value, helper);
      return card;
   }

   private Node createChartRow() {
      HBox row = new HBox(16);
      row.getChildren().addAll(createVisitsChartCard(), createTrendChartCard());
      for (Node node : row.getChildren()) {
         HBox.setHgrow(node, Priority.ALWAYS);
      }
      return row;
   }

   private VBox createVisitsChartCard() {
      VBox card = new VBox(10);
      card.getStyleClass().add("chart-card");
      card.setPadding(new Insets(16));

      Label title = new Label("Kunjungan Bulanan");
      title.getStyleClass().add("card-title");

      monthlyVisitsChartHost = new StackPane();
      monthlyVisitsChartHost.getStyleClass().add("report-chart-host");
      VBox.setVgrow(monthlyVisitsChartHost, Priority.ALWAYS);

      card.getChildren().addAll(title, monthlyVisitsChartHost);
      return card;
   }

   private VBox createTrendChartCard() {
      VBox card = new VBox(10);
      card.getStyleClass().add("chart-card");
      card.setPadding(new Insets(16));

      HBox header = new HBox();
      header.setAlignment(Pos.CENTER_LEFT);
      Label title = new Label("Tren Peminjaman");
      title.getStyleClass().add("card-title");
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      HBox legend = new HBox(12);
      legend.setAlignment(Pos.CENTER_RIGHT);
      legend.getChildren().addAll(
            createLegendChip("Pinjam", "legend-blue"),
            createLegendChip("Kembali", "legend-green"));

      header.getChildren().addAll(title, spacer, legend);

      loanTrendChartHost = new StackPane();
      loanTrendChartHost.getStyleClass().add("report-chart-host");
      VBox.setVgrow(loanTrendChartHost, Priority.ALWAYS);

      card.getChildren().addAll(header, loanTrendChartHost);
      return card;
   }

   private Node createLegendChip(String text, String colorClass) {
      HBox chip = new HBox(5);
      chip.setAlignment(Pos.CENTER_LEFT);
      Label dot = new Label("\u25CF");
      dot.getStyleClass().addAll("legend-dot", colorClass);
      Label label = new Label(text);
      label.getStyleClass().add("legend-text");
      chip.getChildren().addAll(dot, label);
      return chip;
   }

   private VBox createTableCard() {
      VBox card = new VBox(10);
      card.getStyleClass().add("list-card");
      card.setPadding(new Insets(16));

      Label title = new Label("Daftar Peminjaman Terlambat");
      title.getStyleClass().add("card-title");

      overdueTable = new TableView<>();
      overdueTable.getStyleClass().add("report-overdue-table");
      overdueTable.setItems(overdueItems);
      overdueTable.setFixedCellSize(TABLE_ROW_HEIGHT);
      overdueTable.setMinHeight(0);
      overdueTable.setPrefHeight(TABLE_EMPTY_HEIGHT);
      overdueTable.setMaxHeight(Region.USE_PREF_SIZE);
      overdueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

      TableColumn<OverdueLoanReportItem, OverdueLoanReportItem> borrowerColumn = new TableColumn<>("PEMINJAM");
      borrowerColumn.getStyleClass().add("report-column-borrower");
      borrowerColumn.setPrefWidth(250);
      borrowerColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
      borrowerColumn.setCellFactory(column -> new TableCell<>() {
         @Override
         protected void updateItem(OverdueLoanReportItem item, boolean empty) {
            super.updateItem(item, empty);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER_LEFT);
            setStyle("-fx-alignment: CENTER-LEFT;");
            if (empty || item == null) {
               setText(null);
               setGraphic(null);
               return;
            }

            Label name = new Label(safe(item.getBorrowerName()));
            name.getStyleClass().add("list-item-title");

            Label nim = new Label("NIM: " + safe(item.getMemberCode()));
            nim.getStyleClass().add("list-item-subtitle");

            VBox box = new VBox(2, name, nim);
            box.setAlignment(Pos.CENTER_LEFT);
            setText(null);
            setGraphic(box);
         }
      });

      TableColumn<OverdueLoanReportItem, String> bookColumn = new TableColumn<>("BUKU");
      bookColumn.setPrefWidth(320);
      bookColumn.setStyle("-fx-alignment: CENTER;");
      bookColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(cell.getValue().getBookTitle())));
      bookColumn.setCellFactory(column -> new TableCell<>() {
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
            HBox wrapper = new HBox(value);
            wrapper.setAlignment(Pos.CENTER);
            setText(null);
            setGraphic(wrapper);
         }
      });

      TableColumn<OverdueLoanReportItem, LocalDate> dueDateColumn = new TableColumn<>("JATUH TEMPO");
      dueDateColumn.setPrefWidth(146);
      dueDateColumn.setStyle("-fx-alignment: CENTER;");
      dueDateColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getDueDate()));
      dueDateColumn.setCellFactory(column -> new TableCell<>() {
         @Override
         protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);
            if (empty) {
               setText(null);
               setGraphic(null);
               return;
            }

            Label value = new Label(formatDate(item));
            StackPane wrapper = new StackPane(value);
            wrapper.setAlignment(Pos.CENTER);
            setText(null);
            setGraphic(wrapper);
         }
      });

      TableColumn<OverdueLoanReportItem, BigDecimal> fineColumn = new TableColumn<>("DENDA");
      fineColumn.setPrefWidth(136);
      fineColumn.setStyle("-fx-alignment: CENTER;");
      fineColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getFineAmount()));
      fineColumn.setCellFactory(column -> new TableCell<>() {
         @Override
         protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            if (empty) {
               setAlignment(Pos.CENTER);
               setText(null);
               setGraphic(null);
               getStyleClass().remove("report-fine-highlight");
               return;
            }
            setAlignment(Pos.CENTER);
            Label value = new Label(formatCurrency(item));
            if (!getStyleClass().contains("report-fine-highlight")) {
               getStyleClass().add("report-fine-highlight");
            }
            StackPane wrapper = new StackPane(value);
            wrapper.setAlignment(Pos.CENTER);
            setText(null);
            setGraphic(wrapper);
         }
      });

      TableColumn<OverdueLoanReportItem, String> statusColumn = new TableColumn<>("STATUS");
      statusColumn.setPrefWidth(132);
      statusColumn.setStyle("-fx-alignment: CENTER;");
      statusColumn.setCellValueFactory(cell -> new SimpleStringProperty("Terlambat"));
      statusColumn.setCellFactory(column -> new TableCell<>() {
         @Override
         protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);
            setStyle("-fx-alignment: CENTER;");
            if (empty || item == null) {
               setText(null);
               setGraphic(null);
               return;
            }

            Label badge = new Label(item);
            badge.getStyleClass().addAll("status-badge", "status-warning");
            HBox wrapper = new HBox(badge);
            wrapper.setAlignment(Pos.CENTER);
            setText(null);
            setGraphic(wrapper);
         }
      });

      overdueTable.getColumns().setAll(borrowerColumn, bookColumn, dueDateColumn, fineColumn, statusColumn);
      card.getChildren().addAll(title, overdueTable);
      return card;
   }

   private void onExportReport() {
      Window owner = exportButton == null || exportButton.getScene() == null ? null
            : exportButton.getScene().getWindow();

      FileChooser chooser = new FileChooser();
      chooser.setTitle("Ekspor Laporan");
      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
      chooser.setInitialFileName("laporan-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv");

      File target = chooser.showSaveDialog(owner);
      if (target == null) {
         return;
      }

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(target))) {
         writer.write("Total Peminjaman," + escapeCsv(totalLoansLabel.getText()));
         writer.newLine();
         writer.write("Dikembalikan," + escapeCsv(returnedLoansLabel.getText()));
         writer.newLine();
         writer.write("Terlambat," + escapeCsv(overdueLoansLabel.getText()));
         writer.newLine();
         writer.write("Total Denda," + escapeCsv(totalFineLabel.getText()));
         writer.newLine();
         writer.newLine();
         writer.write("PEMINJAM,NIM,BUKU,JATUH TEMPO,DENDA,STATUS");
         writer.newLine();

         for (OverdueLoanReportItem item : overdueItems) {
            writer.write(String.join(",",
                  escapeCsv(safe(item.getBorrowerName())),
                  escapeCsv(safe(item.getMemberCode())),
                  escapeCsv(safe(item.getBookTitle())),
                  escapeCsv(formatDate(item.getDueDate())),
                  escapeCsv(formatCurrency(item.getFineAmount())),
                  escapeCsv("Terlambat")));
            writer.newLine();
         }

         showInfo("Laporan berhasil diekspor ke: " + target.getAbsolutePath());
      } catch (IOException exception) {
         showError("Gagal mengekspor laporan: " + exception.getMessage());
      }
   }

   private BarChart<String, Number> buildVisitsChart(LinkedHashMap<String, Integer> data) {
      CategoryAxis xAxis = new CategoryAxis();
      NumberAxis yAxis = new NumberAxis();
      configureNumericAxis(yAxis);

      BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
      chart.getStyleClass().add("dashboard-chart");
      chart.setLegendVisible(false);
      chart.setAnimated(false);
      chart.setCategoryGap(12);
      chart.setBarGap(4);
      chart.setPrefHeight(250);

      XYChart.Series<String, Number> series = new XYChart.Series<>();
      data.forEach((month, total) -> series.getData().add(new XYChart.Data<>(month, total)));
      chart.getData().setAll(series);
      return chart;
   }

   private BarChart<String, Number> buildLoanTrendChart(LinkedHashMap<String, int[]> data) {
      CategoryAxis xAxis = new CategoryAxis();
      NumberAxis yAxis = new NumberAxis();
      configureNumericAxis(yAxis);

      BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
      chart.getStyleClass().add("dashboard-chart");
      chart.setLegendVisible(false);
      chart.setAnimated(false);
      chart.setCategoryGap(12);
      chart.setBarGap(4);
      chart.setPrefHeight(250);

      XYChart.Series<String, Number> loanSeries = new XYChart.Series<>();
      XYChart.Series<String, Number> returnSeries = new XYChart.Series<>();
      data.forEach((month, values) -> {
         loanSeries.getData().add(new XYChart.Data<>(month, values[0]));
         returnSeries.getData().add(new XYChart.Data<>(month, values[1]));
      });

      chart.getData().setAll(loanSeries, returnSeries);
      return chart;
   }

   private void installBarValueLabels(BarChart<String, Number> chart, Pane overlay) {
      overlay.getChildren().clear();
      for (XYChart.Series<String, Number> series : chart.getData()) {
         for (XYChart.Data<String, Number> dataPoint : series.getData()) {
            Number value = dataPoint.getYValue();
            if (value == null || value.doubleValue() <= 0) {
               continue;
            }

            Label label = new Label(formatNumber(value.intValue()));
            label.getStyleClass().add("bar-value-label");
            overlay.getChildren().add(label);

            Node barNode = dataPoint.getNode();
            if (barNode != null) {
               barNode.boundsInParentProperty()
                     .addListener((obs, oldBounds, newBounds) -> positionChartValueLabel(barNode, label, overlay));
               positionChartValueLabel(barNode, label, overlay);
            }

            dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
               if (newNode != null) {
                  newNode.boundsInParentProperty()
                        .addListener((bObs, oldBounds, newBounds) -> positionChartValueLabel(newNode, label, overlay));
                  positionChartValueLabel(newNode, label, overlay);
               }
            });
         }
      }
   }

   private void positionChartValueLabel(Node barNode, Label label, Pane overlay) {
      if (barNode == null || overlay.getScene() == null) {
         return;
      }

      Bounds sceneBounds = barNode.localToScene(barNode.getBoundsInLocal());
      Point2D topLeft = overlay.sceneToLocal(sceneBounds.getMinX(), sceneBounds.getMinY());
      label.applyCss();
      label.autosize();

      double x = topLeft.getX() + (sceneBounds.getWidth() - label.getWidth()) / 2.0;
      double y = topLeft.getY() - label.getHeight() - 4;
      label.relocate(x, Math.max(0, y));
   }

   private void configureNumericAxis(NumberAxis axis) {
      axis.setForceZeroInRange(true);
      axis.setTickLabelsVisible(false);
      axis.setTickMarkVisible(false);
      axis.setMinorTickVisible(false);
      axis.setLabel(null);
   }

   private void adjustTableHeight(int rowCount) {
      if (overdueTable == null) {
         return;
      }

      Platform.runLater(() -> {
         if (overdueTable.getScene() == null) {
            return;
         }

         overdueTable.applyCss();
         overdueTable.layout();

         if (rowCount <= 0) {
            overdueTable.setPlaceholder(new Label("Tidak ada data peminjaman terlambat."));
            overdueTable.setPrefHeight(TABLE_EMPTY_HEIGHT);
            overdueTable.setMinHeight(TABLE_EMPTY_HEIGHT);
            overdueTable.setMaxHeight(TABLE_EMPTY_HEIGHT);
            return;
         }

         overdueTable.setPlaceholder(new Label(""));
         double headerHeight = 34.0;
         Node headerNode = overdueTable.lookup(".column-header-background");
         if (headerNode != null) {
            headerHeight = headerNode.prefHeight(-1);
         }

         double calculatedHeight = headerHeight + (rowCount * TABLE_ROW_HEIGHT) + 4.0;
         double finalHeight = Math.max(calculatedHeight, TABLE_EMPTY_HEIGHT);
         overdueTable.setPrefHeight(finalHeight);
         overdueTable.setMinHeight(finalHeight);
         overdueTable.setMaxHeight(finalHeight);
      });
   }

   private String formatNumber(int value) {
      return String.format(ID_LOCALE, "%d", value);
   }

   private String formatCurrency(BigDecimal amount) {
      NumberFormat formatter = NumberFormat.getCurrencyInstance(ID_LOCALE);
      String value = formatter.format(amount == null ? BigDecimal.ZERO : amount);
      return value.replace("Rp", "Rp ");
   }

   private String formatDate(LocalDate date) {
      if (date == null) {
         return "-";
      }
      return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
   }

private String safeText(String value) {
    return value == null || value.isBlank() ? "-" : value;
}

   private String safe(String value) {
      return value == null || value.isBlank() ? "-" : value;
   }

   private String escapeCsv(String value) {
      String normalized = value == null ? "" : value;
      return "\"" + normalized.replace("\"", "\"\"") + "\"";
   }

   private <T> T safeLoad(Supplier<T> supplier, T fallback) {
      try {
         return supplier.get();
      } catch (RuntimeException exception) {
         return fallback;
      }
   }

   private void showInfo(String message) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
   }

   private void showError(String message) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
   }
}
