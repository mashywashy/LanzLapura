package org.yohi.lapura_evaluation_system;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yohi.lapura_evaluation_system.API.AIService;
import org.yohi.lapura_evaluation_system.API.Subject;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AppController {

    @FXML private TextField nameField;
    @FXML private TextField idNumberField;
    @FXML private ComboBox<String> newStudentComboBox;
    @FXML private ComboBox<String> programComboBox;
    @FXML private ComboBox<String> yearLevelComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private TextField subjectsField;
    @FXML private Button nextButton;
    @FXML private Button cancelButton;

    @FXML private StackPane loadingPane;
    @FXML private ImageView loadingGif;
    private List<Subject> currentRecommendedSubjects;

    // Add the recommendation service
    private final Service<String> recommendationService = new Service<>() {
        @Override
        protected Task<String> createTask() {
            return new RecommendationTask(
                    programComboBox.getValue(),
                    Integer.parseInt(yearLevelComboBox.getValue()),
                    Integer.parseInt(semesterComboBox.getValue()),
                    "Yes".equals(newStudentComboBox.getValue()) ? new HashMap<>() : getSubjectsMap()
            );
        }
    };

    private Map<String, Boolean> getSubjectsMap() {
        // Implement logic to get subject status if needed
        return new HashMap<>();
    }


    // Add this inner class
    private static class RecommendationTask extends Task<String> {
        private final String program;
        private final int year;
        private final int semester;
        private final Map<String, Boolean> subjects;

        public RecommendationTask(String program, int year, int semester,
                                  Map<String, Boolean> subjects) {
            this.program = program;
            this.year = year;
            this.semester = semester;
            this.subjects = subjects;
        }

        @Override
        protected String call() throws Exception {
            AIService aiService = new AIService();
            return aiService.getRecommendations(program, year, semester, subjects);
        }
    }

    @FXML
    public void initialize() {
        // Initialize ComboBoxes
        newStudentComboBox.getItems().addAll("Yes", "No");
        programComboBox.getItems().addAll("BSIT", "BSA", "BSN", "BSMT");
        yearLevelComboBox.getItems().addAll("1", "2", "3", "4");
        semesterComboBox.getItems().addAll("1", "2");

        // Add listener to newStudentComboBox to enable/disable subjectsField
        newStudentComboBox.setOnAction(e -> updateSubjectsFieldState());

        // Add listeners to year and semester ComboBoxes
        yearLevelComboBox.setOnAction(e -> updateSubjectsFieldState());
        semesterComboBox.setOnAction(e -> updateSubjectsFieldState());

        // Handle Next button click
        nextButton.setOnAction(e -> handleNextButton());
        cancelButton.setOnAction(e -> handleCancelButton());


        // Initialize service handlers
        recommendationService.setOnRunning(e -> {
            loadingPane.setVisible(true);
            loadingGif.setVisible(true);
            nextButton.setDisable(true);
        });

        recommendationService.setOnSucceeded(e -> {
            loadingPane.setVisible(false);
            nextButton.setDisable(false);

            String recommendation = recommendationService.getValue();
            openRecommendationWindowAfterLoading(
                    nameField.getText(),
                    idNumberField.getText(),
                    programComboBox.getValue(),
                    recommendation
            );

            // Close current window after opening new one
            ((Stage) nextButton.getScene().getWindow()).close();
        });

        recommendationService.setOnFailed(e -> {
            loadingPane.setVisible(false);
            nextButton.setDisable(false);
            showAlert("Error", "Failed to generate recommendations: " +
                    e.getSource().getException().getMessage());
        });
    }

    private void displayRecommendedSubjects(String id, List<Subject> recommendedSubjects) {
        if (!isNumeric(id)) {
            showAlert("Error", "ID Number must contain only digits.");
            return;
        }

        currentRecommendedSubjects = recommendedSubjects;
        recommendationService.restart();
    }

    // Separate method for opening the recommendation window after loading
    private void openRecommendationWindowAfterLoading(String name, String id,
                                                      String program, String recommendation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recommended_subjects.fxml"));
            Parent root = loader.load();

            RecommendedSubjectsController controller = loader.getController();
            controller.setupRecommendedSubjects(name, Integer.parseInt(id), program, recommendation);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

            // Close current window after opening new one
            ((Stage) nextButton.getScene().getWindow()).close();

        } catch (IOException ex) {
            showAlert("Error", "Could not open recommendations: " + ex.getMessage());
        }
    }

    private void updateSubjectsFieldState() {
        boolean isNewStudent = "Yes".equals(newStudentComboBox.getValue());

        // Set default values for new students
        if (isNewStudent) {
            yearLevelComboBox.setValue("1");
            semesterComboBox.setValue("1");
        }

        // Disable fields for new students
        subjectsField.setDisable(isNewStudent);
        yearLevelComboBox.setDisable(isNewStudent);
        semesterComboBox.setDisable(isNewStudent);

        // Clear subjects field if disabled
        if (isNewStudent) {
            subjectsField.clear();
        }
    }

    private void handleNextButton() {
        String name = nameField.getText();
        String id = idNumberField.getText();
        String isNewStudent = newStudentComboBox.getValue();
        String program = programComboBox.getValue();
        String yearLevel = yearLevelComboBox.getValue();
        String semester = semesterComboBox.getValue();

        // Validate input fields
        if (name.isEmpty() || id.isEmpty() || isNewStudent == null || program == null) {
            showAlert("Error", "Please complete all required fields.");
            return;
        }
        if (!isAlphanumeric(name)) {
            showAlert("Error", "Name must not contain numbers.");
            return;
        }

        // Validate that ID is numeric
        if (!isNumeric(id)) {
            showAlert("Error", "ID Number must contain only digits.");
            return;
        }

        boolean isFirstYearFirstSem = "1".equals(yearLevel) && "1".equals(semester);

        if ("No".equals(isNewStudent) && !isFirstYearFirstSem) {
            if (subjectsField.getText().isEmpty() || !isNumeric(subjectsField.getText())) {
                showAlert("Error", "Please enter a valid number of subjects taken.");
                return;
            }

            int subjectCount = Integer.parseInt(subjectsField.getText());
            int yearLevelInt = Integer.parseInt(yearLevel);

            // Updated validation for 8-10 subjects
            if (subjectCount < 8 || subjectCount > 10) {
                showAlert("Error", "You must take between 8 and 10 subjects.");
                return;
            }
        }

        // For new students, show recommendations directly
        if ("Yes".equals(isNewStudent)) {
            String firstYearSubjects =
                    "First Year Recommended Subjects (Total Units: 26):\n" +
                            "• eng100 - 3 units (General Education)\n" +
                            "• socio102 - 3 units (General Education)\n" +
                            "• math100 - 3 units (General Education)\n" +
                            "• psych101 - 3 units (General Education)\n" +
                            "• cc-intcom11 - 3 units (Major)\n" +
                            "• cc-comprog11 - 3 units (Major)\n" +
                            "• it-webdev11 - 3 units (Major)\n" +
                            "• pe101 - 2 units (Physical Education)\n" +
                            "• nstp101 - 3 units (Civic Welfare)";

            // Directly open recommendations without API call
            openRecommendationWindowAfterLoading(
                    nameField.getText(),
                    idNumberField.getText(),
                    programComboBox.getValue(),
                    firstYearSubjects
            );
        } else {
            // Continuing students
            if (subjectsField.getText().isEmpty() || !isNumeric(subjectsField.getText())) {
                showAlert("Error", "Please enter a valid number of subjects taken.");
                return;
            }
            int subjectCount = Integer.parseInt(subjectsField.getText());

            // Move this inside the else block
            openSubjectSelectionScreen(subjectCount, program, name, id, yearLevel, semester);
        }

        if ("Yes".equals(isNewStudent)) {
        } else {
            // For existing students - collect subjects
           // recommendationService.restart();
        }
    }

    private void openSubjectSelectionScreen(int subjectCount, String program, String name, String id, String year, String semester) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("subject_input.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            List<String> subjectCodes = CurriculumParser.parseSubjects(program);

            if (controller instanceof SubjectSelectionController) {
                ((SubjectSelectionController) controller).setupSubjects(
                        subjectCount,
                        program,
                        name,
                        Integer.parseInt(id),
                        Integer.parseInt(year),
                        Integer.parseInt(semester),
                        subjectCodes
                );
            }

            Stage stage = new Stage();
            stage.setTitle("Subject Selection");
            stage.setScene(new Scene(root, 600, 400));
            stage.setMaximized(true);
            stage.show();

            // Close the current window
            Stage currentStage = (Stage) nextButton.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open subject selection screen: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAlphanumeric(String str) {
        // Matches only letters and spaces, no numbers
        return str.matches("[a-zA-Z ]+");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleCancelButton() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
