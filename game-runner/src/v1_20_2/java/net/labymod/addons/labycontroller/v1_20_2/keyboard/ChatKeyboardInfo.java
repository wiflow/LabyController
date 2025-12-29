package net.labymod.addons.labycontroller.v1_20_2.keyboard;

public interface ChatKeyboardInfo {
    float labycontroller$getKeyboardShift();
    static float getKeyboardShift(Object screen) {
        if (screen instanceof ChatKeyboardInfo info) return info.labycontroller$getKeyboardShift();
        return 0f;
    }
}
