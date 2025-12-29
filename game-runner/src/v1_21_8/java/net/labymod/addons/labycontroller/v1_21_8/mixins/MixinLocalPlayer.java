package net.labymod.addons.labycontroller.v1_21_8.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.v1_21_8.input.ControllerInput;
import net.labymod.addons.labycontroller.v1_21_8.input.DualInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {

        @Inject(method = "tick", at = @At("HEAD"))
    private void labycontroller$ensureCorrectInput(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        LabyControllerAddon addon = LabyControllerAddon.getInstance();

        boolean shouldBeController = addon != null && addon.isControllerActive();

        if (shouldBeController) {
            // Should use DualInput
            if (!(self.input instanceof DualInput)) {
                // Replace with DualInput
                self.input = new DualInput(
                    new KeyboardInput(Minecraft.getInstance().options),
                    new ControllerInput(self)
                );
            }
        } else {
            // Should use KeyboardInput only
            if (!(self.input instanceof KeyboardInput) || self.input instanceof DualInput) {
                self.input = new KeyboardInput(Minecraft.getInstance().options);
            }
        }
    }
}
