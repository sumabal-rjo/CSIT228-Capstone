package com.example.capstone.database;

import com.example.capstone.util.SessionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaHelper {

    private SchemaHelper() {
    }

    public static int getCurrentCompanyId() {
        if (SessionManager.getInstance().isLoggedIn()) {
            return SessionManager.getInstance().getCurrentUser().getCompanyId();
        }
        return 0;
    }

    public static void ensureAppSchema() {
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS companies ("
                    + "company_id INT NOT NULL AUTO_INCREMENT, "
                    + "name VARCHAR(150) NOT NULL UNIQUE, "
                    + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (company_id))");

            ensureColumn(statement, "users", "company_id", "INT NULL AFTER user_id");
            ensureColumn(statement, "users", "email", "VARCHAR(150) AFTER full_name");
            ensureColumn(statement, "categories", "company_id", "INT NULL AFTER category_id");
            ensureColumn(statement, "suppliers", "company_id", "INT NULL AFTER supplier_id");
            ensureColumn(statement, "products", "company_id", "INT NULL AFTER product_id");
            ensureColumn(statement, "sales", "company_id", "INT NULL AFTER sale_id");
            ensureColumn(statement, "sales", "product_id", "INT NULL AFTER user_id");
            ensureColumn(statement, "sales", "product_name", "VARCHAR(150) AFTER product_id");
            ensureColumn(statement, "sales", "quantity_sold", "INT NOT NULL DEFAULT 0 AFTER product_name");
            ensureColumn(statement, "sales", "unit_price", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00 AFTER quantity_sold");
            ensureColumn(statement, "sales", "total_amount", "DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER sale_date");
            removeOldCategoryNameUniqueIndex(statement);
            copyOldSoldItemData(statement);
            fillMissingSaleProductNames(statement);
            ensureSaleProductDeleteRule(statement);
            assignOldRowsToDemoCompany(statement);
        } catch (Exception e) {
            System.out.println("Schema check error: " + e.getMessage());
        }
    }

    private static void ensureColumn(Statement statement, String tableName, String columnName, String columnDefinition) throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() "
                + "AND TABLE_NAME = '" + tableName + "' "
                + "AND COLUMN_NAME = '" + columnName + "'";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next() && resultSet.getInt("total") == 0) {
            statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }

    private static boolean tableExists(Statement statement, String tableName) throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = DATABASE() "
                + "AND TABLE_NAME = '" + tableName + "'";
        ResultSet resultSet = statement.executeQuery(sql);
        return resultSet.next() && resultSet.getInt("total") > 0;
    }

    private static void removeOldCategoryNameUniqueIndex(Statement statement) {
        try {
            statement.executeUpdate("ALTER TABLE categories DROP INDEX name");
        } catch (Exception e) {

        }
    }

    private static void copyOldSoldItemData(Statement statement) throws Exception {
        if (tableExists(statement, "sold_items")) {
            statement.executeUpdate("UPDATE sales s "
                    + "JOIN sold_items si ON s.sale_id = si.sale_id "
                    + "LEFT JOIN products p ON si.product_id = p.product_id "
                    + "SET s.product_id = si.product_id, "
                    + "s.product_name = COALESCE(NULLIF(s.product_name, ''), p.name), "
                    + "s.quantity_sold = si.quantity_sold, "
                    + "s.unit_price = si.unit_price, "
                    + "s.total_amount = si.subtotal "
                    + "WHERE s.product_id IS NULL OR s.product_name IS NULL");
        }

        if (tableExists(statement, "sale_items")) {
            statement.executeUpdate("UPDATE sales s "
                    + "JOIN sale_items si ON s.sale_id = si.sale_id "
                    + "LEFT JOIN products p ON si.product_id = p.product_id "
                    + "SET s.product_id = si.product_id, "
                    + "s.product_name = COALESCE(NULLIF(s.product_name, ''), p.name), "
                    + "s.quantity_sold = si.quantity_sold, "
                    + "s.unit_price = si.unit_price, "
                    + "s.total_amount = si.subtotal "
                    + "WHERE s.product_id IS NULL OR s.product_name IS NULL");
        }
    }

    private static void fillMissingSaleProductNames(Statement statement) throws Exception {
        statement.executeUpdate("UPDATE sales s "
                + "LEFT JOIN products p ON s.product_id = p.product_id "
                + "SET s.product_name = p.name "
                + "WHERE (s.product_name IS NULL OR s.product_name = '') "
                + "AND p.name IS NOT NULL");
    }

    private static void ensureSaleProductDeleteRule(Statement statement) {
        try {
            ResultSet resultSet = statement.executeQuery("SELECT DELETE_RULE FROM information_schema.REFERENTIAL_CONSTRAINTS "
                    + "WHERE CONSTRAINT_SCHEMA = DATABASE() "
                    + "AND TABLE_NAME = 'sales' "
                    + "AND CONSTRAINT_NAME = 'fk_sale_product'");
            if (resultSet.next() && !"SET NULL".equalsIgnoreCase(resultSet.getString("DELETE_RULE"))) {
                statement.executeUpdate("ALTER TABLE sales DROP FOREIGN KEY fk_sale_product");
                statement.executeUpdate("ALTER TABLE sales ADD CONSTRAINT fk_sale_product "
                        + "FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE SET NULL");
            }
        } catch (Exception e) {
            System.out.println("Sale product foreign key check skipped: " + e.getMessage());
        }
    }

    private static void assignOldRowsToDemoCompany(Statement statement) throws Exception {
        statement.executeUpdate("INSERT IGNORE INTO companies (name) VALUES ('CoreStock Demo Company')");
        ResultSet resultSet = statement.executeQuery("SELECT company_id FROM companies WHERE name = 'CoreStock Demo Company'");
        if (!resultSet.next()) {
            return;
        }

        int companyId = resultSet.getInt("company_id");
        statement.executeUpdate("UPDATE users SET company_id = " + companyId + " WHERE company_id IS NULL");
        statement.executeUpdate("UPDATE categories SET company_id = " + companyId + " WHERE company_id IS NULL");
        statement.executeUpdate("UPDATE suppliers SET company_id = " + companyId + " WHERE company_id IS NULL");
        statement.executeUpdate("UPDATE products SET company_id = " + companyId + " WHERE company_id IS NULL");
        statement.executeUpdate("UPDATE sales SET company_id = " + companyId + " WHERE company_id IS NULL");
    }
}
