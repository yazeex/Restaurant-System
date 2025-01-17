package restaurant_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TableManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableDAO tableDAO;

    public TableManagementPanel() {
        tableDAO = new TableDAO();

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
        JButton refreshButton = new JButton("Refresh");

        // Add buttons to the button panel
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // Add the button panel to the bottom of the main panel
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> addTable());
        editButton.addActionListener(e -> editTable());
        deleteButton.addActionListener(e -> deleteTable());
        refreshButton.addActionListener(e -> loadTableData());

        // Load table data initially
        loadTableData();
    }

    private void loadTableData() {
        List<String[]> tables = tableDAO.getAllTables();
        tableModel.setRowCount(0); // Clear existing data
        for (String[] row : tables) {
            tableModel.addRow(row);
        }
    }

    private void addTable() {
        String tableNumber = JOptionPane.showInputDialog(this, "Enter Table Number:");
        String capacityStr = JOptionPane.showInputDialog(this, "Enter Capacity:");
        String location = JOptionPane.showInputDialog(this, "Enter Location:");

        if (tableNumber != null && capacityStr != null && location != null) {
            int capacity = Integer.parseInt(capacityStr);
            if (tableDAO.addTable(tableNumber, capacity, location)) {
                JOptionPane.showMessageDialog(this, "Table added successfully!");
                loadTableData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add table.");
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
                int capacity = Integer.parseInt(capacityStr);
                if (tableDAO.editTable(tableId, tableNumber, capacity, location)) {
                    JOptionPane.showMessageDialog(this, "Table updated successfully!");
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update table.");
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
            if (tableDAO.deleteTable(tableId)) {
                JOptionPane.showMessageDialog(this, "Table deleted successfully!");
                loadTableData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete table.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a table to delete.");
        }
    }
}

