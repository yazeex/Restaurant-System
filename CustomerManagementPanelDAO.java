package aaaaaa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerManagementPanelDAO extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Connection connection;

    public CustomerManagementPanelDAO() {
        connection = DatabaseConnection.getConnection();

        // Set layout for the panel
        setLayout(new BorderLayout());

        // Table columns
        String[] columnNames = {"Customer ID", "Name", "Phone", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Add the table to the panel
        add(tableScrollPane, BorderLayout.CENTER);

        // Create buttons for actions
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Customer");
        JButton editButton = new JButton("Edit Customer");
        JButton deleteButton = new JButton("Delete Customer");

        // Add buttons to the button panel
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // Add the button panel to the bottom of the main panel
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> addCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        // Load customer data initially
        loadCustomerData();
    }

    // Database operations
    public boolean addCustomer(String name, String phone, String email) {
        String query = "INSERT INTO Customers (name, phone, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding customer: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCustomer(int customerId, String name, String phone, String email) {
        String query = "UPDATE Customers SET name = ?, phone = ?, email = ? WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setInt(4, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating customer: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCustomer(int customerId) {
        String query = "DELETE FROM Customers WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    private List<String[]> getAllCustomers() {
        List<String[]> customers = new ArrayList<>();
        String query = "SELECT * FROM Customers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                customers.add(new String[]{
                    String.valueOf(rs.getInt("customer_id")),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("email")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error fetching customers: " + e.getMessage());
        }
        return customers;
    }

    // UI operations
    private void loadCustomerData() {
        List<String[]> customers = getAllCustomers();
        tableModel.setRowCount(0); // Clear existing data
        for (String[] row : customers) {
            tableModel.addRow(row);
        }
    }

    private void addCustomer() {
        String name = JOptionPane.showInputDialog(this, "Enter Customer Name:");
        String phone = JOptionPane.showInputDialog(this, "Enter Customer Phone:");
        String email = JOptionPane.showInputDialog(this, "Enter Customer Email:");

        if (name != null && phone != null && email != null && 
            !name.trim().isEmpty() && !phone.trim().isEmpty() && !email.trim().isEmpty()) {
            if (addCustomer(name, phone, email)) {
                JOptionPane.showMessageDialog(this, "Customer added successfully!");
                loadCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add customer.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "All fields are required.");
        }
    }

    private void editCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int customerId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
            String name = JOptionPane.showInputDialog(this, "Enter Customer Name:", 
                         tableModel.getValueAt(selectedRow, 1));
            String phone = JOptionPane.showInputDialog(this, "Enter Customer Phone:", 
                         tableModel.getValueAt(selectedRow, 2));
            String email = JOptionPane.showInputDialog(this, "Enter Customer Email:", 
                         tableModel.getValueAt(selectedRow, 3));

            if (name != null && phone != null && email != null && 
                !name.trim().isEmpty() && !phone.trim().isEmpty() && !email.trim().isEmpty()) {
                if (updateCustomer(customerId, name, phone, email)) {
                    JOptionPane.showMessageDialog(this, "Customer updated successfully!");
                    loadCustomerData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update customer.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "All fields are required.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.");
        }
    }

    private void deleteCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int customerId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this customer?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (deleteCustomer(customerId)) {
                    JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                    loadCustomerData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete customer.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
        }
    }
}