package dev.skypaolo.model.snake;

import java.util.Objects;

/**
 * Vault-Tec Model: Snake Point (Position)
 * Represents a discrete grid coordinate in the game arena.
 *
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Immutable position data structure
 * - Perception: Equals/hashCode for collision detection
 * - Endurance: Copy constructor for safe cloning
 *
 * Because even in the wasteland, coordinates matter.
 */
public class SnakePoint {
    private int x;
    private int y;

    public SnakePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor - Vault-Tec approved cloning mechanism
     */
    public SnakePoint(SnakePoint other) {
        this.x = other.x;
        this.y = other.y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Create a new point by applying a direction delta
     *
     * @param direction Direction to move
     * @return New point at the translated position
     */
    public SnakePoint translate(SnakeDirection direction) {
        return new SnakePoint(
            this.x + direction.getDeltaX(),
            this.y + direction.getDeltaY()
        );
    }

    /**
     * Check if this point is within grid boundaries
     *
     * @param width Grid width
     * @param height Grid height
     * @return true if within bounds
     */
    public boolean isWithinBounds(int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnakePoint that = (SnakePoint) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "SnakePoint{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }
}
