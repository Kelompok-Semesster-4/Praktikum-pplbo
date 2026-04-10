package com.library.app.ui.panel;

import com.library.app.service.VisitService;
import com.library.app.util.UiUtil;

import javax.swing.*;
import java.awt.*;

public class KioskVisitPanel extends JPanel {
    private final VisitService visitService = new VisitService();
    private final JTextField memberCodeField = new JTextField();

    public KioskVisitPanel() {
        setLayout(new GridBagLayout());
        JPanel card = new JPanel(new GridLayout(0, 1, 8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.add(new JLabel("Silakan masukkan NIM/NIS/NIDN"));
        card.add(memberCodeField);

        JButton visitButton = new JButton("Absen Kunjungan");
        visitButton.addActionListener(event -> recordVisit());
        card.add(visitButton);

        add(card);
    }

    private void recordVisit() {
        try {
            visitService.recordMemberVisit(memberCodeField.getText());
            UiUtil.showInfo(this, "Kunjungan berhasil dicatat.");
            memberCodeField.setText("");
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }
}
