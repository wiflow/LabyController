package net.labymod.addons.labycontroller.sdl;

import static dev.isxander.sdl3java.api.SdlInit.*;
import static dev.isxander.sdl3java.api.SdlSubSystemConst.*;
import static dev.isxander.sdl3java.api.error.SdlError.*;
import static dev.isxander.sdl3java.api.hints.SdlHintConsts.*;
import static dev.isxander.sdl3java.api.hints.SdlHints.*;

public class LoadedSDLNatives {

    private boolean hasAttemptedStart = false;

    void startSDL3() {
        if (hasAttemptedStart) {
            System.out.println("[Controlify] SDL3 has already been started, not starting again");
            return;
        }
        hasAttemptedStart = true;

        // Set hints for better controller support
        SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_ENHANCED_REPORTS, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI_STEAM, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_ROG_CHAKRAM, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_ALLOW_BACKGROUND_EVENTS, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_LINUX_DEADZONES, "1");

        // Disable Steam's virtual gamepad to prevent double input
        SDL_SetHint("SDL_HINT_JOYSTICK_HIDAPI_STEAM_HORI", "0");

        // Initialize SDL with joystick, gamepad, and events subsystems
        if (!SDL_Init(SDL_INIT_JOYSTICK | SDL_INIT_GAMEPAD | SDL_INIT_EVENTS)) {
            String error = SDL_GetError();
            System.err.println("[Controlify] Failed to initialize SDL3: " + error);
            throw new RuntimeException("Failed to initialize SDL3: " + error);
        }

        System.out.println("[Controlify] Successfully initialized SDL3 subsystems");
    }
}
