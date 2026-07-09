package ui;

import database.DBConnection;
import util.CSVExporter;
import util.PDFGenerator;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class PaymentForm extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbPatient, cmbDoctor, cmbStatus;
    private JTextField txtConsult, txtLab, txtMedicine, txtTotal, txtDate, txtSearch;
    private int selectedId = -1;

    public PaymentForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new String[] { "ID", "Patient", "Doctor", "Consult", "Lab", "Medicine", "Total", "Status", "Date" },
                0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getSelectionModel().addListSelectionListener(e -> loadSelectedRow());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(buildSearch(), BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(buildButtons(), BorderLayout.SOUTH);

        add(buildForm(), BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        loadData("");
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Payment Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        cmbStatus = new JComboBox<>(new String[] { "UNPAID", "PAID", "PENDING" });
        txtConsult = new JTextField(8);
        txtLab = new JTextField(8);
        txtMedicine = new JTextField(8);
        txtTotal = new JTextField(8);
        txtDate = new JTextField(10);
        txtTotal.setEditable(false);
        txtTotal.setBackground(new Color(220, 240, 220));

        loadPatientCombo();
        loadDoctorCombo();

        javax.swing.event.DocumentListener autoTotal = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                calcTotal();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                calcTotal();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                calcTotal();
            }
        };
        txtConsult.getDocument().addDocumentListener(autoTotal);
        txtLab.getDocument().addDocumentListener(autoTotal);
        txtMedicine.getDocument().addDocumentListener(autoTotal);

        JButton btnRefreshCombos = new JButton("↻ Refresh Lists");
        btnRefreshCombos.setFont(new Font("Arial", Font.PLAIN, 11));
        btnRefreshCombos.addActionListener(e -> {
            loadPatientCombo();
            loadDoctorCombo();
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Patient (ID - Name):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(cmbPatient, gbc);
        gbc.weightx = 0;
        gbc.gridx = 2;
        panel.add(new JLabel("Doctor (ID - Name):"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        panel.add(cmbDoctor, gbc);
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Consultation Fee (ETB):"), gbc);
        gbc.gridx = 1;
        panel.add(txtConsult, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Lab Fee (ETB):"), gbc);
        gbc.gridx = 3;
        panel.add(txtLab, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Medicine Fee (ETB):"), gbc);
        gbc.gridx = 1;
        panel.add(txtMedicine, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("TOTAL (Auto Calculated):"), gbc);
        gbc.gridx = 3;
        panel.add(txtTotal, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Payment Status:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbStatus, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3;
        panel.add(txtDate, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        panel.add(btnRefreshCombos, gbc);
        return panel;
    }

    private void calcTotal() {
        try {
            double c = txtConsult.getText().isEmpty() ? 0 : Double.parseDouble(txtConsult.getText());
            double l = txtLab.getText().isEmpty() ? 0 : Double.parseDouble(txtLab.getText());
            double m = txtMedicine.getText().isEmpty() ? 0 : Double.parseDouble(txtMedicine.getText());
            txtTotal.setText(String.format("%.2f", c + l + m));
        } catch (NumberFormatException ignored) {
        }
    }

    public void loadPatientCombo() {
        cmbPatient.removeAllItems();
        try (Connection con = DBConnection.getConnection();
                ResultSet rs = con.createStatement().executeQuery("SELECT id, name, phone FROM patients ORDER BY id")) {
            while (rs.next())
                cmbPatient.addItem(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("phone"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void loadDoctorCombo() {
        cmbDoctor.removeAllItems();
        try (Connection con = DBConnection.getConnection();
                ResultSet rs = con.createStatement()
                        .executeQuery("SELECT id, name, specialization FROM doctors ORDER BY id")) {
            while (rs.next())
                cmbDoctor.addItem(
                        rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("specialization"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private JPanel buildSearch() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export CSV");
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(39, 174, 96));
        btnRefresh.setForeground(Color.WHITE);
        btnExport.setBackground(new Color(155, 89, 182));
        btnExport.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            clearForm();
            loadData("");
        });
        btnExport.addActionListener(e -> CSVExporter.exportTable(table, "payments_export.csv"));
        panel.add(new JLabel("Search:"));
        panel.add(txtSearch);
        panel.add(btnSearch);
        panel.add(btnRefresh);
        panel.add(btnExport);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Add Payment");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnPDF = new JButton("Print Receipt");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnUpdate.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnPDF.setBackground(new Color(155, 89, 182));
        btnPDF.setForeground(Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(120, 30));
        btnUpdate.setPreferredSize(new Dimension(100, 30));
        btnDelete.setPreferredSize(new Dimension(100, 30));
        btnClear.setPreferredSize(new Dimension(100, 30));
        btnPDF.setPreferredSize(new Dimension(120, 30));
        btnAdd.addActionListener(e -> addPayment());
        btnUpdate.addActionListener(e -> updatePayment());
        btnDelete.addActionListener(e -> deletePayment());
        btnClear.addActionListener(e -> clearForm());
        btnPDF.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a payment first.");
                return;
            }
            PDFGenerator.generateReceipt(
                    (String) model.getValueAt(row, 1), (String) model.getValueAt(row, 2),
                    model.getValueAt(row, 3).toString(), model.getValueAt(row, 4).toString(),
                    model.getValueAt(row, 5).toString(), model.getValueAt(row, 6).toString(),
                    (String) model.getValueAt(row, 7), (String) model.getValueAt(row, 8));
        });
        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);
        panel.add(btnPDF);
        return panel;
    }

    private int getPatientId() {
        String s = (String) cmbPatient.getSelectedItem();
        if (s == null)
            return -1;
        return Integer.parseInt(s.split(" \\| ")[0].trim());
    }

    private int getDoctorId() {
        String s = (String) cmbDoctor.getSelectedItem();
        if (s == null)
            return -1;
        return Integer.parseInt(s.split(" \\| ")[0].trim());
    }

    private void addPayment() {
        if (getPatientId() == -1 || getDoctorId() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient and doctor.");
            return;
        }
        if (txtDate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a payment date.");
            return;
        }
        String sql = "INSERT INTO payments (patient_id,doctor_id,consultation_fee,lab_fee,medicine_fee,total_amount,payment_status,payment_date) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, getPatientId());
            ps.setInt(2, getDoctorId());
            ps.setDouble(3, txtConsult.getText().isEmpty() ? 0 : Double.parseDouble(txtConsult.getText()));
            ps.setDouble(4, txtLab.getText().isEmpty() ? 0 : Double.parseDouble(txtLab.getText()));
            ps.setDouble(5, txtMedicine.getText().isEmpty() ? 0 : Double.parseDouble(txtMedicine.getText()));
            ps.setDouble(6, txtTotal.getText().isEmpty() ? 0 : Double.parseDouble(txtTotal.getText()));
            ps.setString(7, (String) cmbStatus.getSelectedItem());
            ps.setString(8, txtDate.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Payment added successfully.");
            clearForm();
            loadData("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updatePayment() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a payment first.");
            return;
        }
        String sql = "UPDATE payments SET patient_id=?,doctor_id=?,consultation_fee=?,lab_fee=?,medicine_fee=?,total_amount=?,payment_status=?,payment_date=? WHERE id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, getPatientId());
            ps.setInt(2, getDoctorId());
            ps.setDouble(3, txtConsult.getText().isEmpty() ? 0 : Double.parseDouble(txtConsult.getText()));
            ps.setDouble(4, txtLab.getText().isEmpty() ? 0 : Double.parseDouble(txtLab.getText()));
            ps.setDouble(5, txtMedicine.getText().isEmpty() ? 0 : Double.parseDouble(txtMedicine.getText()));
            ps.setDouble(6, txtTotal.getText().isEmpty() ? 0 : Double.parseDouble(txtTotal.getText()));
            ps.setString(7, (String) cmbStatus.getSelectedItem());
            ps.setString(8, txtDate.getText().trim());
            ps.setInt(9, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Payment updated.");
            clearForm();
            loadData("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deletePayment() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a payment first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this payment?") != JOptionPane.YES_OPTION)
            return;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM payments WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Payment deleted.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadData(String search) {
        model.setRowCount(0);
        String sql = "SELECT pay.id, p.name AS patient, d.name AS doctor, pay.consultation_fee, " +
                "pay.lab_fee, pay.medicine_fee, pay.total_amount, pay.payment_status, pay.payment_date " +
                "FROM payments pay JOIN patients p ON pay.patient_id=p.id " +
                "JOIN doctors d ON pay.doctor_id=d.id WHERE p.name LIKE ? OR pay.payment_status LIKE ? ORDER BY pay.id DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("patient"), rs.getString("doctor"),
                        rs.getDouble("consultation_fee"), rs.getDouble("lab_fee"),
                        rs.getDouble("medicine_fee"), rs.getDouble("total_amount"),
                        rs.getString("payment_status"), rs.getString("payment_date")
                });
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void loadSelectedRow() {
        int row = table.getSelectedRow();
        if (row == -1)
            return;
        selectedId = (int) model.getValueAt(row, 0);
        txtConsult.setText(model.getValueAt(row, 3).toString());
        txtLab.setText(model.getValueAt(row, 4).toString());
        txtMedicine.setText(model.getValueAt(row, 5).toString());
        txtTotal.setText(model.getValueAt(row, 6).toString());
        cmbStatus.setSelectedItem(model.getValueAt(row, 7));
        Object date = model.getValueAt(row, 8);
        txtDate.setText(date != null ? date.toString() : "");
    }

    private void clearForm() {
        selectedId = -1;
        txtConsult.setText("");
        txtLab.setText("");
        txtMedicine.setText("");
        txtTotal.setText("");
        txtDate.setText("");
        cmbStatus.setSelectedIndex(0);
    }
}