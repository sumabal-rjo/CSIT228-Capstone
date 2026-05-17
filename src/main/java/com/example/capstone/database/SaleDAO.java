package com.example.capstone.database;

import com.example.capstone.model.Product;
import com.example.capstone.model.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    public synchronized boolean recordSale(Product product, int quantitySold, Integer userId, String notes) {
        SchemaHelper.ensureAppSchema();

        if (product == null || quantitySold <= 0 || quantitySold > product.getQuantity()) {
            return false;
        }

        double subtotal = product.getPrice() * quantitySold;
        int newQuantity = product.getQuantity() - quantitySold;
        String saleSql = "INSERT INTO sales (company_id, user_id, product_id, product_name, quantity_sold, unit_price, total_amount, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE products SET quantity = ? WHERE product_id = ? AND company_id = ?";
        String logSql = "INSERT INTO inventory_logs (product_id, user_id, change_type, quantity_before, quantity_change, quantity_after, note) "
                + "VALUES (?, ?, 'sale', ?, ?, ?, ?)";

        try {
            getConnection().setAutoCommit(false);

            PreparedStatement saleStatement = getConnection().prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            saleStatement.setInt(1, SchemaHelper.getCurrentCompanyId());
            if (userId == null) {
                saleStatement.setNull(2, Types.INTEGER);
            } else {
                saleStatement.setInt(2, userId);
            }
            saleStatement.setInt(3, product.getProductId());
            saleStatement.setString(4, product.getName());
            saleStatement.setInt(5, quantitySold);
            saleStatement.setDouble(6, product.getPrice());
            saleStatement.setDouble(7, subtotal);
            saleStatement.setString(8, notes);
            saleStatement.executeUpdate();

            ResultSet keys = saleStatement.getGeneratedKeys();
            if (!keys.next()) {
                getConnection().rollback();
                return false;
            }

            int saleId = keys.getInt(1);
            PreparedStatement updateStatement = getConnection().prepareStatement(updateSql);
            updateStatement.setInt(1, newQuantity);
            updateStatement.setInt(2, product.getProductId());
            updateStatement.setInt(3, SchemaHelper.getCurrentCompanyId());
            updateStatement.executeUpdate();

            PreparedStatement logStatement = getConnection().prepareStatement(logSql);
            logStatement.setInt(1, product.getProductId());
            if (userId == null) {
                logStatement.setNull(2, Types.INTEGER);
            } else {
                logStatement.setInt(2, userId);
            }
            logStatement.setInt(3, product.getQuantity());
            logStatement.setInt(4, -quantitySold);
            logStatement.setInt(5, newQuantity);
            logStatement.setString(6, "Sale ID: " + saleId);
            logStatement.executeUpdate();

            getConnection().commit();
            return true;
        } catch (Exception e) {
            try {
                getConnection().rollback();
            } catch (Exception rollbackError) {
                System.out.println("Rollback error: " + rollbackError.getMessage());
            }
            System.out.println("Error recording sale: " + e.getMessage());
            return false;
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (Exception e) {
                System.out.println("Auto commit error: " + e.getMessage());
            }
        }
    }

    public List<Sale> getSoldItems() {
        SchemaHelper.ensureAppSchema();

        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.sale_date, s.total_amount, s.notes, "
                + "COALESCE(u.full_name, 'System') AS recorded_by, COALESCE(s.product_name, p.name) AS product_name, "
                + "s.quantity_sold, s.unit_price "
                + "FROM sales s "
                + "LEFT JOIN users u ON s.user_id = u.user_id "
                + "LEFT JOIN products p ON s.product_id = p.product_id "
                + "WHERE s.company_id = ? "
                + "ORDER BY s.sale_date DESC";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, SchemaHelper.getCurrentCompanyId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Sale sale = new Sale();
                sale.setSaleId(resultSet.getInt("sale_id"));
                sale.setSaleDate(resultSet.getString("sale_date"));
                sale.setRecordedBy(resultSet.getString("recorded_by"));
                sale.setProductName(resultSet.getString("product_name"));
                sale.setQuantitySold(resultSet.getInt("quantity_sold"));
                sale.setUnitPrice(resultSet.getDouble("unit_price"));
                sale.setSubtotal(resultSet.getDouble("total_amount"));
                sale.setTotalAmount(resultSet.getDouble("total_amount"));
                sale.setNotes(resultSet.getString("notes"));
                sales.add(sale);
            }
        } catch (Exception e) {
            System.out.println("Error loading sold items: " + e.getMessage());
        }

        return sales;
    }
}
