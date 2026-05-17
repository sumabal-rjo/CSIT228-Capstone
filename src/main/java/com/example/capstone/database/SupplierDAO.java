package com.example.capstone.database;

import com.example.capstone.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO implements GenericDAO<Supplier> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public boolean add(Supplier supplier) {
        SchemaHelper.ensureAppSchema();

        String sql = "INSERT INTO suppliers (company_id, name, contact_name, phone, email, address) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, SchemaHelper.getCurrentCompanyId());
            statement.setString(2, supplier.getName());
            statement.setString(3, supplier.getContactName());
            statement.setString(4, supplier.getPhone());
            statement.setString(5, supplier.getEmail());
            statement.setString(6, supplier.getAddress());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error adding supplier: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Supplier supplier) {
        SchemaHelper.ensureAppSchema();

        String sql = "UPDATE suppliers SET name = ?, contact_name = ?, phone = ?, email = ?, address = ? WHERE supplier_id = ? AND company_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getContactName());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getAddress());
            statement.setInt(6, supplier.getSupplierId());
            statement.setInt(7, SchemaHelper.getCurrentCompanyId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating supplier: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        SchemaHelper.ensureAppSchema();

        String sql = "DELETE FROM suppliers WHERE supplier_id = ? AND company_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            statement.setInt(2, SchemaHelper.getCurrentCompanyId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting supplier: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Supplier> getAll() {
        List<Supplier> suppliers = new ArrayList<>();
        SchemaHelper.ensureAppSchema();
        String sql = "SELECT * FROM suppliers WHERE company_id = ? ORDER BY name";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, SchemaHelper.getCurrentCompanyId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Supplier supplier = new Supplier();
                supplier.setSupplierId(resultSet.getInt("supplier_id"));
                supplier.setCompanyId(resultSet.getInt("company_id"));
                supplier.setName(resultSet.getString("name"));
                supplier.setContactName(resultSet.getString("contact_name"));
                supplier.setPhone(resultSet.getString("phone"));
                supplier.setEmail(resultSet.getString("email"));
                supplier.setAddress(resultSet.getString("address"));
                suppliers.add(supplier);
            }
        } catch (Exception e) {
            System.out.println("Error loading suppliers: " + e.getMessage());
        }

        return suppliers;
    }

    @Override
    public Supplier getById(int id) {
        SchemaHelper.ensureAppSchema();

        String sql = "SELECT * FROM suppliers WHERE supplier_id = ? AND company_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            statement.setInt(2, SchemaHelper.getCurrentCompanyId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Supplier supplier = new Supplier();
                supplier.setSupplierId(resultSet.getInt("supplier_id"));
                supplier.setCompanyId(resultSet.getInt("company_id"));
                supplier.setName(resultSet.getString("name"));
                supplier.setContactName(resultSet.getString("contact_name"));
                supplier.setPhone(resultSet.getString("phone"));
                supplier.setEmail(resultSet.getString("email"));
                supplier.setAddress(resultSet.getString("address"));
                return supplier;
            }
        } catch (Exception e) {
            System.out.println("Error finding supplier: " + e.getMessage());
        }

        return null;
    }
}
