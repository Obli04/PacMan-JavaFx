package ijae.xscaccd00;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * This class is responsible for loading the level, initializing ghosts,
 * creating the visual representation of the board, and managing
 * player movement and collision with ghosts.
 * 
 * @author Davide Scaccia
 */
public class Board {
    private BoardElement[][] grid; //2D array representing the grid of board elements
    private int rows; //Number of rows in the board
    private int cols; //Number of columns in the board
    private boolean gateOpen; //Is the gate open
    private int playerScore; //The player's current score
    private Point2D playerPosition; //The player's current position
    private final List<Point2D> ghostPositions; //List of ghost positions on the board
    private static final int CELL_SIZE = 32; //Cell size
    private final Random random = new Random(); //Random number generator for ghost movements
    private final List<Ghost> ghosts = new ArrayList<>(); //List of ghosts on the board
    private boolean hasWon = false; //Has the player won the game
    private boolean isGameOver = false; //Is the game over due to collision
    private Direction currentDirection = Direction.RIGHT; //The current direction the player is facing
    private BoardElement previousElementUnderPlayer; //Track the element under the player
    private Point2D previousPlayerPosition; //Player previous position to restore the item under the player
    private boolean canEatGhosts = false; //Flag to indicate if ghosts can be eaten
    private long eatGhostsEndTime = 0; //Timestamp when eating ghosts effect ends
    private double gameSpeed = 1.0; //Default game speed

    /**
     * Constructor for the Board class, initializes the board and loads the level
     * @param levelPath the path to the level configuration file
     */
    public Board(String levelPath) {
        ghostPositions = new ArrayList<>(); //Initialize the ghost positions list
        loadLevel(levelPath); //Load the level
    }

