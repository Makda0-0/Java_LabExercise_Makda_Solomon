package com.template;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import java.io.*;
import java.util.Optional;
import java.util.Stack;

public class Notepad extends Application {

    private TextArea textArea;
    private Stage primaryStage;
    private File currentFile;
    private Label statusLabel;
    private Stack<String> undoStack = new Stack<>();
    private Stack<String> redoStack = new Stack<>();
    private boolean isUndoRedoInProgress = false;//to prevent from saving when undoing
    private Button undoBtn;
    private Button redoBtn;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        // Text area
        textArea = new TextArea();
        textArea.setStyle(
                "-fx-control-inner-background: #1e1e1e;" +
                        "-fx-text-fill: #d4d4d4;" +
                        "-fx-font-family: 'Consolas';" +
                        "-fx-font-size: 14px;"
        );

        // Undo/Redo setup
        saveToUndoStack();
        textArea.textProperty().addListener((obs, old, newText) -> {
            if (!isUndoRedoInProgress) {
                saveToUndoStack();
                redoStack.clear();//since new changes where made
                updateUndoRedoButtons();

            }
            updateStatusBar();

        });

        // Status bar
        statusLabel = new Label(" Ready");
        statusLabel.setStyle("-fx-padding: 8; -fx-background-color: #3c3f41; -fx-text-fill: #a0a0a0;");

        // Menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #3c3f41;");

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        newItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newItem.setOnAction(e -> newFile());

        MenuItem openItem = new MenuItem("Open");
        openItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openItem.setOnAction(e -> openFile());

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveItem.setOnAction(e -> saveFile());

