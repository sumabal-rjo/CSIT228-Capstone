package com.example.capstone.database;

import com.example.capstone.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements GenericDAO<Product> {

    private static final String PRODUCT_SELECT =
            "SELECT p.*, c.name AS category_name, s.name AS supplier_name "
                    + "FROM products p "
                    + "LEFT JOIN categories c ON p.category_id = c.category_id "
                    + "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id ";

    public ProductDAO() {
        SchemaHelper.ensureAppSchema();
    }

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    private int currentCompanyId() {
        return SchemaHelper.getCurrentCompanyId();
    }

    @Override
    public boolean add(Product product) {
        String sql = "INSERT INTO products "
                + "(company_id, category_id, supplier_id, name, description, quantity, price, low_stock_threshold) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, currentCompanyId());
            setCategoryAndSupplier(statement, product, 2);
            statement.setString(4, product.getName());
            statement.setString(5, product.getDescription());
            statement.setInt(6, product.getQuantity());
            statement.setDouble(7, product.getPrice());
            statement.setInt(8, product.getLowStockThreshold());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET category_id = ?, supplier_id = ?, name = ?, description = ?, "
                + "quantity = ?, price = ?, low_stock_threshold = ? "
                + "WHERE product_id = ? AND company_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            setCategoryAndSupplier(statement, product, 1);
            statement.setString(3, product.getName());
            statement.setString(4, product.getDescription());
            statement.setInt(5, product.getQuantity());
            statement.setDouble(6, product.getPrice());
            statement.setInt(7, product.getLowStockThreshold());
            statement.setInt(8, product.getProductId());
            statement.setInt(9, currentCompanyId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE product_id = ? AND company_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            statement.setInt(2, currentCompanyId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Product> getAll() {
        return findProducts("WHERE p.company_id = ? ORDER BY p.name", currentCompanyId());
    }

    @Override
    public Product getById(int id) {
        List<Product> products = findProducts(
                "WHERE p.product_id = ? AND p.company_id = ?",
                id,
                currentCompanyId()
        );

        if (products.isEmpty()) {
            return null;
        }

        return products.get(0);
    }

    public List<Product> search(String keyword) {
        String searchText = "%" + keyword + "%";
        return findProducts(
                "WHERE p.company_id = ? AND (p.name LIKE ? OR p.description LIKE ?) ORDER BY p.name",
                currentCompanyId(),
                searchText,
                searchText
        );
    }

    public List<Product> searchWithFilters(String keyword, Integer categoryId, String stockFilter) {
        String searchText = "%" + keyword + "%";
        String sql = "WHERE p.company_id = ? "
                + "AND (p.name LIKE ? OR p.description LIKE ? OR c.name LIKE ? OR s.name LIKE ?) ";

        List<Object> values = new ArrayList<>();
        values.add(currentCompanyId());
        values.add(searchText);
        values.add(searchText);
        values.add(searchText);
        values.add(searchText);

        if (categoryId != null && categoryId > 0) {
            sql += "AND p.category_id = ? ";
            values.add(categoryId);
        }

        if ("LOW".equalsIgnoreCase(stockFilter)) {
            sql += "AND p.quantity <= p.low_stock_threshold ";
        } else if ("AVAILABLE".equalsIgnoreCase(stockFilter)) {
            sql += "AND p.quantity > 0 ";
        } else if ("OUT".equalsIgnoreCase(stockFilter)) {
            sql += "AND p.quantity = 0 ";
        }

        return findProducts(sql + "ORDER BY p.name", values.toArray());
    }

    public List<Product> getLowStock() {
        return findProducts(
                "WHERE p.company_id = ? AND p.quantity <= p.low_stock_threshold ORDER BY p.quantity",
                currentCompanyId()
        );
    }

    public synchronized boolean adjustStock(int productId, int changeAmount, String note, Integer userId) {
        Product product = getById(productId);

        if (product == null) {
            return false;
        }

        int oldQuantity = product.getQuantity();
        int newQuantity = oldQuantity + changeAmount;

        if (newQuantity < 0) {
            return false;
        }

        try {
            updateStockQuantity(productId, newQuantity);
            saveStockLog(productId, userId, oldQuantity, changeAmount, newQuantity, note);
            return true;
        } catch (Exception e) {
            System.out.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    public int[] getSummary() {
        int[] summary = new int[2];
        String sql = "SELECT COUNT(*) AS total_skus, "
                + "SUM(quantity <= low_stock_threshold) AS low_stock_count "
                + "FROM products WHERE company_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, currentCompanyId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                summary[0] = resultSet.getInt("total_skus");
                summary[1] = resultSet.getInt("low_stock_count");
            }
        } catch (Exception e) {
            System.out.println("Error loading summary: " + e.getMessage());
        }

        return summary;
    }

    public double getTotalValue() {
        String sql = "SELECT SUM(quantity * price) AS total_value FROM products WHERE company_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, currentCompanyId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("total_value");
            }
        } catch (Exception e) {
            System.out.println("Error computing total value: " + e.getMessage());
        }

        return 0;
    }

    private List<Product> findProducts(String whereClause, Object... values) {
        List<Product> products = new ArrayList<>();

        try {
            PreparedStatement statement = getConnection().prepareStatement(PRODUCT_SELECT + whereClause);
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                products.add(readProduct(resultSet));
            }
        } catch (Exception e) {
            System.out.println("Error loading products: " + e.getMessage());
        }

        return products;
    }

    private void updateStockQuantity(int productId, int newQuantity) throws Exception {
        String sql = "UPDATE products SET quantity = ? WHERE product_id = ? AND company_id = ?";

        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setInt(1, newQuantity);
        statement.setInt(2, productId);
        statement.setInt(3, currentCompanyId());
        statement.executeUpdate();
    }

    private void saveStockLog(int productId, Integer userId, int oldQuantity, int changeAmount,
                              int newQuantity, String note) throws Exception {
        String sql = "INSERT INTO inventory_logs "
                + "(product_id, user_id, change_type, quantity_before, quantity_change, quantity_after, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setInt(1, productId);

        if (userId == null) {
            statement.setNull(2, Types.INTEGER);
        } else {
            statement.setInt(2, userId);
        }

        statement.setString(3, changeAmount >= 0 ? "add" : "remove");
        statement.setInt(4, oldQuantity);
        statement.setInt(5, changeAmount);
        statement.setInt(6, newQuantity);
        statement.setString(7, note);
        statement.executeUpdate();
    }

    private Product readProduct(ResultSet resultSet) throws Exception {
        Product product = new Product();
        product.setProductId(resultSet.getInt("product_id"));
        product.setCompanyId(resultSet.getInt("company_id"));
        product.setCategoryId(resultSet.getInt("category_id"));
        product.setSupplierId(resultSet.getInt("supplier_id"));
        product.setName(resultSet.getString("name"));
        product.setDescription(resultSet.getString("description"));
        product.setQuantity(resultSet.getInt("quantity"));
        product.setPrice(resultSet.getDouble("price"));
        product.setLowStockThreshold(resultSet.getInt("low_stock_threshold"));
        product.setCategoryName(resultSet.getString("category_name"));
        product.setSupplierName(resultSet.getString("supplier_name"));
        return product;
    }

    private void setCategoryAndSupplier(PreparedStatement statement, Product product, int startIndex) throws Exception {
        if (product.getCategoryId() > 0) {
            statement.setInt(startIndex, product.getCategoryId());
        } else {
            statement.setNull(startIndex, Types.INTEGER);
        }

        if (product.getSupplierId() > 0) {
            statement.setInt(startIndex + 1, product.getSupplierId());
        } else {
            statement.setNull(startIndex + 1, Types.INTEGER);
        }
    }
}
