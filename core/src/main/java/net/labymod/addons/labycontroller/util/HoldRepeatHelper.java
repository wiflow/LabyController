package net.labymod.addons.labycontroller.util;

/**
 * Helper class for handling hold-to-repeat input behavior.
 * Based on Controlify's implementation.
 *
 * On first press, the action triggers immediately.
 * If held, there's an initial delay before repeating starts.
 * After that, the action repeats at a faster interval.
 */
public class HoldRepeatHelper {
    private final int initialDelay;
    private final int repeatDelay;
    private int currentDelay;
    private boolean hasResetThisTick = false;

    /**
     * Create a new HoldRepeatHelper.
     * @param initialDelay Ticks to wait before first repeat (after initial press)
     * @param repeatDelay Ticks between repeats after initial delay
     */
    public HoldRepeatHelper(int initialDelay, int repeatDelay) {
        this.initialDelay = initialDelay;
        this.repeatDelay = repeatDelay;
        this.currentDelay = 0;
    }

    /**
     * Check if navigation can occur (delay has passed).
     * Decrements the delay counter.
     */
    public boolean canNavigate() {
        return --currentDelay <= 0;
    }

    /**
     * Reset the delay to the initial value.
     * Called on first press.
     */
    public void reset() {
        currentDelay = initialDelay;
        hasResetThisTick = true;
    }

    /**
     * Clear the delay entirely.
     */
    public void clearDelay() {
        currentDelay = 0;
    }

    /**
     * Called after a navigation action occurs.
     * Sets the delay to the repeat interval (unless just reset).
     */
    public void onNavigate() {
        if (!hasResetThisTick) {
            currentDelay = repeatDelay;
        } else {
            hasResetThisTick = false;
        }
    }

    /**
     * Check if action should trigger based on button state.
     * Handles both initial press and hold-to-repeat.
     *
     * @param pressedNow Is the button currently pressed
     * @param pressedPrev Was the button pressed last tick
     * @return true if the action should trigger
     */
    public boolean shouldAction(boolean pressedNow, boolean pressedPrev) {
        boolean shouldAction = pressedNow && (canNavigate() || !pressedPrev);
        if (shouldAction && !pressedPrev) {
            reset();
        }
        return shouldAction;
    }
}
