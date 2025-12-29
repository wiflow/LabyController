package net.labymod.addons.labycontroller.command;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.sdl.SDLControllerManager;
import net.labymod.addons.labycontroller.sdl.SDLGamepad;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;

public class ControllerStatusCommand extends Command {

    private final LabyControllerAddon addon;

    public ControllerStatusCommand(LabyControllerAddon addon) {
        super("controllerstatus", "cs", "controlify");
        this.addon = addon;
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        displayMessage(Component.text("=== Controlify Debug ===").color(NamedTextColor.GOLD));
        displayMessage(Component.text("Addon enabled: " + addon.configuration().enabled().get()).color(NamedTextColor.WHITE));
        displayMessage(Component.text("Input backend: SDL3").color(NamedTextColor.AQUA));

        SDLControllerManager sdlManager = addon.getSDLControllerManager();

        displayMessage(Component.text("--- Controller Status ---").color(NamedTextColor.YELLOW));

        if (sdlManager == null) {
            displayMessage(Component.text("SDL3 not loaded!").color(NamedTextColor.RED));
            return true;
        }

        displayMessage(Component.text("Controllers: " + sdlManager.getGamepadCount()).color(NamedTextColor.WHITE));

        for (SDLGamepad gamepad : sdlManager.getGamepads()) {
            displayMessage(Component.text("  - " + gamepad.getName() + " (" + gamepad.getType().getDisplayName() + ")").color(NamedTextColor.GREEN));
        }

        sdlManager.getActiveGamepad().ifPresentOrElse(
            gamepad -> {
                displayMessage(Component.text("Active: " + gamepad.getName()).color(NamedTextColor.GREEN));

                ControllerState state = gamepad.getState();
                displayMessage(Component.text("hasAnyInput: " + state.hasAnyInput()).color(NamedTextColor.WHITE));

                // Show axis values
                displayMessage(Component.text(String.format("  LX=%.3f LY=%.3f RX=%.3f RY=%.3f",
                    state.getLeftStickX(), state.getLeftStickY(),
                    state.getRightStickX(), state.getRightStickY()
                )).color(NamedTextColor.WHITE));
                displayMessage(Component.text(String.format("  LT=%.3f RT=%.3f",
                    state.getLeftTrigger(), state.getRightTrigger()
                )).color(NamedTextColor.WHITE));
            },
            () -> displayMessage(Component.text("Active: None").color(NamedTextColor.RED))
        );

        boolean controllerActive = addon.getInputHandler() != null && addon.getInputHandler().isControllerActive();
        displayMessage(Component.text("Controller mode: " + controllerActive).color(
            controllerActive ? NamedTextColor.GREEN : NamedTextColor.RED));

        // Handle "force" argument
        if (arguments.length > 0 && arguments[0].equalsIgnoreCase("force")) {
            if (addon.getInputHandler() != null) {
                addon.getInputHandler().forceActivate();
                displayMessage(Component.text(">>> Controller mode FORCE ACTIVATED! <<<").color(NamedTextColor.GREEN));
            }
        } else {
            displayMessage(Component.text("---").color(NamedTextColor.GRAY));
            displayMessage(Component.text("Tip: /cs force - Force activate controller mode").color(NamedTextColor.GRAY));
        }

        return true;
    }
}
