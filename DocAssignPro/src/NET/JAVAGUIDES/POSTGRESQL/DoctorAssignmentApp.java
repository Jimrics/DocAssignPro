package NET.JAVAGUIDES.POSTGRESQL;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.PriorityQueue;

class MedicalProfessional implements Comparable<MedicalProfessional> {
    String name;
    int priority;

    public MedicalProfessional(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    @Override
    public int compareTo(MedicalProfessional other) {
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString() {
        return name;
    }
}

@SuppressWarnings("serial")
public class DoctorAssignmentApp extends JFrame {

    private PriorityQueue<MedicalProfessional> availableDoctors;
    private DefaultTableModel tableModel;
    private JTextField patientNameField;
    private JButton assignButton;

    // JDBC connection parameters
    private static final String DB_URL = "jdbc:postgresql://localhost/test1";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public DoctorAssignmentApp() {
        availableDoctors = new PriorityQueue<>();
        tableModel = new DefaultTableModel(new Object[]{"Patient", "Assigned Doctor"}, 0);

        // Adding some initial medical professionals for testing
        availableDoctors.add(new MedicalProfessional("Dr. Smith", 1));
        availableDoctors.add(new MedicalProfessional("Dr. Johnson", 2));
        availableDoctors.add(new MedicalProfessional("Dr. Davis", 3));

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Doctor Assignment App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        assignButton = new JButton("Assign Medical Professional");
        assignButton.addActionListener(e -> assignMedicalProfessional());

        patientNameField = new JTextField(20);

        JTable assignmentTable = new JTable(tableModel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(100, 149, 237));
        JLabel headerLabel = new JLabel("Doctor Assignment App");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(headerLabel);

        // Input Panel
        JPanel inputPanel = createInputPanel();
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table Panel
        JScrollPane tableScrollPane = new JScrollPane(assignmentTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Add components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(assignButton, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.WEST);
        panel.add(tableScrollPane, BorderLayout.SOUTH);

        add(panel);
        setLocationRelativeTo(null);

        // Create the 'patient_assignments' table
        createPatientAssignmentsTable();
    }

    private void createPatientAssignmentsTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS patient_assignments ("
                    + "id SERIAL PRIMARY KEY, "
                    + "patient_name VARCHAR(255) NOT NULL, "
                    + "assigned_doctor VARCHAR(255) NOT NULL)";
            
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createTableQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showWarningMessage("Error creating the 'patient_assignments' table.", "Database Error");
        }
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Enter Patient Name: "), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(patientNameField, gbc);

        return inputPanel;
    }

    private void assignMedicalProfessional() {
        String patientName = patientNameField.getText().trim();

        if (patientName.isEmpty()) {
            showWarningMessage("Please enter the patient's name.", "Missing Information");
            return;
        }

        if (availableDoctors.isEmpty()) {
            showMessage("No available medical professionals at the moment.", "No Professional Available", JOptionPane.INFORMATION_MESSAGE);
        } else {
            MedicalProfessional assignedMedicalProfessional = availableDoctors.poll();
            showMessage("Assigned Medical Professional: " + assignedMedicalProfessional + " to Patient: " + patientName,
                    "Medical Professional Assigned", JOptionPane.INFORMATION_MESSAGE);

            // Insert assignment into the database
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String insertQuery = "INSERT INTO patient_assignments (patient_name, assigned_doctor) VALUES (?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, patientName);
                    preparedStatement.setString(2, assignedMedicalProfessional.toString());
                    preparedStatement.executeUpdate();
                }

                // Update the UI table
                tableModel.addRow(new Object[]{patientName, assignedMedicalProfessional.toString()});
                updateAssignButtonState();

            } catch (SQLException e) {
                e.printStackTrace();
                showWarningMessage("Error connecting to the database.", "Database Error");
            }
        }
    }

    private void showWarningMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void updateAssignButtonState() {
        assignButton.setEnabled(!availableDoctors.isEmpty());
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new DoctorAssignmentApp().setVisible(true));
    }
}
