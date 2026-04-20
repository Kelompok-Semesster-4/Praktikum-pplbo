package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.ProcurementRequest;
import com.library.app.model.enums.RequestStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProcurementRequestDAO {
    public void save(ProcurementRequest request) {
        String sql = """
                INSERT INTO procurement_requests(
                    member_id, requester_name, title, author, publisher, publication_year, isbn,
                    note, status, response_note, responded_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, request.getMemberId());
            statement.setString(2, request.getRequesterName());
            statement.setString(3, request.getTitle());
            statement.setString(4, request.getAuthor());
            statement.setString(5, request.getPublisher());
            if (request.getPublicationYear() == null) {
                statement.setNull(6, Types.INTEGER);
            } else {
                statement.setInt(6, request.getPublicationYear());
            }
            statement.setString(7, request.getIsbn());
            statement.setString(8, request.getNote());
            statement.setString(9, request.getStatus().name());
            statement.setString(10, request.getResponseNote());
            if (request.getRespondedAt() == null) {
                statement.setTimestamp(11, null);
            } else {
                statement.setTimestamp(11, Timestamp.valueOf(request.getRespondedAt()));
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    request.setId(keys.getLong(1));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan permintaan pengadaan.", exception);
        }
    }

    public List<ProcurementRequest> findAll() {
        List<ProcurementRequest> requests = new ArrayList<>();
        String sql = """
                SELECT id, member_id, requester_name, title, author, publisher, publication_year, isbn,
                       note, status, response_note, created_at, responded_at
                FROM procurement_requests
                ORDER BY created_at DESC
                """;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                requests.add(map(resultSet));
            }
            return requests;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memuat permintaan pengadaan.", exception);
        }
    }

    public void reviewRequest(Long requestId, RequestStatus status, String responseNote) {
        String sql = "UPDATE procurement_requests SET status = ?, response_note = ?, responded_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setString(2, responseNote);
            statement.setLong(3, requestId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memperbarui status permintaan.", exception);
        }
    }

    public int countByStatus(RequestStatus status) {
        String sql = "SELECT COUNT(*) FROM procurement_requests WHERE status = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung permintaan.", exception);
        }
    }

    public int countPending() {
        return countByStatus(RequestStatus.PENDING);
    }

    private ProcurementRequest map(ResultSet resultSet) throws SQLException {
        Timestamp respondedAt = resultSet.getTimestamp("responded_at");
        return new ProcurementRequest(
                resultSet.getLong("id"),
                (Long) resultSet.getObject("member_id"),
                resultSet.getString("requester_name"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getString("publisher"),
                (Integer) resultSet.getObject("publication_year"),
                resultSet.getString("isbn"),
                resultSet.getString("note"),
                RequestStatus.fromDbValue(resultSet.getString("status")),
                resultSet.getString("response_note"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                respondedAt == null ? null : respondedAt.toLocalDateTime());
    }
}