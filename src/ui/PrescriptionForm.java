package ui;

import auth.UserSession;
import database.DBConnection;
import util.CSVExporter;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class PrescriptionForm extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbPatient, cmbDoctor;
    private JTextField txtMedicine, txtDosage, txtDuration, txtDate, txtSearch;
    private JTextArea txtNotes;
    private int selectedId = -1;

    public PrescriptionForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buildForm(), BorderLayout.NORTH);
        add(buildSearch(), BorderLayout.CENTER);

        model = new DefaultTableModel(
                new String[] { "ID", "Patient", "Doctor", "Medicine", "Dosage", "Duration", "Notes", "Date" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.getSelectionModel().addListSelectionListener(e -> loadSelectedRow());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(buildButtons(), BorderLayout.SOUTH);
        add(tablePanel, BorderLayout.SOUTH);
        loadData("");
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Prescription Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        txtMedicine = new JTextField(15);
        txtDosage = new JTextField(10);
        txtDuration = new JTextField(10);
        txtDate = new JTextField(10);
        txtNotes = new JTextArea(2, 20);

        String role = UserSession.getInstance().getRole();
        if (role.equals("DOCTOR")) {
            int docId = UserSession.getInstance().getLinkedId();
            try (Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                            "SELECT DISTINCT p.id, p.name FROM patients p JOIN appointments a ON p.id=a.patient_id WHERE a.doctor_id=?")) {
                ps.setInt(1, docId);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    cmbPatient.addItem(rs.getInt("id") + " - " + rs.getString("name"));
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            cmbDoctor.addItem(docId + " - (You)");
            cmbDoctor.setEnabled(false);
        } else {
            loadAllPatients();
            loadAllDoctors();
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbPatient, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 3;
        panel.add(cmbDoctor, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Medicine:"), gbc);
        gbc.gridx = 1;
        panel.add(txtMedicine, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Dosage:"), gbc);
        gbc.gridx = 3;
        panel.add(txtDosage, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Duration:"), gbc);
        gbc.gridx = 1;
        panel.add(txtDuration, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3;
        panel.add(txtDate, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(new JScrollPane(txtNotes), gbc);
        return panel;
    }

    private void loadAllPatients() {
        cmbPatient.removeAllItems();
        try (Connection con = DBConnection.getConnection();
                ResultSet rs = con.createStatement().executeQuery("SELECT id, name FROM patients")) {
            while (rs.next())
                cmbPatient.addItem(rs.getInt("id") + " - " + rs.getString("name"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void loadAllDoctors() {
        cmbDoctor.removeAllItems();
        try (Connection con = DBConnection.getConnection();
                ResultSet rs = con.createStatement().executeQuery("SELECT id, name FROM doctors")) {
            while (rs.next())
                cmbDoctor.addItem(rs.getInt("id") + " - " + rs.getString("name"));
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
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            clearForm();
            loadData("");
        });
        btnExport.addActionListener(e -> CSVExporter.exportTable(table, "prescriptions_export.csv"));
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
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addPrescription());
        btnDelete.addActionListener(e -> deletePrescription());
        btnClear.addActionListener(e -> clearForm());
        panel.add(btnAdd);
        panel.add(btnDelete);
        panel.add(btnClear);
        return panel;
    }

    private int getPatientId() {
        String s = (String) cmbPatient.getSelectedItem();
        return s == null ? -1 : Integer.parseInt(s.split(" - ")[0]);
    }

    private int getDoctorId() {
        String role = UserSession.getInstance().getRole();
        if (role.equals("DOCTOR"))
            return UserSession.getInstance().getLinkedId();
        String s = (String) cmbDoctor.getSelectedItem();
        return s == null ? -1 : Integer.parseInt(s.split(" - ")[0]);
    }

    private void addPrescription() {
        if (txtMedicine.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Medicine required.");
            return;
        }
        String sql = "INSERT INTO prescriptions (patient_id,doctor_id,medicine,dosage,duration,notes,date_created) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, getPatientId());
            ps.setInt(2, getDoctorId());
            ps.setString(3, txtMedicine.getText().trim());
            ps.setString(4, txtDosage.getText().trim());
            ps.setString(5, txtDuration.getText().trim());
            ps.setString(6, txtNotes.getText().trim());
            ps.setString(7, txtDate.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Prescription added.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deletePrescription() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a prescription first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete?") != JOptionPane.YES_OPTION)
            return;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM prescriptions WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadData(String search) {
        model.setRowCount(0);
        String sql = "SELECT pr.id, p.name AS patient, d.name AS doctor, pr.medicine, pr.dosage, " +
                "pr.duration, pr.notes, pr.date_created FROM prescriptions pr " +
                "JOIN patients p ON pr.patient_id=p.id JOIN doctors d ON pr.doctor_id=d.id " +
                "WHERE p.name LIKE ? OR pr.medicine LIKE ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("patient"), rs.getString("doctor"),
                        rs.getString("medicine"), rs.getString("dosage"), rs.getString("duration"),
                        rs.getString("notes"), rs.getString("date_created")
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
        txtMedicine.setText((String) model.getValueAt(row, 3));
        txtDosage.setText((String) model.getValueAt(row, 4));
        txtDuration.setText((String) model.getValueAt(row, 5));
        txtNotes.setText((String) model.getValueAt(row, 6));
        txtDate.setText((String) model.getValueAt(row, 7));
    }

    private void clearForm() {
        selectedId = -1;
        txtMedicine.setText("");
        txtDosage.setText("");
        txtDuration.setText("");
        txtDate.setText("");
        txtNotes.setText("");
    }
}