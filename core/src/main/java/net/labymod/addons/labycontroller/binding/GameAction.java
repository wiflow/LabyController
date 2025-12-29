package net.labymod.addons.labycontroller.binding;

public enum GameAction {
    // Movement
    MOVE_FORWARD("key.forward", "Move Forward", Category.MOVEMENT),
    MOVE_BACKWARD("key.back", "Move Backward", Category.MOVEMENT),
    MOVE_LEFT("key.left", "Strafe Left", Category.MOVEMENT),
    MOVE_RIGHT("key.right", "Strafe Right", Category.MOVEMENT),
    JUMP("key.jump", "Jump", Category.MOVEMENT),
    SNEAK("key.sneak", "Sneak", Category.MOVEMENT),
    SPRINT("key.sprint", "Sprint", Category.MOVEMENT),

    // Camera (special - uses analog)
    LOOK_UP("controlify.look_up", "Look Up", Category.CAMERA),
    LOOK_DOWN("controlify.look_down", "Look Down", Category.CAMERA),
    LOOK_LEFT("controlify.look_left", "Look Left", Category.CAMERA),
    LOOK_RIGHT("controlify.look_right", "Look Right", Category.CAMERA),

    // Combat & Interaction
    ATTACK("key.attack", "Attack/Destroy", Category.GAMEPLAY),
    USE("key.use", "Use Item/Place Block", Category.GAMEPLAY),
    PICK_BLOCK("key.pickItem", "Pick Block", Category.GAMEPLAY),

    // Inventory & Items
    INVENTORY("key.inventory", "Open Inventory", Category.INVENTORY),
    DROP_ITEM("key.drop", "Drop Item", Category.INVENTORY),
    SWAP_HANDS("key.swapOffhand", "Swap Hands", Category.INVENTORY),
    HOTBAR_1("key.hotbar.1", "Hotbar Slot 1", Category.INVENTORY),
    HOTBAR_2("key.hotbar.2", "Hotbar Slot 2", Category.INVENTORY),
    HOTBAR_3("key.hotbar.3", "Hotbar Slot 3", Category.INVENTORY),
    HOTBAR_4("key.hotbar.4", "Hotbar Slot 4", Category.INVENTORY),
    HOTBAR_5("key.hotbar.5", "Hotbar Slot 5", Category.INVENTORY),
    HOTBAR_6("key.hotbar.6", "Hotbar Slot 6", Category.INVENTORY),
    HOTBAR_7("key.hotbar.7", "Hotbar Slot 7", Category.INVENTORY),
    HOTBAR_8("key.hotbar.8", "Hotbar Slot 8", Category.INVENTORY),
    HOTBAR_9("key.hotbar.9", "Hotbar Slot 9", Category.INVENTORY),
    HOTBAR_NEXT("controlify.hotbar_next", "Next Hotbar Slot", Category.INVENTORY),
    HOTBAR_PREV("controlify.hotbar_prev", "Previous Hotbar Slot", Category.INVENTORY),

    // UI & Misc
    CHAT("key.chat", "Open Chat", Category.MISC),
    COMMAND("key.command", "Open Command", Category.MISC),
    PAUSE("controlify.pause", "Pause Game", Category.MISC),
    PLAYERLIST("key.playerlist", "Show Player List", Category.MISC),
    SCREENSHOT("key.screenshot", "Screenshot", Category.MISC),
    TOGGLE_PERSPECTIVE("key.togglePerspective", "Toggle Perspective", Category.MISC),
    FULLSCREEN("key.fullscreen", "Toggle Fullscreen", Category.MISC),
    RADIAL_MENU("controlify.radial_menu", "Radial Menu", Category.MISC),

    // GUI Navigation (special)
    GUI_UP("controlify.gui_up", "GUI Up", Category.GUI),
    GUI_DOWN("controlify.gui_down", "GUI Down", Category.GUI),
    GUI_LEFT("controlify.gui_left", "GUI Left", Category.GUI),
    GUI_RIGHT("controlify.gui_right", "GUI Right", Category.GUI),
    GUI_SELECT("controlify.gui_select", "GUI Select", Category.GUI),
    GUI_BACK("controlify.gui_back", "GUI Back/Cancel", Category.GUI);

    public enum Category {
        MOVEMENT("Movement"),
        CAMERA("Camera"),
        GAMEPLAY("Gameplay"),
        INVENTORY("Inventory"),
        MISC("Miscellaneous"),
        GUI("GUI Navigation");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String keyBindingId;
    private final String displayName;
    private final Category category;

    GameAction(String keyBindingId, String displayName, Category category) {
        this.keyBindingId = keyBindingId;
        this.displayName = displayName;
        this.category = category;
    }

    public String getKeyBindingId() {
        return keyBindingId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Category getCategory() {
        return category;
    }

        public boolean isCustomAction() {
        return keyBindingId.startsWith("controlify.");
    }

        public boolean isAnalogAction() {
        return category == Category.CAMERA ||
            this == HOTBAR_NEXT || this == HOTBAR_PREV;
    }
}
