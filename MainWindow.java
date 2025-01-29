package restaurant_system;

import javax.swing.*;

public class MainWindow extends JFrame 
{
    public MainWindow() 
    {
        // Set the title of the main window
        setTitle("Restaurant Table Reservation System");
        // Set the size of the window
        setSize(800, 600);
        // Close the application when the window is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        // Add tabs for each module
        tabbedPane.addTab("Table Management", new TableManagementPanel());
        tabbedPane.addTab("Reservation Management", new ReservationManagementPanel());
        tabbedPane.addTab("Customer Management", new CustomerManagementPanel());
        //tabbedPane.addTab("Reporting", new ReportingPanel());
        // Add the tabbed pane to the main window
        add(tabbedPane);
        // Center the window on the screen
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) 
    {
        // Create and show the main window
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}