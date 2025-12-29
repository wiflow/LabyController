package net.labymod.addons.labycontroller.listener;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;

public class InputModeListener {

    private final LabyControllerAddon addon;

    public InputModeListener(LabyControllerAddon addon) {
        this.addon = addon;
    }

    @Subscribe
    public void onKeyPress(KeyEvent event) {
        // Only process if mixed input mode is enabled
        if (!addon.configuration().mixedInputMode().get()) {
            return;
        }

        // Notify the input handler that keyboard/mouse was used
        if (addon.getInputHandler() != null) {
            addon.getInputHandler().onKeyboardMouseInput();
        }
    }

    @Subscribe
    public void onMouseButton(MouseButtonEvent event) {
        // Only process if mixed input mode is enabled
        if (!addon.configuration().mixedInputMode().get()) {
            return;
        }

        // Notify the input handler that keyboard/mouse was used
        if (addon.getInputHandler() != null) {
            addon.getInputHandler().onKeyboardMouseInput();
        }
    }
}
