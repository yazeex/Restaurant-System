package restaurant_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationManagementPanelDAO extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Connection connection;

    public ReservationManagementPanelDAO() {
        connection = DatabaseConnection.getConnection();
        
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

    // Database operations
    public boolean addReservation(int tableId, int customerId, Timestamp reservationTime, int duration) {
        String query = "INSERT INTO Reservations (table_id, customer_id, reservation_time, duration) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            stmt.setInt(2, customerId);
            stmt.setTimestamp(3, reservationTime);
            stmt.setInt(4, duration);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding reservation: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelReservation(int reservationId) {
        String query = "DELETE FROM Reservations WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error canceling reservation: " + e.getMessage());
            return false;
        }
    }

    private List<String[]> getReservations() {
        List<String[]> reservations = new ArrayList<>();
        String query = "SELECT r.reservation_id, t.table_number, c.name, r.reservation_time, r.duration " +
                      "FROM Reservations r " +
                      "JOIN Tables t ON r.table_id = t.table_id " +
                      "JOIN Customers c ON r.customer_id = c.customer_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                reservations.add(new String[]{
                    String.valueOf(rs.getInt("reservation_id")),
                    rs.getString("table_number"),
                    rs.getString("name"),
                    rs.getTimestamp("reservation_time").toString(),
                    String.valueOf(rs.getInt("duration"))
                });
            }
        } catch (SQLException e) {
            System.out.println("Error fetching reservations: " + e.getMessage());
        }
        return reservations;
    }

    // UI operations
    private void loadReservationData() {
        List<String[]> reservations = getReservations();
        tableModel.setRowCount(0); // Clear existing data
        for (String[] row : reservations) {
            tableModel.addRow(row);
        }
    }

    private void bookTable() throws NumberFormatException {
        String tableIdStr = JOptionPane.showInputDialog(this, "Enter Table ID:");
        String customerIdStr = JOptionPane.showInputDialog(this, "Enter Customer ID:");
        String reservationTimeStr = JOptionPane.showInputDialog(this, "Enter Reservation Time (yyyy-MM-dd HH:mm:ss):");
        String durationStr = JOptionPane.showInputDialog(this, "Enter Duration (minutes):");

        if (tableIdStr != null && customerIdStr != null && reservationTimeStr != null && durationStr != null) {
            try {
                int tableId = Integer.parseInt(tableIdStr);
                int customerId = Integer.parseInt(customerIdStr);
                Timestamp reservationTime = Timestamp.valueOf(reservationTimeStr);
                int duration = Integer.parseInt(durationStr);

                if (addReservation(tableId, customerId, reservationTime, duration)) {
                    JOptionPane.showMessageDialog(this, "Reservation added successfully!");
                    loadReservationData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add reservation.");
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Invalid input format. Please check your entries.");
            }
        }
    }

    private void cancelReservation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int reservationId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
            if (cancelReservation(reservationId)) {
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