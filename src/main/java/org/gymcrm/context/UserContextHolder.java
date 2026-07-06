package org.gymcrm.context;

public class UserContextHolder {
    private static final ThreadLocal<UserCredentials> context = new ThreadLocal<>();

    public static void setCredentials(String username, String password) {
        context.set(new UserCredentials(username, password));
    }

    public static UserCredentials getCredentials() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }

    public record UserCredentials(String username, String password) {}
}