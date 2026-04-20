package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.Visit;
import com.library.app.model.enums.VisitPresenceStatus;
import com.library.app.model.enums.VisitType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisitDAO {
    public boolean existsMemberVisitToday(long memberId) {
        String sql = "SELECT COUNT(*) FROM visits WHERE member_id = ? AND visit_date = CURDATE() AND visit_type = 'MEMBER'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memeriksa kunjungan hari ini.", exception);
        }
    }

    public void save(Visit visit) {
        String sql = "INSERT INTO visits(member_id, visitor_name, visitor_identifier, visit_type, visit_status, institution, purpose, visit_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (visit.getMemberId() == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, visit.getMemberId());
            }
            statement.setString(2, visit.getVisitorName());
            statement.setString(3, visit.getVisitorIdentifier());
            statement.setString(4, visit.getVisitType().name());
            VisitPresenceStatus visitStatus = visit.getVisitStatus() == null
                    ? VisitPresenceStatus.SELESAI
                    : visit.getVisitStatus();
            statement.setString(5, visitStatus.name());
            statement.setString(6, visit.getInstitution());
            statement.setString(7, visit.getPurpose());
            statement.setDate(8, Date.valueOf(visit.getVisitDate()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan kunjungan.", exception);
        }
    }

    public Optional<Visit> findLatestMemberVisitToday(long memberId) {
        String sql = "SELECT id, member_id, visitor_name, visitor_identifier, visit_type, visit_status, institution, purpose, visit_date " +
                "FROM visits " +
                "WHERE member_id = ? AND visit_date = CURDATE() AND visit_type = 'MEMBER' " +
                "ORDER BY created_at DESC, id DESC LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(resultSet));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil status kunjungan anggota hari ini.", exception);
        }
    }

    public void updateStatus(long visitId, VisitPresenceStatus status) {
        String sql = "UPDATE visits SET visit_status = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, visitId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memperbarui status kunjungan.", exception);
        }
    }

    public List<Visit> findRecent(int limit) {
        List<Visit> visits = new ArrayList<>();
        String sql = "SELECT id, member_id, visitor_name, visitor_identifier, visit_type, visit_status, institution, purpose, visit_date " +
                "FROM visits ORDER BY visit_date DESC, id DESC LIMIT ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    visits.add(map(resultSet));
                }
            }
            return visits;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil data kunjungan.", exception);
        }
    }

    public int countToday() {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM visits WHERE visit_date = CURDATE()");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung kunjungan hari ini.", exception);
        }
    }

    private Visit map(ResultSet resultSet) throws SQLException {
        return new Visit(
                resultSet.getLong("id"),
                (Long) resultSet.getObject("member_id"),
                resultSet.getString("visitor_name"),
                resultSet.getString("visitor_identifier"),
                VisitType.valueOf(resultSet.getString("visit_type")),
                parseVisitStatus(resultSet.getString("visit_status")),
                resultSet.getString("institution"),
                resultSet.getString("purpose"),
                resultSet.getDate("visit_date").toLocalDate()
        );
    }

    private VisitPresenceStatus parseVisitStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return VisitPresenceStatus.SELESAI;
        }
        try {
            return VisitPresenceStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException exception) {
            return VisitPresenceStatus.SELESAI;
        }
    }
}