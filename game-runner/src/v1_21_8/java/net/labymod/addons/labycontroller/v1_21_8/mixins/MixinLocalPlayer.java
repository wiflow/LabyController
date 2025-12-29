package net.labymod.addons.labycontroller.v1_21_8.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.v1_21_8.input.ControllerInput;
import net.labymod.addons.labycontroller.v1_21_8.input.DualInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {

    // Track if we've already set up DualInput to avoid recreating it
    @Unique
    private boolean labycontroller$dualInputInstalled = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void labycontroller$ensureCorrectInput(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        LabyControllerAddon addon = LabyControllerAddon.getInstance();

        boolean shouldBeController = addon != null && addon.isControllerActive();

        if (shouldBeController) {
            // Should use DualInput - only create once
            if (!labycontroller$dualInputInstalled || !(self.input instanceof DualInput)) {
                self.input = new DualInput(
                    new KeyboardInput(Minecraft.getInstance().options),
                    new ControllerInput(self)
                );
                labycontroller$dualInputInstalled = true;
            }
        } else {
            // Should use KeyboardInput only
            if (labycontroller$dualInputInstalled) {
                self.input = new KeyboardInput(Minecraft.getInstance().options);
                labycontroller$dualInputInstalled = false;
            }
        }
    }
}
