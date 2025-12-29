package net.labymod.addons.labycontroller.v1_21_5.input;

import net.minecraft.client.player.ClientInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

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
    public void tick() {
        // Tick both inputs
        keyboardInput.tick();
        controllerInput.tick();

        // Combine move vectors
        Vec2 keyboardMove = keyboardInput.getMoveVector();
        Vec2 controllerMove = controllerInput.getMoveVector();
        this.moveVector = new Vec2(
            Mth.clamp(keyboardMove.x + controllerMove.x, -1, 1),
            Mth.clamp(keyboardMove.y + controllerMove.y, -1, 1)
        );

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
