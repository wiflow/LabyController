package net.labymod.addons.labycontroller.v1_21_8.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Shadow
    private boolean isDestroying;

    @Unique
    private boolean labycontroller$wasDestroying = false;

    @Unique
    private int labycontroller$destroyTick = 0;

        @Inject(method = "continueDestroyBlock", at = @At("RETURN"))
    private void labycontroller$onContinueDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (isDestroying) {
            labycontroller$destroyTick++;

            // Subtle haptic pulse every 4 ticks while mining
            if (labycontroller$destroyTick % 4 == 0) {
                LabyControllerAddon addon = LabyControllerAddon.getInstance();
                if (addon == null || !addon.isControllerActive()) return;
                if (!addon.configuration().vibrationEnabled().get()) return;
                if (!addon.configuration().blockBreakVibration().get()) return;

                float strength = addon.configuration().vibrationStrength().get();
                addon.vibrate(0.1f * strength, 0.05f * strength, 50);
            }
        }
    }

        @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void labycontroller$onStartDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        labycontroller$destroyTick = 0;

        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) return;
        if (!addon.configuration().vibrationEnabled().get()) return;
        if (!addon.configuration().blockBreakVibration().get()) return;

        float strength = addon.configuration().vibrationStrength().get();
        addon.vibrate(0.15f * strength, 0.1f * strength, 50);
    }

        @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void labycontroller$onBlockDestroyed(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            LabyControllerAddon addon = LabyControllerAddon.getInstance();
            if (addon == null || !addon.isControllerActive()) return;
            if (!addon.configuration().vibrationEnabled().get()) return;
            if (!addon.configuration().blockBreakVibration().get()) return;

            float strength = addon.configuration().vibrationStrength().get();
            addon.vibrate(0.4f * strength, 0.2f * strength, 100);
        }
    }

        @Inject(method = "stopDestroyBlock", at = @At("HEAD"))
    private void labycontroller$onStopDestroy(CallbackInfo ci) {
        labycontroller$destroyTick = 0;
    }
}
