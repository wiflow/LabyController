package net.labymod.addons.labycontroller.v1_20_2.input;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.LabyControllerConfiguration;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;

public class ControllerInput extends Input {
    private final LocalPlayer player;
    private boolean wasFlying, wasPassenger, lastJumpPressed, lastSneakPressed, lastSprintPressed;
    private boolean jumpHeld, sneakHeld, sprintToggled;
    private static final float ACTIVATION_THRESHOLD = 0.5f;

    public ControllerInput(LocalPlayer player) { this.player = player; }

    @Override
    public void tick(boolean slowDown, float movementMultiplier) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || Minecraft.getInstance().screen != null || player == null) {
            forwardImpulse = leftImpulse = 0; up = down = left = right = jumping = shiftKeyDown = false; return;
        }
        InputHandler input = addon.getInputHandler();
        if (input == null) { forwardImpulse = leftImpulse = 0; up = down = left = right = jumping = shiftKeyDown = false; return; }
        LabyControllerConfiguration config = addon.configuration();

        float forwardRaw = input.getMoveForward();
        float strafeRaw = -input.getMoveStrafe();

        float forward, strafe;
        if (addon.shouldUseKeyboardLikeMovement()) {
            forward = Math.abs(forwardRaw) >= ACTIVATION_THRESHOLD ? Math.copySign(1.0f, forwardRaw) : 0;
            strafe = Math.abs(strafeRaw) >= ACTIVATION_THRESHOLD ? Math.copySign(1.0f, strafeRaw) : 0;
        } else { forward = forwardRaw; strafe = strafeRaw; }

        up = forward > 0; down = forward < 0; left = strafe > 0; right = strafe < 0;

        if (slowDown) { forward *= movementMultiplier; strafe *= movementMultiplier; }
        forwardImpulse = forward; leftImpulse = strafe;

        boolean jumpPressed = input.isActionActive(GameAction.JUMP);
        if (jumpPressed && !lastJumpPressed) jumpHeld = true;
        if (!jumpPressed) jumpHeld = false;
        lastJumpPressed = jumpPressed; jumping = jumpHeld;

        boolean sneakPressed = input.isActionActive(GameAction.SNEAK);
        if (player.getAbilities().flying || (player.isInWater() && !player.onGround()) || player.getVehicle() != null) {
            if (sneakPressed && !lastSneakPressed) sneakHeld = true;
            if (!sneakPressed) sneakHeld = false;
        } else if (config.toggleSneak().get()) { if (sneakPressed && !lastSneakPressed) sneakHeld = !sneakHeld; }
        else { sneakHeld = sneakPressed; }
        if ((!player.getAbilities().flying && wasFlying && player.onGround()) || (!player.isPassenger() && wasPassenger)) sneakHeld = false;
        lastSneakPressed = sneakPressed; shiftKeyDown = sneakHeld;

        boolean sprintPressed = input.isActionActive(GameAction.SPRINT);
        if (config.toggleSprint().get()) {
            if (sprintPressed && !lastSprintPressed) sprintToggled = !sprintToggled;
            if (sprintToggled && forward <= 0) sprintToggled = false;
        } else { sprintToggled = sprintPressed; }
        lastSprintPressed = sprintPressed;

        wasFlying = player.getAbilities().flying; wasPassenger = player.isPassenger();
    }

    public boolean isSprintToggled() { return sprintToggled; }
}
