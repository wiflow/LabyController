package net.labymod.addons.labycontroller.v1_20_4.input;

import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;

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
        keyboardInput.tick(slowDown, movementMultiplier);
        controllerInput.tick(slowDown, movementMultiplier);

        this.forwardImpulse = Mth.clamp(keyboardInput.forwardImpulse + controllerInput.forwardImpulse, -1, 1);
        this.leftImpulse = Mth.clamp(keyboardInput.leftImpulse + controllerInput.leftImpulse, -1, 1);

        this.up = keyboardInput.up || controllerInput.up;
        this.down = keyboardInput.down || controllerInput.down;
        this.left = keyboardInput.left || controllerInput.left;
        this.right = keyboardInput.right || controllerInput.right;
        this.jumping = keyboardInput.jumping || controllerInput.jumping;
        this.shiftKeyDown = keyboardInput.shiftKeyDown || controllerInput.shiftKeyDown;
    }

    public Input getControllerInput() { return controllerInput; }
}
