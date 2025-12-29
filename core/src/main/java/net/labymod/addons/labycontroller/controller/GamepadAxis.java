package net.labymod.addons.labycontroller.controller;

public enum GamepadAxis {
    LEFT_STICK_X("Left Stick X", false),
    LEFT_STICK_Y("Left Stick Y", false),
    RIGHT_STICK_X("Right Stick X", false),
    RIGHT_STICK_Y("Right Stick Y", false),
    LEFT_TRIGGER("Left Trigger", true),
    RIGHT_TRIGGER("Right Trigger", true);

    private final String displayName;
    private final boolean isTrigger;

    GamepadAxis(String displayName, boolean isTrigger) {
        this.displayName = displayName;
        this.isTrigger = isTrigger;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTrigger() {
        return isTrigger;
    }

    public static int getAxisCount() {
        return values().length;
    }
}
