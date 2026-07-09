package ui;

import auth.AuthService;
import auth.UserSession;
import database.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {

    private JTable userTable;
    private DefaultTableModel userModel;
    private JTextField txtUsername, txtPassword, txtLinkedId, txtSearch;
    private JComboBox<String> cmbRole;
    private JLabel lblPatients, lblDoctors, lblAppointments, lblRevenue;

    public AdminDashboard() {
        setTitle("Admin Dashboard - " + UserSession.getInstance().getUsername());
        setSize(1150, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(30, 60, 90));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        JLabel lblUser = new JLabel("Logged in as: " + UserSession.getInstance().getUsername() + " [ADMIN]");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Arial", Font.BOLD, 13));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.addActionListener(e -> {
            UserSession.getInstance().clear();
            dispose();
            new Login();
        });
        topBar.add(lblUser, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", buildDashboardTab());
        tabs.addTab("User Management", buildUserTab());
        tabs.addTab("Patients", new PatientForm());
        tabs.addTab("Doctors", new DoctorForm());
        tabs.addTab("Appointments", new AppointmentForm());
        tabs.addTab("Payments", new PaymentForm());
        tabs.addTab("Activity Logs", buildLogsTab());

        // Refresh stats every time Dashboard tab is selected
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0)
                loadStats();
        });

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadStats();
        setVisible(true);
    }

    private JPanel buildDashboardTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(240, 240, 245));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(new Color(240, 240, 245));
        JLabel header = new JLabel("  Hospital Overview", SwingConstants.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        JButton btnRefresh = new JButton("↻ Refresh Stats");
        btnRefresh.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadStats());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(240, 240, 245));
        btnPanel.add(btnRefresh);
        titleRow.add(header, BorderLayout.WEST);
        titleRow.add(btnPanel, BorderLayout.EAST);
        outer.add(titleRow, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15));
        cards.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        cards.setBackground(new Color(240, 240, 245));

        lblPatients = createStatCard("Total Patients", "0", new Color(52, 152, 219), cards);
        lblDoctors = createStatCard("Total Doctors", "0", new Color(46, 204, 113), cards);
        lblAppointments = createStatCard("Appointments", "0", new Color(155, 89, 182), cards);
        lblRevenue = createStatCard("Revenue (ETB)", "0", new Color(231, 76, 60), cards);

        outer.add(cards, BorderLayout.CENTER);
        return outer;
    }

    private JLabel createStatCard(String title, String value, Color color, JPanel parent) {
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
            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM doctors");
            if (rs.next())
                lblDoctors.setText(rs.getString(1));
            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM appointments");
            if (rs.next())
                lblAppointments.setText(rs.getString(1));
            rs = con.createStatement()
                    .executeQuery("SELECT IFNULL(SUM(total_amount),0) FROM payments WHERE payment_status='PAID'");
            if (rs.next())
                lblRevenue.setText(rs.getString(1));
        } catch (SQLException e) {
            System.out.println("Stats error: " + e.getMessage());
        }
    }

    private JPanel buildUserTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Create New User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(12);
        txtPassword = new JTextField(12);
        cmbRole = new JComboBox<>(new String[] { "DOCTOR", "RECEPTIONIST", "PATIENT" });
        txtLinkedId = new JTextField(8);

        JLabel lblLinkedHint = new JLabel(
                "<html><font color='gray' size='2'>Enter the Doctor/Patient ID<br>from the Doctors/Patients tab</font></html>");

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(txtUsername, gbc);
        gbc.gridx = 2;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 3;
        form.add(txtPassword, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        form.add(cmbRole, gbc);
        gbc.gridx = 2;
        form.add(new JLabel("Linked ID:"), gbc);
        gbc.gridx = 3;
        form.add(txtLinkedId, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(lblLinkedHint, gbc);

        JButton btnCreate = new JButton("Create User");
        btnCreate.setBackground(new Color(30, 60, 90));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(btnCreate, gbc);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        userModel = new DefaultTableModel(
                new String[] { "ID", "Username", "Role", "Linked ID", "First Login", "Active", "Created" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        userTable = new JTable(userModel);
        userTable.setRowHeight(25);

        JButton btnEnable = new JButton("Enable");
        JButton btnDisable = new JButton("Disable");
        JButton btnDelete = new JButton("Delete");
        btnEnable.setBackground(new Color(46, 204, 113));
        btnEnable.setForeground(Color.WHITE);
        btnDisable.setBackground(new Color(230, 126, 34));
        btnDisable.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnEnable.setPreferredSize(new Dimension(90, 28));
        btnDisable.setPreferredSize(new Dimension(90, 28));
        btnDelete.setPreferredSize(new Dimension(90, 28));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnEnable);
        bottom.add(btnDisable);
        bottom.add(btnDelete);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        tablePanel.add(bottom, BorderLayout.SOUTH);

        panel.add(form, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.SOUTH);

        loadUsers("");

        btnCreate.addActionListener(e -> {
            String u = txtUsername.getText().trim();
            String p = txtPassword.getText().trim();
            String r = (String) cmbRole.getSelectedItem();
            int lid = 0;
            try {
                lid = Integer.parseInt(txtLinkedId.getText().trim());
            } catch (Exception ignored) {
            }
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password required.");
                return;
            }
            if (AuthService.createUser(u, p, r, lid)) {
                JOptionPane.showMessageDialog(this, "User created. First login will require password change.");
                loadUsers("");
                loadStats();
            } else {
                JOptionPane.showMessageDialog(this, "Error. Username may already exist.");
            }
        });

        btnSearch.addActionListener(e -> loadUsers(txtSearch.getText().trim()));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadUsers("");
        });

        btnEnable.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a user.");
                return;
            }
            AuthService.toggleUserActive((int) userModel.getValueAt(row, 0), 1);
            loadUsers("");
        });
        btnDisable.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a user.");
                return;
            }
            AuthService.toggleUserActive((int) userModel.getValueAt(row, 0), 0);
            loadUsers("");
        });
        btnDelete.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a user.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Delete this user?") == JOptionPane.YES_OPTION) {
                AuthService.deleteUser((int) userModel.getValueAt(row, 0));
                loadUsers("");
            }
        });

        return panel;
    }

    private void loadUsers(String search) {
        userModel.setRowCount(0);
        String sql = "SELECT id, username, role, linked_id, first_login, is_active, created_at FROM users WHERE username LIKE ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                userModel.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("username"), rs.getString("role"),
                        rs.getInt("linked_id"),
                        rs.getInt("first_login") == 1 ? "YES" : "NO",
                        rs.getInt("is_active") == 1 ? "ACTIVE" : "DISABLED",
                        rs.getString("created_at")
                });
            }
        } catch (SQLException e) {
            System.out.println("Load users error: " + e.getMessage());
        }
    }

    private JPanel buildLogsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel logModel = new DefaultTableModel(new String[] { "ID", "Username", "Action", "Time" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable logTable = new JTable(logModel);
        logTable.setRowHeight(22);
        JButton btnLoad = new JButton("Load Logs");
        btnLoad.addActionListener(e -> {
            logModel.setRowCount(0);
            try (Connection con = DBConnection.getConnection();
                    ResultSet rs = con.createStatement()
                            .executeQuery("SELECT * FROM activity_logs ORDER BY log_time DESC LIMIT 200")) {
                while (rs.next()) {
                    logModel.addRow(new Object[] {
                            rs.getInt("id"), rs.getString("username"),
                            rs.getString("action"), rs.getString("log_time")
                    });
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnLoad);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(logTable), BorderLayout.CENTER);
        return panel;
    }
}