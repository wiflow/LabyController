package net.labymod.addons.labycontroller;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.util.MethodOrder;
import net.labymod.addons.labycontroller.radial.RadialMenuConfiguration;

@ConfigName("settings")
@SpriteTexture("settings")
public class LabyControllerConfiguration extends AddonConfig {

    @SwitchSetting
    @SpriteSlot(x = 1)
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

    // General Settings
    @SettingSection("general")
    @SwitchSetting
    private final ConfigProperty<Boolean> autoDetectController = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> showControllerHints = new ConfigProperty<>(true);

    // Movement Settings
    @SettingSection("movement")
    @SliderSetting(min = 0.1f, max = 2.0f, steps = 0.05f)
    private final ConfigProperty<Float> moveSensitivity = new ConfigProperty<>(1.0f);

    // Controls Settings
    @SettingSection("controls")
    @SwitchSetting
    private final ConfigProperty<Boolean> toggleSprint = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> toggleSneak = new ConfigProperty<>(false);

    // Controller Button Bindings - clickable subsettings
    private final ControllerBindingsSubSettings bindings = new ControllerBindingsSubSettings();

    // Radial Menu Configuration - clickable subsettings
    private final RadialMenuConfiguration radialMenu = new RadialMenuConfiguration();

    @SwitchSetting
    private final ConfigProperty<Boolean> aimSensitivityReduction = new ConfigProperty<>(true);

    @SliderSetting(min = 0.1f, max = 1.0f, steps = 0.05f)
    private final ConfigProperty<Float> aimSensitivityMultiplier = new ConfigProperty<>(0.5f);

    // Sensitivity Settings
    @SettingSection("sensitivity")
    @SliderSetting(min = 0.1f, max = 5.0f, steps = 0.1f)
    private final ConfigProperty<Float> lookSensitivity = new ConfigProperty<>(1.0f);

    @SliderSetting(min = 1.0f, max = 4.0f, steps = 0.1f)
    private final ConfigProperty<Float> lookAcceleration = new ConfigProperty<>(2.0f);

    @SwitchSetting
    private final ConfigProperty<Boolean> invertYAxis = new ConfigProperty<>(false);

    @SwitchSetting
    private final ConfigProperty<Boolean> invertXAxis = new ConfigProperty<>(false);

    // Deadzone Settings
    @SettingSection("deadzone")
    @SliderSetting(min = 0.0f, max = 0.5f, steps = 0.01f)
    private final ConfigProperty<Float> leftStickDeadzone = new ConfigProperty<>(0.15f);

    @SliderSetting(min = 0.0f, max = 0.5f, steps = 0.01f)
    private final ConfigProperty<Float> rightStickDeadzone = new ConfigProperty<>(0.15f);

    @SliderSetting(min = 0.0f, max = 0.5f, steps = 0.01f)
    private final ConfigProperty<Float> triggerDeadzone = new ConfigProperty<>(0.1f);

    // Vibration / Haptic Settings
    @SettingSection("vibration")
    @SwitchSetting
    private final ConfigProperty<Boolean> vibrationEnabled = new ConfigProperty<>(true);

    @SliderSetting(min = 0.0f, max = 2.0f, steps = 0.1f)
    private final ConfigProperty<Float> vibrationStrength = new ConfigProperty<>(1.0f);

    // Vibration Events - clickable subsettings
    private final VibrationEventsSubSettings vibrationEvents = new VibrationEventsSubSettings();

    // Light Bar Settings
    @SettingSection("lightbar")
    @SwitchSetting
    private final ConfigProperty<Boolean> lightBarEnabled = new ConfigProperty<>(true);

    // GUI Settings
    @SettingSection("gui")
    @SwitchSetting
    private final ConfigProperty<Boolean> guiNavigation = new ConfigProperty<>(true);

    @SliderSetting(min = 0.5f, max = 3.0f, steps = 0.1f)
    private final ConfigProperty<Float> virtualMouseSpeed = new ConfigProperty<>(1.0f);

    @SliderSetting(min = 50, max = 500, steps = 10)
    private final ConfigProperty<Integer> guiRepeatDelay = new ConfigProperty<>(200);

