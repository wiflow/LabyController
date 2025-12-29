package net.labymod.addons.labycontroller.controller;

public enum GamepadButton {
    // Face buttons
    A("A", "Cross"),
    B("B", "Circle"),
    X("X", "Square"),
    Y("Y", "Triangle"),

    // Shoulder buttons
    LEFT_BUMPER("LB", "L1"),
    RIGHT_BUMPER("RB", "R1"),

    // Menu buttons
    BACK("Back", "Select"),
    START("Start", "Options"),
    GUIDE("Guide", "PS"),

    // Stick buttons
    LEFT_STICK("LS", "L3"),
    RIGHT_STICK("RS", "R3"),

    // D-Pad
    DPAD_UP("D-Up", "D-Up"),
    DPAD_DOWN("D-Down", "D-Down"),
    DPAD_LEFT("D-Left", "D-Left"),
    DPAD_RIGHT("D-Right", "D-Right");

    private final String xboxName;
    private final String playstationName;

    GamepadButton(String xboxName, String playstationName) {
        this.xboxName = xboxName;
        this.playstationName = playstationName;
    }

    public String getXboxName() {
        return xboxName;
    }

    public String getPlaystationName() {
        return playstationName;
    }

    public String getDisplayName(ControllerType type) {
        return switch (type) {
            case PLAYSTATION -> playstationName;
            default -> xboxName;
        };
    }

    public static int getButtonCount() {
        return values().length;
    }
}
