package restaurant_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationManagementPanelDAO extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Connection connection;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public ReservationManagementPanelDAO() {
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
        String[] columnNames = {"Reservation ID", "Table Number", "Customer Name", "Reservation Time", "Duration"};
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
        JButton addButton = new JButton("Book a Table");
        JButton cancelButton = new JButton("Cancel Reservation");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);

        // Layout components
        add(searchPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> bookTable());
        cancelButton.addActionListener(e -> cancelReservation());
        refreshButton.addActionListener(e -> loadReservationData());

        loadReservationData();
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
    public boolean addReservation(int tableId, int customerId, Timestamp reservationTime, int duration) {
        // Check if the table is already reserved
        if (isTableReserved(tableId, reservationTime, duration)) {
            JOptionPane.showMessageDialog(this, "Table is already reserved during the requested time.");
            return false;
        }

        String query = "INSERT INTO Reservations (table_id, customer_id, reservation_time, duration) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            stmt.setInt(2, customerId);
            stmt.setTimestamp(3, reservationTime);
            stmt.setInt(4, duration);

            // Update table status to "reserved"
            if (stmt.executeUpdate() > 0) {
                updateTableStatus(tableId, "reserved");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error adding reservation: " + e.getMessage());
        }
        return false;
    }

    public boolean cancelReservation(int reservationId) {
        String query = "DELETE FROM Reservations WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);

            // Get the table ID before deleting the reservation
            int tableId = getTableIdFromReservation(reservationId);

            if (stmt.executeUpdate() > 0) {
                // Update table status to "available"
                updateTableStatus(tableId, "available");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error canceling reservation: " + e.getMessage());
        }
        return false;
    }

    private boolean isTableReserved(int tableId, Timestamp reservationTime, int duration) {
        String query = "SELECT * FROM Reservations WHERE table_id = ? AND " +
                       "(reservation_time <= ? AND DATE_ADD(reservation_time, INTERVAL duration MINUTE) >= ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            stmt.setTimestamp(2, new Timestamp(reservationTime.getTime() + duration * 60 * 1000));
            stmt.setTimestamp(3, reservationTime);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If a record exists, the table is reserved
        } catch (SQLException e) {
            System.out.println("Error checking table reservation: " + e.getMessage());
        }
        return false;
    }

    private int getTableIdFromReservation(int reservationId) {
        String query = "SELECT table_id FROM Reservations WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("table_id");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching table ID: " + e.getMessage());
        }
        return -1;
    }

    private void updateTableStatus(int tableId, String status) {
        String query = "UPDATE Tables SET status = ? WHERE table_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, tableId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating table status: " + e.getMessage());
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

    private void bookTable() {
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