    @SwitchSetting
    private final ConfigProperty<Boolean> showOnScreenKeyboard = new ConfigProperty<>(true);

    // Advanced Settings
    @SettingSection("advanced")
    @SwitchSetting
    private final ConfigProperty<Boolean> mixedInputMode = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> reducedMotion = new ConfigProperty<>(false);

    @Override
    public ConfigProperty<Boolean> enabled() {
        return this.enabled;
    }

    // Getters for general settings
    public ConfigProperty<Boolean> autoDetectController() {
        return this.autoDetectController;
    }

    public ConfigProperty<Boolean> showControllerHints() {
        return this.showControllerHints;
    }

    // Getters for movement settings
    public ConfigProperty<Float> moveSensitivity() {
        return this.moveSensitivity;
    }

    // Getters for controls settings
    public ConfigProperty<Boolean> toggleSprint() {
        return this.toggleSprint;
    }

    public ConfigProperty<Boolean> toggleSneak() {
        return this.toggleSneak;
    }

    public ControllerBindingsSubSettings bindings() {
        return this.bindings;
    }

    public RadialMenuConfiguration radialMenu() {
        return this.radialMenu;
    }

    public ConfigProperty<Boolean> aimSensitivityReduction() {
        return this.aimSensitivityReduction;
    }

    public ConfigProperty<Float> aimSensitivityMultiplier() {
        return this.aimSensitivityMultiplier;
    }

    // Getters for sensitivity settings
    public ConfigProperty<Float> lookSensitivity() {
        return this.lookSensitivity;
    }

    public ConfigProperty<Float> lookAcceleration() {
        return this.lookAcceleration;
    }

    public ConfigProperty<Boolean> invertYAxis() {
        return this.invertYAxis;
    }

    public ConfigProperty<Boolean> invertXAxis() {
        return this.invertXAxis;
    }

    // Getters for deadzone settings
    public ConfigProperty<Float> leftStickDeadzone() {
        return this.leftStickDeadzone;
    }

    public ConfigProperty<Float> rightStickDeadzone() {
        return this.rightStickDeadzone;
    }

    public ConfigProperty<Float> triggerDeadzone() {
        return this.triggerDeadzone;
    }

    // Getters for vibration settings
    public ConfigProperty<Boolean> vibrationEnabled() {
        return this.vibrationEnabled;
    }

    public ConfigProperty<Float> vibrationStrength() {
        return this.vibrationStrength;
    }

    public VibrationEventsSubSettings vibrationEvents() {
        return this.vibrationEvents;
    }

    public ConfigProperty<Boolean> damageVibration() {
        return this.vibrationEvents.damage();
    }

    public ConfigProperty<Boolean> blockBreakVibration() {
        return this.vibrationEvents.blockBreak();
    }

    public ConfigProperty<Boolean> explosionVibration() {
        return this.vibrationEvents.explosion();
    }

    public ConfigProperty<Boolean> attackVibration() {
        return this.vibrationEvents.attack();
    }

    public ConfigProperty<Boolean> useVibration() {
        return this.vibrationEvents.use();
    }

    public ConfigProperty<Boolean> fishingVibration() {
        return this.vibrationEvents.fishing();
    }

    public ConfigProperty<Boolean> bowVibration() {
        return this.vibrationEvents.bow();
    }

    // Getters for light bar settings
    public ConfigProperty<Boolean> lightBarEnabled() {
        return this.lightBarEnabled;
    }

    // Getters for GUI settings
    public ConfigProperty<Boolean> guiNavigation() {
        return this.guiNavigation;
    }

    public ConfigProperty<Float> virtualMouseSpeed() {
        return this.virtualMouseSpeed;
    }

    public ConfigProperty<Integer> guiRepeatDelay() {
        return this.guiRepeatDelay;
    }

    public ConfigProperty<Boolean> showOnScreenKeyboard() {
        return this.showOnScreenKeyboard;
    }

    // Getters for advanced settings
    public ConfigProperty<Boolean> mixedInputMode() {
        return this.mixedInputMode;
    }

    public ConfigProperty<Boolean> reducedMotion() {
        return this.reducedMotion;
    }
}
