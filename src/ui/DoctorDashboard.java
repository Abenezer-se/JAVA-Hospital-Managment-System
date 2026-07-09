package ui;

import auth.UserSession;
import database.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class DoctorDashboard extends JFrame {

    private DefaultTableModel apptModel;
    private JTable apptTable;
    private JLabel lblTotalPatients, lblTotalAppts, lblSeenAppts, lblPendingAppts;

    public DoctorDashboard() {
        setTitle("Doctor Dashboard - " + UserSession.getInstance().getUsername());
        setSize(1050, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(39, 174, 96));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        JLabel lblUser = new JLabel("Logged in as: " + UserSession.getInstance().getUsername() + " [DOCTOR]");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Arial", Font.BOLD, 13));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            UserSession.getInstance().clear();
            dispose();
            new Login();
        });
        topBar.add(lblUser, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", buildOverviewTab());
        tabs.addTab("My Appointments", buildAppointmentsTab());
        tabs.addTab("My Patients", buildPatientsTab());
        tabs.addTab("Prescriptions", new PrescriptionForm());
        tabs.addTab("Medical Records", buildMedicalRecordsTab());

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0)
                loadDoctorStats();
        });

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadAppointments();
        loadDoctorStats();
        setVisible(true);
    }

    private JPanel buildOverviewTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(240, 245, 240));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(new Color(240, 245, 240));
        JLabel header = new JLabel("  My Overview", SwingConstants.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        JButton btnRefresh = new JButton("↻ Refresh");
        btnRefresh.setBackground(new Color(39, 174, 96));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadDoctorStats());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setBackground(new Color(240, 245, 240));
        btnP.add(btnRefresh);
        titleRow.add(header, BorderLayout.WEST);
        titleRow.add(btnP, BorderLayout.EAST);
        outer.add(titleRow, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15));
        cards.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        cards.setBackground(new Color(240, 245, 240));

        lblTotalPatients = createCard("My Patients", "0", new Color(52, 152, 219), cards);
        lblTotalAppts = createCard("Total Appts", "0", new Color(155, 89, 182), cards);
        lblSeenAppts = createCard("Seen", "0", new Color(46, 204, 113), cards);
        lblPendingAppts = createCard("Pending", "0", new Color(231, 126, 34), cards);

        outer.add(cards, BorderLayout.CENTER);
        return outer;
    }

    private JLabel createCard(String title, String value, Color color, JPanel parent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel lTitle = new JLabel(title, SwingConstants.CENTER);
        lTitle.setForeground(Color.WHITE);
        lTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        JLabel lValue = new JLabel(value, SwingConstants.CENTER);
        lValue.setForeground(Color.WHITE);
        lValue.setFont(new Font("Arial", Font.BOLD, 34));
        lValue.setBackground(color);
        lValue.setOpaque(true);
        card.add(lTitle, BorderLayout.NORTH);
        card.add(lValue, BorderLayout.CENTER);
        parent.add(card);
        return lValue;
    }

    private void loadDoctorStats() {
        int docId = UserSession.getInstance().getLinkedId();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs;
            rs = con.createStatement().executeQuery(
                    "SELECT COUNT(DISTINCT patient_id) FROM appointments WHERE doctor_id=" + docId);
            if (rs.next())
                lblTotalPatients.setText(rs.getString(1));
            rs = con.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM appointments WHERE doctor_id=" + docId);
            if (rs.next())
                lblTotalAppts.setText(rs.getString(1));
            rs = con.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM appointments WHERE doctor_id=" + docId + " AND seen_status='SEEN'");
            if (rs.next())
                lblSeenAppts.setText(rs.getString(1));
            rs = con.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM appointments WHERE doctor_id=" + docId + " AND status='PENDING'");
            if (rs.next())
                lblPendingAppts.setText(rs.getString(1));
        } catch (SQLException e) {
            System.out.println("Doctor stats error: " + e.getMessage());
        }
    }

    private JPanel buildAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        apptModel = new DefaultTableModel(
                new String[] { "ID", "Patient", "Date", "Time", "Status", "Seen", "Notes" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        apptTable = new JTable(apptModel);
        apptTable.setRowHeight(25);

        JButton btnSeen = new JButton("Mark as SEEN");
        JButton btnComplete = new JButton("Mark COMPLETED");
        JButton btnRefresh = new JButton("Refresh");

        btnSeen.setBackground(new Color(52, 152, 219));
        btnSeen.setForeground(Color.WHITE);
        btnComplete.setBackground(new Color(46, 204, 113));
        btnComplete.setForeground(Color.WHITE);
        btnSeen.setPreferredSize(new Dimension(140, 30));
        btnComplete.setPreferredSize(new Dimension(150, 30));
        btnRefresh.setPreferredSize(new Dimension(100, 30));

        btnSeen.addActionListener(e -> {
            int row = apptTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an appointment.");
                return;
            }
            updateSeenStatus((int) apptModel.getValueAt(row, 0), "SEEN");
        });
        btnComplete.addActionListener(e -> {
            int row = apptTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an appointment.");
                return;
            }
            updateAppointmentStatus((int) apptModel.getValueAt(row, 0), "COMPLETED");
        });
        btnRefresh.addActionListener(e -> loadAppointments());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnRefresh);
        bottom.add(btnSeen);
        bottom.add(btnComplete);

        panel.add(new JScrollPane(apptTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPatientsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "Name", "Age", "Gender", "Blood Group", "Phone" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            model.setRowCount(0);
            loadPatientsIntoModel(model);
        });

        loadPatientsIntoModel(model);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(btnRefresh);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadPatientsIntoModel(DefaultTableModel model) {
        int docId = UserSession.getInstance().getLinkedId();
        String sql = "SELECT DISTINCT p.id, p.name, p.age, p.gender, p.blood_group, p.phone " +
                "FROM patients p JOIN appointments a ON p.id=a.patient_id WHERE a.doctor_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, docId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                        rs.getString("gender"), rs.getString("blood_group"), rs.getString("phone")
                });
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private JPanel buildMedicalRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Add Medical Record"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbPatient = new JComboBox<>();
        JTextField txtDate = new JTextField(10);
        JTextArea txtDiagnosis = new JTextArea(3, 20);
        JTextArea txtTreatment = new JTextArea(3, 20);

        int docId = UserSession.getInstance().getLinkedId();

        Runnable loadPatients = () -> {
            cmbPatient.removeAllItems();
            try (Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                            "SELECT DISTINCT p.id, p.name FROM patients p JOIN appointments a ON p.id=a.patient_id WHERE a.doctor_id=?")) {
                ps.setInt(1, docId);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    cmbPatient.addItem(rs.getInt("id") + " | " + rs.getString("name"));
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        };
        loadPatients.run();

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1;
        form.add(cmbPatient, gbc);
        gbc.gridx = 2;
        form.add(new JLabel("Visit Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3;
        form.add(txtDate, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        form.add(new JScrollPane(txtDiagnosis), gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        form.add(new JLabel("Treatment:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        form.add(new JScrollPane(txtTreatment), gbc);

        JButton btnSave = new JButton("Save Record");
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        form.add(btnSave, gbc);

        DefaultTableModel recModel = new DefaultTableModel(
                new String[] { "ID", "Patient", "Visit Date", "Diagnosis", "Treatment" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable recTable = new JTable(recModel);
        recTable.setRowHeight(25);

        Runnable loadRecords = () -> {
            recModel.setRowCount(0);
            String sql = "SELECT r.id, p.name, r.visit_date, r.diagnosis, r.treatment " +
                    "FROM medical_records r JOIN patients p ON r.patient_id=p.id WHERE r.doctor_id=?";
            try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, docId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    recModel.addRow(new Object[] {
                            rs.getInt("id"), rs.getString("name"), rs.getString("visit_date"),
                            rs.getString("diagnosis"), rs.getString("treatment")
                    });
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        };

        btnSave.addActionListener(e -> {
            String sel = (String) cmbPatient.getSelectedItem();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "No patient selected.");
                return;
            }
            int patId = Integer.parseInt(sel.split(" \\| ")[0].trim());
            String sql = "INSERT INTO medical_records (patient_id, doctor_id, visit_date, diagnosis, treatment) VALUES (?,?,?,?,?)";
            try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, patId);
                ps.setInt(2, docId);
                ps.setString(3, txtDate.getText().trim());
                ps.setString(4, txtDiagnosis.getText().trim());
                ps.setString(5, txtTreatment.getText().trim());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Record saved.");
                txtDate.setText("");
                txtDiagnosis.setText("");
                txtTreatment.setText("");
                loadRecords.run();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(recTable), BorderLayout.CENTER);
        loadRecords.run();
        return panel;
    }

    private void loadAppointments() {
        apptModel.setRowCount(0);
        int docId = UserSession.getInstance().getLinkedId();
        String sql = "SELECT a.id, p.name, a.appointment_date, a.appointment_time, a.status, a.seen_status, a.notes " +
                "FROM appointments a JOIN patients p ON a.patient_id=p.id WHERE a.doctor_id=? ORDER BY a.appointment_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, docId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                apptModel.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("appointment_date"), rs.getString("appointment_time"),
                        rs.getString("status"), rs.getString("seen_status"), rs.getString("notes")
                });
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateSeenStatus(int id, String status) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE appointments SET seen_status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Marked as SEEN.");
            loadAppointments();
            loadDoctorStats();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateAppointmentStatus(int id, String status) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE appointments SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Status updated.");
            loadAppointments();
            loadDoctorStats();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}