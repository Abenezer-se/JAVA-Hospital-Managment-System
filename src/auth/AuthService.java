package auth;

import database.DBConnection;
import java.sql.*;

public class AuthService {

    public static int login(String username, String password) {
        // Returns: -1 = failed, 0 = inactive, 1 = success normal, 2 = first login
        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("is_active") == 0)
                    return 0;
                UserSession.getInstance().setUser(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getInt("linked_id"));
                logActivity(username, "Logged in");
                return rs.getInt("first_login") == 1 ? 2 : 1;
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return -1;
    }

    public static boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password=?, first_login=0 WHERE id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean createUser(String username, String password, String role, int linkedId) {
        String sql = "INSERT INTO users (username, password, role, linked_id, first_login) VALUES (?,?,?,?,1)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.setInt(4, linkedId);
            ps.executeUpdate();
            logActivity(UserSession.getInstance().getUsername(), "Created user: " + username);
            return true;
        } catch (SQLException e) {
            System.out.println("Create user error: " + e.getMessage());
            return false;
        }
    }

    public static boolean toggleUserActive(int userId, int status) {
        String sql = "UPDATE users SET is_active=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void logActivity(String username, String action) {
        String sql = "INSERT INTO activity_logs (username, action) VALUES (?,?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, action);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Log error: " + e.getMessage());
        }
    }
}