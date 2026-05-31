package com.pos.service;

import com.pos.dao.UserDAO;
import com.pos.model.User;
import com.pos.util.AppContext;
import com.pos.util.PasswordUtils;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required");
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (!PasswordUtils.verify(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        AppContext.setCurrentUser(user);
        return user;
    }

    public void logout() {
        AppContext.logout();
    }
}
