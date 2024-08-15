package HotelReservationSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HotelReservationSystem {
    private List<Room> rooms = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();
    private List<Payment> payments = new ArrayList<>();
    private DefaultTableModel searchResultsModel;
    private DefaultTableModel bookingsModel;
    private DefaultTableModel paymentsModel;
    private DefaultTableModel roomsTableModel; // Added for room table

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelReservationSystem().createAndShowGUI());
    }

    private void createAndShowGUI() {
        // Initialize rooms
        initializeRooms();

        JFrame frame = new JFrame("Hotel Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Search Rooms", createSearchRoomsPanel());
        tabbedPane.add("Make Reservation", createMakeReservationPanel());
        tabbedPane.add("View Bookings", createViewBookingsPanel());
        tabbedPane.add("Payment Processing", createPaymentProcessingPanel());

        frame.add(tabbedPane);
        frame.setVisible(true);

        // Populate the search results table with all available rooms when the GUI is loaded
        searchRooms(""); // Pass an empty query to show all rooms
    }

    private JPanel createSearchRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");

        // Initialize the table model and JTable
        searchResultsModel = new DefaultTableModel(new String[]{"Room Number", "Type", "Price"}, 0);
        JTable resultsTable = new JTable(searchResultsModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        searchButton.addActionListener(e -> SwingUtilities.invokeLater(() -> searchRooms(searchField.getText())));

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void searchRooms(String query) {
        // Clear the previous results
        searchResultsModel.setRowCount(0);

        // Perform the search
        boolean found = false;
        for (Room room : rooms) {
            if (room.getType().toLowerCase().contains(query.toLowerCase())) {
                searchResultsModel.addRow(new Object[]{room.getNumber(), room.getType(), room.getPrice()});
                found = true;
            }
        }

        // If no rooms are found, display a message
        if (!found && !query.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No rooms found matching your search criteria.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createMakeReservationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel for reservation form
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField nameField = new JTextField(15);
        JTextField roomNumberField = new JTextField(15);
        JTextField checkInField = new JTextField(15);
        JTextField checkOutField = new JTextField(15);
        JButton reserveButton = new JButton("Reserve");

        reserveButton.addActionListener(e -> makeReservation(nameField.getText(), roomNumberField.getText(), checkInField.getText(), checkOutField.getText()));

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Room Number:"));
        formPanel.add(roomNumberField);
        formPanel.add(new JLabel("Check-in Date (YYYY-MM-DD):"));
        formPanel.add(checkInField);
        formPanel.add(new JLabel("Check-out Date (YYYY-MM-DD):"));
        formPanel.add(checkOutField);
        formPanel.add(new JLabel(""));
        formPanel.add(reserveButton);

        // Panel for room table
        JPanel tablePanel = new JPanel(new BorderLayout());
        roomsTableModel = new DefaultTableModel(new String[]{"Room Number", "Type", "Price"}, 0);
        JTable roomsTable = new JTable(roomsTableModel);
        JScrollPane tableScrollPane = new JScrollPane(roomsTable);

        tablePanel.add(new JLabel("Available Rooms:"), BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Populate the room table with available rooms
        refreshRoomsTable();

        // Combine both panels
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private void refreshRoomsTable() {
        // Clear the previous room entries
        roomsTableModel.setRowCount(0);

        // Populate the rooms table with current available rooms
        for (Room room : rooms) {
            roomsTableModel.addRow(new Object[]{room.getNumber(), room.getType(), room.getPrice()});
        }
    }

    private void makeReservation(String name, String roomNumber, String checkIn, String checkOut) {
        // Validate inputs
        if (name.trim().isEmpty() || roomNumber.trim().isEmpty() || checkIn.trim().isEmpty() || checkOut.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields must be filled out.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate name
        if (!name.matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(null, "Name should contain only letters and spaces.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate room number
        Room room = getRoomByNumber(roomNumber);
        if (room == null) {
            JOptionPane.showMessageDialog(null, "Room not found!", "Reservation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate date format and check dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            Date checkInDate = sdf.parse(checkIn);
            Date checkOutDate = sdf.parse(checkOut);

            if (!checkInDate.before(checkOutDate)) {
                JOptionPane.showMessageDialog(null, "Check-in date must be before check-out date.", "Date Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Date format should be YYYY-MM-DD.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // If all validations pass
        bookings.add(new Booking(name, room, checkIn, checkOut));
        rooms.remove(room); // Remove the booked room from available rooms
        JOptionPane.showMessageDialog(null, "Reservation made successfully!");

        // Refresh the search results and view bookings to reflect the changes
        searchRooms(""); // Refresh the room list
        updateBookingsPanel();
        refreshRoomsTable(); // Refresh the rooms table in the Make Reservation panel
    }

    private JPanel createViewBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        bookingsModel = new DefaultTableModel(new String[]{"Name", "Room Number", "Check-in", "Check-out"}, 0);
        JTable bookingsTable = new JTable(bookingsModel);
        JScrollPane scrollPane = new JScrollPane(bookingsTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateBookingsPanel() {
        // Clear the previous bookings
        bookingsModel.setRowCount(0);

        // Populate the bookings table
        for (Booking booking : bookings) {
            bookingsModel.addRow(new Object[]{booking.getName(), booking.getRoom().getNumber(), booking.getCheckIn(), booking.getCheckOut()});
        }
    }

    private JPanel createPaymentProcessingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel for payment form
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        JTextField nameField = new JTextField(15); // Adjusted size
        JTextField cardNumberField = new JTextField(15); // Adjusted size
        JTextField expiryDateField = new JTextField(15); // Adjusted size
        JButton payButton = new JButton("Pay");

        // Initialize table model and JTable
        paymentsModel = new DefaultTableModel(new String[]{"Name", "Card Number", "Expiry Date"}, 0);
        JTable paymentsTable = new JTable(paymentsModel);
        JScrollPane tableScrollPane = new JScrollPane(paymentsTable);

        payButton.addActionListener(e -> processPayment(nameField.getText(), cardNumberField.getText(), expiryDateField.getText()));

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Card Number:"));
        formPanel.add(cardNumberField);
        formPanel.add(new JLabel("Expiry Date (MM/YY):"));
        formPanel.add(expiryDateField);
        formPanel.add(new JLabel(""));
        formPanel.add(payButton);

        // Add form and table to panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JLabel("Payment Transactions:"), BorderLayout.CENTER);
        panel.add(tableScrollPane, BorderLayout.SOUTH);

        // Populate the payments table with existing transactions
        updatePaymentsPanel();

        return panel;
    }

    private void processPayment(String name, String cardNumber, String expiryDate) {
        // Validate inputs
        if (name.trim().isEmpty() || cardNumber.trim().isEmpty() || expiryDate.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields must be filled out.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Simple payment logic
        payments.add(new Payment(name, cardNumber, expiryDate));
        JOptionPane.showMessageDialog(null, "Payment processed successfully!");

        // Refresh the payments table to reflect the new transaction
        updatePaymentsPanel();
    }

    private void updatePaymentsPanel() {
        // Clear the previous payments
        paymentsModel.setRowCount(0);

        // Populate the payments table
        for (Payment payment : payments) {
            paymentsModel.addRow(new Object[]{payment.getName(), payment.getCardNumber(), payment.getExpiryDate()});
        }
    }

    private Room getRoomByNumber(String number) {
        for (Room room : rooms) {
            if (room.getNumber().equals(number)) {
                return room;
            }
        }
        return null;
    }

    private void initializeRooms() {
        // Initial room data
        rooms.add(new Room("101", "Standard", 100));
        rooms.add(new Room("102", "Deluxe", 150));
        rooms.add(new Room("103", "Suite", 200));
        rooms.add(new Room("104", "Presidential Suite", 500));
        rooms.add(new Room("105", "Economy", 75));
        rooms.add(new Room("106", "Family Room", 250));

        // Adding more rooms for testing
        for (int i = 107; i <= 156; i++) {
            rooms.add(new Room(String.format("%03d", i), "Room Type " + i, Math.round(Math.random() * 500) + 50));
        }
    }

    // Room class
    class Room {
        private String number;
        private String type;
        private double price;

        public Room(String number, String type, double price) {
            this.number = number;
            this.type = type;
            this.price = price;
        }

        public String getNumber() {
            return number;
        }

        public String getType() {
            return type;
        }

        public double getPrice() {
            return price;
        }
    }

    // Booking class
    class Booking {
        private String name;
        private Room room;
        private String checkIn;
        private String checkOut;

        public Booking(String name, Room room, String checkIn, String checkOut) {
            this.name = name;
            this.room = room;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        public String getName() {
            return name;
        }

        public Room getRoom() {
            return room;
        }

        public String getCheckIn() {
            return checkIn;
        }

        public String getCheckOut() {
            return checkOut;
        }
    }

    // Payment class
    class Payment {
        private String name;
        private String cardNumber;
        private String expiryDate;

        public Payment(String name, String cardNumber, String expiryDate) {
            this.name = name;
            this.cardNumber = cardNumber;
            this.expiryDate = expiryDate;
        }

        public String getName() {
            return name;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public String getExpiryDate() {
            return expiryDate;
        }
    }
}
