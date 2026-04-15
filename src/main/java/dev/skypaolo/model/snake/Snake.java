package dev.skypaolo.model.snake;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Vault-Tec Model: The Serpent
 * Manages the snake's body segments using a Deque for O(1) head/tail operations.
 *
 * S.P.E.C.I.A.L. Stats:
 * - Strength: Efficient O(1) movement operations
 * - Perception: Collision detection via HashSet
 * - Endurance: Robust body segment management
 * - Agility: Buffered direction changes for responsive input
 *
 * Warning: Do not feed after midnight. Or during atomic winter.
 */
public class Snake {
    private Deque<SnakePoint> body;
    private SnakeDirection currentDirection;
    private SnakeDirection nextDirection;
    private boolean growPending;

    /**
     * Create a new snake at the specified position
     *
     * @param startX Initial X position
     * @param startY Initial Y position
     * @param initialLength Starting length (minimum 1)
     * @param initialDirection Initial facing direction
     */
    public Snake(int startX, int startY, int initialLength, SnakeDirection initialDirection) {
        this.body = new ArrayDeque<>();
        this.currentDirection = initialDirection;
        this.nextDirection = initialDirection;
        this.growPending = false;

        // Build initial body extending opposite to direction
        int dx = -initialDirection.getDeltaX();
        int dy = -initialDirection.getDeltaY();

        for (int i = 0; i < initialLength; i++) {
            body.addLast(new SnakePoint(startX + dx * i, startY + dy * i));
        }
    }

    /**
     * Get the head position (front of the snake)
     *
     * @return Head point
     */
    public SnakePoint getHead() {
        return body.peekFirst();
    }

    /**
     * Get the tail position (back of the snake)
     *
     * @return Tail point
     */
    public SnakePoint getTail() {
        return body.peekLast();
    }

    /**
     * Get all body segments (head first)
     *
     * @return Body segments
     */
    public Deque<SnakePoint> getBody() {
        return new ArrayDeque<>(body);
    }

    /**
     * Get current length of the snake
     *
     * @return Number of segments
     */
    public int getLength() {
        return body.size();
    }

    /**
     * Get the current direction
     *
     * @return Current direction
     */
    public SnakeDirection getCurrentDirection() {
        return currentDirection;
    }

    /**
     * Queue a direction change. Will be applied on next move() if valid.
     * Prevents 180-degree turns which would cause immediate self-collision.
     *
     * @param newDirection Desired new direction
     */
    public void queueDirectionChange(SnakeDirection newDirection) {
        // Prevent reversing directly into self
        if (!newDirection.isOpposite(currentDirection)) {
            this.nextDirection = newDirection;
        }
    }

    /**
     * Move the snake one step forward.
     * Applies queued direction change, moves head, optionally removes tail.
     *
     * @return The new head position after movement
     */
    public SnakePoint move() {
        // Apply queued direction
        currentDirection = nextDirection;

        // Calculate new head position
        SnakePoint newHead = getHead().translate(currentDirection);

        // Add new head
        body.addFirst(newHead);

        // Remove tail unless growing
        if (!growPending) {
            body.removeLast();
        } else {
            growPending = false;
        }

        return newHead;
    }

    /**
     * Schedule the snake to grow on next move
     */
    public void grow() {
        this.growPending = true;
    }

    /**
     * Check if the snake collides with itself.
     * Occurs when head position matches any body segment.
     *
     * @return true if self-collision detected
     */
    public boolean collidesWithSelf() {
        SnakePoint head = getHead();
        Set<SnakePoint> bodySet = new HashSet<>(body);
        // If there's a duplicate, head is colliding with body
        return bodySet.size() < body.size();
    }

    /**
     * Check if the snake's head is outside grid boundaries
     *
     * @param gridWidth Grid width
     * @param gridHeight Grid height
     * @return true if wall collision detected
     */
    public boolean collidesWithWall(int gridWidth, int gridHeight) {
        return !getHead().isWithinBounds(gridWidth, gridHeight);
    }

    /**
     * Check if a point is occupied by any snake segment
     *
     * @param point Point to check
     * @return true if occupied
     */
    public boolean occupies(SnakePoint point) {
        return body.contains(point);
    }

    /**
     * Get all body segments as a Set for O(1) collision checks
     *
     * @return Set of occupied points
     */
    public Set<SnakePoint> getBodyAsSet() {
        return new HashSet<>(body);
    }

    @Override
    public String toString() {
        return "Snake{" +
               "length=" + getLength() +
               ", head=" + getHead() +
               ", direction=" + currentDirection +
               '}';
    }
}
