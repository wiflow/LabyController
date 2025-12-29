package net.labymod.addons.labycontroller.v1_21_5.input;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.LabyControllerConfiguration;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

public class ControllerInput extends ClientInput {
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
    public void tick() {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || Minecraft.getInstance().screen != null || player == null) {
            this.moveVector = Vec2.ZERO;
            this.keyPresses = Input.EMPTY;
            return;
        }

        InputHandler input = addon.getInputHandler();
        if (input == null) {
            this.moveVector = Vec2.ZERO;
            this.keyPresses = Input.EMPTY;
            return;
        }

        LabyControllerConfiguration config = addon.configuration();

        // Get raw movement input
        float forwardRaw = input.getMoveForward();
        float leftRaw = -input.getMoveStrafe();

        // Apply keyboard-like conversion if enabled (for anti-cheat compatibility)
        float forward, left;
        if (addon.shouldUseKeyboardLikeMovement()) {
            forward = Math.abs(forwardRaw) >= ACTIVATION_THRESHOLD ? Math.copySign(1.0f, forwardRaw) : 0;
            left = Math.abs(leftRaw) >= ACTIVATION_THRESHOLD ? Math.copySign(1.0f, leftRaw) : 0;
        } else {
            forward = forwardRaw;
            left = leftRaw;
        }

        boolean up = forward > 0;
        boolean down = forward < 0;
        boolean leftDir = left > 0;
        boolean right = left < 0;

        this.moveVector = new Vec2(left, forward);

        // Handle jump
        boolean jumpPressed = input.isActionActive(GameAction.JUMP);
        if (jumpPressed && !lastJumpPressed) jumpHeld = true;
        if (!jumpPressed) jumpHeld = false;
        lastJumpPressed = jumpPressed;

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

        // Handle sprint
        boolean sprintPressed = input.isActionActive(GameAction.SPRINT);
        boolean useToggleSprint = config.toggleSprint().get();
        boolean sprinting;

        if (useToggleSprint) {
            if (sprintPressed && !lastSprintPressed) sprintToggled = !sprintToggled;
            if (sprintToggled && forward <= 0) sprintToggled = false;
            sprinting = sprintToggled;
        } else {
            sprinting = sprintPressed;
        }
        lastSprintPressed = sprintPressed;

        this.keyPresses = new Input(up, down, leftDir, right, jumpHeld, sneakHeld, sprinting);

        this.wasFlying = player.getAbilities().flying;
        this.wasPassenger = player.isPassenger();
    }
}
