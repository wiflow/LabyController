package net.labymod.addons.labycontroller.input;

import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.GamepadButton;

public class VirtualMouse {

    // Target position (updated per tick)
    private double targetX;
    private double targetY;

    // Current position (interpolated per frame)
    private double currentX;
    private double currentY;

    private int screenWidth = 800;
    private int screenHeight = 600;
    private float cursorSpeed = 15.0f;
    private boolean enabled = true;

    // Button state tracking for click detection
    private boolean lastAPressed = false;
    private boolean lastBPressed = false;
    private boolean clickPending = false;
    private boolean rightClickPending = false;
    private boolean backPending = false;

    // D-pad navigation cooldown
    private int dpadCooldown = 0;
    private static final int DPAD_COOLDOWN_TICKS = 8;

    // Interpolation smoothing factor (higher = faster catch-up)
    private static final float LERP_SPEED = 0.4f;

    public VirtualMouse() {
        resetPosition();
    }

    public void resetPosition() {
        targetX = screenWidth / 2.0;
        targetY = screenHeight / 2.0;
        currentX = targetX;
        currentY = targetY;
    }

    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        // Clamp positions to new bounds
        targetX = Math.max(0, Math.min(screenWidth, targetX));
        targetY = Math.max(0, Math.min(screenHeight, targetY));
        currentX = Math.max(0, Math.min(screenWidth, currentX));
        currentY = Math.max(0, Math.min(screenHeight, currentY));
    }

    public void updateFromState(ControllerState state) {
        if (!enabled || state == null) {
            return;
        }

        // Decrease D-pad cooldown
        if (dpadCooldown > 0) dpadCooldown--;

        // Left stick for smooth cursor movement
        float moveX = state.getLeftStickX();
        float moveY = state.getLeftStickY();

        // Apply deadzone
        if (Math.abs(moveX) < 0.15f) moveX = 0;
        if (Math.abs(moveY) < 0.15f) moveY = 0;

        // Apply acceleration curve for finer control (cubic easing)
        if (moveX != 0 || moveY != 0) {
            float absX = Math.abs(moveX);
            float absY = Math.abs(moveY);

            // Cubic acceleration for smoother control
            float accelX = moveX * absX * absX;
            float accelY = moveY * absY * absY;

            // Scale by window size for consistent feel
            float windowScale = Math.max(screenWidth, screenHeight) / 800f;

            targetX += accelX * cursorSpeed * windowScale;
            targetY += accelY * cursorSpeed * windowScale;
        }

        // D-pad for precise snapping (with cooldown)
        if (dpadCooldown == 0) {
            boolean dpadMoved = false;
            int snapDistance = 50; // Move in larger increments with D-pad

            if (state.isButtonPressed(GamepadButton.DPAD_LEFT)) {
                targetX -= snapDistance;
                dpadMoved = true;
            } else if (state.isButtonPressed(GamepadButton.DPAD_RIGHT)) {
                targetX += snapDistance;
                dpadMoved = true;
            }

            if (state.isButtonPressed(GamepadButton.DPAD_UP)) {
                targetY -= snapDistance;
                dpadMoved = true;
            } else if (state.isButtonPressed(GamepadButton.DPAD_DOWN)) {
                targetY += snapDistance;
                dpadMoved = true;
            }

            if (dpadMoved) {
                dpadCooldown = DPAD_COOLDOWN_TICKS;
                // Snap current to target immediately for D-pad
                currentX = targetX;
                currentY = targetY;
            }
        }

        // Clamp target to screen bounds
        targetX = Math.max(0, Math.min(screenWidth - 1, targetX));
        targetY = Math.max(0, Math.min(screenHeight - 1, targetY));

        // A button for click
        boolean aPressed = state.isButtonPressed(GamepadButton.A);
        if (aPressed && !lastAPressed) {
            clickPending = true;
        }
        lastAPressed = aPressed;

        // B button for back/right-click
        boolean bPressed = state.isButtonPressed(GamepadButton.B);
        if (bPressed && !lastBPressed) {
            backPending = true;
        }
        lastBPressed = bPressed;

        // X button for right-click (secondary action)
        if (state.isButtonJustPressed(GamepadButton.X)) {
            rightClickPending = true;
        }
    }

    public boolean interpolate(float deltaTicks) {
        if (!enabled) {
            return false;
        }

        // Check if we need to update (using epsilon comparison like Controlify)
        boolean needsUpdate = Math.round(targetX * 100) / 100.0 != Math.round(currentX * 100) / 100.0
                           || Math.round(targetY * 100) / 100.0 != Math.round(currentY * 100) / 100.0;

        if (needsUpdate) {
            // Use Controlify's lerp formula: current + (target - current) * delta
            currentX = currentX + (targetX - currentX) * deltaTicks;
            currentY = currentY + (targetY - currentY) * deltaTicks;

            // Clamp to screen bounds
            currentX = Math.max(0, Math.min(screenWidth - 1, currentX));
            currentY = Math.max(0, Math.min(screenHeight - 1, currentY));

            return true;
        } else {
            // Snap exactly to target when close enough
            currentX = targetX;
            currentY = targetY;
            return false;
        }
    }

    private double lerp(double start, double end, float t) {
        return start + (end - start) * t;
    }

    public boolean consumeClick() {
        if (clickPending) {
            clickPending = false;
            return true;
        }
        return false;
    }

    public boolean consumeRightClick() {
        if (rightClickPending) {
            rightClickPending = false;
            return true;
        }
        return false;
    }

    public boolean consumeBack() {
        if (backPending) {
            backPending = false;
            return true;
        }
        return false;
    }

    // Getters and setters

    public double getX() {
        return currentX;
    }

    public double getY() {
        return currentY;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public int getIntX() {
        return (int) currentX;
    }

    public int getIntY() {
        return (int) currentY;
    }

    public int getIntX(float partialTicks) {
        return (int) lerp(currentX, targetX, partialTicks);
    }

    public int getIntY(float partialTicks) {
        return (int) lerp(currentY, targetY, partialTicks);
    }

    public void setPosition(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        this.currentX = x;
        this.currentY = y;
    }

    public float getCursorSpeed() {
        return cursorSpeed;
    }

    public void setCursorSpeed(float speed) {
        this.cursorSpeed = Math.max(1.0f, Math.min(50.0f, speed));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}
