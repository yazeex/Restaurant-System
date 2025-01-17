package restaurant_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class ReservationManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private ReservationDAO reservationDAO;

    public ReservationManagementPanel() {
        reservationDAO = new ReservationDAO();

        // Set layout for the panel
        setLayout(new BorderLayout());

        // Table columns
        String[] columnNames = {"Reservation ID", "Table Number", "Customer Name", "Reservation Time", "Duration"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Add the table to the panel
        add(tableScrollPane, BorderLayout.CENTER);

        // Create buttons for actions
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Book a Table");
        JButton cancelButton = new JButton("Cancel Reservation");
        JButton refreshButton = new JButton("Refresh");

        // Add buttons to the button panel
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);

        // Add the button panel to the bottom of the main panel
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> bookTable());
        cancelButton.addActionListener(e -> cancelReservation());
        refreshButton.addActionListener(e -> loadReservationData());

        // Load reservation data initially
        loadReservationData();
    }

    private void loadReservationData() {
        List<String[]> reservations = reservationDAO.getReservations();
        tableModel.setRowCount(0); // Clear existing data
        for (String[] row : reservations) {
            tableModel.addRow(row);
        }
    }

    private void bookTable() {
        String tableIdStr = JOptionPane.showInputDialog(this, "Enter Table ID:");
        String customerIdStr = JOptionPane.showInputDialog(this, "Enter Customer ID:");
        String reservationTimeStr = JOptionPane.showInputDialog(this, "Enter Reservation Time (yyyy-MM-dd HH:mm:ss):");
        String durationStr = JOptionPane.showInputDialog(this, "Enter Duration (minutes):");

        if (tableIdStr != null && customerIdStr != null && reservationTimeStr != null && durationStr != null) {
            int tableId = Integer.parseInt(tableIdStr);
            int customerId = Integer.parseInt(customerIdStr);
            Timestamp reservationTime = Timestamp.valueOf(reservationTimeStr);
            int duration = Integer.parseInt(durationStr);

            if (reservationDAO.addReservation(tableId, customerId, reservationTime, duration)) {
                JOptionPane.showMessageDialog(this, "Reservation added successfully!");
                loadReservationData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add reservation.");
            }
        }
    }

    private void cancelReservation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int reservationId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
            if (reservationDAO.cancelReservation(reservationId)) {
                JOptionPane.showMessageDialog(this, "Reservation canceled successfully!");
                loadReservationData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel reservation.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.");
        }
    }
}
