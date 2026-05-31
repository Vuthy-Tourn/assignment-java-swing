package com.pos.service;

import com.pos.dao.UserDAO;
import com.pos.model.Role;
import com.pos.model.User;
import com.pos.util.PasswordUtils;

import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<User> getAll() { return userDAO.findAll(); }
    public List<Role> getRoles() { return userDAO.findAllRoles(); }

    public User create(String username, String password, String fullName, Long roleId) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username is required");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        if (userDAO.findByUsername(username) != null)
            throw new IllegalArgumentException("Username already exists");

        User user = new User(username.trim(), PasswordUtils.hash(password), fullName, roleId);
        return userDAO.save(user);
    }

    public void update(User user) { userDAO.update(user); }

    public void changePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        userDAO.updatePassword(userId, PasswordUtils.hash(newPassword));
    }

    public void delete(Long id) { userDAO.delete(id); }
}
