package net.labymod.addons.labycontroller.radial;

import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@ConfigName("radialMenu")
public class RadialMenuConfiguration extends Config {

    @DropdownSetting
    private final ConfigProperty<RadialAction> slot1 = new ConfigProperty<>(RadialAction.HOTBAR_1);

    @DropdownSetting
    private final ConfigProperty<RadialAction> slot2 = new ConfigProperty<>(RadialAction.HOTBAR_2);

    @DropdownSetting
    private final ConfigProperty<RadialAction> slot3 = new ConfigProperty<>(RadialAction.HOTBAR_3);

    @DropdownSetting
    private final ConfigProperty<RadialAction> slot4 = new ConfigProperty<>(RadialAction.HOTBAR_4);

    @DropdownSetting
    private final ConfigProperty<RadialAction> slot5 = new ConfigProperty<>(RadialAction.HOTBAR_5);

    @DropdownSetting
    private final ConfigProperty<RadialAction> slot6 = new ConfigProperty<>(RadialAction.HOTBAR_6);

    @DropdownSetting
    private final ConfigProperty<RadialAction> slot7 = new ConfigProperty<>(RadialAction.HOTBAR_7);

    @DropdownSetting
    private final ConfigProperty<RadialAction> slot8 = new ConfigProperty<>(RadialAction.HOTBAR_8);

    public RadialAction getSlot(int index) {
        return switch (index) {
            case 0 -> slot1.get();
            case 1 -> slot2.get();
            case 2 -> slot3.get();
            case 3 -> slot4.get();
            case 4 -> slot5.get();
            case 5 -> slot6.get();
            case 6 -> slot7.get();
            case 7 -> slot8.get();
            default -> RadialAction.NONE;
        };
    }

    public RadialAction[] getAllSlots() {
        return new RadialAction[] {
            slot1.get(),
            slot2.get(),
            slot3.get(),
            slot4.get(),
            slot5.get(),
            slot6.get(),
            slot7.get(),
            slot8.get()
        };
    }

    public ConfigProperty<RadialAction> slot1() {
        return slot1;
    }

    public ConfigProperty<RadialAction> slot2() {
        return slot2;
    }

    public ConfigProperty<RadialAction> slot3() {
        return slot3;
    }

    public ConfigProperty<RadialAction> slot4() {
        return slot4;
    }

    public ConfigProperty<RadialAction> slot5() {
        return slot5;
    }

    public ConfigProperty<RadialAction> slot6() {
        return slot6;
    }

    public ConfigProperty<RadialAction> slot7() {
        return slot7;
    }

    public ConfigProperty<RadialAction> slot8() {
        return slot8;
    }
}
