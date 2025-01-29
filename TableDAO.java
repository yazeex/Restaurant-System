package restaurant_system;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    private Connection connection;

    public TableDAO() {
        connection = DatabaseConnection.getConnection();
    }

    // Add a new table
    public boolean addTable(String tableNumber, int capacity, String location) {
        String query = "INSERT INTO Tables (table_number, capacity, location, status) VALUES (?, ?, ?, 'available')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tableNumber);
            stmt.setInt(2, capacity);
            stmt.setString(3, location);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding table: " + e.getMessage());
            return false;
        }
    }

    // Edit table details
    public boolean editTable(int tableId, String tableNumber, int capacity, String location) {
        String query = "UPDATE Tables SET table_number = ?, capacity = ?, location = ? WHERE table_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tableNumber);
            stmt.setInt(2, capacity);
            stmt.setString(3, location);
            stmt.setInt(4, tableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error editing table: " + e.getMessage());
            return false;
        }
    }

    // Delete a table
    public boolean deleteTable(int tableId) {
        String query = "DELETE FROM Tables WHERE table_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting table: " + e.getMessage());
            return false;
        }
    }

    // Get all tables
    public List<String[]> getAllTables() {
        List<String[]> tables = new ArrayList<>();
        String query = "SELECT * FROM Tables";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                tables.add(new String[]{
                    String.valueOf(rs.getInt("table_id")),
                    rs.getString("table_number"),
                    String.valueOf(rs.getInt("capacity")),
                    rs.getString("location"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error fetching tables: " + e.getMessage());
        }
        return tables;
    }
}
