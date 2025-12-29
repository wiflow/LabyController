package net.labymod.addons.labycontroller.sdl;

import com.sun.jna.ptr.IntByReference;
import dev.isxander.sdl3java.api.gamepad.SDL_Gamepad;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.ControllerType;
import net.labymod.addons.labycontroller.controller.GamepadAxis;
import net.labymod.addons.labycontroller.controller.GamepadButton;

import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadAxis.*;
import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadButton.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepad.*;
import static dev.isxander.sdl3java.api.power.SDL_PowerState.*;

public class SDLGamepad {

    private final SDL_JoystickID joystickId;
    private final SDL_Gamepad gamepad;
    private final String name;
    private final ControllerType type;
    private final ControllerState state;

    private boolean connected;

    // Raw state arrays for updating ControllerState
    private final boolean[] rawButtons;
    private final float[] rawAxes;

    public SDLGamepad(SDL_JoystickID joystickId) {
        this.joystickId = joystickId;
        this.gamepad = SDL_OpenGamepad(joystickId);
        
        if (gamepad == null) {
            throw new RuntimeException("Failed to open SDL gamepad for joystick " + joystickId);
        }

        this.name = SDL_GetGamepadName(gamepad);
        this.type = ControllerType.fromName(name);
        this.state = new ControllerState();
        this.connected = true;

        this.rawButtons = new boolean[GamepadButton.getButtonCount()];
        this.rawAxes = new float[GamepadAxis.getAxisCount()];

        System.out.println("[Controlify] Opened SDL gamepad: " + name + " (type: " + type + ")");
    }

        public void poll() {
        if (!connected || gamepad == null) {
            return;
        }

        // Read buttons
        rawButtons[GamepadButton.A.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_SOUTH);
        rawButtons[GamepadButton.B.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_EAST);
        rawButtons[GamepadButton.X.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_WEST);
        rawButtons[GamepadButton.Y.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_NORTH);
        rawButtons[GamepadButton.LEFT_BUMPER.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_LEFT_SHOULDER);
        rawButtons[GamepadButton.RIGHT_BUMPER.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER);
        rawButtons[GamepadButton.BACK.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_BACK);
        rawButtons[GamepadButton.START.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_START);
        rawButtons[GamepadButton.GUIDE.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_GUIDE);
        rawButtons[GamepadButton.LEFT_STICK.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_LEFT_STICK);
        rawButtons[GamepadButton.RIGHT_STICK.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_RIGHT_STICK);
        rawButtons[GamepadButton.DPAD_UP.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_DPAD_UP);
        rawButtons[GamepadButton.DPAD_DOWN.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_DPAD_DOWN);
        rawButtons[GamepadButton.DPAD_LEFT.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_DPAD_LEFT);
        rawButtons[GamepadButton.DPAD_RIGHT.ordinal()] = SDL_GetGamepadButton(gamepad, SDL_GAMEPAD_BUTTON_DPAD_RIGHT);

        // Read axes (SDL returns short values, normalize to float -1 to 1)
        rawAxes[GamepadAxis.LEFT_STICK_X.ordinal()] = mapShortToFloat(SDL_GetGamepadAxis(gamepad, SDL_GAMEPAD_AXIS_LEFTX));
        rawAxes[GamepadAxis.LEFT_STICK_Y.ordinal()] = mapShortToFloat(SDL_GetGamepadAxis(gamepad, SDL_GAMEPAD_AXIS_LEFTY));
        rawAxes[GamepadAxis.RIGHT_STICK_X.ordinal()] = mapShortToFloat(SDL_GetGamepadAxis(gamepad, SDL_GAMEPAD_AXIS_RIGHTX));
        rawAxes[GamepadAxis.RIGHT_STICK_Y.ordinal()] = mapShortToFloat(SDL_GetGamepadAxis(gamepad, SDL_GAMEPAD_AXIS_RIGHTY));
        
        // Triggers: SDL returns 0 to 32767, we need to convert to -1 to 1 for ControllerState normalization
        // Then ControllerState will convert from -1..1 to 0..1
        rawAxes[GamepadAxis.LEFT_TRIGGER.ordinal()] = mapTriggerToFloat(SDL_GetGamepadAxis(gamepad, SDL_GAMEPAD_AXIS_LEFT_TRIGGER));
        rawAxes[GamepadAxis.RIGHT_TRIGGER.ordinal()] = mapTriggerToFloat(SDL_GetGamepadAxis(gamepad, SDL_GAMEPAD_AXIS_RIGHT_TRIGGER));

        // Update state with raw values
        state.updateDirect(rawButtons, rawAxes);
    }

        private float mapShortToFloat(short value) {
        return value / 32767.0f;
    }

        private float mapTriggerToFloat(short value) {
        // Convert 0..32767 to -1..1
        float normalized = value / 32767.0f;  // 0 to 1
        return (normalized * 2.0f) - 1.0f;     // -1 to 1
    }

