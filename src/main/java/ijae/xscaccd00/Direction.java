package ijae.xscaccd00;

/**
 * The Direction enum represents the four possible movement directions
 * for entities within the game board. Each direction is associated with a
 * change in the x-coordinate (dx) and y-coordinate (dy).
 * 
 * @author Davide Scaccia
 */
public enum Direction {
    UP(0, -1), //Upward movement
    DOWN(0, 1), //Downward movement
    LEFT(-1, 0), //Leftward movement
    RIGHT(1, 0); //Rightward movement

    private final int dx; //The change in the x-coordinate when moving in this direction
    private final int dy; //The change in the y-coordinate when moving in this direction

    /**
     * Constructs the Direction with the specified changes in coordinates.
     * @param dx the change in the x-coordinate
     * @param dy the change in the y-coordinate
     */
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns the change in the x-coordinate associated with this direction.
     * @return the x-coordinate change
     */
    public int getDx() {
        return dx;
    }

    /**
     * Returns the change in the y-coordinate associated with this direction.
     * @return the y-coordinate change
     */
    public int getDy() {
        return dy;
    }
}