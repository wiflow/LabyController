package net.labymod.addons.labycontroller.binding;

import java.util.EnumMap;
import java.util.Map;
import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.ControllerBindingsSubSettings;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.GamepadAxis;
import net.labymod.addons.labycontroller.controller.GamepadButton;

public class BindingManager {

    private final Map<GameAction, ControllerBinding> bindings;

    public BindingManager() {
        this.bindings = new EnumMap<>(GameAction.class);
        initializeDefaultBindings();
    }

        private GamepadButton getConfiguredButton(GameAction action) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || addon.configuration() == null) {
            return getDefaultButton(action);
        }

        ControllerBindingsSubSettings bindings = addon.configuration().bindings();
        if (bindings == null) {
            return getDefaultButton(action);
        }

        return switch (action) {
            case JUMP -> bindings.jump().get();
            case SNEAK -> bindings.sneak().get();
            case SPRINT -> bindings.sprint().get();
            case INVENTORY -> bindings.inventory().get();
            case SWAP_HANDS -> bindings.swapHands().get();
            case DROP_ITEM -> bindings.dropItem().get();
            case PICK_BLOCK -> bindings.pickBlock().get();
            case CHAT -> bindings.chat().get();
            case PAUSE -> bindings.pause().get();
            case TOGGLE_PERSPECTIVE -> bindings.perspective().get();
            case GUI_SELECT -> bindings.guiSelect().get();
            case GUI_BACK -> bindings.guiBack().get();
            case RADIAL_MENU -> bindings.radialMenu().get();
            default -> getDefaultButton(action);
        };
    }

        private GamepadButton getDefaultButton(GameAction action) {
        return switch (action) {
            case JUMP, GUI_SELECT -> GamepadButton.A;
            case SNEAK -> GamepadButton.RIGHT_STICK;
            case SPRINT -> GamepadButton.LEFT_STICK;
            case INVENTORY -> GamepadButton.Y;
            case SWAP_HANDS -> GamepadButton.X;
            case DROP_ITEM -> GamepadButton.DPAD_DOWN;
            case PICK_BLOCK -> GamepadButton.DPAD_LEFT;
            case CHAT -> GamepadButton.DPAD_UP;
            case PAUSE -> GamepadButton.START;
            case TOGGLE_PERSPECTIVE -> GamepadButton.BACK;
            case GUI_BACK -> GamepadButton.B;
            case HOTBAR_NEXT -> GamepadButton.RIGHT_BUMPER;
            case HOTBAR_PREV -> GamepadButton.LEFT_BUMPER;
            case RADIAL_MENU -> GamepadButton.DPAD_RIGHT;
            default -> null;
        };
    }

        private void initializeDefaultBindings() {
        // Movement - Left stick
        addBinding(GameAction.MOVE_FORWARD, ControllerInput.axisNegative(GamepadAxis.LEFT_STICK_Y));
        addBinding(GameAction.MOVE_BACKWARD, ControllerInput.axisPositive(GamepadAxis.LEFT_STICK_Y));
        addBinding(GameAction.MOVE_LEFT, ControllerInput.axisNegative(GamepadAxis.LEFT_STICK_X));
        addBinding(GameAction.MOVE_RIGHT, ControllerInput.axisPositive(GamepadAxis.LEFT_STICK_X));

        // Camera - Right stick
        addBinding(GameAction.LOOK_UP, ControllerInput.axisNegative(GamepadAxis.RIGHT_STICK_Y));
        addBinding(GameAction.LOOK_DOWN, ControllerInput.axisPositive(GamepadAxis.RIGHT_STICK_Y));
        addBinding(GameAction.LOOK_LEFT, ControllerInput.axisNegative(GamepadAxis.RIGHT_STICK_X));
        addBinding(GameAction.LOOK_RIGHT, ControllerInput.axisPositive(GamepadAxis.RIGHT_STICK_X));

        // Core actions - Matching Controlify defaults exactly
        addBinding(GameAction.JUMP, ControllerInput.button(GamepadButton.A));           // south button
        addBinding(GameAction.SNEAK, ControllerInput.button(GamepadButton.RIGHT_STICK)); // R3
        addBinding(GameAction.SPRINT, ControllerInput.button(GamepadButton.LEFT_STICK)); // L3

        // Combat & Interaction - Triggers
        addBinding(GameAction.ATTACK, ControllerInput.rightTrigger());  // RT
        addBinding(GameAction.USE, ControllerInput.leftTrigger());       // LT
        addBinding(GameAction.PICK_BLOCK, ControllerInput.button(GamepadButton.DPAD_LEFT)); // D-pad left

        // Inventory - Matching Controlify
        addBinding(GameAction.INVENTORY, ControllerInput.button(GamepadButton.Y));        // north button (Y)
        addBinding(GameAction.DROP_ITEM, ControllerInput.button(GamepadButton.DPAD_DOWN)); // D-pad down
        addBinding(GameAction.SWAP_HANDS, ControllerInput.button(GamepadButton.X));        // west button (X)

        // Hotbar navigation - Bumpers
        addBinding(GameAction.HOTBAR_NEXT, ControllerInput.button(GamepadButton.RIGHT_BUMPER)); // RB
        addBinding(GameAction.HOTBAR_PREV, ControllerInput.button(GamepadButton.LEFT_BUMPER));  // LB

        // Individual hotbar slots - unbound by default (use radial menu in Controlify)
        addBinding(GameAction.HOTBAR_1, null);
        addBinding(GameAction.HOTBAR_2, null);
        addBinding(GameAction.HOTBAR_3, null);
        addBinding(GameAction.HOTBAR_4, null);
        addBinding(GameAction.HOTBAR_5, null);
        addBinding(GameAction.HOTBAR_6, null);
        addBinding(GameAction.HOTBAR_7, null);
        addBinding(GameAction.HOTBAR_8, null);
        addBinding(GameAction.HOTBAR_9, null);

        // UI & Misc - Matching Controlify
        addBinding(GameAction.CHAT, ControllerInput.button(GamepadButton.DPAD_UP));    // D-pad up opens chat
        addBinding(GameAction.COMMAND, null);
        addBinding(GameAction.PAUSE, ControllerInput.button(GamepadButton.START));     // Start/Menu button
        addBinding(GameAction.PLAYERLIST, ControllerInput.button(GamepadButton.TOUCHPAD));  // Touchpad shows player list
        addBinding(GameAction.SCREENSHOT, null);
        addBinding(GameAction.TOGGLE_PERSPECTIVE, ControllerInput.button(GamepadButton.BACK)); // Back/Select
        addBinding(GameAction.FULLSCREEN, null);

        // GUI Navigation
        addBinding(GameAction.GUI_UP, ControllerInput.axisNegative(GamepadAxis.LEFT_STICK_Y));
        addBinding(GameAction.GUI_DOWN, ControllerInput.axisPositive(GamepadAxis.LEFT_STICK_Y));
        addBinding(GameAction.GUI_LEFT, ControllerInput.axisNegative(GamepadAxis.LEFT_STICK_X));
        addBinding(GameAction.GUI_RIGHT, ControllerInput.axisPositive(GamepadAxis.LEFT_STICK_X));
        addBinding(GameAction.GUI_SELECT, ControllerInput.button(GamepadButton.A));
        addBinding(GameAction.GUI_BACK, ControllerInput.button(GamepadButton.B));

        // Radial Menu - D-pad right (matching Controlify)
        addBinding(GameAction.RADIAL_MENU, ControllerInput.button(GamepadButton.DPAD_RIGHT));
    }

    private void addBinding(GameAction action, ControllerInput defaultInput) {
        bindings.put(action, new ControllerBinding(action, defaultInput));
    }

        public ControllerBinding getBinding(GameAction action) {
        return bindings.get(action);
    }

        public boolean isActive(GameAction action, ControllerState state) {
        // For button-configurable actions, check the configured button
        GamepadButton configuredButton = getConfiguredButton(action);
        if (configuredButton != null) {
            return state.isButtonPressed(configuredButton);
        }

        // For axis-based actions (movement, camera), use the binding system
        ControllerBinding binding = bindings.get(action);
        return binding != null && binding.isActive(state);
    }

        public boolean wasJustActivated(GameAction action, ControllerState state) {
        // For button-configurable actions, check the configured button
        GamepadButton configuredButton = getConfiguredButton(action);
        if (configuredButton != null) {
            return state.isButtonJustPressed(configuredButton);
        }

        // For axis-based actions, use the binding system
        ControllerBinding binding = bindings.get(action);
        return binding != null && binding.wasJustActivated(state);
    }

        public float getAnalogValue(GameAction action, ControllerState state) {
        ControllerBinding binding = bindings.get(action);
        return binding != null ? binding.getAnalogValue(state) : 0.0f;
    }

        public Map<GameAction, ControllerBinding> getAllBindings() {
        return bindings;
    }

        public void resetAllToDefaults() {
        for (ControllerBinding binding : bindings.values()) {
            binding.resetToDefault();
        }
    }

        public void resetToDefault(GameAction action) {
        ControllerBinding binding = bindings.get(action);
        if (binding != null) {
            binding.resetToDefault();
        }
    }

        public void setInput(GameAction action, ControllerInput input) {
        ControllerBinding binding = bindings.get(action);
        if (binding != null) {
            binding.setInput(input);
        }
    }

        public void clearInput(GameAction action) {
        setInput(action, null);
    }

        public boolean hasModifiedBindings() {
        for (ControllerBinding binding : bindings.values()) {
            if (binding.isModified()) {
                return true;
            }
        }
        return false;
    }

        public Map<String, String> serialize() {
        Map<String, String> serialized = new java.util.HashMap<>();
        for (Map.Entry<GameAction, ControllerBinding> entry : bindings.entrySet()) {
            ControllerInput input = entry.getValue().getInput();
            if (input != null) {
                serialized.put(entry.getKey().name(), input.serialize());
            }
        }
        return serialized;
    }

        public void deserialize(Map<String, String> data) {
        if (data == null) {
            return;
        }

        for (Map.Entry<String, String> entry : data.entrySet()) {
            try {
                GameAction action = GameAction.valueOf(entry.getKey());
                ControllerInput input = ControllerInput.deserialize(entry.getValue());
                if (input != null) {
                    setInput(action, input);
                }
            } catch (IllegalArgumentException e) {
                // Ignore unknown actions
            }
        }
    }
}
