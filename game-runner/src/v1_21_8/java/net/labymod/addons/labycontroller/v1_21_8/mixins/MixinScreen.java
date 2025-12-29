package net.labymod.addons.labycontroller.v1_21_8.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.GamepadButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class MixinScreen {

        @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
    private void labycontroller$onInit(Minecraft mc, int width, int height, CallbackInfo ci) {
        // Screen input is handled by MixinMinecraft's virtual cursor system
    }

        @Inject(method = "tick", at = @At("HEAD"))
    private void labycontroller$onTick(CallbackInfo ci) {
        // Screen input is handled by MixinMinecraft's virtual cursor system
        // This ensures compatibility with both vanilla Minecraft screens and LabyMod's Fancy GUI
    }

        @Inject(method = "hasShiftDown", at = @At("RETURN"), cancellable = true)
    private static void labycontroller$checkControllerShift(CallbackInfoReturnable<Boolean> cir) {
        // If already true from keyboard, don't need to check controller
        if (cir.getReturnValue()) {
            return;
        }

        // Check if controller left bumper is pressed
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon != null && addon.isControllerActive()) {
            ControllerState state = addon.getActiveControllerState().orElse(null);
            if (state != null && state.isButtonPressed(GamepadButton.LEFT_BUMPER)) {
                cir.setReturnValue(true);
            }
        }
    }
}
