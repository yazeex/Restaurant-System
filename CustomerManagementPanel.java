package restaurant_system;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private CustomerDAO customerDAO;

    public CustomerManagementPanel() {
        customerDAO = new CustomerDAO();

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
        JButton refreshButton = new JButton("Refresh");

        // Add buttons to the button panel
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // Add the button panel to the bottom of the main panel
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> addCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        refreshButton.addActionListener(e -> loadCustomerData());

        // Load customer data initially
        loadCustomerData();
    }

    private void loadCustomerData() {
        List<String[]> customers = customerDAO.getAllCustomers();
        tableModel.setRowCount(0); // Clear existing data
        for (String[] row : customers) {
            tableModel.addRow(row);
        }
    }

    private void addCustomer() {
        String name = JOptionPane.showInputDialog(this, "Enter Customer Name:");
        String phone = JOptionPane.showInputDialog(this, "Enter Customer Phone:");
        String email = JOptionPane.showInputDialog(this, "Enter Customer Email:");

        if (name != null && phone != null && email != null) {
            if (customerDAO.addCustomer(name, phone, email)) {
                JOptionPane.showMessageDialog(this, "Customer added successfully!");
                loadCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add customer.");
            }
        }
    }

    private void editCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int customerId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
            String name = JOptionPane.showInputDialog(this, "Enter Customer Name:", tableModel.getValueAt(selectedRow, 1));
            String phone = JOptionPane.showInputDialog(this, "Enter Customer Phone:", tableModel.getValueAt(selectedRow, 2));
            String email = JOptionPane.showInputDialog(this, "Enter Customer Email:", tableModel.getValueAt(selectedRow, 3));

            if (name != null && phone != null && email != null) {
                if (customerDAO.updateCustomer(customerId, name, phone, email)) {
                    JOptionPane.showMessageDialog(this, "Customer updated successfully!");
                    loadCustomerData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update customer.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.");
        }
    }

    private void deleteCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int customerId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
            if (customerDAO.deleteCustomer(customerId)) {
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                loadCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete customer.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
        }
    }
}
