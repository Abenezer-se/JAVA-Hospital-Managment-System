package ui;

import database.DBConnection;
import util.CSVExporter;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AppointmentForm extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbPatient, cmbDoctor, cmbStatus;
    private JTextField txtDate, txtTime, txtNotes, txtSearch;
    private int selectedId = -1;

    public AppointmentForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new String[] { "ID", "Patient", "Doctor", "Date", "Time", "Status", "Seen", "Notes" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(25);
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
        panel.setBorder(BorderFactory.createTitledBorder("Appointment Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        cmbStatus = new JComboBox<>(new String[] { "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED" });
        txtDate = new JTextField(10);
        txtTime = new JTextField(8);
        txtNotes = new JTextField(20);

        loadPatientCombo();
        loadDoctorCombo();

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
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        panel.add(txtDate, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Time (e.g. 10:00 AM):"), gbc);
        gbc.gridx = 3;
        panel.add(txtTime, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbStatus, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 3;
        panel.add(txtNotes, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        panel.add(btnRefreshCombos, gbc);
        return panel;
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
        btnExport.addActionListener(e -> CSVExporter.exportTable(table, "appointments_export.csv"));
        panel.add(new JLabel("Search:"));
        panel.add(txtSearch);
        panel.add(btnSearch);
        panel.add(btnRefresh);
        panel.add(btnExport);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnUpdate.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(100, 30));
        btnUpdate.setPreferredSize(new Dimension(100, 30));
        btnDelete.setPreferredSize(new Dimension(100, 30));
        btnClear.setPreferredSize(new Dimension(100, 30));
        btnAdd.addActionListener(e -> addAppointment());
        btnUpdate.addActionListener(e -> updateAppointment());
        btnDelete.addActionListener(e -> deleteAppointment());
        btnClear.addActionListener(e -> clearForm());
        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);
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

    private void addAppointment() {
        if (getPatientId() == -1 || getDoctorId() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient and doctor.");
            return;
        }
        String sql = "INSERT INTO appointments (patient_id,doctor_id,appointment_date,appointment_time,status,notes) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, getPatientId());
            ps.setInt(2, getDoctorId());
            ps.setString(3, txtDate.getText().trim());
            ps.setString(4, txtTime.getText().trim());
            ps.setString(5, (String) cmbStatus.getSelectedItem());
            ps.setString(6, txtNotes.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment added successfully.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateAppointment() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select an appointment first.");
            return;
        }
        String sql = "UPDATE appointments SET patient_id=?,doctor_id=?,appointment_date=?,appointment_time=?,status=?,notes=? WHERE id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, getPatientId());
            ps.setInt(2, getDoctorId());
            ps.setString(3, txtDate.getText().trim());
            ps.setString(4, txtTime.getText().trim());
            ps.setString(5, (String) cmbStatus.getSelectedItem());
            ps.setString(6, txtNotes.getText().trim());
            ps.setInt(7, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment updated.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteAppointment() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select an appointment first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this appointment?") != JOptionPane.YES_OPTION)
            return;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM appointments WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment deleted.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadData(String search) {
        model.setRowCount(0);
        String sql = "SELECT a.id, p.name AS patient, d.name AS doctor, a.appointment_date, " +
                "a.appointment_time, a.status, a.seen_status, a.notes FROM appointments a " +
                "JOIN patients p ON a.patient_id=p.id JOIN doctors d ON a.doctor_id=d.id " +
                "WHERE p.name LIKE ? OR d.name LIKE ? OR a.status LIKE ? ORDER BY a.id DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ps.setString(3, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("patient"), rs.getString("doctor"),
                        rs.getString("appointment_date"), rs.getString("appointment_time"),
                        rs.getString("status"), rs.getString("seen_status"), rs.getString("notes")
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
        txtDate.setText((String) model.getValueAt(row, 3));
        txtTime.setText((String) model.getValueAt(row, 4));
        cmbStatus.setSelectedItem(model.getValueAt(row, 5));
        Object notes = model.getValueAt(row, 7);
        txtNotes.setText(notes != null ? notes.toString() : "");
    }

    private void clearForm() {
        selectedId = -1;
        txtDate.setText("");
        txtTime.setText("");
        txtNotes.setText("");
        cmbStatus.setSelectedIndex(0);
    }
}