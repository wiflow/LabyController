package net.labymod.addons.labycontroller.v1_21_1.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public LocalPlayer player;

    @Shadow
    protected abstract boolean startAttack();

    @Shadow
    protected abstract void startUseItem();

    @Unique
    private boolean labycontroller$lastAttackState = false;

    @Unique
    private boolean labycontroller$lastUseState = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void labycontroller$onTick(CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) {
            return;
        }

        if (player == null) {
            return;
        }

        InputHandler input = addon.getInputHandler();

        // Handle Attack (Right Trigger) - only on press, not hold
        boolean attackPressed = input.isActionActive(GameAction.ATTACK);
        if (attackPressed && !labycontroller$lastAttackState) {
            startAttack();
        }
        labycontroller$lastAttackState = attackPressed;

        // Handle Use Item (Left Trigger) - only on press
        boolean usePressed = input.isActionActive(GameAction.USE);
        if (usePressed && !labycontroller$lastUseState) {
            startUseItem();
        }
        labycontroller$lastUseState = usePressed;
    }
}
