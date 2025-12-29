package net.labymod.addons.labycontroller.util;

public class HoldRepeatHelper {
    private final int initialDelay;
    private final int repeatDelay;
    private int currentDelay;
    private boolean hasResetThisTick = false;

    public HoldRepeatHelper(int initialDelay, int repeatDelay) {
        this.initialDelay = initialDelay;
        this.repeatDelay = repeatDelay;
        this.currentDelay = 0;
    }

    public boolean canNavigate() { return --currentDelay <= 0; }
    public void reset() { currentDelay = initialDelay; hasResetThisTick = true; }
    public void clearDelay() { currentDelay = 0; }

    public void onNavigate() {
        if (!hasResetThisTick) currentDelay = repeatDelay;
        else hasResetThisTick = false;
    }

    public boolean shouldAction(boolean pressedNow, boolean pressedPrev) {
        boolean shouldAction = pressedNow && (canNavigate() || !pressedPrev);
        if (shouldAction && !pressedPrev) reset();
        return shouldAction;
    }
}
