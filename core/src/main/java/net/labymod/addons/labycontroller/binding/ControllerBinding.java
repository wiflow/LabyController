package net.labymod.addons.labycontroller.binding;

import net.labymod.addons.labycontroller.controller.ControllerState;

public class ControllerBinding {

    private final GameAction action;
    private ControllerInput input;
    private final ControllerInput defaultInput;

    private boolean wasActive = false;

    public ControllerBinding(GameAction action, ControllerInput defaultInput) {
        this.action = action;
        this.defaultInput = defaultInput;
        this.input = defaultInput;
    }

        public boolean isActive(ControllerState state) {
        return input != null && input.isActive(state);
    }

        public boolean wasJustActivated(ControllerState state) {
        return input != null && input.wasJustActivated(state);
    }

        public float getAnalogValue(ControllerState state) {
        return input != null ? input.getAnalogValue(state) : 0.0f;
    }

        public boolean updateState(ControllerState state) {
        boolean currentlyActive = isActive(state);
        boolean stateChanged = currentlyActive != wasActive;
        wasActive = currentlyActive;
        return stateChanged;
    }

        public boolean wasPressed(ControllerState state) {
        boolean currentlyActive = isActive(state);
        boolean pressed = currentlyActive && !wasActive;
        wasActive = currentlyActive;
        return pressed;
    }

        public boolean wasReleased(ControllerState state) {
        boolean currentlyActive = isActive(state);
        boolean released = !currentlyActive && wasActive;
        wasActive = currentlyActive;
        return released;
    }

        public void resetToDefault() {
        this.input = defaultInput;
    }

        public boolean isModified() {
        if (input == null && defaultInput == null) {
            return false;
        }
        if (input == null || defaultInput == null) {
            return true;
        }
        return !input.serialize().equals(defaultInput.serialize());
    }

    // Getters and setters
    public GameAction getAction() {
        return action;
    }

    public ControllerInput getInput() {
        return input;
    }

    public void setInput(ControllerInput input) {
        this.input = input;
    }

    public ControllerInput getDefaultInput() {
        return defaultInput;
    }

    public String getDisplayName() {
        return action.getDisplayName();
    }

    public String getInputDisplayName() {
        return input != null ? input.getDisplayName() : "Unbound";
    }

    @Override
    public String toString() {
        return String.format("Binding[%s -> %s]", action.name(),
            input != null ? input.getDisplayName() : "Unbound");
    }
}
