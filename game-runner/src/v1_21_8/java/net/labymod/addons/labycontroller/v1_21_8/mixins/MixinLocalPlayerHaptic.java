package net.labymod.addons.labycontroller.v1_21_8.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayerHaptic {

    @Unique
    private int labycontroller$lastHurtTime = 0;

    @Unique
    private boolean labycontroller$wasDrawingBow = false;

    @Unique
    private int labycontroller$bowRumbleCounter = 0;

        @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void labycontroller$onEntityEvent(byte id, CallbackInfo ci) {
        // Entity event 2 is the hurt animation
        if (id == 2) {
            labycontroller$triggerDamageHaptic();
        }
    }

        @Inject(method = "tick", at = @At("HEAD"))
    private void labycontroller$onTick(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        // Detect when hurt time increases (player just got hurt)
        if (self.hurtTime > labycontroller$lastHurtTime && self.hurtTime == 10) {
            labycontroller$triggerDamageHaptic();
        }
        labycontroller$lastHurtTime = self.hurtTime;

        // Handle bow/crossbow draw progressive rumble
        labycontroller$handleBowDrawRumble(self);
    }

        @Unique
    private void labycontroller$handleBowDrawRumble(LocalPlayer player) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) {
            labycontroller$wasDrawingBow = false;
            return;
        }
        if (!addon.configuration().vibrationEnabled().get()) {
            labycontroller$wasDrawingBow = false;
            return;
        }
        if (!addon.configuration().bowVibration().get()) {
            labycontroller$wasDrawingBow = false;
            return;
        }

        boolean isDrawing = false;
        float drawProgress = 0f;

        if (player.isUsingItem()) {
            ItemStack useItem = player.getUseItem();
            if (!useItem.isEmpty()) {
                if (useItem.getItem() instanceof BowItem) {
                    isDrawing = true;
                    // Bow takes 20 ticks to fully draw
                    int useTime = player.getTicksUsingItem();
                    drawProgress = Math.min(1.0f, useTime / 20.0f);
                } else if (useItem.getItem() instanceof CrossbowItem) {
                    isDrawing = !CrossbowItem.isCharged(useItem);
                    if (isDrawing) {
                        // Crossbow charge time varies, use a default of 25 ticks
                        int useTime = player.getTicksUsingItem();
                        drawProgress = Math.min(1.0f, useTime / 25.0f);
                    }
                }
            }
        }

        if (isDrawing) {
            // Progressive rumble - starts light and increases as bow is drawn
            labycontroller$bowRumbleCounter++;

            // Only trigger rumble every few ticks to avoid constant vibration
            if (labycontroller$bowRumbleCounter % 3 == 0) {
                float strength = addon.configuration().vibrationStrength().get();
                // Low frequency rumble that increases with draw progress
                float lowFreq = 0.1f + (drawProgress * 0.4f);
                float highFreq = 0.05f + (drawProgress * 0.2f);

                // Short pulse
                addon.vibrate(lowFreq * strength, highFreq * strength, 50);
            }

            // Trigger pulse on full draw
            if (drawProgress >= 1.0f && !labycontroller$wasDrawingBow) {
                float strength = addon.configuration().vibrationStrength().get();
                addon.vibrate(0.6f * strength, 0.8f * strength, 100);
            }

            labycontroller$wasDrawingBow = drawProgress >= 1.0f;
        } else {
            labycontroller$bowRumbleCounter = 0;
            labycontroller$wasDrawingBow = false;
        }
    }

    @Unique
    private void labycontroller$triggerDamageHaptic() {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) return;
        if (!addon.configuration().vibrationEnabled().get()) return;
        if (!addon.configuration().damageVibration().get()) return;

        // Apply vibration strength multiplier
        float strength = addon.configuration().vibrationStrength().get();
        addon.vibrate(0.8f * strength, 0.5f * strength, 150);
    }
}
