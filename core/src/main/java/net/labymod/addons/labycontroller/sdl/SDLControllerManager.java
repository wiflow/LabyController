package net.labymod.addons.labycontroller.sdl;

import dev.isxander.sdl3java.api.events.events.SDL_Event;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.ControllerType;

import java.util.*;

import static dev.isxander.sdl3java.api.events.SDL_EventType.*;
import static dev.isxander.sdl3java.api.events.SdlEvents.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepad.*;
import static dev.isxander.sdl3java.api.joystick.SdlJoystick.*;

public class SDLControllerManager {

    private final Map<Integer, SDLGamepad> gamepads = new HashMap<>();
    private SDLGamepad activeGamepad = null;
    private SDL_Event event = new SDL_Event();
    private boolean initialized = false;

    // Callbacks for connection events
    private Runnable onControllerConnected;
    private Runnable onControllerDisconnected;

    public SDLControllerManager() {
    }

        public void initialize() {
        if (initialized) {
            return;
        }

        if (!SDLNativesLoader.isLoaded()) {
            System.err.println("[Controlify] Cannot initialize SDLControllerManager - SDL not loaded");
            return;
        }

        // Scan for already connected gamepads
        scanForGamepads();
        initialized = true;

        System.out.println("[Controlify] SDLControllerManager initialized with " + gamepads.size() + " gamepad(s)");
    }

        public void scanForGamepads() {
        gamepads.clear();

        SDL_JoystickID[] joysticks = SDL_GetJoysticks();
        if (joysticks == null) {
            return;
        }

        for (SDL_JoystickID jid : joysticks) {
            if (SDL_IsGamepad(jid)) {
                try {
                    SDLGamepad gamepad = new SDLGamepad(jid);
                    gamepads.put(jid.intValue(), gamepad);

                    if (activeGamepad == null) {
                        activeGamepad = gamepad;
                    }
                } catch (Exception e) {
                    System.err.println("[Controlify] Failed to open gamepad " + jid.intValue() + ": " + e.getMessage());
                }
            }
        }
    }

        public void update() {
        if (!initialized) {
            return;
        }

        // Process SDL events
        SDL_PumpEvents();

        if (event == null) {
            event = new SDL_Event();
        }

        while (SDL_PollEvent(event)) {
            int eventType = event.type;

            switch (eventType) {
                case SDL_EVENT_JOYSTICK_ADDED -> {
                    SDL_JoystickID jid = event.jdevice.which;
                    if (jid != null && SDL_IsGamepad(jid) && !gamepads.containsKey(jid.intValue())) {
                        System.out.println("[Controlify] SDL: Gamepad connected: " + jid.intValue());
                        try {
                            SDLGamepad gamepad = new SDLGamepad(jid);
                            gamepads.put(jid.intValue(), gamepad);

                            if (activeGamepad == null) {
                                activeGamepad = gamepad;
                            }

                            if (onControllerConnected != null) {
                                onControllerConnected.run();
                            }
                        } catch (Exception e) {
                            System.err.println("[Controlify] Failed to open gamepad: " + e.getMessage());
                        }
                    }
                }
                case SDL_EVENT_JOYSTICK_REMOVED -> {
                    SDL_JoystickID jid = event.jdevice.which;
                    if (jid != null) {
                        System.out.println("[Controlify] SDL: Gamepad disconnected: " + jid.intValue());
                        SDLGamepad removed = gamepads.remove(jid.intValue());
                        if (removed != null) {
                            removed.close();
                            if (removed == activeGamepad) {
                                activeGamepad = gamepads.isEmpty() ? null : gamepads.values().iterator().next();
                                if (onControllerDisconnected != null) {
                                    onControllerDisconnected.run();
                                }
                            }
                        }
                    }
                }
            }
        }

        // Update gamepad states
        SDL_UpdateGamepads();

        // Poll all gamepads
        for (SDLGamepad gamepad : gamepads.values()) {
            gamepad.poll();
        }

        // Auto-select active gamepad based on input
        if (activeGamepad == null && !gamepads.isEmpty()) {
            for (SDLGamepad gamepad : gamepads.values()) {
                if (gamepad.getState().hasAnyInput()) {
                    setActiveGamepad(gamepad);
                    break;
                }
            }
        }
    }

        public Optional<SDLGamepad> getActiveGamepad() {
        return Optional.ofNullable(activeGamepad);
    }

        public void setActiveGamepad(SDLGamepad gamepad) {
        this.activeGamepad = gamepad;
    }

        public Collection<SDLGamepad> getGamepads() {
        return Collections.unmodifiableCollection(gamepads.values());
    }

        public boolean hasGamepad() {
        return !gamepads.isEmpty();
    }

        public int getGamepadCount() {
        return gamepads.size();
    }

        public void setOnControllerConnected(Runnable callback) {
        this.onControllerConnected = callback;
    }

        public void setOnControllerDisconnected(Runnable callback) {
        this.onControllerDisconnected = callback;
    }

        public void shutdown() {
        for (SDLGamepad gamepad : gamepads.values()) {
            gamepad.close();
        }
        gamepads.clear();
        activeGamepad = null;
        initialized = false;
    }

        public boolean isInitialized() {
        return initialized;
    }

    // ===== RUMBLE / VIBRATION =====

        public boolean rumble(float lowFrequency, float highFrequency, int durationMs) {
        if (activeGamepad != null) {
            return activeGamepad.rumble(lowFrequency, highFrequency, durationMs);
        }
        return false;
    }

        public boolean rumbleTriggers(float left, float right, int durationMs) {
        if (activeGamepad != null) {
            return activeGamepad.rumbleTriggers(left, right, durationMs);
        }
        return false;
    }

        public void stopRumble() {
        if (activeGamepad != null) {
            activeGamepad.stopRumble();
        }
    }

    // ===== LED / LIGHT BAR =====

        public boolean setLED(int red, int green, int blue) {
        if (activeGamepad != null) {
            return activeGamepad.setLED(red, green, blue);
        }
        return false;
    }

        public boolean setLEDColor(int color) {
        if (activeGamepad != null) {
            return activeGamepad.setLEDColor(color);
        }
        return false;
    }

        public boolean hasLED() {
        return activeGamepad != null && activeGamepad.hasLED();
    }

        public boolean hasRumble() {
        return activeGamepad != null && activeGamepad.hasRumble();
    }
}
