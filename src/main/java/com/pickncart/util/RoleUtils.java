package com.pickncart.util;

import com.pickncart.model.User;

public final class RoleUtils {

    private RoleUtils() {
    }

    public static boolean isAdmin(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String role = user.getRole().trim();
        return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
    }
}
