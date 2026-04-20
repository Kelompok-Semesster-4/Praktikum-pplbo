package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.DashboardSummary;
import com.library.app.model.OverdueLoanReportItem;
import com.library.app.model.ReportSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardDAO {
    private final BookDAO bookDAO = new BookDAO();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final VisitDAO visitDAO = new VisitDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private final ProcurementRequestDAO procurementRequestDAO = new ProcurementRequestDAO();

    public DashboardSummary getSummary() {
        DashboardSummary summary = new DashboardSummary();
        summary.setTotalBooks(bookDAO.countBooks());
        summary.setTotalCopies(bookCopyDAO.countAll());
        summary.setAvailableCopies(bookCopyDAO.countAvailable());
        summary.setTotalMembers(memberDAO.countAll());
        summary.setVisitsToday(visitDAO.countToday());
        summary.setActiveLoans(loanDAO.countActiveLoans());
        summary.setPendingRequests(procurementRequestDAO.countPending());
        return summary;
    }

    public ReportSummary getReportSummary() {
        String sql = "SELECT COUNT(*) AS total_loans, " +
                "SUM(CASE WHEN return_date IS NOT NULL OR status = 'RETURNED' THEN 1 ELSE 0 END) AS returned_total, " +
                "SUM(CASE WHEN return_date IS NULL AND due_date < CURDATE() THEN 1 ELSE 0 END) AS overdue_total, " +
                "COALESCE(SUM(fine_amount), 0) AS total_fine " +
                "FROM loans";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            ReportSummary summary = new ReportSummary();
            if (resultSet.next()) {
                summary.setTotalLoans(resultSet.getInt("total_loans"));
                summary.setReturnedLoans(resultSet.getInt("returned_total"));
                summary.setOverdueLoans(resultSet.getInt("overdue_total"));
                summary.setTotalFineAmount(resultSet.getBigDecimal("total_fine"));
            }
            return summary;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil ringkasan laporan.", exception);
        }
    }

    public LinkedHashMap<String, Integer> findMonthlyVisits(int monthCount) {
        String sql = "SELECT YEAR(visit_date) AS year_no, MONTH(visit_date) AS month_no, COUNT(*) AS total " +
                "FROM visits " +
                "WHERE visit_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                "GROUP BY YEAR(visit_date), MONTH(visit_date) " +
                "ORDER BY YEAR(visit_date), MONTH(visit_date)";

        Map<YearMonth, Integer> totalsByMonth = new LinkedHashMap<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, monthCount - 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    YearMonth key = YearMonth.of(resultSet.getInt("year_no"), resultSet.getInt("month_no"));
                    totalsByMonth.put(key, resultSet.getInt("total"));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil tren kunjungan bulanan.", exception);
        }

        return fillMonthlySingleSeries(totalsByMonth, monthCount);
    }

    public LinkedHashMap<String, int[]> findMonthlyLoanReturnTrend(int monthCount) {
        String sql = "SELECT YEAR(ref_date) AS year_no, MONTH(ref_date) AS month_no, " +
                "SUM(CASE WHEN event_type = 'LOAN' THEN 1 ELSE 0 END) AS loan_total, " +
                "SUM(CASE WHEN event_type = 'RETURN' THEN 1 ELSE 0 END) AS return_total " +
                "FROM (" +
                "  SELECT loan_date AS ref_date, 'LOAN' AS event_type FROM loans " +
                "  WHERE loan_date IS NOT NULL AND loan_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                "  UNION ALL " +
                "  SELECT return_date AS ref_date, 'RETURN' AS event_type FROM loans " +
                "  WHERE return_date IS NOT NULL AND return_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)" +
                ") AS monthly_events " +
                "GROUP BY YEAR(ref_date), MONTH(ref_date) " +
                "ORDER BY YEAR(ref_date), MONTH(ref_date)";

        Map<YearMonth, int[]> totalsByMonth = new LinkedHashMap<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, monthCount - 1);
            statement.setInt(2, monthCount - 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    YearMonth key = YearMonth.of(resultSet.getInt("year_no"), resultSet.getInt("month_no"));
                    int loanTotal = resultSet.getInt("loan_total");
                    int returnTotal = resultSet.getInt("return_total");
                    totalsByMonth.put(key, new int[] { loanTotal, returnTotal });
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil tren peminjaman dan pengembalian.", exception);
        }

        return fillMonthlyPairSeries(totalsByMonth, monthCount);
    }

    public List<String[]> findRecentLoans(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT m.name AS member_name, COALESCE(m.major, '-') AS member_major, " +
                "b.title AS book_title, l.due_date, l.status " +
                "FROM loans l " +
                "JOIN members m ON m.id = l.member_id " +
                "JOIN book_copies c ON c.id = l.copy_id " +
                "JOIN books b ON b.id = c.book_id " +
                "ORDER BY COALESCE(l.created_at, TIMESTAMP(l.loan_date)) DESC, l.id DESC " +
                "LIMIT ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String[] row = new String[5];
                    row[0] = resultSet.getString("member_name");
                    row[1] = resultSet.getString("member_major");
                    row[2] = resultSet.getString("book_title");
                    row[3] = String.valueOf(resultSet.getDate("due_date"));
                    row[4] = resultSet.getString("status");
                    rows.add(row);
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil daftar peminjaman terkini.", exception);
        }
    }

    public List<String[]> findTodayVisits(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT visitor_name, COALESCE(visitor_identifier, '-') AS visitor_identifier, visit_type, visit_status, " +
                "DATE_FORMAT(created_at, '%H:%i') AS visit_time " +
                "FROM visits " +
                "WHERE visit_date = CURDATE() " +
                "ORDER BY created_at DESC, id DESC " +
                "LIMIT ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String[] row = new String[5];
                    row[0] = resultSet.getString("visitor_name");
                    row[1] = resultSet.getString("visitor_identifier");
                    row[2] = resultSet.getString("visit_type");
                    row[3] = resultSet.getString("visit_status");
                    row[4] = resultSet.getString("visit_time");
                    rows.add(row);
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil kunjungan hari ini.", exception);
        }
    }

    public List<OverdueLoanReportItem> findOverdueLoans(int limit) {
        List<OverdueLoanReportItem> rows = new ArrayList<>();
        String sql = "SELECT m.name AS member_name, m.member_code, b.title AS book_title, " +
                "l.due_date, COALESCE(l.fine_amount, 0) AS fine_amount, l.status " +
                "FROM loans l " +
                "JOIN members m ON m.id = l.member_id " +
                "JOIN book_copies c ON c.id = l.copy_id " +
                "JOIN books b ON b.id = c.book_id " +
                "WHERE l.return_date IS NULL AND l.due_date < CURDATE() " +
                "ORDER BY l.due_date ASC, l.id DESC " +
                "LIMIT ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OverdueLoanReportItem item = new OverdueLoanReportItem();
                    item.setBorrowerName(resultSet.getString("member_name"));
                    item.setMemberCode(resultSet.getString("member_code"));
                    item.setBookTitle(resultSet.getString("book_title"));
                    item.setDueDate(resultSet.getObject("due_date", java.time.LocalDate.class));
                    item.setFineAmount(resultSet.getBigDecimal("fine_amount"));
                    item.setStatus(resultSet.getString("status"));
                    rows.add(item);
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil daftar pinjaman terlambat.", exception);
        }
    }

    private LinkedHashMap<String, Integer> fillMonthlySingleSeries(Map<YearMonth, Integer> source, int monthCount) {
        LinkedHashMap<String, Integer> output = new LinkedHashMap<>();
        YearMonth start = YearMonth.now().minusMonths(monthCount - 1L);
        Locale locale = Locale.forLanguageTag("id-ID");
        for (int offset = 0; offset < monthCount; offset++) {
            YearMonth key = start.plusMonths(offset);
            String label = capitalizeMonth(key.getMonth().getDisplayName(TextStyle.SHORT, locale));
            output.put(label, source.getOrDefault(key, 0));
        }
        return output;
    }

    private LinkedHashMap<String, int[]> fillMonthlyPairSeries(Map<YearMonth, int[]> source, int monthCount) {
        LinkedHashMap<String, int[]> output = new LinkedHashMap<>();
        YearMonth start = YearMonth.now().minusMonths(monthCount - 1L);
        Locale locale = Locale.forLanguageTag("id-ID");
        for (int offset = 0; offset < monthCount; offset++) {
            YearMonth key = start.plusMonths(offset);
            String label = capitalizeMonth(key.getMonth().getDisplayName(TextStyle.SHORT, locale));
            output.put(label, source.getOrDefault(key, new int[] { 0, 0 }));
        }
        return output;
    }

    private String capitalizeMonth(String month) {
        if (month == null || month.isBlank()) {
            return "-";
        }
        if (month.length() == 1) {
            return month.toUpperCase(Locale.ROOT);
        }
        return month.substring(0, 1).toUpperCase(Locale.ROOT) + month.substring(1);
    }
}
