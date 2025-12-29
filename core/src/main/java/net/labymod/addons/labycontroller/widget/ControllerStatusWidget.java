package net.labymod.addons.labycontroller.widget;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.sdl.SDLGamepad;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@ConfigName("controllerStatus")
public class ControllerStatusWidget extends Config {

    // Dummy property to make this show up as a clickable subsetting
    private final ConfigProperty<Boolean> showDetails = new ConfigProperty<>(false);

        public String getDisplayText() {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.hasController()) {
            return "No Controller Connected";
        }

        String name = addon.getActiveControllerName().orElse("Unknown Controller");
        int battery = addon.getBatteryPercent();
        SDLGamepad.BatteryState state = addon.getBatteryState();

        if (battery >= 0) {
            String stateText = "";
            if (state == SDLGamepad.BatteryState.CHARGING) {
                stateText = " (Charging)";
            } else if (state == SDLGamepad.BatteryState.CHARGED) {
                stateText = " (Charged)";
            }

            return name + " - Battery: " + battery + "%" + stateText;
        } else if (state == SDLGamepad.BatteryState.NO_BATTERY) {
            return name + " (Wired)";
        }

        return name;
    }

    public ConfigProperty<Boolean> showDetails() {
        return showDetails;
    }
}
