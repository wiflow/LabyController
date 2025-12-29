package net.labymod.addons.labycontroller.api;

import java.util.Optional;

public interface LabyControllerAPI {

        boolean hasController();

        boolean isControllerActive();

        Optional<String> getActiveControllerName();

        Optional<String> getActiveControllerType();

        boolean isActionPressed(String actionId);

        float getActionValue(String actionId);

        void vibrate(float lowFrequency, float highFrequency, int durationMs);
}
