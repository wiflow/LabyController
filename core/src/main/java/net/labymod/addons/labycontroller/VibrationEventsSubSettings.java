package net.labymod.addons.labycontroller;

import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@ConfigName("vibrationEvents")
public class VibrationEventsSubSettings extends Config {

    @SwitchSetting
    private final ConfigProperty<Boolean> damage = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> blockBreak = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> explosion = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> attack = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> use = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> fishing = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> bow = new ConfigProperty<>(true);

    // Getters
    public ConfigProperty<Boolean> damage() { return damage; }
    public ConfigProperty<Boolean> blockBreak() { return blockBreak; }
    public ConfigProperty<Boolean> explosion() { return explosion; }
    public ConfigProperty<Boolean> attack() { return attack; }
    public ConfigProperty<Boolean> use() { return use; }
    public ConfigProperty<Boolean> fishing() { return fishing; }
    public ConfigProperty<Boolean> bow() { return bow; }
}
