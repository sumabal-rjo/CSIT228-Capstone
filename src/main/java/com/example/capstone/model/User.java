package com.example.capstone.model;

public class User extends BaseEntity {

    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;
    private int companyId;
    private String companyName;

    public User() {
    }

    public User(int userId, String username, String password, String fullName, String role) {
        this.id = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public int getUserId() {
        return id;
    }

    public void setUserId(int userId) {
        this.id = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String getDisplayLabel() {
        return fullName;
    }
}
