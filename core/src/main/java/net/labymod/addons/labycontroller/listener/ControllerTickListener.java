package net.labymod.addons.labycontroller.listener;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;

public class ControllerTickListener {

    private final LabyControllerAddon addon;

    public ControllerTickListener(LabyControllerAddon addon) {
        this.addon = addon;
    }

    @Subscribe
    public void onGameTick(GameTickEvent event) {
        // Update controller state every tick
        addon.tick();
    }
}
