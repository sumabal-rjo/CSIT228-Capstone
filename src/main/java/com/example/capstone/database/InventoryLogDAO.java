package com.example.capstone.database;

import com.example.capstone.model.InventoryLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InventoryLogDAO {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    public List<InventoryLog> getAll() {
        SchemaHelper.ensureAppSchema();

        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, p.name AS product_name, COALESCE(u.full_name, 'System') AS user_name "
                + "FROM inventory_logs l "
                + "JOIN products p ON l.product_id = p.product_id "
                + "LEFT JOIN users u ON l.user_id = u.user_id "
                + "WHERE p.company_id = ? "
                + "ORDER BY l.logged_at DESC";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, SchemaHelper.getCurrentCompanyId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                InventoryLog log = new InventoryLog();
                log.setLogId(resultSet.getInt("log_id"));
                log.setProductId(resultSet.getInt("product_id"));
                log.setProductName(resultSet.getString("product_name"));
                log.setUserId(resultSet.getInt("user_id"));
                log.setUserName(resultSet.getString("user_name"));
                log.setChangeType(resultSet.getString("change_type"));
                log.setQuantityBefore(resultSet.getInt("quantity_before"));
                log.setQuantityChange(resultSet.getInt("quantity_change"));
                log.setQuantityAfter(resultSet.getInt("quantity_after"));
                log.setNote(resultSet.getString("note"));
                log.setLoggedAt(resultSet.getString("logged_at"));
                logs.add(log);
            }
        } catch (Exception e) {
            System.out.println("Error loading inventory logs: " + e.getMessage());
        }

        return logs;
    }
}
