package ijae.xscaccd00;

import java.nio.file.Paths;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * The Game class serves as the main entry point for the PacMan game application.
 * Important functionalities:
 * <ul>
 *   <li>Staring the application, setting up the menu handlers and the game loop.</li>
 *   <li>Managing player inputs.</li>
 *   <li>Handling game states such as pausing, starting, and resetting the game.</li>
 *   <li>Updating and displaying the player's score.</li>
 * </ul>
 * 
 * The main game loop is managed using AnimationTimer, which handles
 * the timing for updating game elements and rendering changes on the screen.
 * 
 * @author Davide Scaccia
 */
public class Game extends Application {
    private Board board; //The game board where all game elements such as the player and ghosts reside.
    private Label scoreLabel; //Label to display the player's current score.
    private GameMenu menu; //The game menu providing options to start, pause, change speed, and select levels.
    private boolean isPaused = false; //Indicates whether the game is currently paused.
    private double gameSpeed = 1.0; //The current speed of the game, affecting the game loop's update intervals.
    private static final long BASE_FRAME_INTERVAL = 200_000_000; //Frame interval in nanoseconds (200ms)
    private long lastUpdate = 0; //Timestamp of the last player update.
    private long lastGhostUpdate = 0; //Timestamp of the last ghost update.
    private HBox root; //The root layout container for the game scene.
    private AnimationTimer gameTimer; //The animation timer managing the game loop.
    private boolean gameStarted = false; //Indicates whether the game has started.
    private Direction lastDirection = Direction.RIGHT; //The last direction the player moved in.
    private final String soundPath = Paths.get("src/main/resources/assets/deathsound.mp3").toUri().toString(); //Get the path to the death sound
    private final Media sound = new Media(soundPath); //Create the media player
    private final MediaPlayer mediaPlayer = new MediaPlayer(sound); //Create the media player

    /**
     * Launches the application.
     * @param primaryStage the Stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        menu = new GameMenu(); //Create the game menu
        board = new Board(menu.getSelectedLevelPath()); //Create the board
        root = new HBox(10); //Create the new root layout (hbox with 10px spacing, it arranges the children in a single horizontal row)
        root.setStyle("-fx-background-color: #333333;"); //Set the root background color
        root.setPadding(new Insets(10)); //Set the root padding
        
        VBox gameArea = new VBox(10); //Create the game area (vbox with 10px spacing, it arranges the children in a single vertical column)
        gameArea.setStyle("-fx-background-color: black;"); //Set the game area background color
        gameArea.setPadding(new Insets(10)); //Set the game area padding
        
        scoreLabel = new Label("Score: 0"); //Create the score label
        scoreLabel.setStyle("-fx-text-fill: white;"); //Set the score label text color
        gameArea.getChildren().addAll(scoreLabel, board.createBoardView()); //Add the score label and the board view to the game area
        
        root.getChildren().addAll(menu, gameArea); //Add the menu and the game area to the root layout
        Scene scene = new Scene(root, 800, 600); //Create the scene with the root layout and the size of the window
        setupMenuHandlers(); //Setup the menu handlers
        setupKeyHandlers(scene); //Setup the key handlers

        primaryStage.setTitle("PacMan Game"); //Set the title of the window
        primaryStage.setScene(scene); //Set the scene to the primary stage
        primaryStage.setResizable(false); //Set the window to not be resizable
        primaryStage.show(); //Show the window
        
        gameArea.requestFocus(); //Request focus to the game area
    }

    /**
     * Sets up event handlers for the game menu interactions.
     * Handles actions such as starting the game, pausing, changing speed, and selecting levels.
     */
    private void setupMenuHandlers() {
        menu.setOnStart(() -> {
            gameStarted = true; //Set the game started to true
            isPaused = false; //Set the game paused to false
            lastUpdate = 0; //Set the last update to 0
            lastGhostUpdate = 0; //Set the last ghost update to 0

            if (gameTimer != null) gameTimer.stop(); //Stop the game timer
            setupGameLoop(); //Setup the game loop
            refreshBoardView(); //Refresh the board view
        });

        menu.setOnPause(() -> {
            isPaused = !isPaused; //Toggle the game paused state
            menu.updatePauseButtonText(isPaused); //Update the pause button text
        });

        menu.setOnSpeedChange(newSpeed -> {
            gameSpeed = newSpeed; //Set the game speed to the new speed
            board.setGameSpeed(gameSpeed); //Update the game speed in the board
            return null;
        });

        menu.setOnLevelSelect(() -> {
            resetGame(); //Reset the game
            isPaused = true; //Pause the game
            board = new Board(menu.getSelectedLevelPath()); //Create a new board with the selected level
            board.setGameSpeed(gameSpeed); //Set the initial game speed
            refreshBoardView(); //Refresh the board view
        });
    }

