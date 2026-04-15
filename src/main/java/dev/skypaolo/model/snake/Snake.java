package dev.skypaolo.model.snake;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class Snake {

    private Deque<SnakePoint> body;
    private SnakeDirection currentDirection;
    private SnakeDirection nextDirection;
    private boolean growPending;

    public Snake(
        int startX,
        int startY,
        int initialLength,
        SnakeDirection initialDirection
    ) {
        this.body = new ArrayDeque<>();
        this.currentDirection = initialDirection;
        this.nextDirection = initialDirection;
        this.growPending = false;

        int dx = -initialDirection.getDeltaX();
        int dy = -initialDirection.getDeltaY();

        for (int i = 0; i < initialLength; i++) {
            body.addLast(new SnakePoint(startX + dx * i, startY + dy * i));
        }
    }

    public SnakePoint getHead() {
        return body.peekFirst();
    }

    public SnakePoint getTail() {
        return body.peekLast();
    }

    public Deque<SnakePoint> getBody() {
        return new ArrayDeque<>(body);
    }

    public int getLength() {
        return body.size();
    }

    public SnakeDirection getCurrentDirection() {
        return currentDirection;
    }

    public void queueDirectionChange(SnakeDirection newDirection) {
        if (!newDirection.isOpposite(currentDirection)) {
            this.nextDirection = newDirection;
        }
    }

    public SnakePoint move() {
        currentDirection = nextDirection;

        SnakePoint newHead = getHead().translate(currentDirection);

        body.addFirst(newHead);

        if (!growPending) {
            body.removeLast();
        } else {
            growPending = false;
        }

        return newHead;
    }

    public void grow() {
        this.growPending = true;
    }

    public boolean collidesWithSelf() {
        // SnakePoint head = getHead();
        Set<SnakePoint> bodySet = new HashSet<>(body);
        return bodySet.size() < body.size();
    }

    public boolean collidesWithWall(int gridWidth, int gridHeight) {
        return !getHead().isWithinBounds(gridWidth, gridHeight);
    }

    public boolean occupies(SnakePoint point) {
        return body.contains(point);
    }

    public Set<SnakePoint> getBodyAsSet() {
        return new HashSet<>(body);
    }

    @Override
    public String toString() {
        return (
            "Snake{" +
            "length=" +
            getLength() +
            ", head=" +
            getHead() +
            ", direction=" +
            currentDirection +
            '}'
        );
    }
}
