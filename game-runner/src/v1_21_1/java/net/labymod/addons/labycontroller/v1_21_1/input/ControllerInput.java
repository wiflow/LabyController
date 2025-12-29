package net.labymod.addons.labycontroller.v1_21_1.input;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.LabyControllerConfiguration;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;

public class ControllerInput extends Input {
    private final LocalPlayer player;
    private boolean wasFlying;
    private boolean wasPassenger;
    private boolean lastJumpPressed;
    private boolean lastSneakPressed;
    private boolean lastSprintPressed;
    private boolean jumpHeld;
    private boolean sneakHeld;
    private boolean sprintToggled;

    private static final float ACTIVATION_THRESHOLD = 0.5f;

    public ControllerInput(LocalPlayer player) {
        this.player = player;
    }

    @Override
    public void tick(boolean slowDown, float movementMultiplier) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || Minecraft.getInstance().screen != null || player == null) {
            forwardImpulse = leftImpulse = 0;
            up = down = left = right = jumping = shiftKeyDown = false;
            return;
        }

        InputHandler input = addon.getInputHandler();
        if (input == null) {
            forwardImpulse = leftImpulse = 0;
            up = down = left = right = jumping = shiftKeyDown = false;
            return;
        }

        LabyControllerConfiguration config = addon.configuration();

        // Get raw movement input
        float forwardRaw = input.getMoveForward();
        float strafeRaw = -input.getMoveStrafe();

        // Apply keyboard-like conversion if enabled (for anti-cheat compatibility)
        float forward, strafe;
        if (addon.shouldUseKeyboardLikeMovement()) {
            forward = Math.abs(forwardRaw) >= ACTIVATION_THRESHOLD ? Math.copySign(1.0f, forwardRaw) : 0;
            strafe = Math.abs(strafeRaw) >= ACTIVATION_THRESHOLD ? Math.copySign(1.0f, strafeRaw) : 0;
        } else {
            forward = forwardRaw;
            strafe = strafeRaw;
        }

        this.up = forward > 0;
        this.down = forward < 0;
        this.left = strafe > 0;
        this.right = strafe < 0;

        // Apply slowdown if sneaking
        if (slowDown) {
            forward *= movementMultiplier;
            strafe *= movementMultiplier;
        }

        this.forwardImpulse = forward;
        this.leftImpulse = strafe;

        // Handle jump
        boolean jumpPressed = input.isActionActive(GameAction.JUMP);
        if (jumpPressed && !lastJumpPressed) jumpHeld = true;
        if (!jumpPressed) jumpHeld = false;
        lastJumpPressed = jumpPressed;
        this.jumping = jumpHeld;

        // Handle sneak
        boolean sneakPressed = input.isActionActive(GameAction.SNEAK);
        boolean useToggleSneak = config.toggleSneak().get();

        if (player.getAbilities().flying || (player.isInWater() && !player.onGround()) || player.getVehicle() != null) {
            if (sneakPressed && !lastSneakPressed) sneakHeld = true;
            if (!sneakPressed) sneakHeld = false;
        } else if (useToggleSneak) {
            if (sneakPressed && !lastSneakPressed) sneakHeld = !sneakHeld;
        } else {
            sneakHeld = sneakPressed;
        }

        if ((!player.getAbilities().flying && wasFlying && player.onGround()) || (!player.isPassenger() && wasPassenger)) {
            sneakHeld = false;
        }
        lastSneakPressed = sneakPressed;
        this.shiftKeyDown = sneakHeld;

        // Handle sprint
        boolean sprintPressed = input.isActionActive(GameAction.SPRINT);
        boolean useToggleSprint = config.toggleSprint().get();

        if (useToggleSprint) {
            if (sprintPressed && !lastSprintPressed) sprintToggled = !sprintToggled;
            if (sprintToggled && forward <= 0) sprintToggled = false;
        } else {
            sprintToggled = sprintPressed;
        }
        lastSprintPressed = sprintPressed;

        this.wasFlying = player.getAbilities().flying;
        this.wasPassenger = player.isPassenger();
    }

    public boolean isSprintToggled() {
        return sprintToggled;
    }
}
