package net.labymod.addons.labycontroller.binding;

import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.GamepadAxis;
import net.labymod.addons.labycontroller.controller.GamepadButton;

public class ControllerInput {

    public enum InputType {
        BUTTON,
        AXIS_POSITIVE,
        AXIS_NEGATIVE,
        TRIGGER
    }

    private final InputType type;
    private final GamepadButton button;
    private final GamepadAxis axis;
    private final String displayName;

    private ControllerInput(InputType type, GamepadButton button, GamepadAxis axis, String displayName) {
        this.type = type;
        this.button = button;
        this.axis = axis;
        this.displayName = displayName;
    }

    public static ControllerInput button(GamepadButton button) {
        return new ControllerInput(InputType.BUTTON, button, null, button.getXboxName());
    }

    public static ControllerInput axisPositive(GamepadAxis axis) {
        return new ControllerInput(InputType.AXIS_POSITIVE, null, axis, axis.getDisplayName() + "+");
    }

    public static ControllerInput axisNegative(GamepadAxis axis) {
        return new ControllerInput(InputType.AXIS_NEGATIVE, null, axis, axis.getDisplayName() + "-");
    }

    public static ControllerInput trigger(GamepadAxis axis) {
        if (!axis.isTrigger()) {
            throw new IllegalArgumentException("Axis must be a trigger");
        }
        return new ControllerInput(InputType.TRIGGER, null, axis, axis.getDisplayName());
    }

    public static ControllerInput leftTrigger() {
        return trigger(GamepadAxis.LEFT_TRIGGER);
    }

    public static ControllerInput rightTrigger() {
        return trigger(GamepadAxis.RIGHT_TRIGGER);
    }

    public boolean isActive(ControllerState state) {
        if (state == null) {
            return false;
        }

        return switch (type) {
            case BUTTON -> state.isButtonPressed(button);
            case AXIS_POSITIVE -> state.getAxisValue(axis) > 0.5f;
            case AXIS_NEGATIVE -> state.getAxisValue(axis) < -0.5f;
            case TRIGGER -> state.getAxisValue(axis) > 0.5f;
        };
    }

    public boolean wasJustActivated(ControllerState state) {
        if (state == null) {
            return false;
        }

        return switch (type) {
            case BUTTON -> state.isButtonJustPressed(button);
            case AXIS_POSITIVE -> state.getAxisValue(axis) > 0.5f &&
                state.getPreviousAxisValue(axis) <= 0.5f;
            case AXIS_NEGATIVE -> state.getAxisValue(axis) < -0.5f &&
                state.getPreviousAxisValue(axis) >= -0.5f;
            case TRIGGER -> state.getAxisValue(axis) > 0.5f &&
                state.getPreviousAxisValue(axis) <= 0.5f;
        };
    }

    public float getAnalogValue(ControllerState state) {
        if (state == null) {
            return 0.0f;
        }

        return switch (type) {
            case BUTTON -> state.isButtonPressed(button) ? 1.0f : 0.0f;
            case AXIS_POSITIVE -> Math.max(0, state.getAxisValue(axis));
            case AXIS_NEGATIVE -> Math.abs(Math.min(0, state.getAxisValue(axis)));
            case TRIGGER -> state.getAxisValue(axis);
        };
    }

    public InputType getType() {
        return type;
    }

    public GamepadButton getButton() {
        return button;
    }

    public GamepadAxis getAxis() {
        return axis;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String serialize() {
        return switch (type) {
            case BUTTON -> "button:" + button.name();
            case AXIS_POSITIVE -> "axis+:" + axis.name();
            case AXIS_NEGATIVE -> "axis-:" + axis.name();
            case TRIGGER -> "trigger:" + axis.name();
        };
    }

    public static ControllerInput deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String[] parts = value.split(":", 2);
        if (parts.length != 2) {
            return null;
        }

        try {
            return switch (parts[0]) {
                case "button" -> button(GamepadButton.valueOf(parts[1]));
                case "axis+" -> axisPositive(GamepadAxis.valueOf(parts[1]));
                case "axis-" -> axisNegative(GamepadAxis.valueOf(parts[1]));
                case "trigger" -> trigger(GamepadAxis.valueOf(parts[1]));
                default -> null;
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