        public boolean isConnected() {
        return connected && gamepad != null;
    }

        public void close() {
        if (gamepad != null) {
            SDL_CloseGamepad(gamepad);
        }
        connected = false;
        state.reset();
    }

    // ===== RUMBLE / VIBRATION =====

        public boolean rumble(float lowFrequency, float highFrequency, int durationMs) {
        if (!connected || gamepad == null) {
            return false;
        }

        // Convert 0-1 floats to 0-65535 (SDL uses 16-bit unsigned values as char)
        char lowFreq = (char) (Math.max(0, Math.min(1, lowFrequency)) * 65535);
        char highFreq = (char) (Math.max(0, Math.min(1, highFrequency)) * 65535);

        return SDL_RumbleGamepad(gamepad, lowFreq, highFreq, durationMs);
    }

        public boolean rumbleTriggers(float leftTrigger, float rightTrigger, int durationMs) {
        if (!connected || gamepad == null) {
            return false;
        }

        char left = (char) (Math.max(0, Math.min(1, leftTrigger)) * 65535);
        char right = (char) (Math.max(0, Math.min(1, rightTrigger)) * 65535);

        return SDL_RumbleGamepadTriggers(gamepad, left, right, durationMs);
    }

        public void stopRumble() {
        if (connected && gamepad != null) {
            SDL_RumbleGamepad(gamepad, (char) 0, (char) 0, 0);
        }
    }

    // ===== LED / LIGHT BAR =====

        public boolean setLED(int red, int green, int blue) {
        if (!connected || gamepad == null) {
            return false;
        }

        // Clamp to 0-255
        int r = Math.max(0, Math.min(255, red));
        int g = Math.max(0, Math.min(255, green));
        int b = Math.max(0, Math.min(255, blue));

        return SDL_SetGamepadLED(gamepad, (byte) r, (byte) g, (byte) b);
    }

        public boolean setLEDColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return setLED(r, g, b);
    }

        public boolean hasLED() {
        // PlayStation and Switch Pro controllers support LED
        return type == ControllerType.PLAYSTATION || type == ControllerType.SWITCH;
    }

        public boolean hasRumble() {
        // Most modern controllers support rumble
        return connected && gamepad != null;
    }

    // ===== BATTERY / POWER =====

        public enum BatteryState {
        UNKNOWN,        // Cannot determine state
        ON_BATTERY,     // Not charging, on battery power
        CHARGING,       // Currently charging
        CHARGED,        // Fully charged, plugged in
        NO_BATTERY      // Wired controller, no battery
    }

    // Cached battery state to avoid polling too frequently
    private int cachedBatteryPercent = -1;
    private BatteryState cachedBatteryState = BatteryState.UNKNOWN;
    private long lastBatteryPollTime = 0;
    private static final long BATTERY_POLL_INTERVAL_MS = 5000; // Poll every 5 seconds

        public int getBatteryPercent() {
        if (!connected || gamepad == null) {
            return -1;
        }

        updateBatteryInfo();
        return cachedBatteryPercent;
    }

        public BatteryState getBatteryState() {
        if (!connected || gamepad == null) {
            return BatteryState.UNKNOWN;
        }

        updateBatteryInfo();
        return cachedBatteryState;
    }

        private void updateBatteryInfo() {
        long now = System.currentTimeMillis();
        if (now - lastBatteryPollTime < BATTERY_POLL_INTERVAL_MS) {
            return; // Use cached values
        }
        lastBatteryPollTime = now;

        try {
            IntByReference percentRef = new IntByReference();
            int powerState = SDL_GetGamepadPowerInfo(gamepad, percentRef);

            cachedBatteryPercent = percentRef.getValue();

            cachedBatteryState = switch (powerState) {
                case SDL_POWERSTATE_ON_BATTERY -> BatteryState.ON_BATTERY;
                case SDL_POWERSTATE_CHARGING -> BatteryState.CHARGING;
                case SDL_POWERSTATE_CHARGED -> BatteryState.CHARGED;
                case SDL_POWERSTATE_NO_BATTERY -> BatteryState.NO_BATTERY;
                default -> BatteryState.UNKNOWN;
            };
        } catch (Exception e) {
            cachedBatteryPercent = -1;
            cachedBatteryState = BatteryState.UNKNOWN;
        }
    }

        public boolean isBatteryLow() {
        int percent = getBatteryPercent();
        return percent >= 0 && percent < 20;
    }

    // Getters
    public SDL_JoystickID getJoystickId() {
        return joystickId;
    }

    public String getName() {
        return name;
    }

    public ControllerType getType() {
        return type;
    }

    public ControllerState getState() {
        return state;
    }

    @Override
    public String toString() {
        return String.format("SDLGamepad[id=%d, name=%s, type=%s, connected=%b]",
            joystickId.intValue(), name, type, connected);
    }
}
