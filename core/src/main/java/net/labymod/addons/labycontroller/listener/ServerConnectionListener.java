package net.labymod.addons.labycontroller.listener;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;

public class ServerConnectionListener {

    private final LabyControllerAddon addon;

    public ServerConnectionListener(LabyControllerAddon addon) {
        this.addon = addon;
    }

    @Subscribe
    public void onServerJoin(ServerJoinEvent event) {
        String serverList = addon.configuration().keyboardLikeServers().get();
        if (serverList == null || serverList.trim().isEmpty()) {
            addon.setForceKeyboardLike(false);
            return;
        }

        String serverAddress = event.serverData().address().getHost().toLowerCase();
        String[] servers = serverList.split(",");

        for (String server : servers) {
            String trimmed = server.trim().toLowerCase();
            if (!trimmed.isEmpty() && serverAddress.contains(trimmed)) {
                addon.logger().info("[Controlify] Detected anti-cheat server: " + serverAddress + " (matched: " + trimmed + ")");
                addon.logger().info("[Controlify] Enabling keyboard-like movement for this session");
                addon.setForceKeyboardLike(true);
                return;
            }
        }

        addon.setForceKeyboardLike(false);
    }

    @Subscribe
    public void onServerDisconnect(ServerDisconnectEvent event) {
        addon.setForceKeyboardLike(false);
    }
}
