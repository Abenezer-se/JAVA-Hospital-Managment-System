package ui;

import database.DBConnection;
import util.CSVExporter;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class PatientForm extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtName, txtAge, txtPhone, txtAddress, txtSearch;
    private JComboBox<String> cmbGender, cmbBlood;
    private int selectedId = -1;

    public PatientForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new String[] { "ID", "Name", "Age", "Gender", "Blood Group", "Phone", "Address" }, 0) {
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
        panel.setBorder(BorderFactory.createTitledBorder("Patient Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(12);
        txtAge = new JTextField(5);
        txtPhone = new JTextField(12);
        txtAddress = new JTextField(15);
        cmbGender = new JComboBox<>(new String[] { "", "Male", "Female", "Other" });
        cmbBlood = new JComboBox<>(new String[] { "", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-" });

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(txtName, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 3;
        panel.add(txtAge, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbGender, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Blood Group:"), gbc);
        gbc.gridx = 3;
        panel.add(cmbBlood, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPhone, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 3;
        panel.add(txtAddress, gbc);
        return panel;
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
        btnExport.addActionListener(e -> CSVExporter.exportTable(table, "patients_export.csv"));
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
        btnAdd.addActionListener(e -> addPatient());
        btnUpdate.addActionListener(e -> updatePatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnClear.addActionListener(e -> clearForm());
        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);
        return panel;
    }

    private void addPatient() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name required.");
            return;
        }
        String sql = "INSERT INTO patients (name,age,gender,phone,address,blood_group) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setInt(2, txtAge.getText().isEmpty() ? 0 : Integer.parseInt(txtAge.getText().trim()));
            ps.setString(3, (String) cmbGender.getSelectedItem());
            ps.setString(4, txtPhone.getText().trim());
            ps.setString(5, txtAddress.getText().trim());
            ps.setString(6, (String) cmbBlood.getSelectedItem());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient added.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updatePatient() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient first.");
            return;
        }
        String sql = "UPDATE patients SET name=?,age=?,gender=?,phone=?,address=?,blood_group=? WHERE id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setInt(2, txtAge.getText().isEmpty() ? 0 : Integer.parseInt(txtAge.getText().trim()));
            ps.setString(3, (String) cmbGender.getSelectedItem());
            ps.setString(4, txtPhone.getText().trim());
            ps.setString(5, txtAddress.getText().trim());
            ps.setString(6, (String) cmbBlood.getSelectedItem());
            ps.setInt(7, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient updated.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deletePatient() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this patient?") != JOptionPane.YES_OPTION)
            return;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM patients WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient deleted.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadData(String search) {
        model.setRowCount(0);
        String sql = "SELECT * FROM patients WHERE name LIKE ? OR phone LIKE ? OR blood_group LIKE ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ps.setString(3, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                        rs.getString("gender"), rs.getString("blood_group"),
                        rs.getString("phone"), rs.getString("address")
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
        txtName.setText((String) model.getValueAt(row, 1));
        txtAge.setText(String.valueOf(model.getValueAt(row, 2)));
        cmbGender.setSelectedItem(model.getValueAt(row, 3));
        cmbBlood.setSelectedItem(model.getValueAt(row, 4));
        txtPhone.setText((String) model.getValueAt(row, 5));
        txtAddress.setText((String) model.getValueAt(row, 6));
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtAge.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        cmbGender.setSelectedIndex(0);
        cmbBlood.setSelectedIndex(0);
    }
}