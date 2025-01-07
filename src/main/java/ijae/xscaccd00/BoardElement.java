package ijae.xscaccd00;

/**
 * Represents the different elements that can appear on the game board.
 * Each element is associated with a unique symbol used in the level configuration.
 * Available elements:
 * <ul>
 *   <li>WALL - Represents a wall that cannot be traversed.</li>
 *   <li>GATE - Represents a gate that can be opened(with the key) to win the game.</li>
 *   <li>EMPTY - Represents an empty space.</li>
 *   <li>PLAYER - Represents the player's character.</li>
 *   <li>GHOST - Represents an enemy.</li>
 *   <li>KEY - Represents a key that can be collected (to open the gate).</li>
 *   <li>POINT - Represents a coin that can be collected.</li>
 *   <li>APPLE - Represents an eatable that allows Pac-Man to eat ghosts temporarily.</li>
 *   <li>GHOST_EATABLE - Represents a ghost that is currently eatable.</li>
 * </ul>
 * 
 * @author Davide Scaccia
 */
public enum BoardElement {
    WALL('W'), //Represent a wall to limit the area where the player can move
    GATE('G'), //Represent a gate to win the game
    EMPTY('.'), //Represent an empty space
    PLAYER('P'), //Represent the player's character
    GHOST('C'), //Represent an enemy
    KEY('K'), //Represent a key to open the gate
    POINT('o'), //Represent a coin to collect
    APPLE('A'), //Represent an apple that allows eating ghosts
    GHOST_EATABLE('E'); //Represent a ghost that is currently eatable
    private final char symbol; //The symbol associated with the board element

    /**
     * Constructs the BoardElement with the specified symbol.
     * @param symbol the character symbol representing the board element
     */
    BoardElement(char symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the BoardElement corresponding to the given symbol. (e.g. 'W' -> WALL, 'G' -> GATE, etc.)
     * @param symbol the character symbol to look up
     * @return the corresponding BoardElement
     * @throws IllegalArgumentException if the symbol does not correspond to any board elements
     */
    public static BoardElement fromSymbol(char symbol) {
        for (BoardElement element : values()) //For each board element
            if (element.symbol == symbol) return element; //Return the board element if the symbol is found
        throw new IllegalArgumentException("Unknown symbol: " + symbol); //If the symbol is not found, throw an exception
    }
}