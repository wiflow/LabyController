package net.labymod.addons.labycontroller.v1_21_8.radial;

import net.labymod.addons.labycontroller.radial.RadialAction;
import net.labymod.addons.labycontroller.radial.RadialMenuHandler;
import net.minecraft.client.Minecraft;

public class RadialMenuOpenerImpl implements RadialMenuHandler.RadialMenuOpener {

    @Override
    public Object openRadialMenu(RadialAction[] actions) {
        Minecraft mc = Minecraft.getInstance();
        RadialMenuScreen screen = new RadialMenuScreen(actions);
        mc.setScreen(screen);
        return screen;
    }

    @Override
    public void executeAndClose(Object menu) {
        if (menu instanceof RadialMenuScreen radialMenu) {
            radialMenu.executeSelection();
        }
    }

    @Override
    public void closeMenu(Object menu) {
        if (menu instanceof RadialMenuScreen radialMenu) {
            radialMenu.onClose();
        }
    }

    @Override
    public boolean isScreenOpen() {
        return Minecraft.getInstance().screen != null;
    }
}
