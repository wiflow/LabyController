package net.labymod.addons.labycontroller.v1_21_1.keyboard;

/**
 * Interface to get keyboard info from ChatScreen mixin.
 */
public interface ChatKeyboardInfo {

    /**
     * Get how much of the screen the keyboard takes up (0.0 to 1.0).
     */
    float labycontroller$getKeyboardShift();

    /**
     * Helper to safely get keyboard shift from any screen.
     */
    static float getKeyboardShift(Object screen) {
        if (screen instanceof ChatKeyboardInfo info) {
            return info.labycontroller$getKeyboardShift();
        }
        return 0f;
    }
}
