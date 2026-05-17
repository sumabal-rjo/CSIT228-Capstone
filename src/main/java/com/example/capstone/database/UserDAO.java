package com.example.capstone.database;

import com.example.capstone.model.User;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements GenericDAO<User> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    public User login(String username, String password) {
        SchemaHelper.ensureAppSchema();

        String sql = "SELECT u.*, c.name AS company_name "
                + "FROM users u "
                + "LEFT JOIN companies c ON u.company_id = c.company_id "
                + "WHERE u.username = ? AND u.is_active = 1";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next() && passwordMatches(password, resultSet.getString("password"))) {
                return readUser(resultSet);
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }

    public boolean updateProfile(User user) {
        SchemaHelper.ensureAppSchema();

        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, email = ?, company_id = ? WHERE user_id = ?";

        try {
            int companyId = ensureCompany(user.getCompanyName());
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, user.getUsername());
            statement.setString(2, hashPassword(user.getPassword()));
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            setCompanyId(statement, 5, companyId);
            statement.setInt(6, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating profile: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean add(User user) {
        SchemaHelper.ensureAppSchema();

        String sql = "INSERT INTO users (username, password, full_name, email, role, company_id) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            int companyId = ensureCompany(user.getCompanyName());
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, user.getUsername());
            statement.setString(2, hashPassword(user.getPassword()));
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, normalizeRole(user.getRole()));
            setCompanyId(statement, 6, companyId);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(User user) {
        SchemaHelper.ensureAppSchema();

        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, email = ?, role = ?, company_id = ? WHERE user_id = ?";

        try {
            int companyId = ensureCompany(user.getCompanyName());
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, user.getUsername());
            statement.setString(2, hashPassword(user.getPassword()));
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, normalizeRole(user.getRole()));
            setCompanyId(statement, 6, companyId);
            statement.setInt(7, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        SchemaHelper.ensureAppSchema();

        String sql = "UPDATE users SET is_active = 0 WHERE user_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<User> getAll() {
        SchemaHelper.ensureAppSchema();

        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, c.name AS company_name "
                + "FROM users u LEFT JOIN companies c ON u.company_id = c.company_id "
                + "WHERE u.is_active = 1 ORDER BY u.full_name";

        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                users.add(readUser(resultSet));
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }

        return users;
    }

    @Override
    public User getById(int id) {
        SchemaHelper.ensureAppSchema();

        String sql = "SELECT u.*, c.name AS company_name "
                + "FROM users u LEFT JOIN companies c ON u.company_id = c.company_id "
                + "WHERE u.user_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return readUser(resultSet);
            }
        } catch (Exception e) {
            System.out.println("Error finding user: " + e.getMessage());
        }

        return null;
    }

    public boolean usernameExists(String username) {
        SchemaHelper.ensureAppSchema();

        String sql = "SELECT COUNT(*) AS total FROM users WHERE username = ? AND is_active = 1";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getInt("total") > 0;
        } catch (Exception e) {
            System.out.println("Error checking username: " + e.getMessage());
            return false;
        }
    }

    private User readUser(ResultSet resultSet) throws Exception {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setRole(resultSet.getString("role"));
        user.setCompanyId(resultSet.getInt("company_id"));
        user.setCompanyName(resultSet.getString("company_name"));
        return user;
    }

    private int ensureCompany(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return 0;
        }

        String cleanName = companyName.trim();
        String insertSql = "INSERT IGNORE INTO companies (name) VALUES (?)";
        String selectSql = "SELECT company_id FROM companies WHERE name = ?";

        try {
            PreparedStatement insertStatement = getConnection().prepareStatement(insertSql);
            insertStatement.setString(1, cleanName);
            insertStatement.executeUpdate();

            PreparedStatement selectStatement = getConnection().prepareStatement(selectSql);
            selectStatement.setString(1, cleanName);
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("company_id");
            }
        } catch (Exception e) {
            System.out.println("Error saving company: " + e.getMessage());
        }

        return 0;
    }

    private void setCompanyId(PreparedStatement statement, int index, int companyId) throws Exception {
        if (companyId > 0) {
            statement.setInt(index, companyId);
        } else {
            statement.setNull(index, Types.INTEGER);
        }
    }

    private boolean passwordMatches(String password, String storedPassword) {
        return storedPassword.equals(password) || storedPassword.equals(hashPassword(password));
    }

    private String normalizeRole(String role) {
        if ("Admin".equalsIgnoreCase(role)) {
            return "admin";
        }
        return "staff";
    }

    private String hashPassword(String password) {
        if (password != null && password.matches("(?i)[0-9a-f]{64}")) {
            return password;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : encoded) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception e) {
            return password;
        }
    }
}
