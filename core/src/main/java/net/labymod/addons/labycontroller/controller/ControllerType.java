package net.labymod.addons.labycontroller.controller;

public enum ControllerType {
    UNKNOWN("Unknown", "unknown"),
    XBOX("Xbox Controller", "xbox"),
    PLAYSTATION("PlayStation Controller", "playstation"),
    SWITCH("Nintendo Switch Controller", "switch"),
    STEAM_DECK("Steam Deck", "steamdeck"),
    GENERIC("Generic Gamepad", "generic");

    private final String displayName;
    private final String id;

    ControllerType(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

        public static ControllerType fromName(String name) {
        if (name == null) {
            return UNKNOWN;
        }

        String lowerName = name.toLowerCase();

        if (lowerName.contains("xbox") || lowerName.contains("microsoft")) {
            return XBOX;
        }
        if (lowerName.contains("playstation") || lowerName.contains("dualshock") ||
            lowerName.contains("dualsense") || lowerName.contains("ps4") || lowerName.contains("ps5")) {
            return PLAYSTATION;
        }
        if (lowerName.contains("nintendo") || lowerName.contains("switch") ||
            lowerName.contains("pro controller") || lowerName.contains("joy-con")) {
            return SWITCH;
        }
        if (lowerName.contains("steam deck") || lowerName.contains("steamdeck")) {
            return STEAM_DECK;
        }

        return GENERIC;
    }
}
