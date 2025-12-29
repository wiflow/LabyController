package net.labymod.addons.labycontroller;

import net.labymod.addons.labycontroller.binding.BindingManager;
import net.labymod.addons.labycontroller.command.ControllerStatusCommand;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.labymod.addons.labycontroller.input.VirtualMouse;
import net.labymod.addons.labycontroller.listener.ControllerTickListener;
import net.labymod.addons.labycontroller.listener.InputModeListener;
import net.labymod.addons.labycontroller.radial.RadialMenuHandler;
import net.labymod.addons.labycontroller.sdl.SDLControllerManager;
import net.labymod.addons.labycontroller.sdl.SDLGamepad;
import net.labymod.addons.labycontroller.sdl.SDLNativesLoader;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import java.util.Optional;

@AddonMain
public class LabyControllerAddon extends LabyAddon<LabyControllerConfiguration> {

    private static LabyControllerAddon instance;

    private SDLControllerManager sdlControllerManager;
    private BindingManager bindingManager;
    private InputHandler inputHandler;
    private VirtualMouse virtualMouse;
    private RadialMenuHandler radialMenuHandler;
    private boolean onScreenKeyboardActive = false;

    @Override
    protected void enable() {
        instance = this;

        this.logger().info("=== Controlify Addon Starting ===");

        // Initialize binding manager
        this.bindingManager = new BindingManager();
        this.virtualMouse = new VirtualMouse();

        // Load SDL3 for controller support
        this.logger().info("Loading SDL3 for controller support...");
        boolean sdlLoaded = SDLNativesLoader.tryLoad();

        if (!sdlLoaded) {
            this.logger().error("Failed to load SDL3! Controller support will not be available.");
            this.logger().error("Please report this issue with your system details.");
            return;
        }

        this.logger().info("SDL3 loaded successfully!");
        sdlControllerManager = new SDLControllerManager();

        // Set up controller connection callbacks
        sdlControllerManager.setOnControllerConnected(() -> {
            SDLGamepad gamepad = sdlControllerManager.getActiveGamepad().orElse(null);
            if (gamepad != null) {
                this.logger().info("[Controlify] Controller connected: " + gamepad.getName());
                applyConfiguration();
            }
        });

        sdlControllerManager.setOnControllerDisconnected(() -> {
            this.logger().info("[Controlify] Controller disconnected");
        });

        // Initialize SDL controller manager
        sdlControllerManager.initialize();

        // Create input handler
        this.inputHandler = new InputHandler(this);

        // Create radial menu handler
        this.radialMenuHandler = new RadialMenuHandler(this);

        // Apply configuration
        applyConfiguration();

        // Register setting category
        this.registerSettingCategory();

        // Register command for debugging
        this.registerCommand(new ControllerStatusCommand(this));

        // Register listeners
        this.registerListener(new ControllerTickListener(this));
        this.registerListener(new InputModeListener(this));

        this.logger().info("Controlify enabled - Controller support active!");
        this.logger().info("Use /controllerstatus or /cs to check controller status");

        // Log detected controllers
        logDetectedControllers();

        this.logger().info("=== Controlify Addon Ready ===");
    }

    @Override
    protected Class<LabyControllerConfiguration> configurationClass() {
        return LabyControllerConfiguration.class;
    }

        public void applyConfiguration() {
        LabyControllerConfiguration config = configuration();

        // Apply sensitivity settings
        if (inputHandler != null) {
            inputHandler.setLookSensitivity(config.lookSensitivity().get());
            inputHandler.setLookAcceleration(config.lookAcceleration().get());
        }

        // Apply deadzone settings to active controller
        if (sdlControllerManager != null) {
            sdlControllerManager.getActiveGamepad().ifPresent(gamepad -> {
                ControllerState state = gamepad.getState();
                state.setLeftStickDeadzone(config.leftStickDeadzone().get());
                state.setRightStickDeadzone(config.rightStickDeadzone().get());
                state.setTriggerDeadzone(config.triggerDeadzone().get());
            });
        }

        // Apply virtual mouse settings
        if (virtualMouse != null) {
            virtualMouse.setEnabled(config.guiNavigation().get());
            virtualMouse.setCursorSpeed(config.virtualMouseSpeed().get());
        }
    }

    // Light bar update tracking
    private int lightBarUpdateCounter = 0;
    private static final int LIGHT_BAR_UPDATE_INTERVAL = 20; // Update every second

        public void tick() {
        if (!configuration().enabled().get() || sdlControllerManager == null) {
            return;
        }

        // Update SDL controller manager
        sdlControllerManager.update();

        // Handle input
        if (inputHandler != null) {
            inputHandler.update();
        }

        // Handle radial menu
        if (radialMenuHandler != null) {
            getActiveControllerState().ifPresent(state -> {
                radialMenuHandler.update(state);
            });
        }

        // Update virtual mouse for GUI navigation
        // Note: The actual cursor movement for screens is handled in MixinMinecraft
        // This just updates the addon's virtual mouse for potential external use
        if (configuration().guiNavigation().get()) {
            getActiveControllerState().ifPresent(state -> {
                virtualMouse.updateFromState(state);
            });
        }

        // Update light bar periodically
        if (configuration().lightBarEnabled().get() && hasLED()) {
            lightBarUpdateCounter++;
            if (lightBarUpdateCounter >= LIGHT_BAR_UPDATE_INTERVAL) {
                lightBarUpdateCounter = 0;
                updateLightBar();
            }
        }
    }

        private void updateLightBar() {
        // Get player health percentage and map to color (green -> yellow -> red)
        float healthPercent = getPlayerHealthPercent();
        int r = (int) ((1.0f - healthPercent) * 255);
        int g = (int) (healthPercent * 255);
        int b = 0;
        setLED(r, g, b);
    }

