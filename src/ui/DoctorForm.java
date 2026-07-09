package ui;

import database.DBConnection;
import util.CSVExporter;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class DoctorForm extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtName, txtSpec, txtPhone, txtEmail, txtDays, txtSearch;
    private int selectedId = -1;

    public DoctorForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new String[] { "ID", "Name", "Specialization", "Phone", "Email", "Available Days" }, 0) {
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
        panel.setBorder(BorderFactory.createTitledBorder("Doctor Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtName = new JTextField(12);
        txtSpec = new JTextField(12);
        txtPhone = new JTextField(12);
        txtEmail = new JTextField(12);
        txtDays = new JTextField(15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(txtName, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Specialization:"), gbc);
        gbc.gridx = 3;
        panel.add(txtSpec, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPhone, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        panel.add(txtEmail, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Available Days:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(txtDays, gbc);
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
        btnExport.addActionListener(e -> CSVExporter.exportTable(table, "doctors_export.csv"));
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
        btnAdd.addActionListener(e -> addDoctor());
        btnUpdate.addActionListener(e -> updateDoctor());
        btnDelete.addActionListener(e -> deleteDoctor());
        btnClear.addActionListener(e -> clearForm());
        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);
        return panel;
    }

    private void addDoctor() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name required.");
            return;
        }
        String sql = "INSERT INTO doctors (name,specialization,phone,email,available_days) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtSpec.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setString(4, txtEmail.getText().trim());
            ps.setString(5, txtDays.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor added.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateDoctor() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }
        String sql = "UPDATE doctors SET name=?,specialization=?,phone=?,email=?,available_days=? WHERE id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtSpec.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setString(4, txtEmail.getText().trim());
            ps.setString(5, txtDays.getText().trim());
            ps.setInt(6, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor updated.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteDoctor() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this doctor?") != JOptionPane.YES_OPTION)
            return;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM doctors WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor deleted.");
            clearForm();
            loadData("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadData(String search) {
        model.setRowCount(0);
        String sql = "SELECT * FROM doctors WHERE name LIKE ? OR specialization LIKE ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"), rs.getString("specialization"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("available_days")
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
        txtSpec.setText((String) model.getValueAt(row, 2));
        txtPhone.setText((String) model.getValueAt(row, 3));
        txtEmail.setText((String) model.getValueAt(row, 4));
        txtDays.setText((String) model.getValueAt(row, 5));
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtSpec.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtDays.setText("");
    }
}