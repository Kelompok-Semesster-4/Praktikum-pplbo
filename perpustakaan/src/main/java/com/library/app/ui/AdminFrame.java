package com.library.app.ui;

import com.library.app.session.UserSession;
import com.library.app.ui.panel.*;

public class AdminFrame extends BaseMainFrame {
   public AdminFrame(UserSession session) {
      super("Library Management System - Admin", session);
      tabbedPane.addTab("Dashboard", new DashboardPanel());
      tabbedPane.addTab("Buku", new BookManagementPanel());
      tabbedPane.addTab("Anggota", new MemberManagementPanel());
      tabbedPane.addTab("Kunjungan", new VisitPanel());
      tabbedPane.addTab("Peminjaman", new LoanPanel());
      tabbedPane.addTab("Pengembalian", new ReturnPanel());
      tabbedPane.addTab("Feedback", new FeedbackPanel(true));
      tabbedPane.addTab("Request", new ProcurementPanel(true));
   }
}
