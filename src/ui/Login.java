package ui;

import auth.AuthService;
import auth.UserSession;

import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;

    private JPanel infoPanel;
    private JPanel loginPanel;

    public Login() {
        setTitle("Hospital Management System - Login");
        setSize(450, 380);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new CardLayout());

        createInfoScreen();
        createLoginScreen();

        add(infoPanel);
        add(loginPanel);

        loginPanel.setVisible(false);
        infoPanel.setVisible(true);

        setVisible(true);
    }

    // ===================== INFO SCREEN =====================
    private void createInfoScreen() {
        infoPanel = new JPanel();
        infoPanel.setBackground(new Color(30, 60, 90));
        infoPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "<html><center>" +
                        "<h2>HOSPITAL MANAGEMENT SYSTEM</h2>" +
                        "<p>This system is done by ATSEE Group Team Members</p><br>" +
                        "<b>1. Abenezer Samson</b> - DDU1600048<br>" +
                        "<b>2. TSION ASRAT</b> - RMD2465<br>" +
                        "<b>3. SUMEYA AHMED</b> - DDU1600683<br>" +
                        "<b>4. ENAS REMEDAN</b> - DDU1600227<br>" +
                        "<b>5. EYERUSALEM BERIHUN</b> - RMD921<br><br>" +
                        "</center></html>");

        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnNext = new JButton("NEXT →");
        btnNext.setFont(new Font("Arial", Font.BOLD, 14));
        btnNext.setFocusPainted(false);

        btnNext.addActionListener(e -> {
            infoPanel.setVisible(false);
            loginPanel.setVisible(true);
        });

        infoPanel.add(title, BorderLayout.CENTER);
        infoPanel.add(btnNext, BorderLayout.SOUTH);
    }

    // ===================== LOGIN SCREEN =====================
    private void createLoginScreen() {
        loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(new Color(240, 240, 240));

        JLabel header = new JLabel("Login", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        form.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        form.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;

        btnLogin = new JButton("LOGIN");
        btnLogin.setBackground(new Color(30, 60, 90));
        btnLogin.setForeground(Color.WHITE);

        form.add(btnLogin, gbc);

        gbc.gridy = 3;
        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setForeground(Color.RED);
        form.add(lblStatus, gbc);

        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());

        loginPanel.add(header, BorderLayout.NORTH);
        loginPanel.add(form, BorderLayout.CENTER);
    }

    // ===================== LOGIN LOGIC =====================
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Enter username and password.");
            return;
        }

        int result = AuthService.login(username, password);

        switch (result) {
            case -1:
                lblStatus.setText("Invalid username or password.");
                break;

            case 0:
                lblStatus.setText("Account disabled.");
                break;

            case 2:
                dispose();
                new ChangePassword(
                        UserSession.getInstance().getUserId(),
                        UserSession.getInstance().getRole());
                break;

            case 1:
                dispose();
                openDashboard(UserSession.getInstance().getRole());
                break;
        }
    }

    // ===================== DASHBOARD ROUTING =====================
    private void openDashboard(String role) {
        switch (role) {
            case "ADMIN":
                new AdminDashboard();
                break;
            case "DOCTOR":
                new DoctorDashboard();
                break;
            case "RECEPTIONIST":
                new ReceptionDashboard();
                break;
            case "PATIENT":
                new PatientDashboard();
                break;
        }
    }

    // ===================== MAIN =====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}