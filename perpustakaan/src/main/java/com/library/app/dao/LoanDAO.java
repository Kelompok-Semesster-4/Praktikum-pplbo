package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.Loan;
import com.library.app.model.enums.LoanStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAO {
    public void save(Loan loan) {
        String sql = "INSERT INTO loans(member_id, copy_id, loan_date, due_date, fine_amount, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, loan.getMemberId());
            statement.setLong(2, loan.getCopyId());
            statement.setDate(3, Date.valueOf(loan.getLoanDate()));
            statement.setDate(4, Date.valueOf(loan.getDueDate()));
            statement.setBigDecimal(5, loan.getFineAmount());
            statement.setString(6, loan.getStatus().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    loan.setId(keys.getLong(1));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan transaksi pinjam.", exception);
        }
    }

    public int countActiveLoansByMember(long memberId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND status = 'ACTIVE'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung pinjaman aktif anggota.", exception);
        }
    }

    public int countActiveLoans() {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE'");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung pinjaman aktif.", exception);
        }
    }

    public Optional<Loan> findActiveLoanByCopyCode(String copyCode) {
        String sql = "SELECT l.id, l.member_id, l.copy_id, m.member_code, m.name AS member_name, " +
                "c.copy_code, b.title AS book_title, l.loan_date, l.due_date, l.return_date, l.fine_amount, l.status " +
                "FROM loans l " +
                "JOIN members m ON m.id = l.member_id " +
                "JOIN book_copies c ON c.id = l.copy_id " +
                "JOIN books b ON b.id = c.book_id " +
                "WHERE c.copy_code = ? AND l.status = 'ACTIVE'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, copyCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mencari pinjaman aktif.", exception);
        }
    }

    public List<Loan> findActiveLoans() {
        return findByStatus(LoanStatus.ACTIVE);
    }

    public List<Loan> findReturnedLoans() {
        return findByStatus(LoanStatus.RETURNED);
    }

    private List<Loan> findByStatus(LoanStatus status) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.id, l.member_id, l.copy_id, m.member_code, m.name AS member_name, " +
                "c.copy_code, b.title AS book_title, l.loan_date, l.due_date, l.return_date, l.fine_amount, l.status " +
                "FROM loans l " +
                "JOIN members m ON m.id = l.member_id " +
                "JOIN book_copies c ON c.id = l.copy_id " +
                "JOIN books b ON b.id = c.book_id " +
                "WHERE l.status = ? ORDER BY l.id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(map(resultSet));
                }
            }
            return loans;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil data pinjaman.", exception);
        }
    }

    public void updateReturn(Loan loan) {
        String sql = "UPDATE loans SET return_date = ?, fine_amount = ?, status = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(loan.getReturnDate()));
            statement.setBigDecimal(2, loan.getFineAmount() == null ? BigDecimal.ZERO : loan.getFineAmount());
            statement.setString(3, loan.getStatus().name());
            statement.setLong(4, loan.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memperbarui transaksi pengembalian.", exception);
        }
    }

    private Loan map(ResultSet resultSet) throws SQLException {
        Loan loan = new Loan();
        loan.setId(resultSet.getLong("id"));
        loan.setMemberId(resultSet.getLong("member_id"));
        loan.setCopyId(resultSet.getLong("copy_id"));
        loan.setMemberCode(resultSet.getString("member_code"));
        loan.setMemberName(resultSet.getString("member_name"));
        loan.setCopyCode(resultSet.getString("copy_code"));
        loan.setBookTitle(resultSet.getString("book_title"));
        loan.setLoanDate(resultSet.getDate("loan_date").toLocalDate());
        loan.setDueDate(resultSet.getDate("due_date").toLocalDate());
        Date returnDate = resultSet.getDate("return_date");
        if (returnDate != null) {
            loan.setReturnDate(returnDate.toLocalDate());
        }
        loan.setFineAmount(resultSet.getBigDecimal("fine_amount"));
        loan.setStatus(LoanStatus.valueOf(resultSet.getString("status")));
        return loan;
    }
}
