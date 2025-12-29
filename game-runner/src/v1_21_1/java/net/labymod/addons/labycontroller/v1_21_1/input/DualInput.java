package net.labymod.addons.labycontroller.v1_21_1.input;

import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;

/**
 * Combines keyboard and controller input for Minecraft versions < 1.21.2.
 * Uses the Input class with boolean fields and float impulses.
 */
public class DualInput extends Input {
    private final Input keyboardInput;
    private final Input controllerInput;

    public DualInput(Input keyboardInput, Input controllerInput) {
        if (keyboardInput instanceof DualInput || controllerInput instanceof DualInput) {
            throw new IllegalArgumentException("Cannot nest DualInputs");
        }
        this.keyboardInput = keyboardInput;
        this.controllerInput = controllerInput;
    }

    @Override
    public void tick(boolean slowDown, float movementMultiplier) {
        // Tick both inputs
        keyboardInput.tick(slowDown, movementMultiplier);
        controllerInput.tick(slowDown, movementMultiplier);

        // Combine move impulses (clamped to -1,1 range)
        this.forwardImpulse = Mth.clamp(keyboardInput.forwardImpulse + controllerInput.forwardImpulse, -1, 1);
        this.leftImpulse = Mth.clamp(keyboardInput.leftImpulse + controllerInput.leftImpulse, -1, 1);

        // Combine key presses (OR them together)
        this.up = keyboardInput.up || controllerInput.up;
        this.down = keyboardInput.down || controllerInput.down;
        this.left = keyboardInput.left || controllerInput.left;
        this.right = keyboardInput.right || controllerInput.right;
        this.jumping = keyboardInput.jumping || controllerInput.jumping;
        this.shiftKeyDown = keyboardInput.shiftKeyDown || controllerInput.shiftKeyDown;
    }

    public Input getControllerInput() {
        return controllerInput;
    }
}
