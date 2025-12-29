package net.labymod.addons.labycontroller.v1_21_1.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void labycontroller$onTick(boolean isSneaking, float sneakFactor, CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null) {
            return;
        }

        // Debug: log every 60 ticks
        InputHandler input = addon.getInputHandler();
        if (input == null) {
            return;
        }

        float forward = input.getMoveForward();
        float strafe = input.getMoveStrafe();

        // Apply controller movement regardless of controller active state
        // as long as there's input
        if (forward != 0 || strafe != 0) {
            KeyboardInput self = (KeyboardInput) (Object) this;

            // Apply controller movement - these fields are in the parent Input class
            self.forwardImpulse = forward;
            self.leftImpulse = strafe;

            // Apply sneak factor if sneaking
            if (isSneaking) {
                self.forwardImpulse *= sneakFactor;
                self.leftImpulse *= sneakFactor;
            }

            // Also set directional flags
            self.up = forward > 0;
            self.down = forward < 0;
            self.left = strafe > 0;
            self.right = strafe < 0;
        }
    }
}
