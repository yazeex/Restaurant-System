package aaaaaa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableManagementPanelDAO extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Connection connection;

    public TableManagementPanelDAO() {
        connection = DatabaseConnection.getConnection();

        // Set layout for the panel
        setLayout(new BorderLayout());

        // Table columns
        String[] columnNames = {"Table ID", "Table Number", "Capacity", "Location", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Add the table to the panel
        add(tableScrollPane, BorderLayout.CENTER);

        // Create buttons for actions
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Table");
        JButton editButton = new JButton("Edit Table");
        JButton deleteButton = new JButton("Delete Table");
        JButton refreshButton = new JButton("Refresh"); // Add Refresh button

        // Add buttons to the button panel
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton); // Add Refresh button to the panel

        // Add the button panel to the bottom of the main panel
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> addTable());
        editButton.addActionListener(e -> editTable());
        deleteButton.addActionListener(e -> deleteTable());
        refreshButton.addActionListener(e -> loadTableData()); // Refresh table data

        // Load table data initially
        loadTableData();
    }

    // Database Operations
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

    private List<String[]> getAllTables() {
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
                    rs.getString("status") // Fetch the status from the database
                });
            }
        } catch (SQLException e) {
            System.out.println("Error fetching tables: " + e.getMessage());
        }
        return tables;
    }

    // UI Operations
    private void loadTableData() {
        List<String[]> tables = getAllTables();
        tableModel.setRowCount(0); // Clear existing data
        for (String[] row : tables) {
            tableModel.addRow(row);
        }
    }

    private void addTable() {
        String tableNumber = JOptionPane.showInputDialog(this, "Enter Table Number:");
        if (tableNumber != null) {
            if (tableNumberExists(tableNumber)) {
                JOptionPane.showMessageDialog(this, "Table number already exists. Please enter a unique table number.");
                return;
            }

            String capacityStr = JOptionPane.showInputDialog(this, "Enter Capacity:");
            String location = JOptionPane.showInputDialog(this, "Enter Location:");

            if (capacityStr != null && location != null) {
                try {
                    int capacity = Integer.parseInt(capacityStr);
                    if (addTable(tableNumber, capacity, location)) {
                        JOptionPane.showMessageDialog(this, "Table added successfully!");
                        loadTableData(); // Refresh table data after adding
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to add table.");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid capacity value. Please enter a number.");
                }
            }
        }
    }

    private void editTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int tableId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
            String tableNumber = JOptionPane.showInputDialog(this, "Enter Table Number:", tableModel.getValueAt(selectedRow, 1));
            String capacityStr = JOptionPane.showInputDialog(this, "Enter Capacity:", tableModel.getValueAt(selectedRow, 2));
            String location = JOptionPane.showInputDialog(this, "Enter Location:", tableModel.getValueAt(selectedRow, 3));

            if (tableNumber != null && capacityStr != null && location != null) {
                try {
                    int capacity = Integer.parseInt(capacityStr);
                    if (editTable(tableId, tableNumber, capacity, location)) {
                        JOptionPane.showMessageDialog(this, "Table updated successfully!");
                        loadTableData(); // Refresh table data after editing
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update table.");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid capacity value. Please enter a number.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a table to edit.");
        }
    }

    private void deleteTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int tableId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
            int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this table?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                if (deleteTable(tableId)) {
                    JOptionPane.showMessageDialog(this, "Table deleted successfully!");
                    loadTableData(); // Refresh table data after deleting
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete table.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a table to delete.");
        }
    }

    public boolean tableNumberExists(String tableNumber) {
        String query = "SELECT COUNT(*) FROM Tables WHERE table_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tableNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking table number: " + e.getMessage());
        }
        return false;
    }
}