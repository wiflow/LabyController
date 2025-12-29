package net.labymod.addons.labycontroller.radial;

public enum RadialAction {
    NONE("None", "none"),
    HOTBAR_1("Hotbar 1", "hotbar_1"),
    HOTBAR_2("Hotbar 2", "hotbar_2"),
    HOTBAR_3("Hotbar 3", "hotbar_3"),
    HOTBAR_4("Hotbar 4", "hotbar_4"),
    HOTBAR_5("Hotbar 5", "hotbar_5"),
    HOTBAR_6("Hotbar 6", "hotbar_6"),
    HOTBAR_7("Hotbar 7", "hotbar_7"),
    HOTBAR_8("Hotbar 8", "hotbar_8"),
    HOTBAR_9("Hotbar 9", "hotbar_9"),
    TOGGLE_PERSPECTIVE("Toggle Perspective", "perspective"),
    DROP_ITEM("Drop Item", "drop"),
    DROP_STACK("Drop Stack", "drop_stack"),
    OPEN_CHAT("Open Chat", "chat"),
    OPEN_COMMAND("Open Command", "command"),
    TOGGLE_HUD("Toggle HUD", "toggle_hud");

    private final String displayName;
    private final String id;

    RadialAction(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }
}
