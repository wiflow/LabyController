package net.labymod.addons.labycontroller;

import net.labymod.addons.labycontroller.controller.GamepadButton;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;

@ConfigName("bindings")
public class ControllerBindingsSubSettings extends Config {

    // Core gameplay bindings
    @DropdownSetting
    private final ConfigProperty<GamepadButton> jump = new ConfigProperty<>(GamepadButton.A);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> sneak = new ConfigProperty<>(GamepadButton.RIGHT_STICK);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> sprint = new ConfigProperty<>(GamepadButton.LEFT_STICK);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> inventory = new ConfigProperty<>(GamepadButton.Y);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> swapHands = new ConfigProperty<>(GamepadButton.X);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> dropItem = new ConfigProperty<>(GamepadButton.DPAD_DOWN);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> pickBlock = new ConfigProperty<>(GamepadButton.DPAD_LEFT);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> chat = new ConfigProperty<>(GamepadButton.DPAD_UP);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> pause = new ConfigProperty<>(GamepadButton.START);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> perspective = new ConfigProperty<>(GamepadButton.BACK);

    // GUI bindings
    @DropdownSetting
    private final ConfigProperty<GamepadButton> guiSelect = new ConfigProperty<>(GamepadButton.A);

    @DropdownSetting
    private final ConfigProperty<GamepadButton> guiBack = new ConfigProperty<>(GamepadButton.B);

    // Radial menu
    @DropdownSetting
    private final ConfigProperty<GamepadButton> radialMenu = new ConfigProperty<>(GamepadButton.DPAD_RIGHT);

    // Reset button
    @ButtonSetting
    public void resetBindings(Setting setting) {
        resetToDefaults();
    }

    // Getters
    public ConfigProperty<GamepadButton> jump() { return jump; }
    public ConfigProperty<GamepadButton> sneak() { return sneak; }
    public ConfigProperty<GamepadButton> sprint() { return sprint; }
    public ConfigProperty<GamepadButton> inventory() { return inventory; }
    public ConfigProperty<GamepadButton> swapHands() { return swapHands; }
    public ConfigProperty<GamepadButton> dropItem() { return dropItem; }
    public ConfigProperty<GamepadButton> pickBlock() { return pickBlock; }
    public ConfigProperty<GamepadButton> chat() { return chat; }
    public ConfigProperty<GamepadButton> pause() { return pause; }
    public ConfigProperty<GamepadButton> perspective() { return perspective; }
    public ConfigProperty<GamepadButton> guiSelect() { return guiSelect; }
    public ConfigProperty<GamepadButton> guiBack() { return guiBack; }
    public ConfigProperty<GamepadButton> radialMenu() { return radialMenu; }

        public void resetToDefaults() {
        jump.set(GamepadButton.A);
        sneak.set(GamepadButton.RIGHT_STICK);
        sprint.set(GamepadButton.LEFT_STICK);
        inventory.set(GamepadButton.Y);
        swapHands.set(GamepadButton.X);
        dropItem.set(GamepadButton.DPAD_DOWN);
        pickBlock.set(GamepadButton.DPAD_LEFT);
        chat.set(GamepadButton.DPAD_UP);
        pause.set(GamepadButton.START);
        perspective.set(GamepadButton.BACK);
        guiSelect.set(GamepadButton.A);
        guiBack.set(GamepadButton.B);
        radialMenu.set(GamepadButton.DPAD_RIGHT);
    }
}
