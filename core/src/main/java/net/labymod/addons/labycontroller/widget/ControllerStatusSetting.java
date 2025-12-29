package net.labymod.addons.labycontroller.widget;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.sdl.SDLGamepad;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;

public class ControllerStatusSetting {

        public static Component getStatusComponent() {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.hasController()) {
            return Component.text("No Controller Connected", NamedTextColor.RED);
        }

        String name = addon.getActiveControllerName().orElse("Unknown Controller");
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
                stateText = " (Charged)";
            }

            return Component.text(name + " - Battery: " + battery + "%" + stateText, color);
        } else if (state == SDLGamepad.BatteryState.NO_BATTERY) {
            return Component.text(name + " (Wired)", NamedTextColor.AQUA);
        }

        return Component.text(name, NamedTextColor.GREEN);
    }

        public static String getStatusString() {
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
}
