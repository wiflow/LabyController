package net.labymod.addons.labycontroller.v1_20_1.input;

import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;

public class DualInput extends Input {
    private final Input keyboardInput, controllerInput;
    public DualInput(Input keyboardInput, Input controllerInput) {
        if (keyboardInput instanceof DualInput || controllerInput instanceof DualInput) throw new IllegalArgumentException("Cannot nest DualInputs");
        this.keyboardInput = keyboardInput; this.controllerInput = controllerInput;
    }
    @Override
    public void tick(boolean slowDown, float movementMultiplier) {
        keyboardInput.tick(slowDown, movementMultiplier); controllerInput.tick(slowDown, movementMultiplier);
        forwardImpulse = Mth.clamp(keyboardInput.forwardImpulse + controllerInput.forwardImpulse, -1, 1);
        leftImpulse = Mth.clamp(keyboardInput.leftImpulse + controllerInput.leftImpulse, -1, 1);
        up = keyboardInput.up || controllerInput.up; down = keyboardInput.down || controllerInput.down;
        left = keyboardInput.left || controllerInput.left; right = keyboardInput.right || controllerInput.right;
        jumping = keyboardInput.jumping || controllerInput.jumping; shiftKeyDown = keyboardInput.shiftKeyDown || controllerInput.shiftKeyDown;
    }
    public Input getControllerInput() { return controllerInput; }
}
