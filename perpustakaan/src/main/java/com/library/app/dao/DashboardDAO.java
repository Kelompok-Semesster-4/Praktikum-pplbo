package com.library.app.dao;

import com.library.app.model.DashboardSummary;

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
}
