package com.library.app.service;

import com.library.app.config.DBConnection;
import com.library.app.dao.NotificationDAO;
import com.library.app.model.AppNotification;
import com.library.app.model.Feedback;
import com.library.app.model.ProcurementRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class NotificationService {
   private static final int LOW_STOCK_THRESHOLD = 2;
   private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");

   private final NotificationDAO notificationDAO = new NotificationDAO();

   public void refreshSystemNotifications() {
      syncOverdueLoanNotifications();
      syncLowStockNotifications();
   }

   public List<AppNotification> getRecentNotifications(int limit) {
      refreshSystemNotifications();
      return notificationDAO.findRecent(limit);
   }

   public int getUnreadCount() {
      refreshSystemNotifications();
      return notificationDAO.countUnread();
   }

   public void markAsRead(long id) {
      notificationDAO.markAsRead(id);
   }

   public void createFeedbackNotification(Feedback feedback) {
      if (feedback == null || feedback.getId() == null) {
         return;
      }
      String headline = feedback.getSubject() == null || feedback.getSubject().isBlank()
            ? feedback.getMessage()
            : feedback.getSubject();
      createFeedbackNotification(
            feedback.getId(),
            feedback.getSenderName() == null ? "Pengguna Kiosk" : feedback.getSenderName(),
            headline
      );
   }

   public void createProcurementNotification(ProcurementRequest request) {
      if (request == null || request.getId() == null) {
         return;
      }
      createProcurementNotification(
            request.getId(),
            request.getRequesterName() == null ? "Pemohon" : request.getRequesterName(),
            request.getTitle()
      );
   }

   public void createFeedbackNotification(long feedbackId, String senderName, String message) {
      AppNotification notification = new AppNotification();
      notification.setNotificationKey("FEEDBACK:" + feedbackId);
      notification.setType("FEEDBACK");
      notification.setTitle("Feedback baru masuk");
      notification.setMessage(senderName + " mengirim feedback: " + preview(message));
      notification.setTargetKey("FX_FEEDBACK_REQUEST");
      notification.setPriority("NORMAL");
      notification.setRead(false);
      notificationDAO.upsert(notification);
   }

   public void createProcurementNotification(long requestId, String requesterName, String title) {
      AppNotification notification = new AppNotification();
      notification.setNotificationKey("PROCUREMENT:" + requestId);
      notification.setType("PROCUREMENT");
      notification.setTitle("Request pengadaan baru");
      notification.setMessage(requesterName + " mengajukan buku: " + preview(title));
      notification.setTargetKey("FX_FEEDBACK_REQUEST");
      notification.setPriority("NORMAL");
      notification.setRead(false);
      notificationDAO.upsert(notification);
   }

   private void syncOverdueLoanNotifications() {
      String sql = "SELECT l.id AS loan_id, m.name AS member_name, b.title AS book_title, l.due_date " +
            "FROM loans l " +
            "JOIN members m ON m.id = l.member_id " +
            "JOIN book_copies c ON c.id = l.copy_id " +
            "JOIN books b ON b.id = c.book_id " +
            "WHERE l.return_date IS NULL AND l.due_date < CURDATE() " +
            "ORDER BY l.due_date ASC, l.id ASC";

      try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {
         while (resultSet.next()) {
            long loanId = resultSet.getLong("loan_id");
            String memberName = resultSet.getString("member_name");
            String bookTitle = resultSet.getString("book_title");
            LocalDate dueDate = resultSet.getObject("due_date", LocalDate.class);

            AppNotification notification = new AppNotification();
            notification.setNotificationKey("OVERDUE_LOAN:" + loanId);
            notification.setType("OVERDUE_LOAN");
            notification.setTitle("Pengembalian terlambat");
            notification.setMessage(memberName + " belum mengembalikan " + bookTitle +
                  " sejak " + formatDate(dueDate) + ".");
            notification.setTargetKey("SWING_RETURN");
            notification.setPriority("HIGH");
            notification.setRead(false);
            notificationDAO.upsert(notification);
         }
      } catch (SQLException exception) {
         throw new RuntimeException("Gagal menyinkronkan notifikasi keterlambatan.", exception);
      }
   }

   private void syncLowStockNotifications() {
      String sql = "SELECT b.id AS book_id, b.title AS book_title, " +
            "COALESCE(SUM(CASE WHEN c.status = 'AVAILABLE' THEN 1 ELSE 0 END), 0) AS available_count " +
            "FROM books b " +
            "LEFT JOIN book_copies c ON c.book_id = b.id " +
            "GROUP BY b.id, b.title " +
            "HAVING available_count <= ? " +
            "ORDER BY available_count ASC, b.title ASC";

      try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
         statement.setInt(1, LOW_STOCK_THRESHOLD);
         try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
               long bookId = resultSet.getLong("book_id");
               String bookTitle = resultSet.getString("book_title");
               int availableCount = resultSet.getInt("available_count");

               AppNotification notification = new AppNotification();
               notification.setNotificationKey("LOW_STOCK_BOOK:" + bookId);
               notification.setType("LOW_STOCK");
               notification.setTitle("Stok buku menipis");
               notification.setMessage(bookTitle + " tersisa " + availableCount + " eksemplar tersedia.");
               notification.setTargetKey("FX_BOOK_MANAGEMENT");
               notification.setPriority("HIGH");
               notification.setRead(false);
               notificationDAO.upsert(notification);
            }
         }
      } catch (SQLException exception) {
         throw new RuntimeException("Gagal menyinkronkan notifikasi stok buku.", exception);
      }
   }

   private String preview(String text) {
      if (text == null || text.isBlank()) {
         return "-";
      }

      String normalized = text.trim().replaceAll("\\s+", " ");
      return normalized.length() <= 80 ? normalized : normalized.substring(0, 77) + "...";
   }

   private String formatDate(LocalDate date) {
      if (date == null) {
         return "-";
      }
      return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", ID_LOCALE));
   }
}