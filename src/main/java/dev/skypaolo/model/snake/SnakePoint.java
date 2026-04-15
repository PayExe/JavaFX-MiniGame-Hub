package dev.skypaolo.model.snake;

import java.util.Objects;

public class SnakePoint {
    private int x;
    private int y;

    public SnakePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

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

    public SnakePoint translate(SnakeDirection direction) {
        return new SnakePoint(
            this.x + direction.getDeltaX(),
            this.y + direction.getDeltaY()
        );
    }

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
