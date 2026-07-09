package ui;

import auth.UserSession;
import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ReceptionDashboard extends JFrame {

    private JLabel lblPatients, lblAppointments, lblPayments, lblUnpaid;

    public ReceptionDashboard() {
        setTitle("Reception Dashboard - " + UserSession.getInstance().getUsername());
        setSize(1050, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(142, 68, 173));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        JLabel lblUser = new JLabel("Logged in as: " + UserSession.getInstance().getUsername() + " [RECEPTIONIST]");
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
        tabs.addTab("Patients", new PatientForm());
        tabs.addTab("Appointments", new AppointmentForm());
        tabs.addTab("Payments", new PaymentForm());

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0)
                loadStats();
        });

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadStats();
        setVisible(true);
    }

    private JPanel buildOverviewTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(245, 240, 245));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(new Color(245, 240, 245));
        JLabel header = new JLabel("  Reception Overview", SwingConstants.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        JButton btnRefresh = new JButton("↻ Refresh");
        btnRefresh.setBackground(new Color(142, 68, 173));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadStats());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setBackground(new Color(245, 240, 245));
        btnP.add(btnRefresh);
        titleRow.add(header, BorderLayout.WEST);
        titleRow.add(btnP, BorderLayout.EAST);
        outer.add(titleRow, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15));
        cards.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        cards.setBackground(new Color(245, 240, 245));

        lblPatients = createCard("Registered Patients", "0", new Color(52, 152, 219), cards);
        lblAppointments = createCard("Total Appointments", "0", new Color(155, 89, 182), cards);
        lblPayments = createCard("Payments Collected", "0", new Color(46, 204, 113), cards);
        lblUnpaid = createCard("Unpaid Bills", "0", new Color(231, 76, 60), cards);

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

    private void loadStats() {
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs;
            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM patients");
            if (rs.next())
                lblPatients.setText(rs.getString(1));
            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM appointments");
            if (rs.next())
                lblAppointments.setText(rs.getString(1));
            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM payments WHERE payment_status='PAID'");
            if (rs.next())
                lblPayments.setText(rs.getString(1));
            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM payments WHERE payment_status='UNPAID'");
            if (rs.next())
                lblUnpaid.setText(rs.getString(1));
        } catch (SQLException e) {
            System.out.println("Reception stats error: " + e.getMessage());
        }
    }
}