    /**
     * Loads the level configuration from the specified file path.
     * Initializes the grid with Board Elements and sets the initial positions.
     * @param levelPath the path to the level configuration file
     * @throws RuntimeException if the level file cannot be loaded or is invalid
     */
    private void loadLevel(String levelPath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(levelPath); //Load the level file as an InputStream using the class loader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            //Read all lines from the level file into a list of strings
            List<String> lines = reader.lines().collect(Collectors.toList());

            //Split the first line into two integers to get the dimensions of the board
            String[] dimensions = lines.get(0).split(" ");
            if (dimensions.length != 2) throw new RuntimeException("Invalid level dimensions.");

            //Parse the dimensions into rows and columns
            rows = Integer.parseInt(dimensions[0]);
            cols = Integer.parseInt(dimensions[1]);
            grid = new BoardElement[rows][cols]; //Initialize the grid

            //Iterate over the lines of the level file
            for (int i = 0; i < rows; i++) {
                String line = lines.get(i + 1); //Get the line from the list
                //Iterate over the columns of the line
                for (int j = 0; j < cols; j++) {
                    BoardElement element = BoardElement.fromSymbol(line.charAt(j)); //Convert the character to a BoardElement
                    grid[i][j] = element; //Set the element in the grid
                    if (element != null) //If the element is not empty
                    switch (element) {
                        case PLAYER -> playerPosition = new Point2D(j, i); //Set the player's position
                        case GHOST -> ghostPositions.add(new Point2D(j, i)); //Add the ghost's position to the list
                        default -> {
                        }
                    }
                }
            }
            initializeGhosts(); //Initialize the ghosts
        } catch (IOException e) {
            throw new RuntimeException("Failed to load level: " + levelPath, e); //Throw an exception in case of any error
        }
    }

    /**
     * Initializes the ghosts based on their positions on the board.
     * Creates new Ghost instances and adds them to the ghost list.
     */
    private void initializeGhosts() {
        for (Point2D pos : ghostPositions) {
            ghosts.add(new Ghost((int) pos.getX(), (int) pos.getY())); //Create a new Ghost instance and add it to the ghost list
        }
    }

    /**
     * Creates and returns a GridPane representing the visual view of the board.
     * Each non-empty cell is rendered as an ImageView with the appropriate sprite.
     * @return a GridPane containing the visual representation of the board
     */
    public GridPane createBoardView() {
        GridPane gridPane = new GridPane(); //Create a new GridPane

        //Iterate over the board
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                BoardElement element = grid[i][j]; //Get the element at the current position
                if (element == BoardElement.EMPTY) continue; //Skip empty cells and player
                ImageView imageView; //Create a new ImageView
                switch (element) {
                    //If the player can eat the ghost return the eatable sprite, else the default ghost sprite. 
                    case GHOST -> imageView = new ImageView(ResourceManager.getSprite(element, canEatGhosts)); //Get the "canEatGhosts" sprite
                    case PLAYER -> imageView = new ImageView(ResourceManager.getSprite(currentDirection)); //Get the rotated sprite for the player
                    default -> imageView = new ImageView(ResourceManager.getSprite(element, false)); //Get the sprite for the other elements
                }
                imageView.setFitWidth(CELL_SIZE); //Set the width and height
                imageView.setFitHeight(CELL_SIZE);
                gridPane.add(imageView, j, i); //Add the image view to the grid pane
            }
        }
        return gridPane;
    }

    /**
     * Moves the player in the specified direction if possible.
     * Updates the player's position, direction, and handles interactions with the board.
     * @param direction the direction in which the player is moving
     * @return true if the move is successful, false otherwise
     */
    public boolean movePlayer(Direction direction) {
        previousPlayerPosition = playerPosition; //Store previous position
        int newX = (int) playerPosition.getX() + direction.getDx(); //Calculate the new x position
        int newY = (int) playerPosition.getY() + direction.getDy(); //Calculate the new y position
        this.currentDirection = direction; //Update current direction for rotation
        BoardElement nextElement = grid[newY][newX]; //Get the element at the new position
        if (nextElement == BoardElement.WALL) return false; //Check walls, return false if there is a wall

        //Restore the previous position under the player, if it was not null then set the cell to the previous element, otherwise set it to empty.
        if (previousElementUnderPlayer != null) grid[(int) previousPlayerPosition.getY()][(int) previousPlayerPosition.getX()] = previousElementUnderPlayer;
        else grid[(int) previousPlayerPosition.getY()][(int) previousPlayerPosition.getX()] = BoardElement.EMPTY;

        playerPosition = new Point2D(newX, newY); //Update the player's position

        //Handle interactions based on the next element
        switch (nextElement) {
            case POINT -> { //If we got a coin then add 10 points and set the cell to empty
                playerScore += 10;
                previousElementUnderPlayer = BoardElement.EMPTY;
            }
            case KEY -> { //If we got a key then open the gate and set the cell to empty
                gateOpen = true;
                previousElementUnderPlayer = BoardElement.EMPTY;
            }
            case GATE -> { //If we got a gate then check if the gate is open, if it is then set the game over flag to true, otherwise just keep the gate.
                if (gateOpen) {
                    hasWon = true;
                    isGameOver = true;
                    previousElementUnderPlayer = BoardElement.EMPTY;
                } else previousElementUnderPlayer = BoardElement.GATE; // If the player does not have the key then redraw the gate.
            }
            case GHOST -> {
                if(canEatGhosts) break;//If we can eat ghosts we continue (logic is gonna be checked by isCollisionWithGhost)
                isGameOver = true; //Set game over if player collides with a ghost and can't currently eat ghosts.
                return false;
            }
            case APPLE -> { //If we got an apple activate the eatable effect and set remove the apple from the cell
                activateGhostEatableEffect();
                previousElementUnderPlayer = BoardElement.EMPTY;
            }
            default -> previousElementUnderPlayer = BoardElement.EMPTY; //Ensure it's set to empty for other elements
        }

        grid[newY][newX] = BoardElement.PLAYER; //Place player in the new position
        return !isCollisionWithGhost();  //Return true if the move was successfull (no collision with ghosts)
    }

    /**
     * Activates the effect that allows Pac-Man to eat ghosts for a few seconds.
     */
    private void activateGhostEatableEffect() {
        canEatGhosts = true; //Can eat ghosts from now on
        double baseDuration = 4000.0 / gameSpeed; //Duration is 4000 milliseconds divided by the game speed (1.0, 2.0 or 3.0)
        eatGhostsEndTime = System.currentTimeMillis() + (long) baseDuration; //Reset the effect duration
    }

    /**
     * Updates the status of the ghost eatable effect based on the current time.
     */
    protected void updateGhostEatableEffect() {
        if (canEatGhosts && System.currentTimeMillis() >= eatGhostsEndTime) { //If the effect is still active and the time has passed 
            canEatGhosts = false; //Disable the ability to eat ghosts
        }
    }

    /**
     * Resets the board state
     */
    public void resetBoardState() {
        canEatGhosts = false; //Reset the ability to eat ghosts
        eatGhostsEndTime = 0; //Reset the end time for the effect
    }

    /**
     * Moves all ghosts on the board.
     * Updates the ghosts' positions and handles interactions with the board.
     */
    public void moveGhosts() {
        updateGhostEatableEffect(); //Update the eatable effect status
        for (Ghost ghost : new ArrayList<>(ghosts)) { //Use a copy of the list to avoid concurrent modification
            boolean validMove = false; //Flag to check if the move is valid
            Direction randomDirection;
            int newX = 0, newY = 0; //New x and y positions
            BoardElement nextElement = null; //Next element

            while (!validMove) { //While we don't have a valid move
                randomDirection = getRandomDirection(); //Generate a random direction
                newX = ghost.getX() + randomDirection.getDx(); //Calculate the new x position
                newY = ghost.getY() + randomDirection.getDy(); //Calculate the new y position
                nextElement = grid[newY][newX]; //Get the element at the new position
                if (nextElement == BoardElement.WALL) continue; //If the move is into a wall then continue (not a valid move)
                validMove = true; // We passed the checks, move is valid.
            }

            // If the ghost was on a point, key or gate then restore the element
            BoardElement currentElement = ghost.getCurrentElement();
            if (currentElement == BoardElement.POINT || currentElement == BoardElement.KEY || currentElement == BoardElement.GATE || currentElement == BoardElement.APPLE) {
                grid[ghost.getY()][ghost.getX()] = ghost.getCurrentElement();
            } 
            else grid[ghost.getY()][ghost.getX()] = BoardElement.EMPTY; //If the ghost was on nothing then set the cell to empty

            ghost.setPosition(newX, newY); //Update the ghost's position
            ghost.setCurrentElement(nextElement); //Track the element the ghost is currently on
            grid[newY][newX] = BoardElement.GHOST; //Place the ghost in the new position on the grid
        }

        isCollisionWithGhost(); //Check for overlapping positions (collision with ghosts)
    }

    /**
     * Helper method to get a random direction
     * @return a random direction
     */
    private Direction getRandomDirection() {
        Direction[] directions = Direction.values(); //Get all the directions
        return directions[random.nextInt(directions.length)]; //Return a random direction
    }

    /**
     * Checks if the game is over due to a collision between the player and any ghost.
     * @return true if a collision is detected, false otherwise
     */
    private boolean isCollisionWithGhost() {
        for (Ghost ghost : ghosts) {
            if (ghost != null && ghost.getX() == (int) playerPosition.getX() && ghost.getY() == (int) playerPosition.getY()) {
                if(canEatGhosts){
                    ghosts.remove(ghost); //Remove ghost from the list
                    playerScore += 50; //Increase player score
                } 
                else {
                    hasWon = false; // Set the game over flag to false  
                    isGameOver = true; // Set game over due to collision
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the game is over.
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Checks if the player has won the game.
     * @return true if the player has won, false otherwise
     */
    public boolean hasWon() {
        return hasWon;
    }

    /**
     * Gets the player's current score.
     * @return the player's current score
     */
    public int getPlayerScore() {
        return playerScore;
    }

    /**
     * Gets the number of rows in the board.
     * @return the number of rows in the board
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns in the board.
     * @return the number of columns in the board
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns the BoardElement at the specified coordinates.
     * @param row the row index
     * @param col the column index
     * @return the BoardElement at the specified position
     */
    public BoardElement getElement(int row, int col) {
        return grid[row][col];
    }

    /**
     * Checks if the gate is open.
     * @return true if the gate is open, false otherwise
     */
    public boolean isGateOpen() {
        return gateOpen;
    }

    /**
     * Sets the game speed.
     * @param speed the speed to set
     */
    public void setGameSpeed(double speed) {
        this.gameSpeed = speed;
    }
}