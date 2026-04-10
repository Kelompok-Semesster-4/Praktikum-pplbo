package com.library.app.ui;

import com.library.app.session.UserSession;
import com.library.app.ui.panel.FeedbackPanel;
import com.library.app.ui.panel.KioskVisitPanel;
import com.library.app.ui.panel.ProcurementPanel;
import com.library.app.ui.panel.SearchBookPanel;

public class KioskFrame extends BaseMainFrame {
   public KioskFrame(UserSession session) {
      super("Library Management System - Kiosk", session);
      tabbedPane.addTab("Absen", new KioskVisitPanel());
      tabbedPane.addTab("Cari Buku", new SearchBookPanel());
      tabbedPane.addTab("Feedback", new FeedbackPanel(false));
      tabbedPane.addTab("Request", new ProcurementPanel(false));
   }
}
