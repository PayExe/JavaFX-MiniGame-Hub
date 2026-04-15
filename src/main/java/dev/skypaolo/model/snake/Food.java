package dev.skypaolo.model.snake;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Vault-Tec Model: Nutritional Supplement (Food)
 * Represents the sustenance that sustains our serpentine subject.
 *
 * S.P.E.C.I.A.L. Stats:
 * - Luck: Random spawn positioning
 * - Perception: Avoids spawning on occupied cells
 * - Intelligence: Smart retry logic for valid placement
 *
 * Warning: May contain traces of Nuka-Cola Quantum.
 */
public class Food {
    private SnakePoint position;
    private int value;
    private static final Random random = ThreadLocalRandom.current();

    /**
     * Create food at a specific position
     *
     * @param position Grid position
     * @param value Points awarded for consumption
     */
    public Food(SnakePoint position, int value) {
        this.position = position;
        this.value = value;
    }

    /**
     * Create food at a random position with default value (10)
     *
     * @param gridWidth Grid width
     * @param gridHeight Grid height
     * @param snakeBody Snake body to avoid
     */
    public Food(int gridWidth, int gridHeight, List<SnakePoint> snakeBody) {
        this.value = 10;
        // Only respawn if valid dimensions provided
        if (gridWidth > 0 && gridHeight > 0) {
            respawn(gridWidth, gridHeight, snakeBody);
        } else {
            // Default position, will be properly set later
            this.position = new SnakePoint(0, 0);
        }
    }

    public SnakePoint getPosition() {
        return position;
    }

    public void setPosition(SnakePoint position) {
        this.position = position;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Respawn food at a new random location
     *
     * @param gridWidth Grid width
     * @param gridHeight Grid height
     * @param snakeBody Snake body segments to avoid
     */
    public void respawn(int gridWidth, int gridHeight, List<SnakePoint> snakeBody) {
        // Validate grid dimensions
        if (gridWidth <= 0 || gridHeight <= 0) {
            System.err.println("[VT-OS WARNING] Invalid grid dimensions for food respawn: " + gridWidth + "x" + gridHeight);
            return;
        }

        Set<SnakePoint> occupied = Set.copyOf(snakeBody);
        SnakePoint newPos;
        int attempts = 0;
        int maxAttempts = Math.max(100, gridWidth * gridHeight * 2);

        do {
            newPos = new SnakePoint(
                random.nextInt(gridWidth),
                random.nextInt(gridHeight)
            );
            attempts++;
            // Prevent infinite loop if grid is almost full
            if (attempts > maxAttempts) {
                // Find first available spot the hard way
                newPos = findFirstAvailable(gridWidth, gridHeight, occupied);
                break;
            }
        } while (occupied.contains(newPos));

        this.position = newPos;
    }

    /**
     * Find first available cell by scanning grid
     * Fallback method when random selection is struggling
     */
    private SnakePoint findFirstAvailable(int gridWidth, int gridHeight, Set<SnakePoint> occupied) {
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                SnakePoint candidate = new SnakePoint(x, y);
                if (!occupied.contains(candidate)) {
                    return candidate;
                }
            }
        }
        // Grid completely full - this shouldn't happen in normal gameplay
        return new SnakePoint(0, 0);
    }

    /**
     * Check if food is at the given position
     *
     * @param point Position to check
     * @return true if food is at this position
     */
    public boolean isAt(SnakePoint point) {
        return position.equals(point);
    }

    @Override
    public String toString() {
        return "Food{" +
               "position=" + position +
               ", value=" + value +
               '}';
    }
}
