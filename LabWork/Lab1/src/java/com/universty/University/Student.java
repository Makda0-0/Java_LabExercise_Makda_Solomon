package com.universty.University;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.rmi.registry.LocateRegistry;

public class Student extends Application {
    private static Server server;
    private static boolean serverAvailable = false;

    private TextField idField, nameField, deptField, sectionField, yearField;
    private TextArea displayArea;
    private Label statusLabel;
    private Button addBtn, updateBtn, deleteBtn, clearBtn, refreshBtn, showBtn, saveBtn, deleteAllBtn;

    // Attractive, modern flat-design color scheme
    private static final String BG_COLOR = "#F8FAFC";    // Warm white slate
    private static final String CARD_BG = "#FFFFFF";     // Pure white for containers
    private static final String TEXT_MAIN = "#1E293B";   // Deep charcoal
    private static final String TEXT_MUTED = "#64748B";  // Soft slate gray
    private static final String ACCENT = "#4F46E5";      // Vibrant Indigo Indigo
    private static final String SUCCESS = "#10B981";     // Emerald Green
    private static final String DANGER = "#EF4444";      // Ruby Red

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // --- HEADER BAR ---
        HBox header = new HBox();
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: " + TEXT_MAIN + ";");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Student Information Portal");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusLabel = new Label("Connecting to Service...");
        statusLabel.setStyle("-fx-text-fill: #FBBF24; -fx-background-color: rgba(255,255,255,0.08); -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-weight: bold;");
        header.getChildren().addAll(title, spacer, statusLabel);
        root.setTop(header);

        // split window into 2
        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Left Panel (Input Form)
        VBox leftPane = new VBox(16);
        leftPane.setPadding(new Insets(24));
        leftPane.setStyle("-fx-background-color: " + CARD_BG + ";");

        Label formTitle = new Label("Registration Form");
        formTitle.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        formTitle.setStyle("-fx-text-fill: " + TEXT_MAIN + ";");

        VBox formGrid = new VBox(10);
        idField = createField("e.g., 10001");
        nameField = createField("e.g., Eleanor Vance");
        deptField = createField("e.g., Computer Science");
        sectionField = createField("e.g., A");
        yearField = createField("e.g., 1, 2, 3, 4");

        formGrid.getChildren().addAll(
                createLabel("Student ID *"), idField,
                createLabel("Full Name *"), nameField,
                createLabel("Department"), deptField,
                createLabel("Section"), sectionField,
                createLabel("Academic Year (1-4)"), yearField
        );

        // Grid Button Actions
        addBtn = btn("Save Student", SUCCESS);
        updateBtn = btn("Update", ACCENT);
        HBox row1 = new HBox(12, addBtn, updateBtn);
        row1.getChildren().forEach(b -> ((Button) b).setMaxWidth(Double.MAX_VALUE));
        HBox.setHgrow(addBtn, Priority.ALWAYS);
        HBox.setHgrow(updateBtn, Priority.ALWAYS);

        deleteBtn = btn("Delete", DANGER);
        clearBtn = btn("Clear fields", TEXT_MUTED);
        HBox row2 = new HBox(12, deleteBtn, clearBtn);
        row2.getChildren().forEach(b -> ((Button) b).setMaxWidth(Double.MAX_VALUE));
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);
        HBox.setHgrow(clearBtn, Priority.ALWAYS);

        // Utilities Section
        Label utilTitle = new Label("System Utilities");
        utilTitle.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        utilTitle.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-padding: 8 0 0 0;");

        refreshBtn = btn("Refresh Directory View", TEXT_MAIN);
        showBtn = btn("Global Database Analytics", TEXT_MAIN);
        saveBtn = btn("Commit Backup Snapshot", ACCENT);
        deleteAllBtn = btn("Purge Live Environment Records", DANGER);

        VBox adminBox = new VBox(8, refreshBtn, showBtn, saveBtn, deleteAllBtn);
        adminBox.getChildren().forEach(b -> ((Button) b).setMaxWidth(Double.MAX_VALUE));

        leftPane.getChildren().addAll(formTitle, formGrid, row1, row2, utilTitle, adminBox);

        // Right Panel (Data Viewer Window)
        VBox rightPane = new VBox(14);
        rightPane.setPadding(new Insets(24));
        rightPane.setStyle("-fx-background-color: " + CARD_BG + ";");

        Label viewTitle = new Label("Live System Directory Monitor");
        viewTitle.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        viewTitle.setStyle("-fx-text-fill: " + TEXT_MAIN + ";");

        displayArea = new TextArea("Syncing with master records server nodes...");
        displayArea.setEditable(false);
        displayArea.setFont(Font.font("Consolas", 13));
        displayArea.setStyle("-fx-control-inner-background: #FAF8F6; -fx-text-fill: #334155; -fx-background-color: transparent; -fx-border-color: #E2E8F0; -fx-border-radius: 6;");
        VBox.setVgrow(displayArea, Priority.ALWAYS);
        rightPane.getChildren().addAll(viewTitle, displayArea);

        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.38);

        // to move
        ScrollPane scrollPane = new ScrollPane(splitPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scrollPane);


        HBox footer = new HBox(new Label("Secured"));
        footer.setPadding(new Insets(10, 24, 10, 24));
        footer.setStyle("-fx-background-color: #F1F5F9; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");
        root.setBottom(footer);


        addBtn.setOnAction(e -> { if(validateForm()) runAsync(() -> server.addStudent(idField.getText(), nameField.getText(), deptField.getText(), sectionField.getText(), yearField.getText()), true); });
        updateBtn.setOnAction(e -> { if(validateForm()) runAsync(() -> server.updateStudent(idField.getText(), nameField.getText(), deptField.getText(), sectionField.getText(), yearField.getText()), true); });
        deleteBtn.setOnAction(e -> { if(checkId() && confirm("Permanently erase student ID: " + idField.getText() + "?")) runAsync(() -> server.deleteStudent(idField.getText()), true); });
        clearBtn.setOnAction(e -> { idField.clear(); nameField.clear(); deptField.clear(); sectionField.clear(); yearField.clear(); idField.requestFocus(); });
        refreshBtn.setOnAction(e -> refreshDisplay());
        showBtn.setOnAction(e -> showDbWindow());
        saveBtn.setOnAction(e -> { if(confirm("Initiate structural database binary snapshot storage?")) runAsync(() -> server.save(), false); });
        deleteAllBtn.setOnAction(e -> purgeDbSequence());

        primaryStage.setTitle("Unified Student Information Node");
        primaryStage.setScene(new Scene(root, 1100, 700));
        primaryStage.show();
        toggleFields(true);

        new Thread(this::connect).start();
    }

    // --- UI FACTORY BUILDERS ---
    private TextField createField(String hintText) {
        TextField t = new TextField();
        t.setPromptText(hintText);
        t.setPrefHeight(36);
        t.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: " + TEXT_MAIN + ";");
        t.focusedProperty().addListener((o, old, isFocused) -> {
            if (isFocused) {
                t.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: " + ACCENT + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: " + TEXT_MAIN + ";");
            } else {
                t.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: " + TEXT_MAIN + ";");
            }
        });
        return t;
    }

    private Label createLabel(String textValue) {
        Label l = new Label(textValue);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11; -fx-font-family: 'Inter';");
        return l;
    }

    private Button btn(String labelText, String hexColor) {
        Button b = new Button(labelText);
        b.setPrefHeight(36);
        b.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        b.setStyle("-fx-background-color: " + hexColor + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: derive(" + hexColor + ", -12%); -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: " + hexColor + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;"));
        return b;
    }

    //  RMI INFRASTRUCTURE CONNECTIVITY
    private void connect() {
        try {
            server = (Server) LocateRegistry.getRegistry("localhost", 1099).lookup("SchoolServer");
            serverAvailable = true;
            Platform.runLater(() -> {
                statusLabel.setText("RMI Online");
                statusLabel.setStyle("-fx-text-fill: white; -fx-background-color: " + SUCCESS + "; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-weight: bold;");
                toggleFields(false);
                refreshDisplay();
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                statusLabel.setText("RMI Server Fault");
                statusLabel.setStyle("-fx-text-fill: white; -fx-background-color: " + DANGER + "; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-weight: bold;");
                displayArea.setText("RMI Engine Failure: Could not safely fetch network registry stub.\nPlease verify remote server configurations.");
            });
        }
    }

    private interface RMIOperation { String execute() throws Exception; }

    private void runAsync(RMIOperation op, boolean triggerRefresh) {
        if (!serverAvailable) { alert("Execution Halted", "Remote interface structural pipe is unavailable."); return; }
        new Thread(() -> {
            try {
                String responseData = op.execute();
                Platform.runLater(() -> { alert("Transaction Feedback", responseData); if (triggerRefresh) refreshDisplay(); });
            } catch (Exception e) { Platform.runLater(() -> alert("Runtime Execution Failure", e.getMessage())); }
        }).start();
    }

    private void refreshDisplay() {
        if (!serverAvailable) return;
        new Thread(() -> {
            try { String rawLogs = server.getAllStudents(); Platform.runLater(() -> displayArea.setText(rawLogs)); }
            catch (Exception e) { Platform.runLater(() -> displayArea.setText("Data stream interrupt: " + e.getMessage())); }
        }).start();
    }

    private void showDbWindow() {
        if (!serverAvailable) return;
        new Thread(() -> {
            try {
                String rawMatrix = server.show();
                Platform.runLater(() -> {
                    Stage internalStage = new Stage(); internalStage.setTitle("System Matrix Data Schema Blueprint");
                    TextArea rawArea = new TextArea(rawMatrix); rawArea.setEditable(false); rawArea.setFont(Font.font("Consolas", 12));
                    internalStage.setScene(new Scene(rawArea, 750, 480)); internalStage.show();
                });
            } catch (Exception e) { Platform.runLater(() -> alert("Deployment  Issue", e.getMessage())); }
        }).start();
    }

    private void purgeDbSequence() {
        TextInputDialog prompt = new TextInputDialog();
        prompt.setTitle("Destructive Cycle Authentication");
        prompt.setHeaderText("CRITICAL ACTIONS: Purging entire transactional master storage clusters.");
        prompt.setContentText("Type 'DELETE ALL' to execute logic cycles:");
        if (prompt.showAndWait().orElse("").equalsIgnoreCase("DELETE ALL")) {
            runAsync(() -> server.deleteAll(), true);
        }
    }

    private boolean checkId() {
        if (idField.getText().trim().isEmpty()) { alert("Validation Error", "Target database row requires a primary sequence index identifier."); return false; }
        return true;
    }

    private boolean validateForm() {
        if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
            alert("Validation Error", "Missing properties: Primary Index token and Full Name parameters cannot be empty strings.");
            return false;
        }
        try {
            Integer.parseInt(idField.getText().trim());
            if (!yearField.getText().trim().isEmpty()) {
                int academicYear = Integer.parseInt(yearField.getText().trim());
                if (academicYear < 1 || academicYear > 4) {
                    alert("Range Boundaries Exceeded", "Structural criteria mismatch: Academic milestones must land exactly between 1 and 4.");
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            alert("Parser Format Rejection", "Numeric formatting criteria violation: Digits parsed contain bad characters.");
            return false;
        }
    }

    private void toggleFields(boolean state) {
        Button[] btnCollection = {addBtn, updateBtn, deleteBtn, clearBtn, refreshBtn, showBtn, saveBtn, deleteAllBtn};
        for (Button b : btnCollection) if (b != null) b.setDisable(state);
        TextField[] fieldCollection = {idField, nameField, deptField, sectionField, yearField};
        for (TextField f : fieldCollection) if (f != null) f.setDisable(state);
    }

    private void alert(String headline, String contentBody) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(headline); a.setHeaderText(null); a.setContentText(contentBody);
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.showAndWait();
    }

    private boolean confirm(String messageBody) {
        return new Alert(Alert.AlertType.CONFIRMATION, messageBody).showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static void main(String[] args) { launch(args); }
}