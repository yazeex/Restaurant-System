package restaurant_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerManagementPanelDAO extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Connection connection;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public CustomerManagementPanelDAO() {
        connection = DatabaseConnection.getConnection();
        setLayout(new BorderLayout());

        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        JLabel searchLabel = new JLabel("Search: ");
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Table columns
        String[] columnNames = {"Customer ID", "Name", "Phone", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        
        // Add row sorter for filtering
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        // Add search functionality
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        JScrollPane tableScrollPane = new JScrollPane(table);

        // Create buttons panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Customer");
        JButton editButton = new JButton("Edit Customer");
        JButton deleteButton = new JButton("Delete Customer");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // Layout components
        add(searchPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> addCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        refreshButton.addActionListener(e -> loadCustomerData());

        loadCustomerData();
    }

    private void search() {
        String text = searchField.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Search across all columns
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
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