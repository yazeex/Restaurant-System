package restaurant_system;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private Connection connection;

    public ReservationDAO() {
        connection = DatabaseConnection.getConnection();
    }

    // Add a reservation
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

    // Cancel a reservation
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

    // Get all reservations
    public List<String[]> getReservations() {
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
}