    /**
     * Sets up keyboard event handlers for player movement and pausing the game.
     * @param scene the current game scene
     */
    private void setupKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W -> lastDirection = Direction.UP; //Set the last direction to up
                case S -> lastDirection = Direction.DOWN; //Set the last direction to down
                case A -> lastDirection = Direction.LEFT; //Set the last direction to left
                case D -> lastDirection = Direction.RIGHT; //Set the last direction to right
                case ESCAPE -> {
                    isPaused = !isPaused; // Toggle the game paused state
                    menu.updatePauseButtonText(isPaused); // Update the pause button text
                }
                default -> {}
            }
            event.consume(); // Consume the event if a key was handled
        });
    }

    /**
     * Sets up and starts the main game loop using AnimationTimer
     * The game loop handles updating ghost movements, player movements,
     * collision detection, and rendering updates based on the elapsed time.
     */
    private void setupGameLoop() {
        gameTimer = new AnimationTimer() { //Create a new AnimationTimer
            @Override
            public void handle(long now) { //Handle the game loop
                if (!isPaused && gameStarted) { //If the game is not paused and the game has started
                    // Handle ghost movement independently
                    if (now - lastGhostUpdate >= BASE_FRAME_INTERVAL / gameSpeed) { //If the last ghost update is greater than the base frame interval divided by the game speed
                        board.moveGhosts(); //Move the ghosts
                        lastGhostUpdate = now; //Set the last ghost update to the current time

                        if (board.isGameOver()) { //If the game is over
                            showGameOver(false); //Show the game over screen
                            stop(); //Stop the game timer
                            return; //Return from the handle method
                        }
                        refreshBoardView(); //Refresh the board view
                    }

                    //Handle Pacman movement
                    if (now - lastUpdate >= BASE_FRAME_INTERVAL / gameSpeed) { //If the last update is greater than the base frame interval divided by the game speed
                        board.movePlayer(lastDirection); //Move the player in the last diretion
                        lastUpdate = now; //Set the last update to the current time

                        //Update the score label after moving the player
                        scoreLabel.setText("Score: " + board.getPlayerScore()); //Update the score label
                        refreshBoardView(); //Refresh the board view

                        //Check if the player has won
                        if (board.hasWon()) {
                            showGameOver(true); //Show the game over screen
                            stop(); //Stop the game timer
                            return; //Return from the handle method
                        }
                    }
                    //Update ghost eatable effect on every game loop iteration
                    board.updateGhostEatableEffect();
                }
            }
        };
        gameTimer.start(); //Start the game timer
    }

    /**
     * Refreshes the visual representation of the game board.
     * Removes the old game area and adds the updated board view with the current score.
     */
    private void refreshBoardView() {
        root.getChildren().remove(1); //Remove the old game area

        VBox gameArea = new VBox(5); //Create the game area (vbox with 5px spacing, it arranges the children in a single vertical column)
        gameArea.setStyle("-fx-background-color: black;"); //Set the game area background color
        gameArea.setPadding(new Insets(5)); //Set the game area padding
        scoreLabel.setStyle("-fx-text-fill: white;"); //Set the score label text color
        gameArea.getChildren().addAll(scoreLabel, board.createBoardView()); //Add the score label and the board view to the game area
        root.getChildren().add(gameArea); //Add the game area to the root layout
    }

    /**
     * Displays a game-over alert dialog indicating whether the player has won or lost.
     * After the alert is closed, the game is reset to its initial state.
     * @param won true if the player has won; false otherwise
     */
    private void showGameOver(boolean won) {
        //We use runLater to ensure thread safety
        Platform.runLater(() -> {
            //Pause the game
            gameStarted = false;
            isPaused = true;
            
            if (gameTimer != null) gameTimer.stop(); //Stop the game timer
            if (!won) playDeathSound(); //Play the death sound if the player lost
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION); //Create the alert dialog
            alert.setTitle(won ? "Victory!" : "Game Over"); //Set the title of the alert dialog
            alert.setHeaderText(null); //Set the header text of the alert dialog to null
            alert.setContentText(won ? //Set the content text of the alert dialog
                "Congratulations! You won! Final Score: " + board.getPlayerScore() : //If the player won
                "Game Over! Final Score: " + board.getPlayerScore()); //If the player lost
            alert.showAndWait(); //Show the alert dialog and wait for the user to close it
            resetGame(); //Reset the game
        });
    }

    /**
     * Resets the game to its initial state by resetting the menu, reinitializing the board,
     * resetting the player's score, and updating the game view.
     */
    private void resetGame() {
        menu.reset(); //Reset the menu
        board.resetBoardState(); //Reset the board state
        board = new Board(menu.getSelectedLevelPath()); //Create a new board with the selected level
        board.setGameSpeed(gameSpeed); //Set the game speed
        scoreLabel.setText("Score: 0"); //Set the score label to 0
        isPaused = false; //Set the game paused to false
        lastUpdate = 0; //Set the last update to 0
        lastGhostUpdate = 0; //Set the last ghost update to 0
        lastDirection = Direction.RIGHT; //Reset direction
        refreshBoardView(); //Refresh the board view
    }

    /**
     * The main entry point for the application.
     * @param args the command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args); //Launch the application
    }

    /**
     * Plays the death sound when the player loses.
     */
    private void playDeathSound() {
        mediaPlayer.setVolume(0.2); //Set the volume to 20%
        mediaPlayer.seek(javafx.util.Duration.ZERO); //Restart the mediaPlayer from the beginning
        mediaPlayer.play(); //Play the sound
    }
}