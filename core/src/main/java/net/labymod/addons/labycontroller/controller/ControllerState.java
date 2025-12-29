package net.labymod.addons.labycontroller.controller;

public class ControllerState {

    private final boolean[] buttons;
    private final boolean[] previousButtons;
    private final float[] axes;
    private final float[] previousAxes;

    private float leftStickDeadzone = 0.15f;
    private float rightStickDeadzone = 0.15f;
    private float triggerDeadzone = 0.1f;

    public ControllerState() {
        this.buttons = new boolean[GamepadButton.getButtonCount()];
        this.previousButtons = new boolean[GamepadButton.getButtonCount()];
        this.axes = new float[GamepadAxis.getAxisCount()];
        this.previousAxes = new float[GamepadAxis.getAxisCount()];
    }

        public void updateDirect(boolean[] rawButtons, float[] rawAxes) {
        // Store previous state
        System.arraycopy(buttons, 0, previousButtons, 0, buttons.length);
        System.arraycopy(axes, 0, previousAxes, 0, axes.length);

        // Update buttons
        int buttonCount = Math.min(rawButtons.length, buttons.length);
        for (int i = 0; i < buttonCount; i++) {
            buttons[i] = rawButtons[i];
        }

        // Update axes with deadzone
        int axisCount = Math.min(rawAxes.length, axes.length);
        for (int i = 0; i < axisCount; i++) {
            GamepadAxis axis = GamepadAxis.values()[i];
            axes[i] = applyDeadzone(rawAxes[i], axis);
        }
    }

        private float applyDeadzone(float value, GamepadAxis axis) {
        float deadzone;
        if (axis.isTrigger()) {
            deadzone = triggerDeadzone;
            // Triggers range from -1 to 1, normalize to 0 to 1
            value = (value + 1.0f) / 2.0f;
        } else {
            // Use appropriate deadzone for left/right stick
            deadzone = axis.ordinal() < 2 ? leftStickDeadzone : rightStickDeadzone;
        }

        if (Math.abs(value) < deadzone) {
            return 0.0f;
        }

        // Scale the value to use the full range after deadzone
        float sign = Math.signum(value);
        float absValue = Math.abs(value);
        return sign * ((absValue - deadzone) / (1.0f - deadzone));
    }

    // Button state queries
    public boolean isButtonPressed(GamepadButton button) {
        return buttons[button.ordinal()];
    }

    public boolean wasButtonPressed(GamepadButton button) {
        return previousButtons[button.ordinal()];
    }

    public boolean isButtonJustPressed(GamepadButton button) {
        return buttons[button.ordinal()] && !previousButtons[button.ordinal()];
    }

    public boolean isButtonJustReleased(GamepadButton button) {
        return !buttons[button.ordinal()] && previousButtons[button.ordinal()];
    }

    // Axis state queries
    public float getAxisValue(GamepadAxis axis) {
        return axes[axis.ordinal()];
    }

    public float getPreviousAxisValue(GamepadAxis axis) {
        return previousAxes[axis.ordinal()];
    }

    // Convenience methods for common operations
    public float getLeftStickX() {
        return getAxisValue(GamepadAxis.LEFT_STICK_X);
    }

    public float getLeftStickY() {
        return getAxisValue(GamepadAxis.LEFT_STICK_Y);
    }

    public float getRightStickX() {
        return getAxisValue(GamepadAxis.RIGHT_STICK_X);
    }

    public float getRightStickY() {
        return getAxisValue(GamepadAxis.RIGHT_STICK_Y);
    }

    public float getLeftTrigger() {
        return getAxisValue(GamepadAxis.LEFT_TRIGGER);
    }

    public float getRightTrigger() {
        return getAxisValue(GamepadAxis.RIGHT_TRIGGER);
    }

    public boolean isLeftTriggerPressed() {
        return getLeftTrigger() > 0.5f;
    }

    public boolean isRightTriggerPressed() {
        return getRightTrigger() > 0.5f;
    }

    public boolean wasLeftTriggerJustPressed(float threshold) {
        float current = getLeftTrigger();
        float previous = getPreviousAxisValue(GamepadAxis.LEFT_TRIGGER);
        return current > threshold && previous <= threshold;
    }

    public boolean wasRightTriggerJustPressed(float threshold) {
        float current = getRightTrigger();
        float previous = getPreviousAxisValue(GamepadAxis.RIGHT_TRIGGER);
        return current > threshold && previous <= threshold;
    }

    // Deadzone configuration
    public void setLeftStickDeadzone(float deadzone) {
        this.leftStickDeadzone = Math.max(0.0f, Math.min(0.9f, deadzone));
    }

    public void setRightStickDeadzone(float deadzone) {
        this.rightStickDeadzone = Math.max(0.0f, Math.min(0.9f, deadzone));
    }

    public void setTriggerDeadzone(float deadzone) {
        this.triggerDeadzone = Math.max(0.0f, Math.min(0.9f, deadzone));
    }

    public float getLeftStickDeadzone() {
        return leftStickDeadzone;
    }

    public float getRightStickDeadzone() {
        return rightStickDeadzone;
    }

    public float getTriggerDeadzone() {
        return triggerDeadzone;
    }

        public boolean hasAnyInput() {
        for (boolean button : buttons) {
            if (button) return true;
        }
        for (float axis : axes) {
            if (Math.abs(axis) > 0.1f) return true;
        }
        return false;
    }

        public void reset() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = false;
            previousButtons[i] = false;
        }
        for (int i = 0; i < axes.length; i++) {
            axes[i] = 0.0f;
            previousAxes[i] = 0.0f;
        }
    }
}
