package auth;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String role;
    private int linkedId;
    private int userId;

    private UserSession() {
    }

    public static UserSession getInstance() {
        if (instance == null)
            instance = new UserSession();
        return instance;
    }

    public void setUser(int userId, String username, String role, int linkedId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.linkedId = linkedId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public int getLinkedId() {
        return linkedId;
    }

    public void clear() {
        userId = 0;
        username = null;
        role = null;
        linkedId = 0;
    }
}