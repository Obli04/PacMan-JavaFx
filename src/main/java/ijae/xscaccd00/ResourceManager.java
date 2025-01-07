package ijae.xscaccd00;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * This class handles loading sprites from files, and rotating pacman based on direction
 * 
 * @author Davide Scaccia
 */
public class ResourceManager {
    private static final Map<BoardElement, Image> sprites = new HashMap<>(); //Map of sprites for each board element
    private static final Map<Direction, Image> rotatedPlayerSprites = new HashMap<>(); //Map of rotated sprites for the player
    private static final int SPRITE_SIZE = 32; //The size of the sprites

    //Call the loadSpritesFromFiles method when the class is loaded
    static {
        loadSpritesFromFiles();
    }

    /**
     * Loads sprites from predefined from the assets folder
     */
    private static void loadSpritesFromFiles() {
        for (String element : new String[]{"WALL", "PLAYER", "GHOST", "KEY", "POINT", "GATE", "EMPTY", "APPLE", "GHOST_EATABLE"}) {
            String imagePath = "";
            switch (element) {
                case "WALL" -> imagePath = "assets/wall.jpeg"; //Wall sprite
                case "PLAYER" -> imagePath = "assets/pacman.png"; //Pacman sprite
                case "GHOST" -> imagePath = "assets/ghost.png"; //Ghost sprite
                case "KEY" -> imagePath = "assets/key.gif"; //Key sprite
                case "POINT" -> imagePath = "assets/point.png"; //Coin sprite
                case "GATE" -> imagePath = "assets/gate.png"; //Gate sprite
                case "APPLE" -> imagePath = "assets/apple.png"; //Apple sprite
                case "GHOST_EATABLE" -> imagePath = "assets/ghost_eatable.png"; //Ghost eatable sprite
                case "EMPTY" -> { 
                    createEmptySprite();
                    continue;
                }
            }
            var resourceUrl = ResourceManager.class.getClassLoader().getResource(imagePath); //Get the resource full path
            Image img = new Image(resourceUrl.toExternalForm(), SPRITE_SIZE, SPRITE_SIZE, true, false); //Create the image
            sprites.put(BoardElement.valueOf(element), img); //Put the image in the sprites map
            
            //If the element is PLAYER we create rotated sprites for each direction
            if (element.equals("PLAYER")) {
                for (Direction dir : Direction.values()) {
                    Image rotatedImg = rotateImage(img, getRotationAngle(dir)); //Rotate the image based on rotation angle
                    rotatedPlayerSprites.put(dir, rotatedImg); //Put the rotated image in the rotatedPlayerSprites map
                }
            }
        }
    }   

    /**
     * Rotates pacman by the specified angle.
     * @param img   The original pacman sprite.
     * @param angle The rotation angle in degrees.
     * @return A new Image object rotated by the specified angle.
     */
    private static Image rotateImage(Image img, double angle) {
        Canvas canvas = new Canvas(SPRITE_SIZE, SPRITE_SIZE); //Create a canvas with the size of the sprite
        GraphicsContext gc = canvas.getGraphicsContext2D(); //Get the graphics context
        gc.translate(SPRITE_SIZE / 2, SPRITE_SIZE / 2); //Translate the canvas to the center
        gc.rotate(angle); //Rotate the canvas by the specified angle
        gc.drawImage(img, -SPRITE_SIZE / 2, -SPRITE_SIZE / 2, SPRITE_SIZE, SPRITE_SIZE); //Draw the image on the canvas passing x,y,width,height
        SnapshotParameters params = new SnapshotParameters(); //Create a snapshot parameters object
        params.setFill(Color.TRANSPARENT); //Set the fill color to transparent
        return canvas.snapshot(params, null); //Return the snapshot of the canvas
    }

    /**
     * Returns the rotation angle based on the direction.
     * @param direction The direction.
     * @return The rotation angle in degrees.
     */
    private static double getRotationAngle(Direction direction) {
        return switch (direction) {
            case RIGHT -> 0;
            case UP -> -90;
            case LEFT -> 180;
            case DOWN -> 90;
            default -> 0;
        };
    }

    /**
     * Creates an empty sprite for the EMPTY board element.
     */
    private static void createEmptySprite() {
        Canvas canvas = new Canvas(SPRITE_SIZE, SPRITE_SIZE); //Create a canvas with the size of the sprite
        GraphicsContext gc = canvas.getGraphicsContext2D(); //Get the graphics context
        gc.setFill(Color.TRANSPARENT); //Set the fill color to transparent
        gc.fillRect(0, 0, SPRITE_SIZE, SPRITE_SIZE); //Fill the canvas with the transparent color
        sprites.put(BoardElement.EMPTY, canvas.snapshot(null, null)); //Put the snapshot of the canvas in the sprites map
    }

    /**
     * Method to retrieve the sprite for the player.
     * @param direction The direction of the player.
     * @return The corresponding Image.
     */
    public static Image getSprite(Direction direction) {
        return rotatedPlayerSprites.get(direction); //Return the rotated player sprite
    }

    /**
     * Overloaded method to retrieve the sprite for all other elements.
     * @param element The board element.
     * @param isEatable True if the ghost is eatable, false otherwise.
     * @return The corresponding Image.
     */
    public static Image getSprite(BoardElement element, boolean isEatable) {
        return isEatable ? sprites.get(BoardElement.GHOST_EATABLE) : sprites.get(element); //If the isEatable flag is true we return the ghost eatable, otherwise the element sprite.
    }
}