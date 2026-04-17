package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.Loan;
import com.library.app.model.enums.LoanStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
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
            statement.setObject(3, loan.getLoanDate());
            statement.setObject(4, loan.getDueDate());
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
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND return_date IS NULL";
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
                PreparedStatement statement = connection
                        .prepareStatement("SELECT COUNT(*) FROM loans WHERE return_date IS NULL");
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
                "WHERE c.copy_code = ? AND l.return_date IS NULL";
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
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.id, l.member_id, l.copy_id, m.member_code, m.name AS member_name, " +
                "c.copy_code, b.title AS book_title, l.loan_date, l.due_date, l.return_date, l.fine_amount, l.status " +
                "FROM loans l " +
                "JOIN members m ON m.id = l.member_id " +
                "JOIN book_copies c ON c.id = l.copy_id " +
                "JOIN books b ON b.id = c.book_id " +
                "WHERE l.return_date IS NULL ORDER BY l.due_date ASC, l.id DESC";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                loans.add(map(resultSet));
            }
            return loans;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil data pinjaman aktif.", exception);
        }
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

    public Object[] getLoanIdentity(long loanId) {
        String loanIdString = String.valueOf(loanId);
        Object[] loanIdentity = new Object[4];
        String sql = "SELECT m.member_code, m.name AS member_name, c.copy_code, b.title AS book_title " +
                "FROM loans l " +
                "JOIN members m ON m.id = l.member_id " +
                "JOIN book_copies c ON c.id = l.copy_id " +
                "JOIN books b ON c.book_id = b.id " +
                "WHERE l.id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loanIdString);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    loanIdentity[0] = resultSet.getString("member_code");
                    loanIdentity[1] = resultSet.getString("member_name");
                    loanIdentity[2] = resultSet.getString("copy_code");
                    loanIdentity[3] = resultSet.getString("book_title");
                }
            }
            return loanIdentity;
        } catch (Exception e) {
            throw new RuntimeException("Gagal mengambil identitas peminjaman: ", e);
        }
    }

    private Loan map(ResultSet resultSet) throws SQLException {
        Loan loan = new Loan();
        loan.setId(resultSet.getLong("id"));
        loan.setMemberId(resultSet.getLong("member_id"));
        loan.setCopyId(resultSet.getLong("copy_id"));
        loan.setLoanDate(resultSet.getObject("loan_date", LocalDate.class));
        loan.setDueDate(resultSet.getObject("due_date", LocalDate.class));
        LocalDate returnDate = resultSet.getObject("return_date", LocalDate.class);
        if (returnDate != null) {
            loan.setReturnDate(returnDate);
        }
        loan.setFineAmount(resultSet.getBigDecimal("fine_amount"));
        loan.setStatus(LoanStatus.valueOf(resultSet.getString("status")));
        return loan;
    }
}
