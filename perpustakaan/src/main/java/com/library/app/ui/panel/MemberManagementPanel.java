package com.library.app.ui.panel;

import com.library.app.model.Member;
import com.library.app.model.enums.MemberType;
import com.library.app.service.MemberService;
import com.library.app.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MemberManagementPanel extends JPanel implements RefreshablePanel {
    private final MemberService memberService = new MemberService();

    private final JTextField codeField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JComboBox<MemberType> typeComboBox = new JComboBox<>(MemberType.values());
    private final JTextField majorField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField searchField = new JTextField();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Kode", "Nama", "Tipe", "Jurusan", "Telepon"}, 0);

    public MemberManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        add(buildHeader(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        refreshData();
    }

    private JComponent buildHeader() {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Tambah Anggota"));

        form.add(new JLabel("NIM/NIS/NIDN"));
        form.add(codeField);
        form.add(new JLabel("Nama"));
        form.add(nameField);
        form.add(new JLabel("Tipe"));
        form.add(typeComboBox);
        form.add(new JLabel("Jurusan"));
        form.add(majorField);
        form.add(new JLabel("Telepon"));
        form.add(phoneField);

        JButton saveButton = new JButton("Simpan Anggota");
        saveButton.addActionListener(event -> saveMember());

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Cari Anggota"));
        JButton searchButton = new JButton("Cari");
        searchButton.addActionListener(event -> refreshData());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        container.add(form, BorderLayout.CENTER);
        container.add(saveButton, BorderLayout.EAST);
        container.add(searchPanel, BorderLayout.SOUTH);
        return container;
    }

    private void saveMember() {
        try {
            memberService.registerMember(
                    codeField.getText(),
                    nameField.getText(),
                    (MemberType) typeComboBox.getSelectedItem(),
                    majorField.getText(),
                    phoneField.getText()
            );
            UiUtil.showInfo(this, "Anggota berhasil disimpan.");
            codeField.setText("");
            nameField.setText("");
            majorField.setText("");
            phoneField.setText("");
            refreshData();
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }

    @Override
    public void refreshData() {
        tableModel.setRowCount(0);
        List<Member> members = memberService.search(searchField.getText());
        for (Member member : members) {
            tableModel.addRow(new Object[]{
                    member.getMemberCode(),
                    member.getName(),
                    member.getMemberType(),
                    member.getMajor(),
                    member.getPhone()
            });
        }
    }
}
