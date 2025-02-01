package aaaaaa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportingPanel extends JPanel 
{
    private Connection connection;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeComboBox;
    private JPanel chartPanel;

    public ReportingPanel() {
        connection = DatabaseConnection.getConnection();
        setLayout(new BorderLayout());

        // Create top panel for controls
        JPanel controlPanel = new JPanel();
        reportTypeComboBox = new JComboBox<>(new String[]{
            "Daily Reservations",
            "Table Utilization",
            "Customer Statistics"
          
        });
        
        JButton generateButton = new JButton("Generate Report");
        
        controlPanel.add(new JLabel("Report Type:"));
        controlPanel.add(reportTypeComboBox);
        controlPanel.add(generateButton);

        // Create center panel for report display
        JPanel centerPanel = new JPanel(new BorderLayout());
        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        
        chartPanel = new JPanel();
        chartPanel.setPreferredSize(new Dimension(400, 300));
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(chartPanel, BorderLayout.SOUTH);

        // Add panels to main panel
        add(controlPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // Add action listeners
        generateButton.addActionListener(e -> generateReport());        
        // Generate initial report
        generateReport();
    }

    private void generateReport() {
        String selectedReport = (String) reportTypeComboBox.getSelectedItem();
        switch (selectedReport) {
            case "Daily Reservations":
                generateDailyReservationsReport();
                break;
            case "Table Utilization":
                generateTableUtilizationReport();
                break;
            case "Customer Statistics":
                generateCustomerStatisticsReport();
                break;
           
        }
    }

    private void generateDailyReservationsReport() {
        String query = "SELECT DATE(reservation_time) as date, COUNT(*) as reservation_count " +
                      "FROM Reservations " +
                      "GROUP BY DATE(reservation_time) " +
                      "ORDER BY date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            // Set up table columns
            tableModel.setColumnCount(0);
            tableModel.addColumn("Date");
            tableModel.addColumn("Number of Reservations");
            
            // Clear existing rows
            tableModel.setRowCount(0);
            
            // Add data to table
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getDate("date"),
                    rs.getInt("reservation_count")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating daily reservations report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateTableUtilizationReport() {
        String query = "SELECT t.table_number, t.capacity, " +
                      "COUNT(r.reservation_id) as reservation_count, " +
                      "AVG(r.duration) as avg_duration " +
                      "FROM Tables t " +
                      "LEFT JOIN Reservations r ON t.table_id = r.table_id " +
                      "GROUP BY t.table_id, t.table_number, t.capacity";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableModel.setColumnCount(0);
            tableModel.addColumn("Table Number");
            tableModel.addColumn("Capacity");
            tableModel.addColumn("Total Reservations");
            tableModel.addColumn("Average Duration (min)");
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("table_number"),
                    rs.getInt("capacity"),
                    rs.getInt("reservation_count"),
                    String.format("%.1f", rs.getDouble("avg_duration"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error generating table utilization report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateCustomerStatisticsReport() {
        String query = "SELECT c.name, COUNT(r.reservation_id) as reservation_count, " +
                      "MAX(r.reservation_time) as last_visit " +
                      "FROM Customers c " +
                      "LEFT JOIN Reservations r ON c.customer_id = r.customer_id " +
                      "GROUP BY c.customer_id, c.name " +
                      "ORDER BY reservation_count DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableModel.setColumnCount(0);
            tableModel.addColumn("Customer Name");
            tableModel.addColumn("Total Reservations");
            tableModel.addColumn("Last Visit");
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getInt("reservation_count"),
                    rs.getTimestamp("last_visit")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error generating customer statistics report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}