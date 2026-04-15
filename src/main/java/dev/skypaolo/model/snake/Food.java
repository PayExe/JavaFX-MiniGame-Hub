package dev.skypaolo.model.snake;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Food {
    private SnakePoint position;
    private int value;
    private static final Random random = ThreadLocalRandom.current();

    public Food(SnakePoint position, int value) {
        this.position = position;
        this.value = value;
    }

    public Food(int gridWidth, int gridHeight, List<SnakePoint> snakeBody) {
        this.value = 10;
        if (gridWidth > 0 && gridHeight > 0) {
            respawn(gridWidth, gridHeight, snakeBody);
        } else {
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

    public void respawn(int gridWidth, int gridHeight, List<SnakePoint> snakeBody) {
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
            if (attempts > maxAttempts) {
                newPos = findFirstAvailable(gridWidth, gridHeight, occupied);
                break;
            }
        } while (occupied.contains(newPos));

        this.position = newPos;
    }

    private SnakePoint findFirstAvailable(int gridWidth, int gridHeight, Set<SnakePoint> occupied) {
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                SnakePoint candidate = new SnakePoint(x, y);
                if (!occupied.contains(candidate)) {
                    return candidate;
                }
            }
        }
        return new SnakePoint(0, 0);
    }

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
