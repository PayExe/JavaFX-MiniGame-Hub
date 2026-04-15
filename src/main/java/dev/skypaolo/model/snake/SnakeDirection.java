package dev.skypaolo.model.snake;

/**
 * Vault-Tec Enumeration: Snake Direction
 * Represents the four cardinal directions the serpent may traverse.
 *
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Opposite direction calculation prevents self-collision
 * - Perception: Clear directional mapping for input handling
 *
 * Vault-Tec Safety Notice: 180-degree turns are strictly prohibited
 * per Vault-Tec Directive 7-B: "Serpentine entities must not consume themselves."
 */
public enum SnakeDirection {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int deltaX;
    private final int deltaY;

    SnakeDirection(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    /**
     * Determine if this direction is opposite to another.
     * Essential for preventing the snake from reversing into itself.
     *
     * @param other The direction to compare
     * @return true if directions are opposite
     */
    public boolean isOpposite(SnakeDirection other) {
        if (other == null) return false;
        return (this == UP && other == DOWN) ||
               (this == DOWN && other == UP) ||
               (this == LEFT && other == RIGHT) ||
               (this == RIGHT && other == LEFT);
    }

    /**
     * Get a random direction for initial spawn or AI.
     * Vault-Tec recommends: Always spawn moving away from walls!
     *
     * @return Random direction
     */
    public static SnakeDirection getRandom() {
        SnakeDirection[] directions = values();
        return directions[(int) (Math.random() * directions.length)];
    }
}
