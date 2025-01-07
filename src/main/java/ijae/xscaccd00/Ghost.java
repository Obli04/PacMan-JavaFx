package ijae.xscaccd00;

/**
 * The Ghost class represents the "enemy" in the game.<br>
 * This class is used to create the ghosts and manage their position on the board.
 * @author Davide Scaccia
 */
public class Ghost {
    private int x; //The current x-coordinate of the ghost on the game board.
    private int y; //The current y-coordinate of the ghost on the game board.
    private BoardElement currentElement; // Track the element the ghost is currently on (e.g. EMPTY, GATE, COIN, etc.)

    /**
     * Constructs a Ghost with the specified initial position.
     * @param x the initial x-coordinate of the ghost
     * @param y the initial y-coordinate of the ghost
     */
    public Ghost(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retrieves the x-coordinate of the ghost.
     * @return the x-coordinate of the ghost
     */
    public int getX() { 
        return x; 
    }

    /**
     * Retrieves the y-coordinate of the ghost.
     * @return the y-coordinate of the ghost
     */
    public int getY() { 
        return y; 
    }

    /**
     * Sets the ghost's position to the specified coordinates.
     * @param x the new x-coordinate for the ghost
     * @param y the new y-coordinate for the ghost
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the current element the ghost is on
     */ 
    public BoardElement getCurrentElement() {
        return currentElement;
    }

    /**
     * Sets the current element the ghost is on
     * @param currentElement the element the ghost is on
     */
    public void setCurrentElement(BoardElement currentElement) {
        this.currentElement = currentElement;
    }
}