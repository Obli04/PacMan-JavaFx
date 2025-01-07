package ijae.xscaccd00;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * The GameMenu class provides the user interface components to control the game
 * Important functionalities:
 * <ul>
 *   <li>Manages game speed.</li>
 *   <li>Selecting of levels and uploading new levels.</li>
 *   <li>Start and pause the game.</li>
 * </ul>
 * @author Davide
 */
public class GameMenu extends VBox {
    final private Slider speedSlider; //Slider to adjust the game speed.
    final private ComboBox<String> levelSelector; //ComboBox for selecting the game level.
    final private Button startButton; //Button to start the game.
    final private Button pauseButton; //Button to pause the game.
    final private Button uploadButton; //Button to upload new level files
    private Runnable onLevelSelect; //Callback to execute when a level is selected.
    final private ImageView logoView; // ImageView for the Pac-Man logo

    /**
     * Constructs a GameMenu with initialized UI components.
     * Sets up the layout, styles, and event handlers for the menu.
     */
    public GameMenu() {

        //Set spacing, padding, background color and width
        setSpacing(10);
        setPadding(new Insets(10)); 
        setStyle("-fx-background-color: #444444;");
        setPrefWidth(300);

        //Speed control
        HBox speedControl = new HBox(10); //HBox with a spacing of 10
        Label speedLabel = new Label("Game Speed:"); //Set the label to "Game Speed:"
        speedLabel.setStyle("-fx-text-fill: white;"); //Color the text white
        speedSlider = new Slider(1, 3, 1); //Create a new Slider with a minimum value of 1, a maximum value of 3 and a default value of 1
        speedSlider.setShowTickLabels(true); //Show the tick labels
        speedSlider.setShowTickMarks(true); //Show the tick marks
        speedSlider.setSnapToTicks(true); //Snap to the ticks
        speedSlider.setMajorTickUnit(1); //Set the major tick unit to 1
        speedSlider.setMinorTickCount(0); //Set the minor tick count to 0
        speedSlider.setBlockIncrement(1); //Set the block increment to 1
        speedControl.getChildren().addAll(speedLabel, speedSlider); //Add the label and the slider to the HBox

        // Level selector
        HBox levelControl = new HBox(10); //HBox with a spacing of 10
        Label levelLabel = new Label("Select Level:"); //Set the label to "Select Level:"
        levelLabel.setStyle("-fx-text-fill: white;"); //Color the text white
        levelSelector = new ComboBox<>(); //Create a new ComboBox
        populateLevelSelector(); //Populate the level selector with all the levels in the levels directory
        levelControl.getChildren().addAll(levelLabel, levelSelector); //Add the label and the selector to the HBox

        // Add listener for level selection
        levelSelector.setOnAction(e -> {
            if(onLevelSelect != null) {
                onLevelSelect.run(); //Run the onLevelSelect action
            }
        });

        // Buttons
        HBox buttonControl = new HBox(10); //HBox with a spacing of 10
        startButton = new Button("Start Game"); //Create a new Button with the text "Start Game"
        pauseButton = new Button("Pause"); //Create a new Button with the text "Pause"
        uploadButton = new Button("Upload Level"); //Create a new Button with the text "Upload Level"
        pauseButton.setDisable(true); //Disable the pause button
        buttonControl.getChildren().addAll(startButton, pauseButton, uploadButton); //Add the start, pause and upload buttons to the HBox

        startButton.setFocusTraversable(false); //Disable the selection of the start button
        pauseButton.setFocusTraversable(false); //Disable the selection of the pause button
        levelSelector.setFocusTraversable(false); //Disable the selection of the level selector
        uploadButton.setFocusTraversable(false); //Disable the selection of the upload button
        speedSlider.setFocusTraversable(false); //Disable the selection of the speed slider 

        //Upload button action (upload a level file)
        uploadButton.setOnAction(e -> uploadLevelFile());

        //Logo view of PacMan
        String imagePath = "assets/logo.png"; //Path to the logo image
        var resourceUrl = getClass().getClassLoader().getResource(imagePath); //Get the resource url
        logoView = new ImageView(new Image(resourceUrl.toExternalForm())); //Create a new ImageView with the logo image
        logoView.setFitWidth(200); //Set the width
        logoView.setFitHeight(200); //Set the height
        getChildren().addAll(speedControl, levelControl, buttonControl, logoView); //Add all the HBoxes and the logo view to the VBox
    }

