package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.Feedback;
import com.library.app.model.enums.FeedbackStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    public void save(Feedback feedback) {
        String sql = """
                INSERT INTO feedbacks(member_id, sender_name, subject, rating, message, status, response_note, responded_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, feedback.getMemberId());
            statement.setString(2, feedback.getSenderName());
            statement.setString(3, feedback.getSubject());
            statement.setInt(4, feedback.getRating() == null ? 0 : feedback.getRating());
            statement.setString(5, feedback.getMessage());
            statement.setString(6, safeStatus(feedback.getStatus()));
            statement.setString(7, feedback.getResponseNote());
            setTimestamp(statement, 8, feedback.getRespondedAt());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    feedback.setId(keys.getLong(1));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan feedback.", exception);
        }
    }

    public List<Feedback> findAll() {
        List<Feedback> feedbacks = new ArrayList<>();
        String sql = """
                SELECT id, member_id, sender_name, subject, rating, message, status, response_note, created_at, responded_at
                FROM feedbacks
                ORDER BY created_at DESC
                """;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                feedbacks.add(map(resultSet));
            }
            return feedbacks;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memuat feedback.", exception);
        }
    }

    public void markAsRead(Long feedbackId) {
        String sql = "UPDATE feedbacks SET status = ? WHERE id = ? AND status = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, FeedbackStatus.READ.name());
            statement.setLong(2, feedbackId);
            statement.setString(3, FeedbackStatus.NEW.name());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memperbarui status feedback.", exception);
        }
    }

    public void respond(Long feedbackId, String responseNote) {
        String sql = "UPDATE feedbacks SET status = ?, response_note = ?, responded_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, FeedbackStatus.RESPONDED.name());
            statement.setString(2, responseNote);
            statement.setLong(3, feedbackId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan respons feedback.", exception);
        }
    }

    public int countAll() {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM feedbacks");
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung feedback.", exception);
        }
    }

    private Feedback map(ResultSet resultSet) throws SQLException {
        return new Feedback(
                resultSet.getLong("id"),
                (Long) resultSet.getObject("member_id"),
                resultSet.getString("sender_name"),
                resultSet.getString("subject"),
                resultSet.getInt("rating"),
                resultSet.getString("message"),
                parseStatus(resultSet.getString("status")),
                resultSet.getString("response_note"),
                toLocalDateTime(resultSet.getTimestamp("created_at")),
                toLocalDateTime(resultSet.getTimestamp("responded_at")));
    }

    private FeedbackStatus parseStatus(String rawValue) {
        return FeedbackStatus.fromDbValue(rawValue);
    }

    private String safeStatus(FeedbackStatus status) {
        return status == null ? FeedbackStatus.NEW.name() : status.name();
    }

    private void setTimestamp(PreparedStatement statement, int parameterIndex, java.time.LocalDateTime value)
            throws SQLException {
        if (value == null) {
            statement.setTimestamp(parameterIndex, null);
            return;
        }
        statement.setTimestamp(parameterIndex, Timestamp.valueOf(value));
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}