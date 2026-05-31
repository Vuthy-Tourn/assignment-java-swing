package com.pos.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private Long roleId;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(String username, String password, String fullName, Long roleId) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.roleId = roleId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() { return fullName != null ? fullName : username; }
}
