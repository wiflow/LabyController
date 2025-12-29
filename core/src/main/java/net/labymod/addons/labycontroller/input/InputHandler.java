package net.labymod.addons.labycontroller.input;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.binding.BindingManager;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.GamepadButton;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;

public class InputHandler {

    private final BindingManager bindingManager;
    private final LabyControllerAddon addon;

    // Camera sensitivity settings
    private float lookSensitivity = 1.0f;
    private float lookAcceleration = 2.0f;

    // Hotbar cycling state
    private int hotbarCooldown = 0;
    private static final int HOTBAR_COOLDOWN_TICKS = 5;

    // Current input mode - now always active if controller has input
    private boolean controllerActive = false;

    // Cooldown before keyboard can deactivate controller mode
    private int keyboardDeactivateCooldown = 0;
    private static final int KEYBOARD_COOLDOWN_TICKS = 10;

    // Movement state tracking
    private float lastMoveForward = 0;
    private float lastMoveStrafe = 0;
    private float lastLookX = 0;
    private float lastLookY = 0;

    // Trigger threshold for analog-to-digital conversion
    private static final float TRIGGER_THRESHOLD = 0.5f;

        public InputHandler(LabyControllerAddon addon) {
        this.addon = addon;
        this.bindingManager = addon.getBindingManager();
    }

        public void update() {
        ControllerState state = getActiveState();
        if (state == null) {
            controllerActive = false;
            return;
        }

        // Decrease keyboard cooldown
        if (keyboardDeactivateCooldown > 0) {
            keyboardDeactivateCooldown--;
        }

        // Check if controller has input - this ALWAYS activates controller mode
        if (state.hasAnyInput()) {
            controllerActive = true;
            keyboardDeactivateCooldown = KEYBOARD_COOLDOWN_TICKS;
        }

        if (!controllerActive) {
            return;
        }

        // Decrease hotbar cooldown
        if (hotbarCooldown > 0) {
            hotbarCooldown--;
        }

        // Get movement values from state
        updateMovementFromState(state);
        updateCameraFromState(state);
        updateHotbarFromState(state);
    }

        private ControllerState getActiveState() {
        if (addon != null) {
            return addon.getActiveControllerState().orElse(null);
        }
        return null;
    }

        private void updateMovementFromState(ControllerState state) {
        // For SDL mode or direct state access, read from state directly
        // Left stick controls movement
        float leftX = state.getLeftStickX();
        float leftY = state.getLeftStickY();

        // Y axis: negative = forward, positive = backward
        lastMoveForward = -leftY;
        // X axis: negative = left, positive = right
        lastMoveStrafe = leftX;
    }

        private void updateCameraFromState(ControllerState state) {
        float lookX = state.getRightStickX();
        float lookY = state.getRightStickY();

        if (Math.abs(lookX) < 0.05f) lookX = 0;
        if (Math.abs(lookY) < 0.05f) lookY = 0;

        // Apply non-linear acceleration for finer control at low values
        if (lookX != 0 || lookY != 0) {
            float absX = Math.abs(lookX);
            float absY = Math.abs(lookY);

            float accelX = (float) Math.pow(absX, lookAcceleration);
            float accelY = (float) Math.pow(absY, lookAcceleration);

            lastLookX = Math.signum(lookX) * accelX * lookSensitivity;
            lastLookY = Math.signum(lookY) * accelY * lookSensitivity;
        } else {
            lastLookX = 0;
            lastLookY = 0;
        }
    }

        private void updateHotbarFromState(ControllerState state) {
        if (hotbarCooldown > 0) {
            return;
        }

        ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
        if (player == null) {
            return;
        }

        // Check button states directly for hotbar cycling
        if (state.isButtonJustPressed(GamepadButton.RIGHT_BUMPER)) {
            hotbarCooldown = HOTBAR_COOLDOWN_TICKS;
        } else if (state.isButtonJustPressed(GamepadButton.LEFT_BUMPER)) {
            hotbarCooldown = HOTBAR_COOLDOWN_TICKS;
        }
    }

        public float getMoveForward() {
        return lastMoveForward;
    }

        public float getMoveStrafe() {
        return lastMoveStrafe;
    }

        public float getLookX() {
        return lastLookX;
    }

        public float getLookY() {
        return lastLookY;
    }

        public boolean isActionActive(GameAction action) {
        ControllerState state = getActiveState();
        if (state == null) {
            return false;
        }

        // Use BindingManager for configurable mappings
        return bindingManager.isActive(action, state);
    }

        public boolean wasActionJustActivated(GameAction action) {
        ControllerState state = getActiveState();
        if (state == null) {
            return false;
        }

        // Use BindingManager for configurable mappings
        return bindingManager.wasJustActivated(action, state);
    }

        public void onKeyboardMouseInput() {
        // Only deactivate if controller hasn't been used recently
        if (keyboardDeactivateCooldown <= 0) {
            controllerActive = false;
        }
    }

        public boolean isControllerActive() {
        return controllerActive;
    }

        public void forceActivate() {
        controllerActive = true;
        keyboardDeactivateCooldown = 100; // Keep active for a while
    }

        public void setLookSensitivity(float sensitivity) {
        this.lookSensitivity = Math.max(0.1f, Math.min(5.0f, sensitivity));
    }

    public float getLookSensitivity() {
        return lookSensitivity;
    }

        public void setLookAcceleration(float acceleration) {
        this.lookAcceleration = Math.max(1.0f, Math.min(4.0f, acceleration));
    }

    public float getLookAcceleration() {
        return lookAcceleration;
    }
}
