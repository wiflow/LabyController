package net.labymod.addons.labycontroller.v1_21_8.input;

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

        // Get movement input
        float forwardImpulse = input.getMoveForward();
        float leftImpulse = -input.getMoveStrafe(); // Negate to fix inversion

        // Set movement direction booleans
        boolean up = forwardImpulse > 0;
        boolean down = forwardImpulse < 0;
        boolean left = leftImpulse > 0;
        boolean right = leftImpulse < 0;

        // Set move vector with length limiting (not normalizing) to preserve analog control
        this.moveVector = new Vec2(leftImpulse, forwardImpulse);
        float length = this.moveVector.length();
        if (length > 1) {
            this.moveVector = this.moveVector.scale(1f / length);
        }

        // Handle jump - only start jumping on press, stop on release
        boolean jumpPressed = input.isActionActive(GameAction.JUMP);
        if (jumpPressed && !lastJumpPressed) {
            jumpHeld = true;
        }
        if (!jumpPressed) {
            jumpHeld = false;
        }
        lastJumpPressed = jumpPressed;

        // Handle sneak - configurable toggle/hold
        boolean sneakPressed = input.isActionActive(GameAction.SNEAK);
        boolean useToggleSneak = config.toggleSneak().get();

        if (player.getAbilities().flying ||
            (player.isInWater() && !player.onGround()) ||
            player.getVehicle() != null) {
            // Always hold mode when flying, swimming, or riding
            if (sneakPressed && !lastSneakPressed) {
                sneakHeld = true;
            }
            if (!sneakPressed) {
                sneakHeld = false;
            }
        } else if (useToggleSneak) {
            // Toggle mode when configured and on ground
            if (sneakPressed && !lastSneakPressed) {
                sneakHeld = !sneakHeld;
            }
        } else {
            // Hold mode
            sneakHeld = sneakPressed;
        }

        // Reset sneak when landing or dismounting
        if ((!player.getAbilities().flying && wasFlying && player.onGround()) ||
            (!player.isPassenger() && wasPassenger)) {
            sneakHeld = false;
        }
        lastSneakPressed = sneakPressed;

        // Handle sprint - configurable toggle/hold
        boolean sprintPressed = input.isActionActive(GameAction.SPRINT);
        boolean useToggleSprint = config.toggleSprint().get();
        boolean sprinting;

        if (useToggleSprint) {
            // Toggle mode
            if (sprintPressed && !lastSprintPressed) {
                sprintToggled = !sprintToggled;
            }
            // Auto-disable sprint when stopped moving
            if (sprintToggled && forwardImpulse <= 0) {
                sprintToggled = false;
            }
            sprinting = sprintToggled;
        } else {
            // Hold mode
            sprinting = sprintPressed;
        }
        lastSprintPressed = sprintPressed;

        // Create the Input record
        this.keyPresses = new Input(up, down, left, right, jumpHeld, sneakHeld, sprinting);

        this.wasFlying = player.getAbilities().flying;
        this.wasPassenger = player.isPassenger();
    }
}
