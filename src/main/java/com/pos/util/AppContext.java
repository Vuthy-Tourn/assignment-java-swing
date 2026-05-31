package com.pos.util;

import com.pos.model.User;

public class AppContext {

    private static User currentUser;
    private static String currency = "USD";

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }

    public static String getCurrency() { return currency; }
    public static void setCurrency(String c) { currency = c; }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRoleName());
    }

    public static void logout() {
        currentUser = null;
    }
}
