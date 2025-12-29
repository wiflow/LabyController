package net.labymod.addons.labycontroller.v1_21_8.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Shadow
    private double xpos;

    @Shadow
    private double ypos;

    @Unique
    private double labycontroller$lastX = 0;

    @Unique
    private double labycontroller$lastY = 0;

    @Unique
    private static final double MOVEMENT_THRESHOLD = 1.0;

        @Inject(method = "onMove", at = @At("HEAD"))
    private void labycontroller$onMouseMove(long window, double x, double y, CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null) {
            return;
        }

        // Only process if mixed input mode is enabled
        if (!addon.configuration().mixedInputMode().get()) {
            return;
        }

        // Check if mouse actually moved significantly
        double deltaX = Math.abs(x - labycontroller$lastX);
        double deltaY = Math.abs(y - labycontroller$lastY);

        if (deltaX > MOVEMENT_THRESHOLD || deltaY > MOVEMENT_THRESHOLD) {
            // Mouse moved significantly - switch to mouse mode
            InputHandler input = addon.getInputHandler();
            if (input != null) {
                input.onKeyboardMouseInput();
            }
        }

        labycontroller$lastX = x;
        labycontroller$lastY = y;
    }
}
