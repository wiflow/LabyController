package net.labymod.addons.labycontroller.radial;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.LabyControllerConfiguration;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.controller.ControllerState;

public class RadialMenuHandler {

    private final LabyControllerAddon addon;
    private boolean wasRadialPressed = false;
    private Object activeMenu = null;  // Reference to version-specific screen
    private RadialMenuOpener menuOpener;
    private boolean loggedOpenerSet = false;

    public RadialMenuHandler(LabyControllerAddon addon) {
        this.addon = addon;
    }

    public void setMenuOpener(RadialMenuOpener opener) {
        this.menuOpener = opener;
        if (!loggedOpenerSet) {
            addon.logger().info("[RadialMenu] Menu opener set: " + (opener != null ? opener.getClass().getName() : "null"));
            loggedOpenerSet = true;
        }
    }

    public void update(ControllerState state) {
        if (state == null || menuOpener == null) return;

        boolean isRadialPressed = addon.getBindingManager().isActive(GameAction.RADIAL_MENU, state);

        // Check if radial button was just pressed
        if (isRadialPressed && !wasRadialPressed) {
            addon.logger().info("[RadialMenu] DPAD_RIGHT pressed, attempting to open radial menu");
            openRadialMenu();
        }

        // Check if radial button was just released while menu is open
        if (!isRadialPressed && wasRadialPressed && activeMenu != null) {
            addon.logger().info("[RadialMenu] DPAD_RIGHT released, executing selection");
            menuOpener.executeAndClose(activeMenu);
            activeMenu = null;
        }

        wasRadialPressed = isRadialPressed;
    }

    private void openRadialMenu() {
        if (activeMenu != null) {
            addon.logger().info("[RadialMenu] Cannot open: menu already active");
            return;
        }
        if (menuOpener == null) {
            addon.logger().info("[RadialMenu] Cannot open: menuOpener is null");
            return;
        }

        // Check if a screen is already open
        if (menuOpener.isScreenOpen()) {
            addon.logger().info("[RadialMenu] Cannot open: another screen is already open");
            return;
        }

        // Get configured actions from settings
        RadialAction[] actions = null;
        LabyControllerConfiguration config = addon.configuration();
        if (config != null && config.radialMenu() != null) {
            actions = config.radialMenu().getAllSlots();
        }

        // Create and display the radial menu
        addon.logger().info("[RadialMenu] Opening radial menu with " + (actions != null ? actions.length : 0) + " actions");
        activeMenu = menuOpener.openRadialMenu(actions);
        addon.logger().info("[RadialMenu] Menu opened: " + (activeMenu != null));
    }

    public boolean isRadialMenuOpen() {
        return activeMenu != null;
    }

    public void closeRadialMenu() {
        if (activeMenu != null && menuOpener != null) {
            menuOpener.closeMenu(activeMenu);
            activeMenu = null;
        }
    }

        public interface RadialMenuOpener {
                Object openRadialMenu(RadialAction[] actions);

                void executeAndClose(Object menu);

                void closeMenu(Object menu);

                boolean isScreenOpen();
    }
}
