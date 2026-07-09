package ui;

import auth.AuthService;
import javax.swing.*;
import java.awt.*;

public class ChangePassword extends JFrame {

    private JPasswordField txtNew, txtConfirm;
    private int userId;
    private String role;

    public ChangePassword(int userId, String role) {
        this.userId = userId;
        this.role = role;

        setTitle("Change Password - First Login");
        setSize(380, 260);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);

        JLabel lblTitle = new JLabel("You must change your password", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lblTitle.setForeground(new Color(200, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        txtNew = new JPasswordField(15);
        panel.add(txtNew, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        txtConfirm = new JPasswordField(15);
        panel.add(txtConfirm, gbc);

        JButton btnChange = new JButton("Change Password");
        btnChange.setBackground(new Color(30, 60, 90));
        btnChange.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(btnChange, gbc);

        JLabel lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setForeground(Color.RED);
        gbc.gridy = 4;
        panel.add(lblStatus, gbc);

        add(panel);

        btnChange.addActionListener(e -> {
            String np = new String(txtNew.getPassword()).trim();
            String cp = new String(txtConfirm.getPassword()).trim();
            if (np.isEmpty()) {
                lblStatus.setText("Enter new password.");
                return;
            }
            if (!np.equals(cp)) {
                lblStatus.setText("Passwords do not match.");
                return;
            }
            if (np.length() < 6) {
                lblStatus.setText("Minimum 6 characters.");
                return;
            }
            if (AuthService.changePassword(userId, np)) {
                JOptionPane.showMessageDialog(this, "Password changed successfully!");
                dispose();
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
        });

        setVisible(true);
    }
}