package com.library.app.ui.panel;

import com.library.app.dao.UserDAO;
import com.library.app.model.AppNotification;
import com.library.app.model.DashboardSummary;
import com.library.app.model.User;
import com.library.app.model.enums.Role;
import com.library.app.service.DashboardService;
import com.library.app.service.NotificationService;
import com.library.app.ui.KioskFrame;
import com.library.app.ui.LoginFrame;
import com.library.app.ui.StageTransition;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.ComboBox;
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
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class DashboardPanel extends JPanel implements RefreshablePanel {
    private final DashboardService dashboardService = new DashboardService();

    private final JLabel totalBooksLabel = new JLabel();
    private final JLabel totalCopiesLabel = new JLabel();
    private final JLabel availableCopiesLabel = new JLabel();
    private final JLabel totalMembersLabel = new JLabel();
    private final JLabel visitsTodayLabel = new JLabel();
    private final JLabel activeLoansLabel = new JLabel();
    private final JLabel pendingRequestsLabel = new JLabel();

    public DashboardPanel() {
        setLayout(new GridLayout(0, 2, 12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(createCard("Total Judul Buku", totalBooksLabel));
        add(createCard("Total Eksemplar", totalCopiesLabel));
        add(createCard("Stok Tersedia", availableCopiesLabel));
        add(createCard("Total Anggota", totalMembersLabel));
        add(createCard("Kunjungan Hari Ini", visitsTodayLabel));
        add(createCard("Pinjaman Aktif", activeLoansLabel));
        add(createCard("Request Pending", pendingRequestsLabel));
        refreshData();
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 24f));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void refreshData() {
        DashboardSummary summary = dashboardService.getSummary();
        totalBooksLabel.setText(String.valueOf(summary.getTotalBooks()));
        totalCopiesLabel.setText(String.valueOf(summary.getTotalCopies()));
        availableCopiesLabel.setText(String.valueOf(summary.getAvailableCopies()));
        totalMembersLabel.setText(String.valueOf(summary.getTotalMembers()));
        visitsTodayLabel.setText(String.valueOf(summary.getVisitsToday()));
        activeLoansLabel.setText(String.valueOf(summary.getActiveLoans()));
        pendingRequestsLabel.setText(String.valueOf(summary.getPendingRequests()));
    }
}

class AdminDashboardFxApp extends Application {
    private static final int MONTH_RANGE = 7;
    private static final int NOTIFICATION_LIMIT = 80;
    private static final double DASHBOARD_LIST_VIEWPORT_HEIGHT = 300;
    private static final Duration DASHBOARD_LIST_SCROLLBAR_HIDE_DELAY = Duration.millis(850);
    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
    private static final List<String> NOTIFICATION_FILTER_OPTIONS = List.of(
            "Semua",
            "Keterlambatan",
            "Stok Buku",
            "Pengadaan",
            "Feedback",
            "Lainnya");

    private final com.library.app.service.DashboardService dashboardService = new com.library.app.service.DashboardService();
    private final NotificationService notificationService = new NotificationService();
    private final UserDAO userDAO = new UserDAO();
    private final Map<String, Button> menuButtons = new LinkedHashMap<>();

    private Label topbarTitleLabel;
    private Button notificationButton;
    private Label notificationBadgeLabel;
    private Popup notificationPopup;
    private String activeNotificationFilter = "Semua";

    private StackPane contentSwitcher;
    private BookManagementPanel bookManagementSectionView;
    private MemberManagementPanel memberManagementSectionView;
    private LoanManagementPanel loanManagementSectionView;
    private ReportPanel reportSectionView;
    private AdminFeedbackRequestPanel feedbackRequestSectionView;

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
        String[] adminIdentity = safeLoad(
                this::loadAdminIdentity,
                new String[] { "Admin Perpustakaan", "admin@perpus.ac.id", "A" });

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setLeft(createSidebar());
        root.setCenter(createContent(summary, visitsPerMonth, loanTrend, recentLoans, todayVisits, adminIdentity));

        StackPane sceneRoot = new StackPane(root);
        sceneRoot.getStyleClass().add("app-shell");

        Scene scene = new Scene(sceneRoot, 1366, 768);

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

        notificationService.refreshSystemNotifications();
        refreshNotificationBadge();

        boolean stageAlreadyVisible = stage.isShowing();
        if (!stageAlreadyVisible) {
            stage.initStyle(StageStyle.DECORATED);
        }

        stage.setTitle("Sistem Manajemen Perpustakaan");
        stage.setWidth(1240);
        stage.setHeight(760);
        stage.setMinWidth(980);
        stage.setMinHeight(620);
        stage.setFullScreenExitHint("");
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.centerOnScreen();
        Platform.runLater(() -> {
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
        });
        if (!stageAlreadyVisible) {
            stage.show();
        }
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

        Separator adminDivider = new Separator();
        adminDivider.getStyleClass().add("sidebar-divider");
        adminDivider.setMaxWidth(Double.MAX_VALUE);

        HBox roleBadge = new HBox(8);
        roleBadge.getStyleClass().addAll("sidebar-badge", "is-admin");
        roleBadge.setAlignment(Pos.CENTER_LEFT);
        roleBadge.setMaxWidth(Region.USE_PREF_SIZE);

        StackPane roleIconWrap = new StackPane();
        roleIconWrap.getStyleClass().add("sidebar-badge-icon-wrap");

        Node roleIcon = createSidebarSvgIcon(
                "M12 4.2a3 3 0 1 1 0 6a3 3 0 0 1 0-6zm0 7.6c-2.7 0-5.1 1.36-6.55 3.44c-.33.47-.17 1.12.34 1.38A14.1 14.1 0 0 0 12 18.1c2.2 0 4.3-.5 6.2-1.48c.5-.26.66-.9.34-1.38A7.98 7.98 0 0 0 12 11.8zm6.4-5.4l.55 1.2l1.28.18l-.94.92l.23 1.3l-1.12-.63l-1.12.63l.23-1.3l-.94-.92l1.28-.18zm0 8.2l.55 1.2l1.28.18l-.94.92l.23 1.3l-1.12-.63l-1.12.63l.23-1.3l-.94-.92l1.28-.18z",
                "sidebar-badge-icon-svg",
                12
        );
        roleIconWrap.getChildren().add(roleIcon);

        Label roleText = new Label("Administrator");
        roleText.getStyleClass().add("sidebar-badge-text");

        roleBadge.getChildren().addAll(roleIconWrap, roleText);

        VBox menuContainer = new VBox(6);
        menuContainer.getStyleClass().add("menu-container");
        menuContainer.getChildren().addAll(
                createMenuButton("Dashboard", createSidebarSvgIcon(
                        "M4 4h7v7H4z M13 4h7v5h-7z M4 13h7v7H4z M13 11h7v9h-7z",
                        "menu-icon-svg",
                        15
                ), true, () -> openFxSection("Dashboard")),
                createMenuButton("Manajemen Buku", "\uD83D\uDCDA", false, () -> openFxSection("Manajemen Buku")),
                createMenuButton("Manajemen Anggota", "\uD83D\uDC65", false, () -> openFxSection("Manajemen Anggota")),
                createMenuButton("Peminjaman & Pengembalian", "\u21C4", false,
                        () -> openFxSection("Peminjaman & Pengembalian")),
                createMenuButton("Laporan", "\uD83D\uDCCA", false, () -> openFxSection("Laporan")),
                createMenuButton("Feedback & Permintaan", "\uD83D\uDCAC", false,
                        () -> openFxSection("Feedback & Permintaan")));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox footerMenu = new VBox(6);
        footerMenu.getChildren().addAll(
                createMenuButton("Mode Kiosk", createSidebarSvgIcon(
                        "M4 6h16a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-6v2h3v1.5H7V21h3v-2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2zm0 2v9h16V8z",
                        "menu-icon-svg",
                        15
                ), false, () -> openFxSection("Mode Kiosk")),
                createMenuButton("Keluar", createSidebarSvgIcon(
                        "M10 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h4v-2H6V6h4zm3.2 3.8l1.4 1.4l-1.8 1.8H20v2h-7.2l1.8 1.8l-1.4 1.4L9 12z",
                        "menu-icon-svg",
                        15
                ), false, this::navigateToLogin));

        sidebar.getChildren().addAll(brandBox, adminDivider, roleBadge, menuContainer, spacer, footerMenu);
        return sidebar;
    }

    private Button createMenuButton(String text, String iconText, boolean active, Runnable onAction) {
        return createMenuButton(text, createSidebarTextIcon(iconText), active, onAction);
    }

    private Button createMenuButton(String text, Node iconNode, boolean active, Runnable onAction) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-item");
        if (active) {
            button.getStyleClass().add("active");
        }

        HBox graphic = new HBox(10);
        graphic.setAlignment(Pos.CENTER_LEFT);

        Label caption = new Label(text);
        caption.getStyleClass().add("menu-caption");

        graphic.getChildren().addAll(iconNode, caption);
        button.setGraphic(graphic);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setOnAction(event -> onAction.run());
        button.setMaxWidth(Double.MAX_VALUE);

        menuButtons.put(text, button);
        return button;
    }

    private Node createSidebarTextIcon(String iconText) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("menu-icon");
        return icon;
    }

    private Node createSidebarSvgIcon(String pathData, String styleClass, double size) {
        SVGPath icon = new SVGPath();
        icon.setContent(pathData);
        icon.getStyleClass().add(styleClass);

        StackPane wrapper = new StackPane(icon);
        wrapper.getStyleClass().add("menu-icon-wrap");
        wrapper.setMinSize(size + 9, size + 9);
        wrapper.setPrefSize(size + 9, size + 9);
        wrapper.setMaxSize(size + 9, size + 9);
        return wrapper;
    }

    private Node createContent(
            DashboardSummary summary,
            LinkedHashMap<String, Integer> visitsPerMonth,
            LinkedHashMap<String, int[]> loanTrend,
            List<String[]> recentLoans,
            List<String[]> todayVisits,
            String[] adminIdentity) {
        VBox content = new VBox(16);
        content.getStyleClass().add("content-body");
        content.setPadding(new Insets(16, 24, 16, 24));
        content.setFillWidth(true);

        contentSwitcher = new StackPane();
        contentSwitcher.getStyleClass().add("content-switcher");
        contentSwitcher.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        contentSwitcher.setMinHeight(0);
        VBox.setVgrow(contentSwitcher, Priority.ALWAYS);
        content.getChildren().add(contentSwitcher);

        showDashboardSection(summary, visitsPerMonth, loanTrend, recentLoans, todayVisits);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().addAll("content-scroll", "app-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
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
        topBar.setPadding(new Insets(16, 24, 16, 24));

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

        notificationButton = new Button();
        notificationButton.getStyleClass().add("topbar-notification-button");
        notificationButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        notificationButton.setPadding(Insets.EMPTY);

        Label notification = new Label("\uD83D\uDD14");
        notification.getStyleClass().add("topbar-icon");

        notificationBadgeLabel = new Label();
        notificationBadgeLabel.getStyleClass().add("notification-badge");

        StackPane notificationGraphic = new StackPane(notification, notificationBadgeLabel);
        notificationGraphic.setAlignment(Pos.CENTER);
        StackPane.setAlignment(notificationBadgeLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(notificationBadgeLabel, new Insets(-5, -4, 0, 0));

        notificationButton.setGraphic(notificationGraphic);
        notificationButton.setOnAction(event -> toggleNotificationPopup());

        Region topbarDivider = new Region();
        topbarDivider.getStyleClass().add("topbar-divider");
        topbarDivider.setPrefWidth(1);
        topbarDivider.setMinWidth(1);
        topbarDivider.setMaxWidth(1);

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

        right.getChildren().addAll(notificationButton, topbarDivider, userCluster);
        topBar.getChildren().addAll(left, spacer, right);

        return topBar;
    }

    private void refreshNotificationBadge() {
        if (notificationBadgeLabel == null) {
            return;
        }

        int unreadCount = notificationService.getUnreadCount();
        boolean visible = unreadCount > 0;
        notificationBadgeLabel.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        notificationBadgeLabel.setVisible(visible);
        notificationBadgeLabel.setManaged(visible);
    }

    private void toggleNotificationPopup() {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }
        showNotificationPopup();
    }

    private void showNotificationPopup() {
        if (notificationButton == null || notificationButton.getScene() == null) {
            return;
        }

        List<AppNotification> notifications = notificationService.getRecentNotifications(NOTIFICATION_LIMIT);
        List<AppNotification> filteredNotifications = filterNotifications(notifications, activeNotificationFilter);
        VBox popupContent = buildNotificationPopupContent(filteredNotifications, notifications.size());

        if (notificationPopup != null) {
            notificationPopup.hide();
        }

        notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);
        notificationPopup.getContent().add(popupContent);

        Bounds bounds = notificationButton.localToScreen(notificationButton.getBoundsInLocal());
        double popupWidth = 360;
        double x = bounds == null ? 0 : Math.max(12, bounds.getMaxX() - popupWidth);
        double y = bounds == null ? 0 : bounds.getMaxY() + 10;

        notificationPopup.show(notificationButton.getScene().getWindow(), x, y);
    }

    private VBox buildNotificationPopupContent(List<AppNotification> notifications, int totalCount) {
        VBox popup = new VBox(10);
        popup.setPrefWidth(388);
        popup.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 16; " +
                        "-fx-border-color: #dfe5ef; -fx-border-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(10, 20, 40, 0.18), 18, 0.18, 0, 6); " +
                        "-fx-padding: 14;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Notifikasi");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1f2c40;");

        Label count = new Label(String.valueOf(totalCount));
        count.setStyle(
                "-fx-background-color: #eef3ff; -fx-text-fill: #3668da; -fx-background-radius: 999; " +
                        "-fx-padding: 2 8 2 8; -fx-font-size: 11px; -fx-font-weight: 700;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, count, spacer);

        Button closeButton = new Button("\u2715");
        closeButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #7d8796; -fx-cursor: hand; " +
                        "-fx-font-size: 12px; -fx-padding: 0 2 0 2;");
        closeButton.setOnAction(event -> notificationPopup.hide());
        header.getChildren().add(closeButton);

        popup.getChildren().add(header);
        popup.getChildren().add(createNotificationFilterRow());

        if (notifications.isEmpty()) {
            Label empty = new Label("Belum ada notifikasi pada kategori ini.");
            empty.setStyle("-fx-text-fill: #7b8798; -fx-font-size: 12px;");
            popup.getChildren().add(empty);
            return popup;
        }

        VBox listContainer = new VBox(10);
        listContainer.setFillWidth(true);

        Map<String, List<AppNotification>> grouped = new LinkedHashMap<>();
        for (AppNotification notification : notifications) {
            String key = notificationTypeLabel(notification.getType());
            grouped.computeIfAbsent(key, ignored -> new java.util.ArrayList<>()).add(notification);
        }

        for (Map.Entry<String, List<AppNotification>> entry : grouped.entrySet()) {
            listContainer.getChildren().add(createNotificationGroupHeader(entry.getKey(), entry.getValue().size()));
            for (AppNotification notification : entry.getValue()) {
                listContainer.getChildren().add(createNotificationItem(notification));
            }
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.getStyleClass().add("notification-popup-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; " +
                        "-fx-border-color: transparent; -fx-padding: 0 2 0 0;");
        scrollPane.setPrefViewportHeight(380);
        scrollPane.setMaxHeight(380);
        scrollPane.setMinHeight(140);

        popup.getChildren().add(scrollPane);
        return popup;
    }

    private Node createNotificationGroupHeader(String groupName, int count) {
        HBox groupHeader = new HBox(8);
        groupHeader.setAlignment(Pos.CENTER_LEFT);
        groupHeader.setPadding(new Insets(2, 2, 0, 2));

        Label title = new Label(groupName);
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #4a5a72;");

        Label amount = new Label(String.valueOf(count));
        amount.setStyle(
                "-fx-background-color: #eef3ff; -fx-text-fill: #356cde; -fx-background-radius: 999; " +
                        "-fx-padding: 1 7 1 7; -fx-font-size: 10px; -fx-font-weight: 700;");

        groupHeader.getChildren().addAll(title, amount);
        return groupHeader;
    }

    private Node createNotificationItem(AppNotification notification) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.TOP_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.setMinWidth(340);
        item.setPrefWidth(340);
        item.setStyle(buildNotificationItemStyle(notification.isRead()));

        Label icon = new Label(notificationIcon(notification.getTargetKey()));
        icon.setMinSize(28, 28);
        icon.setPrefSize(28, 28);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle(
                "-fx-background-color: " + notificationIconBackground(notification.getPriority()) + "; " +
                        "-fx-background-radius: 999; -fx-text-fill: "
                        + notificationIconColor(notification.getPriority()) + "; " +
                        "-fx-font-size: 12px; -fx-font-weight: 700;");

        VBox textBox = new VBox(2);
        textBox.setMinWidth(0);
        textBox.setPrefWidth(252);
        textBox.setMaxWidth(252);

        Label title = new Label(notification.getTitle());
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #1f2c40;");

        Label message = new Label(notification.getMessage());
        message.setWrapText(true);
        message.setMinWidth(0);
        message.setPrefWidth(252);
        message.setMaxWidth(252);
        message.setStyle("-fx-font-size: 12px; -fx-text-fill: #667489;");

        Label meta = new Label(formatNotificationTime(notification.getCreatedAt()) + " • " +
                notificationTypeLabel(notification.getType()));
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #97a3b5;");

        textBox.getChildren().addAll(title, message, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label unreadDot = new Label("\u2022");
        unreadDot.setVisible(!notification.isRead());
        unreadDot.setManaged(!notification.isRead());
        unreadDot.setStyle("-fx-font-size: 16px; -fx-text-fill: #ef4444; -fx-font-weight: 700;");

        item.getChildren().addAll(icon, textBox, spacer, unreadDot);
        item.setOnMouseClicked(event -> {
            if (event.getClickCount() >= 1) {
                handleNotificationAction(notification);
            }
        });

        return item;
    }

    private Node createNotificationFilterRow() {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter");
        filterLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #5f6f86;");

        ComboBox<String> filterDropdown = new ComboBox<>();
        filterDropdown.getItems().setAll(NOTIFICATION_FILTER_OPTIONS);
        filterDropdown.setPrefWidth(170);
        filterDropdown.setVisibleRowCount(NOTIFICATION_FILTER_OPTIONS.size());
        filterDropdown.setStyle("-fx-background-color: #eef2f9; -fx-border-color: #dce4f1; " +
                "-fx-background-radius: 999; -fx-border-radius: 999; -fx-font-size: 11px; " +
                "-fx-font-weight: 700; -fx-text-fill: #5f6f86;");

        String selectedFilter = activeNotificationFilter;
        if (selectedFilter == null || !NOTIFICATION_FILTER_OPTIONS.contains(selectedFilter)) {
            selectedFilter = "Semua";
        }
        filterDropdown.setValue(selectedFilter);
        filterDropdown.setOnAction(event -> {
            String selectedValue = filterDropdown.getValue();
            activeNotificationFilter = selectedValue == null || selectedValue.isBlank() ? "Semua" : selectedValue;
            showNotificationPopup();
        });

        row.getChildren().addAll(filterLabel, filterDropdown);
        return row;
    }

    private List<AppNotification> filterNotifications(List<AppNotification> notifications, String filter) {
        if (filter == null || filter.isBlank() || "Semua".equalsIgnoreCase(filter)) {
            return notifications;
        }

        return notifications.stream()
                .filter(item -> notificationTypeLabel(item.getType()).equalsIgnoreCase(filter))
                .toList();
    }

    private void handleNotificationAction(AppNotification notification) {
        if (!notification.isRead()) {
            notificationService.markAsRead(notification.getId());
        }

        if (notificationPopup != null) {
            notificationPopup.hide();
        }

        refreshNotificationBadge();
        openNotificationTarget(notification);
    }

    private void openNotificationTarget(AppNotification notification) {
        String targetKey = notification.getTargetKey();
        String type = notification.getType();

        if ("LOW_STOCK".equalsIgnoreCase(type) || "FX_BOOK_MANAGEMENT".equals(targetKey)) {
            openFxSection("Manajemen Buku");
            return;
        }

        if ("FEEDBACK".equalsIgnoreCase(type) || "SWING_FEEDBACK".equals(targetKey)) {
            openFxSection("Feedback & Permintaan");
            if (feedbackRequestSectionView != null) {
                feedbackRequestSectionView.showFeedbackTab();
            }
            return;
        }

        if ("PROCUREMENT".equalsIgnoreCase(type)
                || "FX_FEEDBACK_REQUEST".equals(targetKey)
                || "SWING_PROCUREMENT".equals(targetKey)) {
            openFxSection("Feedback & Permintaan");
            if (feedbackRequestSectionView != null) {
                feedbackRequestSectionView.showProcurementTab();
            }
            return;
        }

        if ("OVERDUE_LOAN".equalsIgnoreCase(type)
                || "SWING_RETURN".equals(targetKey)
                || "SWING_LOAN".equals(targetKey)) {
            showLoanManagementActiveSection();
            return;
        }

        showInfo(notification.getMessage());
    }

    private String buildNotificationItemStyle(boolean read) {
        String background = read ? "#fbfcfe" : "#eef4ff";
        String border = read ? "#eef2f8" : "#d9e6ff";
        return "-fx-background-color: " + background + "; -fx-background-radius: 12; " +
                "-fx-border-color: " + border + "; -fx-border-radius: 12; -fx-cursor: hand;";
    }

    private String notificationIcon(String targetKey) {
        if ("SWING_RETURN".equals(targetKey)) {
            return "\u21A9";
        }
        if ("SWING_LOAN".equals(targetKey)) {
            return "\uD83D\uDCD6";
        }
        if ("SWING_PROCUREMENT".equals(targetKey)) {
            return "\u270D";
        }
        if ("SWING_FEEDBACK".equals(targetKey)) {
            return "\uD83D\uDCAC";
        }
        if ("FX_BOOK_MANAGEMENT".equals(targetKey)) {
            return "\uD83D\uDCD5";
        }
        if ("FX_MEMBER_MANAGEMENT".equals(targetKey) || "SWING_MEMBER".equals(targetKey)) {
            return "\uD83D\uDC65";
        }
        return "\uD83D\uDD14";
    }

    private String notificationIconBackground(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return "#ffe8e8";
        }
        return "#e8f1ff";
    }

    private String notificationIconColor(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return "#e23d3d";
        }
        return "#356cde";
    }

    private String formatNotificationTime(java.time.LocalDateTime createdAt) {
        if (createdAt == null) {
            return "Baru saja";
        }
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM HH:mm", ID_LOCALE));
    }

    private String notificationTypeLabel(String type) {
        if (type == null || type.isBlank()) {
            return "Lainnya";
        }

        return switch (type.toUpperCase(ID_LOCALE)) {
            case "OVERDUE_LOAN" -> "Keterlambatan";
            case "LOW_STOCK" -> "Stok Buku";
            case "PROCUREMENT" -> "Pengadaan";
            case "FEEDBACK" -> "Feedback";
            default -> "Lainnya";
        };
    }

    private void showDashboardSection(
            DashboardSummary summary,
            LinkedHashMap<String, Integer> visitsPerMonth,
            LinkedHashMap<String, int[]> loanTrend,
            List<String[]> recentLoans,
            List<String[]> todayVisits) {
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
        refreshNotificationBadge();
    }

    private void showReportSection() {
        setTopbarTitle("Laporan");
        if (contentSwitcher == null) {
            return;
        }
        if (reportSectionView == null) {
            reportSectionView = new ReportPanel(dashboardService);
        }
        contentSwitcher.getChildren().setAll(reportSectionView.create());
    }

    private void showFeedbackRequestSection() {
        setTopbarTitle("Feedback & Permintaan");
        if (contentSwitcher == null) {
            return;
        }
        if (feedbackRequestSectionView == null) {
            feedbackRequestSectionView = new AdminFeedbackRequestPanel();
        }
        contentSwitcher.getChildren().setAll(feedbackRequestSectionView.create());
        feedbackRequestSectionView.refreshData();
    }

    private void showBookManagementSection() {
        setTopbarTitle("Manajemen Buku");
        if (contentSwitcher == null) {
            return;
        }
        if (bookManagementSectionView == null) {
            bookManagementSectionView = new BookManagementPanel();
        }
        contentSwitcher.getChildren().setAll(bookManagementSectionView.create());
        bookManagementSectionView.refreshData();
    }

    private void showMemberManagementSection() {
        setTopbarTitle("Manajemen Anggota");
        if (contentSwitcher == null) {
            return;
        }
        if (memberManagementSectionView == null) {
            memberManagementSectionView = new MemberManagementPanel();
        }
        contentSwitcher.getChildren().setAll(memberManagementSectionView.create());
        memberManagementSectionView.refreshData();
    }

    private void showLoanManagementSection() {
        setTopbarTitle("Peminjaman & Pengembalian");
        if (contentSwitcher == null) {
            return;
        }
        if (loanManagementSectionView == null) {
            loanManagementSectionView = new LoanManagementPanel();
        }
        contentSwitcher.getChildren().setAll(loanManagementSectionView.create());
        loanManagementSectionView.refreshData();
    }

    private void showLoanManagementMemberVisitSection() {
        setActiveMenu("Peminjaman & Pengembalian");
        showLoanManagementSection();
        if (loanManagementSectionView != null) {
            loanManagementSectionView.showMemberVisitTab();
        }
    }

    private void showLoanManagementActiveSection() {
        setActiveMenu("Peminjaman & Pengembalian");
        showLoanManagementSection();
        if (loanManagementSectionView != null) {
            loanManagementSectionView.showActiveLoanTab();
        }
    }

    private void showLoanManagementReturnedSection() {
        setActiveMenu("Peminjaman & Pengembalian");
        showLoanManagementSection();
        if (loanManagementSectionView != null) {
            loanManagementSectionView.showReturnedLoanTab();
        }
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
        Label subtitle = new Label("Ringkasan operasional perpustakaan");
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

        grid.add(createStatCard("Total Buku", summary.getTotalBooks(), "Total judul katalog", "\uD83D\uDCD8",
                "icon-blue"), 0, 0);
        grid.add(createStatCard("Total Anggota", summary.getTotalMembers(), "Anggota terdaftar", "\uD83D\uDC65",
                "icon-green"), 1, 0);
        grid.add(createStatCard("Peminjaman Aktif", summary.getActiveLoans(), "Sedang dipinjam", "\u21C4",
                "icon-orange"), 2, 0);
        grid.add(createStatCard("Kunjungan Hari Ini", summary.getVisitsToday(), "Tanggal " + LocalDate.now(),
                "\uD83E\uDDD1", "icon-purple"), 3, 0);
        grid.add(createStatCard("Buku Tersedia", summary.getAvailableCopies(),
                "Dari " + summary.getTotalCopies() + " eksemplar", "\u2705", "icon-teal"), 4, 0);
        grid.add(createStatCard("Permintaan Pending", summary.getPendingRequests(), "Perlu ditinjau", "\uD83D\uDCE5",
                "icon-sand"), 5, 0);

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

        Label dot = new Label("\u25CF");
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
                            (boundsObservable, oldBounds, newBounds) -> positionBarValueLabel(data.getNode(),
                                    valueLabel, overlay));
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
                this::showLoanManagementReturnedSection);

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
                    case "success" -> "\u2713";
                    case "warning" -> "!";
                    default -> "\u2197";
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

        card.getChildren().add(createScrollableListBody(items));
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
                boolean isInside = "DI_DALAM".equalsIgnoreCase(safeValue(row[3]));
                String status = isInside ? "Di dalam" : "Selesai";
                Label badge = createStatusBadge(status, isInside ? "success" : "muted");
                String iconClass = "MEMBER".equalsIgnoreCase(row[2]) ? "item-icon-member" : "item-icon-guest";

                Node item = createListItem(
                        "MEMBER".equalsIgnoreCase(row[2]) ? "\uD83C\uDF93" : "\uD83D\uDC64",
                        iconClass,
                        row[0],
                        "ID: " + row[1],
                        "Jam: " + safeValue(row[4]),
                        badge);
                items.getChildren().add(item);
            }
        }

        card.getChildren().add(createScrollableListBody(items));
        return card;
    }

    private VBox createListCard(String title, String actionText, Runnable actionHandler) {
        VBox card = new VBox(10);
        card.getStyleClass().add("list-card");
        card.setPadding(new Insets(16));
        card.setFillWidth(true);
        card.setMaxWidth(Double.MAX_VALUE);

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

    private Node createScrollableListBody(VBox items) {
        items.getStyleClass().add("dashboard-list-items");
        items.setFillWidth(true);

        ScrollPane listScrollPane = new ScrollPane(items);
        listScrollPane.getStyleClass().addAll("dashboard-list-scroll", "app-scroll");
        listScrollPane.setFitToWidth(true);
        listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        listScrollPane.setPannable(true);
        listScrollPane.setMinHeight(0);
        listScrollPane.setPrefViewportHeight(DASHBOARD_LIST_VIEWPORT_HEIGHT);
        configureAutoHideListScrollbar(listScrollPane);
        VBox.setVgrow(listScrollPane, Priority.ALWAYS);
        return listScrollPane;
    }

    private void configureAutoHideListScrollbar(ScrollPane listScrollPane) {
        PauseTransition hideTimer = new PauseTransition(DASHBOARD_LIST_SCROLLBAR_HIDE_DELAY);
        hideTimer.setOnFinished(event -> setListScrollbarVisible(listScrollPane, false));

        Runnable showScrollbar = () -> {
            setListScrollbarVisible(listScrollPane, true);
            hideTimer.playFromStart();
        };

        listScrollPane.setOnMouseEntered(event -> showScrollbar.run());
        listScrollPane.setOnMouseMoved(event -> showScrollbar.run());
        listScrollPane.setOnMousePressed(event -> showScrollbar.run());
        listScrollPane.setOnScroll(event -> showScrollbar.run());
        listScrollPane.setOnMouseExited(event -> hideTimer.playFromStart());
        listScrollPane.focusedProperty().addListener((observable, oldValue, isFocused) -> {
            if (isFocused) {
                showScrollbar.run();
            } else {
                hideTimer.playFromStart();
            }
        });
        listScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> showScrollbar.run());

        Platform.runLater(() -> setListScrollbarVisible(listScrollPane, false));
    }

    private void setListScrollbarVisible(ScrollPane listScrollPane, boolean visible) {
        double opacity = visible ? 1.0 : 0.0;
        for (Node node : listScrollPane.lookupAll(".scroll-bar:vertical")) {
            node.setOpacity(opacity);
            node.setMouseTransparent(!visible);
        }
    }

    private Node createListItem(String iconText, String iconClass, String title, String subtitle, String meta,
            Label badge) {
        HBox row = new HBox(12);
        row.getStyleClass().add("list-item");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setMaxWidth(Double.MAX_VALUE);

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
        wrapper.setMaxWidth(Double.MAX_VALUE);
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
        if ("Kunjungan Hari Ini".equals(menuName)) {
            showLoanManagementMemberVisitSection();
            return;
        }

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

        if ("Manajemen Buku".equals(menuName)) {
            showBookManagementSection();
            return;
        }

        if ("Manajemen Anggota".equals(menuName)) {
            showMemberManagementSection();
            return;
        }

        if ("Peminjaman & Pengembalian".equals(menuName)) {
            showLoanManagementSection();
            return;
        }

        if ("Laporan".equals(menuName)) {
            showReportSection();
            return;
        }
        if ("Feedback & Permintaan".equals(menuName)) {
            showFeedbackRequestSection();
            return;
        }
        if ("Mode Kiosk".equals(menuName)) {
            showKioskOnCurrentStage();
            return;
        }

        showInfo("Menu " + menuName + " belum tersedia.");
    }

    private void showKioskOnCurrentStage() {
        Stage currentStage = null;
        if (contentSwitcher != null && contentSwitcher.getScene() != null
                && contentSwitcher.getScene().getWindow() instanceof Stage stage) {
            currentStage = stage;
        }

        if (currentStage != null) {
            Stage targetStage = currentStage;
            new KioskFrame().showOn(targetStage);
            return;
        }

        new KioskFrame().setVisible(true);
    }

    private void navigateToLogin() {
        Stage currentStage = null;
        if (contentSwitcher != null && contentSwitcher.getScene() != null
                && contentSwitcher.getScene().getWindow() instanceof Stage stage) {
            currentStage = stage;
        }

        if (currentStage == null) {
            Platform.exit();
            return;
        }

        Stage targetStage = currentStage;
        StageTransition.switchScene(targetStage, () -> {
            targetStage.setFullScreen(false);
            new LoginFrame().showOn(targetStage);
        });
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
