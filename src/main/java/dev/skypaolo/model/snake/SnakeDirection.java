package dev.skypaolo.model.snake;

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

    public boolean isOpposite(SnakeDirection other) {
        if (other == null) return false;
        return (this == UP && other == DOWN) ||
               (this == DOWN && other == UP) ||
               (this == LEFT && other == RIGHT) ||
               (this == RIGHT && other == LEFT);
    }

    public static SnakeDirection getRandom() {
        SnakeDirection[] directions = values();
        return directions[(int) (Math.random() * directions.length)];
    }
}