        private float getPlayerHealthPercent() {
        try {
            // Access player via LabyMod API
            var minecraft = net.labymod.api.Laby.labyAPI().minecraft();
            var player = minecraft.getClientPlayer();
            if (player != null) {
                float health = player.getHealth();
                // Default max health in Minecraft is 20
                float maxHealth = 20.0f;
                if (maxHealth > 0) {
                    return Math.max(0, Math.min(1, health / maxHealth));
                }
            }
        } catch (Exception e) {
            // Fallback if API fails
        }
        return 1.0f;
    }

    // Static access for other components
    public static LabyControllerAddon getInstance() {
        return instance;
    }

    public SDLControllerManager getSDLControllerManager() {
        return sdlControllerManager;
    }

    public BindingManager getBindingManager() {
        return bindingManager;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public VirtualMouse getVirtualMouse() {
        return virtualMouse;
    }

    public RadialMenuHandler getRadialMenuHandler() {
        return radialMenuHandler;
    }

    public boolean isOnScreenKeyboardActive() {
        return onScreenKeyboardActive;
    }

    public void setOnScreenKeyboardActive(boolean active) {
        this.onScreenKeyboardActive = active;
    }

    public boolean isUsingSDL() {
        return true;
    }

        public Optional<ControllerState> getActiveControllerState() {
        if (sdlControllerManager != null) {
            return sdlControllerManager.getActiveGamepad().map(SDLGamepad::getState);
        }
        return Optional.empty();
    }

        public Optional<String> getActiveControllerName() {
        if (sdlControllerManager != null) {
            return sdlControllerManager.getActiveGamepad().map(SDLGamepad::getName);
        }
        return Optional.empty();
    }

        public boolean hasController() {
        return sdlControllerManager != null && sdlControllerManager.hasGamepad();
    }

        public boolean isControllerActive() {
        return configuration().enabled().get() &&
            hasController() &&
            inputHandler != null &&
            inputHandler.isControllerActive();
    }

        private void logDetectedControllers() {
        if (sdlControllerManager == null) {
            return;
        }

        if (sdlControllerManager.hasGamepad()) {
            this.logger().info("Found " + sdlControllerManager.getGamepadCount() + " controller(s):");
            for (SDLGamepad gamepad : sdlControllerManager.getGamepads()) {
                this.logger().info("  - " + gamepad.getName() + " (" + gamepad.getType().getDisplayName() + ")");
            }
        } else {
            this.logger().info("No controllers detected. Connect a controller to start using it.");
        }
    }

    // ===== RUMBLE / VIBRATION API =====

        public void vibrate(float lowFrequency, float highFrequency, int durationMs) {
        if (!configuration().vibrationEnabled().get() || sdlControllerManager == null) {
            return;
        }
        sdlControllerManager.rumble(lowFrequency, highFrequency, durationMs);
    }

        public void vibratePulse() {
        vibrate(0.5f, 0.5f, 100);
    }

        public void vibrateStrong(int durationMs) {
        vibrate(1.0f, 1.0f, durationMs);
    }

        public void vibrateTriggers(float left, float right, int durationMs) {
        if (!configuration().vibrationEnabled().get() || sdlControllerManager == null) {
            return;
        }
        sdlControllerManager.rumbleTriggers(left, right, durationMs);
    }

        public void stopVibration() {
        if (sdlControllerManager != null) {
            sdlControllerManager.stopRumble();
        }
    }

    // ===== LED / LIGHT BAR API =====

        public void setLED(int red, int green, int blue) {
        if (sdlControllerManager != null) {
            sdlControllerManager.setLED(red, green, blue);
        }
    }

        public void setLEDColor(int color) {
        if (sdlControllerManager != null) {
            sdlControllerManager.setLEDColor(color);
        }
    }

        public boolean hasLED() {
        return sdlControllerManager != null && sdlControllerManager.hasLED();
    }

        public boolean hasRumble() {
        return sdlControllerManager != null && sdlControllerManager.hasRumble();
    }

    // ===== BATTERY API =====

        public int getBatteryPercent() {
        if (sdlControllerManager != null) {
            return sdlControllerManager.getActiveGamepad()
                .map(SDLGamepad::getBatteryPercent)
                .orElse(-1);
        }
        return -1;
    }

        public SDLGamepad.BatteryState getBatteryState() {
        if (sdlControllerManager != null) {
            return sdlControllerManager.getActiveGamepad()
                .map(SDLGamepad::getBatteryState)
                .orElse(SDLGamepad.BatteryState.UNKNOWN);
        }
        return SDLGamepad.BatteryState.UNKNOWN;
    }

        public boolean isBatteryLow() {
        if (sdlControllerManager != null) {
            return sdlControllerManager.getActiveGamepad()
                .map(SDLGamepad::isBatteryLow)
                .orElse(false);
        }
        return false;
    }

        public String getControllerStatusText() {
        if (!hasController()) {
            return "No controller connected";
        }

        String name = getActiveControllerName().orElse("Unknown Controller");
        int battery = getBatteryPercent();
        SDLGamepad.BatteryState state = getBatteryState();

        StringBuilder sb = new StringBuilder(name);

        if (battery >= 0) {
            sb.append(" - ").append(battery).append("%");

            if (state == SDLGamepad.BatteryState.CHARGING) {
                sb.append(" (Charging)");
            } else if (state == SDLGamepad.BatteryState.CHARGED) {
                sb.append(" (Charged)");
            }
        } else if (state == SDLGamepad.BatteryState.NO_BATTERY) {
            sb.append(" (Wired)");
        }

        return sb.toString();
    }
}
