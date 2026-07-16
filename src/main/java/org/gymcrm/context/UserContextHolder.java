package org.gymcrm.context;

public class UserContextHolder {
    private static final ThreadLocal<UserCredentials> credentials = new ThreadLocal<>();
    private static final ThreadLocal<AuthInfo> auth = new ThreadLocal<>();

    public static void setCredentials(String username, String password) {
        credentials.set(new UserCredentials(username, password));
    }

    public static UserCredentials getCredentials() {
        return credentials.get();
    }

    public static void setAuthenticated(String username, String role) {
        credentials.remove();
        auth.set(new AuthInfo(username, role, true));
    }

    public static AuthInfo getAuth() {
        return auth.get();
    }

    public static void clear() {
        credentials.remove();
        auth.remove();
    }

    public record UserCredentials(String username, String password) {}
    public record AuthInfo(String username, String role, boolean isAuthenticated) {}
}