        MenuItem saveAsItem = new MenuItem("Save As");
        saveAsItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        saveAsItem.setOnAction(e -> saveAsFile());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        exitItem.setOnAction(e -> exitApplication());

        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, new SeparatorMenuItem(), exitItem);

        // Edit Menu
        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));
        undoItem.setOnAction(e -> undo());

        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Y"));
        redoItem.setOnAction(e -> redo());

        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        cutItem.setOnAction(e -> textArea.cut());

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        copyItem.setOnAction(e -> textArea.copy());

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setAccelerator(KeyCombination.keyCombination("Ctrl+V"));
        pasteItem.setOnAction(e -> textArea.paste());

        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        selectAllItem.setOnAction(e -> textArea.selectAll());

        MenuItem findReplaceItem = new MenuItem("Find & Replace");
        findReplaceItem.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        findReplaceItem.setOnAction(e -> showFindReplaceDialog());

        editMenu.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(),
                cutItem, copyItem, pasteItem, new SeparatorMenuItem(),
                selectAllItem, new SeparatorMenuItem(), findReplaceItem);

        // Format Menu
        Menu formatMenu = new Menu("Format");
        CheckMenuItem wrapItem = new CheckMenuItem("Word Wrap");
        wrapItem.setOnAction(e -> textArea.setWrapText(wrapItem.isSelected()));
        formatMenu.getItems().add(wrapItem);

        // Toolbar
        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #3c3f41;");

        Button saveBtn = createToolButton("Save");
        saveBtn.setOnAction(e -> saveFile());

        Button copyBtn = createToolButton("Copy");
        copyBtn.setOnAction(e -> textArea.copy());
        Button pasteBtn = createToolButton("Paste");
        pasteBtn.setOnAction(e -> textArea.paste());
        undoBtn = createToolButton("Undo");
        undoBtn.setOnAction(e -> undo());

        redoBtn = createToolButton("Redo");
        redoBtn.setOnAction(e -> redo());

        toolBar.getItems().addAll( saveBtn, new Separator(),
                copyBtn,new Separator(), pasteBtn,undoBtn, new Separator(), redoBtn);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, formatMenu, helpMenu);

        // Layout assembly
        VBox topBox = new VBox(menuBar, toolBar);
        root.setTop(topBox);
        root.setCenter(textArea);
        root.setBottom(statusLabel);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Notepad");
        primaryStage.setScene(scene);
        primaryStage.show();
        updateStatusBar();
    }

    private Button createToolButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #4a4f52; -fx-text-fill: white; -fx-padding: 5 15; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #5a5f62; -fx-text-fill: white; -fx-padding: 5 15;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #4a4f52; -fx-text-fill: white; -fx-padding: 5 15;"));
        return btn;
    }

    private void updateUndoRedoButtons() {
        if (undoBtn != null && redoBtn != null) {
            undoBtn.setDisable(undoStack.size() <= 1);
            redoBtn.setDisable(redoStack.isEmpty());
        }
    }

    private void saveToUndoStack() {
        if (undoStack.size() > 50) undoStack.remove(0);
        undoStack.push(textArea.getText());
        updateUndoRedoButtons();
    }

    private void undo() { //need at least 2 items
        if (undoStack.size() > 1) {
            isUndoRedoInProgress = true;
            redoStack.push(undoStack.pop());
            textArea.setText(undoStack.peek());
            isUndoRedoInProgress = false;
            updateUndoRedoButtons();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            isUndoRedoInProgress = true;
            textArea.setText(redoStack.pop());
            isUndoRedoInProgress = false;
            updateUndoRedoButtons();
        }
    }

    private void newFile() {
        textArea.clear();
        currentFile = null;
        primaryStage.setTitle("Notepad - Untitled");
        undoStack.clear();
        redoStack.clear();
        saveToUndoStack();
        updateUndoRedoButtons();
    }

    private void openFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));//only shows txt files
        File file = fc.showOpenDialog(primaryStage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) content.append(line).append("\n");
                textArea.setText(content.toString());
                currentFile = file;
                primaryStage.setTitle("Notepad - " + file.getName());
                undoStack.clear();
                redoStack.clear();
                saveToUndoStack();
                updateUndoRedoButtons();
            } catch (IOException e) { showError("Error opening file"); }
        }
    }

    private void saveFile() {
        if (currentFile == null) saveAsFile();
        else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                writer.write(textArea.getText());
                primaryStage.setTitle("Notepad - " + currentFile.getName());
            } catch (IOException e) { showError("Error saving file"); }
        }
    }
   // Saves current content to a new file
    private void saveAsFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fc.showSaveDialog(primaryStage);
        if (file != null) {
            currentFile = file;
            saveFile();
        }
    }

    private void exitApplication() {
        System.exit(0);
    }

    private void showFindReplaceDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Find & Replace");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField findField = new TextField();
        findField.setPromptText("Find");
        TextField replaceField = new TextField();
        replaceField.setPromptText("Replace");
        CheckBox caseCheck = new CheckBox("Case Sensitive");

        content.getChildren().addAll(new Label("Find:"), findField,
                new Label("Replace:"), replaceField, caseCheck);

        ButtonType findBtn = new ButtonType("Find Next");
        ButtonType replaceBtn = new ButtonType("Replace");
        ButtonType replaceAllBtn = new ButtonType("Replace All");

        dialog.getDialogPane().getButtonTypes().addAll(findBtn, replaceBtn, replaceAllBtn, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);

        ((Button) dialog.getDialogPane().lookupButton(findBtn)).setOnAction(e -> {
            String find = findField.getText();
            if (!find.isEmpty()) findNext(find, caseCheck.isSelected());
        });

        ((Button) dialog.getDialogPane().lookupButton(replaceBtn)).setOnAction(e -> {
            String find = findField.getText();
            String replace = replaceField.getText();
            if (!find.isEmpty()) replaceCurrent(find, replace, caseCheck.isSelected());
        });

        ((Button) dialog.getDialogPane().lookupButton(replaceAllBtn)).setOnAction(e -> {
            String find = findField.getText();
            String replace = replaceField.getText();
            if (!find.isEmpty()) replaceAll(find, replace, caseCheck.isSelected());
        });

        dialog.show();
    }

    private void findNext(String find, boolean caseSensitive) {
        String content = textArea.getText();
        String searchContent = caseSensitive ? content : content.toLowerCase();
        String searchText = caseSensitive ? find : find.toLowerCase();
        int pos = searchContent.indexOf(searchText, textArea.getCaretPosition());

        if (pos == -1 && textArea.getCaretPosition() > 0)
            pos = searchContent.indexOf(searchText, 0);

        if (pos != -1) {
            textArea.selectRange(pos, pos + find.length());
            textArea.requestFocus();
        } else showError("Text not found");
    }

    private void replaceCurrent(String find, String replace, boolean caseSensitive) {
        String selected = textArea.getSelectedText();
        if (selected != null && (caseSensitive ? selected.equals(find) : selected.equalsIgnoreCase(find))) {
            textArea.replaceSelection(replace);
        }
        findNext(find, caseSensitive);
    }

    private void replaceAll(String find, String replace, boolean caseSensitive) {
        String content = textArea.getText();
        String newContent = caseSensitive ? content.replace(find, replace)
                : content.replaceAll("(?i)" + java.util.regex.Pattern.quote(find),
                java.util.regex.Matcher.quoteReplacement(replace));
        textArea.setText(newContent);
    }
/*
Updates the status bar with current file information
Shows: filename.txt, line count, character count
*/
    private void updateStatusBar() {
        String text = textArea.getText();
        int lines = text.split("\n", -1).length;
        int chars = text.length();// Total characters
        String fileName = currentFile == null ? "Untitled" : currentFile.getName();
        statusLabel.setText(" " + fileName + " | Lines: " + lines + " | Characters: " + chars);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Notepad Application");
        alert.setContentText("Version 1.0\nA simple text editor built with JavaFX");
        alert.showAndWait();// waits for user to click ok
    }

    public static void main(String[] args) {
        launch(args);
    }
}