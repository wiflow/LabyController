package net.labymod.addons.labycontroller;

import net.labymod.addons.labycontroller.sdl.SDLGamepad;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.Exclude;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.AbstractSetting;

@ConfigName("controllerStatus")
public class ControllerStatusSubSettings extends Config {

        public Component getDisplayName() {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.hasController()) {
            return Component.text("No Controller Connected", NamedTextColor.RED);
        }

        String name = addon.getActiveControllerName().orElse("Unknown Controller");
        return Component.text(name, NamedTextColor.GREEN);
    }

        public Component getDescription() {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.hasController()) {
            return Component.text("Connect a controller to use Controlify", NamedTextColor.GRAY);
        }

        int battery = addon.getBatteryPercent();
        SDLGamepad.BatteryState state = addon.getBatteryState();

        if (battery >= 0) {
            TextColor color = battery > 50 ? NamedTextColor.GREEN :
                              battery > 20 ? NamedTextColor.YELLOW :
                              NamedTextColor.RED;

            String stateText = "";
            if (state == SDLGamepad.BatteryState.CHARGING) {
                stateText = " (Charging)";
            } else if (state == SDLGamepad.BatteryState.CHARGED) {
                stateText = " (Fully Charged)";
            }

            return Component.text("Battery: " + battery + "%" + stateText, color);
        } else if (state == SDLGamepad.BatteryState.NO_BATTERY) {
            return Component.text("Wired Controller", NamedTextColor.AQUA);
        }

        return Component.text("Controller ready", NamedTextColor.GRAY);
    }
}
