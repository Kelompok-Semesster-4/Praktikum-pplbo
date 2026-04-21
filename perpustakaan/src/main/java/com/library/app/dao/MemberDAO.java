package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.Member;
import com.library.app.model.enums.MemberType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDAO {

    public void save(Member member) {
        String sql = "INSERT INTO members(member_code, name, member_type, major, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, member.getMemberCode());
            statement.setString(2, member.getName());
            statement.setString(3, member.getMemberType().name());
            statement.setString(4, member.getMajor());
            statement.setString(5, member.getPhone());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    member.setId(keys.getLong(1));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan anggota.", exception);
        }
    }

    public void update(Member member) {
        String sql = "UPDATE members SET member_code = ?, name = ?, member_type = ?, major = ?, phone = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, member.getMemberCode());
            statement.setString(2, member.getName());
            statement.setString(3, member.getMemberType().name());
            statement.setString(4, member.getMajor());
            statement.setString(5, member.getPhone());
            statement.setLong(6, member.getId());
            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memperbarui anggota.", exception);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new RuntimeException("Anggota tidak bisa dihapus karena masih memiliki relasi data transaksi.", exception);
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghapus anggota.", exception);
        }
    }

    public Optional<Member> findById(long id) {
        String sql = "SELECT id, member_code, name, member_type, major, phone FROM members WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
            return Optional.empty();

        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mencari anggota berdasarkan id.", exception);
        }
    }

    public Optional<Member> findByCode(String memberCode) {
        String sql = "SELECT id, member_code, name, member_type, major, phone FROM members WHERE member_code = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, memberCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
            return Optional.empty();

        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mencari anggota.", exception);
        }
    }

    public List<Member> search(String keyword) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT id, member_code, name, member_type, major, phone FROM members " +
                "WHERE member_code LIKE ? OR name LIKE ? OR major LIKE ? OR phone LIKE ? " +
                "ORDER BY name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String value = "%" + keyword + "%";
            statement.setString(1, value);
            statement.setString(2, value);
            statement.setString(3, value);
            statement.setString(4, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(map(resultSet));
                }
            }
            return members;

        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mencari data anggota.", exception);
        }
    }

    public List<Member> findAll() {
        return search("");
    }

    public int countAll() {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM members");
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getInt(1);

        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung anggota.", exception);
        }
    }

    private Member map(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getLong("id"),
                resultSet.getString("member_code"),
                resultSet.getString("name"),
                MemberType.valueOf(resultSet.getString("member_type")),
                resultSet.getString("major"),
                resultSet.getString("phone")
        );
    }
}