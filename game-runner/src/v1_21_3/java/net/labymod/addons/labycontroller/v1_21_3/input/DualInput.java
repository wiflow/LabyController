package net.labymod.addons.labycontroller.v1_21_3.input;

import net.minecraft.client.player.ClientInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;

/**
 * Combines keyboard and controller input for Minecraft 1.21.3.
 * Uses ClientInput with forwardImpulse/leftImpulse, keyPresses record,
 * and tick(boolean, float) parameters.
 */
public class DualInput extends ClientInput {
    private final ClientInput keyboardInput;
    private final ClientInput controllerInput;

    public DualInput(ClientInput keyboardInput, ClientInput controllerInput) {
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
        Input kb = keyboardInput.keyPresses;
        Input ctrl = controllerInput.keyPresses;
        this.keyPresses = new Input(
            kb.forward() || ctrl.forward(),
            kb.backward() || ctrl.backward(),
            kb.left() || ctrl.left(),
            kb.right() || ctrl.right(),
            kb.jump() || ctrl.jump(),
            kb.shift() || ctrl.shift(),
            kb.sprint() || ctrl.sprint()
        );
    }
}
