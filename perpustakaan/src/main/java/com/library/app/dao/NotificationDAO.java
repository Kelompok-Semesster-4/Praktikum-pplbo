package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.AppNotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationDAO {
   public long upsert(AppNotification notification) {
      String sql = "INSERT INTO notifications(notification_key, type, title, message, target_key, priority, is_read) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE type = VALUES(type), title = VALUES(title), message = VALUES(message), " +
            "target_key = VALUES(target_key), priority = VALUES(priority)";

      try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         statement.setString(1, notification.getNotificationKey());
         statement.setString(2, notification.getType());
         statement.setString(3, notification.getTitle());
         statement.setString(4, notification.getMessage());
         statement.setString(5, notification.getTargetKey());
         statement.setString(6, notification.getPriority());
         statement.setBoolean(7, notification.isRead());
         statement.executeUpdate();

         Optional<AppNotification> saved = findByKey(notification.getNotificationKey());
         return saved.map(AppNotification::getId).orElse(0L);
      } catch (SQLException exception) {
         throw new RuntimeException("Gagal menyimpan notifikasi.", exception);
      }
   }

   public List<AppNotification> findRecent(int limit) {
      List<AppNotification> notifications = new ArrayList<>();
      String sql = "SELECT id, notification_key, type, title, message, target_key, priority, is_read, created_at, read_at "
            +
            "FROM notifications ORDER BY is_read ASC, created_at DESC, id DESC LIMIT ?";

      try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
         statement.setInt(1, limit);
         try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
               notifications.add(map(resultSet));
            }
         }
         return notifications;
      } catch (SQLException exception) {
         throw new RuntimeException("Gagal mengambil notifikasi.", exception);
      }
   }

   public int countUnread() {
      String sql = "SELECT COUNT(*) FROM notifications WHERE is_read = FALSE";
      try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {
         resultSet.next();
         return resultSet.getInt(1);
      } catch (SQLException exception) {
         throw new RuntimeException("Gagal menghitung notifikasi belum dibaca.", exception);
      }
   }

   public void markAsRead(long id) {
      String sql = "UPDATE notifications SET is_read = TRUE, read_at = COALESCE(read_at, NOW()) WHERE id = ?";
      try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
         statement.setLong(1, id);
         statement.executeUpdate();
      } catch (SQLException exception) {
         throw new RuntimeException("Gagal menandai notifikasi sebagai dibaca.", exception);
      }
   }

   public Optional<AppNotification> findByKey(String notificationKey) {
      String sql = "SELECT id, notification_key, type, title, message, target_key, priority, is_read, created_at, read_at "
            +
            "FROM notifications WHERE notification_key = ?";
      try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
         statement.setString(1, notificationKey);
         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return Optional.of(map(resultSet));
            }
         }
         return Optional.empty();
      } catch (SQLException exception) {
         throw new RuntimeException("Gagal mengambil notifikasi.", exception);
      }
   }

   private AppNotification map(ResultSet resultSet) throws SQLException {
      AppNotification notification = new AppNotification();
      notification.setId(resultSet.getLong("id"));
      notification.setNotificationKey(resultSet.getString("notification_key"));
      notification.setType(resultSet.getString("type"));
      notification.setTitle(resultSet.getString("title"));
      notification.setMessage(resultSet.getString("message"));
      notification.setTargetKey(resultSet.getString("target_key"));
      notification.setPriority(resultSet.getString("priority"));
      notification.setRead(resultSet.getBoolean("is_read"));

      Timestamp createdAt = resultSet.getTimestamp("created_at");
      if (createdAt != null) {
         notification.setCreatedAt(createdAt.toLocalDateTime());
      }

      Timestamp readAt = resultSet.getTimestamp("read_at");
      if (readAt != null) {
         notification.setReadAt(readAt.toLocalDateTime());
      }

      return notification;
   }
}