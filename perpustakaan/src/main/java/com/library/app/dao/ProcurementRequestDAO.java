package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.ProcurementRequest;
import com.library.app.model.enums.RequestStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProcurementRequestDAO {
    public void save(ProcurementRequest request) {
        String sql = "INSERT INTO procurement_requests(member_id, requester_name, title, author, note, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (request.getMemberId() == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, request.getMemberId());
            }
            statement.setString(2, request.getRequesterName());
            statement.setString(3, request.getTitle());
            statement.setString(4, request.getAuthor());
            statement.setString(5, request.getNote());
            statement.setString(6, request.getStatus().name());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan request pengadaan.", exception);
        }
    }

    public List<ProcurementRequest> findAll() {
        List<ProcurementRequest> requests = new ArrayList<>();
        String sql = "SELECT id, member_id, requester_name, title, author, note, status, response_note, created_at, responded_at " +
                "FROM procurement_requests ORDER BY id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                requests.add(map(resultSet));
            }
            return requests;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil request pengadaan.", exception);
        }
    }

    public void review(long requestId, RequestStatus status, String responseNote) {
        String sql = "UPDATE procurement_requests SET status = ?, response_note = ?, responded_at = NOW() WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setString(2, responseNote);
            statement.setLong(3, requestId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal merespons request pengadaan.", exception);
        }
    }

    public int countPending() {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM procurement_requests WHERE status = 'PENDING'");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung request pending.", exception);
        }
    }

    private ProcurementRequest map(ResultSet resultSet) throws SQLException {
        ProcurementRequest request = new ProcurementRequest();
        request.setId(resultSet.getLong("id"));
        request.setMemberId((Long) resultSet.getObject("member_id"));
        request.setRequesterName(resultSet.getString("requester_name"));
        request.setTitle(resultSet.getString("title"));
        request.setAuthor(resultSet.getString("author"));
        request.setNote(resultSet.getString("note"));
        request.setStatus(RequestStatus.valueOf(resultSet.getString("status")));
        request.setResponseNote(resultSet.getString("response_note"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            request.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp respondedAt = resultSet.getTimestamp("responded_at");
        if (respondedAt != null) {
            request.setRespondedAt(respondedAt.toLocalDateTime());
        }
        return request;
    }
}