package net.labymod.addons.labycontroller.v1_21_1.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {

    @Inject(method = "tick", at = @At("HEAD"))
    private void labycontroller$onTick(CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) {
            return;
        }

        InputHandler input = addon.getInputHandler();
        LocalPlayer self = (LocalPlayer) (Object) this;

        // Handle camera look via player.turn()
        float lookX = input.getLookX();
        float lookY = input.getLookY();

        if (lookX != 0 || lookY != 0) {
            // Scale for sensitivity - multiply by larger factor for noticeable movement
            // player.turn expects degrees, not small fractions
            float sensitivity = 3.0f;
            self.turn(lookX * sensitivity / 0.15, lookY * sensitivity / 0.15);
        }

        // Handle Sprint (Left Stick Click)
        boolean sprintPressed = input.isActionActive(GameAction.SPRINT);
        if (sprintPressed && self.onGround() && !self.isSprinting() && self.input.forwardImpulse > 0) {
            self.setSprinting(true);
        }

        // Handle sneak
        boolean sneakPressed = input.isActionActive(GameAction.SNEAK);
        if (sneakPressed) {
            self.input.shiftKeyDown = true;
        }

        // Handle jump
        boolean jumpPressed = input.isActionActive(GameAction.JUMP);
        if (jumpPressed) {
            self.input.jumping = true;
        }
    }
}
