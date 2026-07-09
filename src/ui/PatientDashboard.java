
package ui;

import auth.UserSession;
import database.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class PatientDashboard extends JFrame {

    private JLabel lblAppts, lblPaid, lblUnpaid, lblPrescriptions;

    public PatientDashboard() {
        setTitle("Patient Portal - " + UserSession.getInstance().getUsername());
        setSize(950, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        JLabel lblUser = new JLabel("Logged in as: " + UserSession.getInstance().getUsername() + " [PATIENT]");
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
        tabs.addTab("My Overview", buildOverviewTab());
        tabs.addTab("My Appointments", buildAppointmentsTab());
        tabs.addTab("My Payments", buildPaymentsTab());
        tabs.addTab("My Prescriptions", buildPrescriptionsTab());
        tabs.addTab("My Medical History", buildMedicalHistoryTab());

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0)
                loadPatientStats();
        });

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadPatientStats();
        setVisible(true);
    }

    private JPanel buildOverviewTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(240, 245, 250));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(new Color(240, 245, 250));
        JLabel header = new JLabel("  My Health Summary", SwingConstants.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        JButton btnRefresh = new JButton("↻ Refresh");
        btnRefresh.setBackground(new Color(41, 128, 185));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadPatientStats());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setBackground(new Color(240, 245, 250));
        btnP.add(btnRefresh);
        titleRow.add(header, BorderLayout.WEST);
        titleRow.add(btnP, BorderLayout.EAST);
        outer.add(titleRow, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15));
        cards.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        cards.setBackground(new Color(240, 245, 250));

        lblAppts = createCard("My Appointments", "0", new Color(155, 89, 182), cards);
        lblPaid = createCard("Paid Bills", "0", new Color(46, 204, 113), cards);
        lblUnpaid = createCard("Unpaid Bills", "0", new Color(231, 76, 60), cards);
        lblPrescriptions = createCard("Prescriptions", "0", new Color(52, 152, 219), cards);

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

    private void loadPatientStats() {
        int pid = UserSession.getInstance().getLinkedId();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs;
            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM appointments WHERE patient_id=" + pid);
            if (rs.next())
                lblAppts.setText(rs.getString(1));
            rs = con.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM payments WHERE patient_id=" + pid + " AND payment_status='PAID'");
            if (rs.next())
                lblPaid.setText(rs.getString(1));
            rs = con.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM payments WHERE patient_id=" + pid + " AND payment_status='UNPAID'");
            if (rs.next())
                lblUnpaid.setText(rs.getString(1));
            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM prescriptions WHERE patient_id=" + pid);
            if (rs.next())
                lblPrescriptions.setText(rs.getString(1));
        } catch (SQLException e) {
            System.out.println("Patient stats error: " + e.getMessage());
        }
    }

    private JPanel buildAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "Doctor", "Date", "Time", "Status", "Seen", "Notes" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        int pid = UserSession.getInstance().getLinkedId();
        String sql = "SELECT a.id, d.name, a.appointment_date, a.appointment_time, a.status, a.seen_status, a.notes " +
                "FROM appointments a JOIN doctors d ON a.doctor_id=d.id WHERE a.patient_id=? ORDER BY a.appointment_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("appointment_date"), rs.getString("appointment_time"),
                        rs.getString("status"), rs.getString("seen_status"), rs.getString("notes")
                });
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPaymentsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "Doctor", "Consult", "Lab", "Medicine", "Total", "Status", "Date" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        int pid = UserSession.getInstance().getLinkedId();
        String sql = "SELECT p.id, d.name, p.consultation_fee, p.lab_fee, p.medicine_fee, " +
                "p.total_amount, p.payment_status, p.payment_date " +
                "FROM payments p JOIN doctors d ON p.doctor_id=d.id WHERE p.patient_id=? ORDER BY p.id DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("consultation_fee"), rs.getDouble("lab_fee"),
                        rs.getDouble("medicine_fee"), rs.getDouble("total_amount"),
                        rs.getString("payment_status"), rs.getString("payment_date")
                });
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPrescriptionsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "Doctor", "Medicine", "Dosage", "Duration", "Notes", "Date" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        int pid = UserSession.getInstance().getLinkedId();
        String sql = "SELECT pr.id, d.name, pr.medicine, pr.dosage, pr.duration, pr.notes, pr.date_created " +
                "FROM prescriptions pr JOIN doctors d ON pr.doctor_id=d.id WHERE pr.patient_id=? ORDER BY pr.id DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"), rs.getString("medicine"),
                        rs.getString("dosage"), rs.getString("duration"),
                        rs.getString("notes"), rs.getString("date_created")
                });
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMedicalHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "Doctor", "Visit Date", "Diagnosis", "Treatment" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        int pid = UserSession.getInstance().getLinkedId();
        String sql = "SELECT r.id, d.name, r.visit_date, r.diagnosis, r.treatment " +
                "FROM medical_records r JOIN doctors d ON r.doctor_id=d.id WHERE r.patient_id=? ORDER BY r.visit_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"), rs.getString("visit_date"),
                        rs.getString("diagnosis"), rs.getString("treatment")
                });
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}