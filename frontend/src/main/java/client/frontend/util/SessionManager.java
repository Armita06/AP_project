package client.frontend.util;

public class SessionManager {

    private static final SessionManager instance = new SessionManager();

    private String token;
    private String username;
    private String role;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return instance;
    }

    public void login(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public void logout() {
        this.token = null;
        this.username = null;
        this.role = null;
    }

    public boolean isLoggedIn() {
        return token != null && !token.trim().isEmpty();
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}