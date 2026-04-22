package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.Visit;
import com.library.app.model.enums.VisitPresenceStatus;
import com.library.app.model.enums.VisitType;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
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
        String sql = "INSERT INTO visits(member_id, visitor_name, visitor_identifier, visit_type, visit_status, institution, purpose, visit_date, check_in_time, check_out_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            if (visit.getCheckInTime() == null) {
                statement.setNull(9, Types.TIME);
            } else {
                statement.setTime(9, Time.valueOf(visit.getCheckInTime()));
            }
            if (visit.getCheckOutTime() == null) {
                statement.setNull(10, Types.TIME);
            } else {
                statement.setTime(10, Time.valueOf(visit.getCheckOutTime()));
            }
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan kunjungan.", exception);
        }
    }

    public Optional<Visit> findLatestMemberVisitToday(long memberId) {
        String sql = "SELECT id, member_id, visitor_name, visitor_identifier, visit_type, visit_status, institution, purpose, visit_date, check_in_time, check_out_time " +
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

    public Optional<Visit> findLatestMemberVisit(long memberId) {
        String sql = "SELECT id, member_id, visitor_name, visitor_identifier, visit_type, visit_status, institution, purpose, visit_date, check_in_time, check_out_time " +
                "FROM visits " +
                "WHERE member_id = ? AND visit_type = 'MEMBER' " +
                "ORDER BY visit_date DESC, created_at DESC, id DESC LIMIT 1";

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
            throw new RuntimeException("Gagal mengambil kunjungan anggota terbaru.", exception);
        }
    }

    public Optional<Visit> findLatestGuestVisitToday(String guestName, String institution) {
        String sql = "SELECT id, member_id, visitor_name, visitor_identifier, visit_type, visit_status, institution, purpose, visit_date, check_in_time, check_out_time " +
                "FROM visits " +
                "WHERE visit_type = 'GUEST' " +
                "  AND visit_date = CURDATE() " +
                "  AND LOWER(TRIM(visitor_name)) = LOWER(TRIM(?)) " +
                "  AND LOWER(TRIM(COALESCE(institution, ''))) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, id DESC LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, guestName);
            statement.setString(2, institution == null ? "" : institution);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(resultSet));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil kunjungan tamu terbaru hari ini.", exception);
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

    public void checkoutMemberVisit(long visitId, LocalTime checkOutTime) {
        String sql = "UPDATE visits SET visit_status = 'SELESAI', check_out_time = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (checkOutTime == null) {
                statement.setNull(1, Types.TIME);
            } else {
                statement.setTime(1, Time.valueOf(checkOutTime));
            }
            statement.setLong(2, visitId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mencatat jam keluar kunjungan.", exception);
        }
    }

    public void checkoutGuestVisit(long visitId, LocalTime checkOutTime) {
        String sql = "UPDATE visits SET visit_status = 'SELESAI', check_out_time = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (checkOutTime == null) {
                statement.setNull(1, Types.TIME);
            } else {
                statement.setTime(1, Time.valueOf(checkOutTime));
            }
            statement.setLong(2, visitId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mencatat jam keluar tamu.", exception);
        }
    }

    public void closeOpenMemberVisitsBefore(LocalDate date) {
        if (date == null) {
            return;
        }

        String sql = "UPDATE visits " +
                "SET visit_status = 'SELESAI', check_out_time = COALESCE(check_out_time, '23:59:00') " +
                "WHERE visit_type = 'MEMBER' " +
                "  AND visit_status = 'DI_DALAM' " +
                "  AND visit_date < ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menutup otomatis kunjungan yang belum selesai.", exception);
        }
    }

    public void closeOpenGuestVisitsBefore(LocalDate date) {
        if (date == null) {
            return;
        }

        String sql = "UPDATE visits " +
                "SET visit_status = 'SELESAI', check_out_time = COALESCE(check_out_time, '23:59:00') " +
                "WHERE visit_type = 'GUEST' " +
                "  AND visit_status = 'DI_DALAM' " +
                "  AND visit_date < ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menutup otomatis kunjungan tamu yang belum selesai.", exception);
        }
    }

    public List<Visit> findRecent(int limit) {
        List<Visit> visits = new ArrayList<>();
        String sql = "SELECT id, member_id, visitor_name, visitor_identifier, visit_type, visit_status, institution, purpose, visit_date, check_in_time, check_out_time " +
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
                resultSet.getDate("visit_date").toLocalDate(),
                toLocalTime(resultSet.getTime("check_in_time")),
                toLocalTime(resultSet.getTime("check_out_time"))
        );
    }

    private LocalTime toLocalTime(Time value) {
        return value == null ? null : value.toLocalTime();
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
