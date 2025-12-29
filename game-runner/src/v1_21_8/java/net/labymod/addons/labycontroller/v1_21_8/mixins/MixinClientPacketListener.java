package net.labymod.addons.labycontroller.v1_21_8.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    // Fishing hook entity event ID for "fish caught"
    @Unique
    private static final byte FISHING_HOOK_CAUGHT_EVENT = 31;

        @Inject(method = "handleEntityEvent", at = @At("RETURN"))
    private void labycontroller$onEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) return;
        if (!addon.configuration().vibrationEnabled().get()) return;
        if (!addon.configuration().fishingVibration().get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        // Check if this is a fishing hook caught event
        if (packet.getEventId() == FISHING_HOOK_CAUGHT_EVENT) {
            Entity entity = packet.getEntity(mc.level);
            if (entity instanceof FishingHook fishingHook) {
                // Only trigger if this is our fishing hook
                if (fishingHook.getPlayerOwner() == mc.player) {
                    // Strong, distinctive rumble to alert the player
                    float strength = addon.configuration().vibrationStrength().get();
                    addon.vibrate(0.8f * strength, 1.0f * strength, 300);
                }
            }
        }
    }

        @Inject(method = "handleExplosion", at = @At("RETURN"))
    private void labycontroller$onExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) return;
        if (!addon.configuration().vibrationEnabled().get()) return;
        if (!addon.configuration().explosionVibration().get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        // Calculate explosion magnitude based on distance
        double x = packet.center().x();
        double y = packet.center().y();
        double z = packet.center().z();

        double distanceSq = mc.player.distanceToSqr(x, y, z);
        float magnitude = labycontroller$calculateMagnitude(distanceSq);

        if (magnitude > 0.05f) {
            // Apply vibration strength multiplier
            float strength = addon.configuration().vibrationStrength().get();
            addon.vibrate(magnitude * strength, magnitude * strength, 100);
        }
    }

        @Unique
    private float labycontroller$calculateMagnitude(double distanceSq) {
        // Max distance for rumble is about 64 blocks (4096 sq distance)
        float maxDistanceSq = 4096f;

        if (distanceSq >= maxDistanceSq) {
            return 0f;
        }

        // Inverse square falloff
        float normalizedDistance = (float) (distanceSq / maxDistanceSq);
        return 1.0f - (float) Math.sqrt(normalizedDistance);
    }
}