    /**
     * Puts the levels in the level selector ComboBox.
     * Retrieves level files from the "src/main/resources/levels" directory
     * and adds their names to the ComboBox. Sets the default selected level to the first available.
     */
    private void populateLevelSelector() {
        File levelsDir = new File("src/main/resources/levels"); //Path containing the levels
        File[] levelFiles = levelsDir.listFiles((dir, name) -> name.endsWith(".txt")); //Get the level files in the levels directory ending with .txt
        for (File file : levelFiles) levelSelector.getItems().add(file.getName()); //Add the level file name to the level selector

        levelSelector.setValue(levelSelector.getItems().get(0)); //Set the default selected level to the first available
    }

    /**
     * Retrieves the path of the selected level.
     * @return the file path of the selected level
     * @throws RuntimeException if no level is selected
     */
    public String getSelectedLevelPath() {
        String selectedLevel = levelSelector.getValue(); //Get the selected level
        String path = "levels/" + selectedLevel; //Create the path to the selected level
        return path; //Return the path
    }

    /**
     * Retrieves the current game speed.
     * @return the game speed as an integer
     */
    public int getGameSpeed() {
        return (int) speedSlider.getValue(); //Return the game speed as an integer
    }

    /**
     * Sets the action to perform when the start button is clicked.
     * @param action the Runnable action to execute on start
     */
    public void setOnStart(Runnable action) {
        startButton.setOnAction(e -> {
            action.run(); //Run the action
            startButton.setDisable(true); //Disable the start button
            speedSlider.setDisable(true); //Disable the speed slider
            pauseButton.setDisable(false); //Enable the pause button
        });
    }

    /**
     * Sets the action to perform when the pause button is clicked.
     * @param action the Runnable action to execute on pause
     */
    public void setOnPause(Runnable action) {
        pauseButton.setOnAction(e -> {
            action.run(); //Run the action
            boolean isPaused = pauseButton.getText().equals("Resume"); //Check if the pause button is paused
            updatePauseButtonText(isPaused); //Update the pause button text
        });
    }

    /**
     * Sets the callback to execute when the game speed is changed.
     * @param callback the Callback to handle speed changes
     */
    public void setOnSpeedChange(javafx.util.Callback<Double, Void> callback) {
        //Add a listener to the speed slider to call the callback when the speed changes
        speedSlider.valueProperty().addListener((obs, old, newValue) -> callback.call(newValue.doubleValue()));
    }
    
    /**
     * Sets the action to execute when the level is selected.
     * @param action the Runnable action to execute on level select
     */
    public void setOnLevelSelect(Runnable action) {
        this.onLevelSelect = action; //Set the onLevelSelect action
    }

    /**
     * Resets the game menu, enabling the speed slider.
     */
    public void reset() {
        speedSlider.setDisable(false); //Re-enable the speed slider
        startButton.setDisable(false); //Re-enable the start button
        pauseButton.setDisable(true); //Disable the pause button
        pauseButton.setText("Pause"); //Set the text of the pause button to "Pause"
    }

    /**
     * Updates the text of the pause button based on the game's paused state.
     * @param isPaused true if the game is paused, false otherwise
     */
    public void updatePauseButtonText(boolean isPaused) {
        if(!pauseButton.isDisabled()) pauseButton.setText(isPaused ? "Resume" : "Pause"); //Set the text of the pause button to resume or pause
    }

    /**
     * Opens a file chooser to select a level file and copies it to the levels directory.
     */
    private void uploadLevelFile() {
        FileChooser fileChooser = new FileChooser(); //Create a new FileChooser
        fileChooser.setTitle("Select Level File"); //Set the title of the file chooser
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt")); //Filter only txt files
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow()); //Show the file chooser and get the selected file
        if (selectedFile != null) {
            String fileName = selectedFile.getName();
            File destination = new File("src/main/resources/levels", fileName); //Create a new File with the destination path
            try {
                Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING); //Copy the selected file to the destination
            } catch (IOException e) {
                System.out.println("Error uploading level file: " + e.getMessage()); //Print an error message if the file cannot be uploaded
            }
            levelSelector.getItems().add(fileName); //Add the new level to the level selector
        }
    }
}