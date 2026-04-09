package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    public void save(Feedback feedback) {
        String sql = "INSERT INTO feedbacks(member_id, sender_name, message) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (feedback.getMemberId() == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, feedback.getMemberId());
            }
            statement.setString(2, feedback.getSenderName());
            statement.setString(3, feedback.getMessage());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan feedback.", exception);
        }
    }

    public List<Feedback> findAll() {
        List<Feedback> feedbacks = new ArrayList<>();
        String sql = "SELECT id, member_id, sender_name, message, created_at FROM feedbacks ORDER BY id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Feedback feedback = new Feedback();
                feedback.setId(resultSet.getLong("id"));
                feedback.setMemberId((Long) resultSet.getObject("member_id"));
                feedback.setSenderName(resultSet.getString("sender_name"));
                feedback.setMessage(resultSet.getString("message"));
                Timestamp timestamp = resultSet.getTimestamp("created_at");
                if (timestamp != null) {
                    feedback.setCreatedAt(timestamp.toLocalDateTime());
                }
                feedbacks.add(feedback);
            }
            return feedbacks;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil feedback.", exception);
        }
    }
}