package net.labymod.addons.labycontroller.v1_20_2.radial;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.ControllerType;
import net.labymod.addons.labycontroller.radial.RadialAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Optional;

public class RadialMenuScreen extends Screen {
    private static final int SLOT_COUNT = 8;
    private static final float RADIUS = 80.0f, BUTTON_SIZE = 40.0f, DEADZONE = 0.3f;
    private final RadialAction[] actions;
    private int selectedSlot = -1;
    private float animationProgress = 0.0f;
    private final float[] slotX = new float[SLOT_COUNT], slotY = new float[SLOT_COUNT];

    public RadialMenuScreen(RadialAction[] actions) {
        super(Component.literal("Radial Menu"));
        this.actions = actions != null ? actions : new RadialAction[]{RadialAction.HOTBAR_1, RadialAction.HOTBAR_2, RadialAction.HOTBAR_3, RadialAction.HOTBAR_4, RadialAction.HOTBAR_5, RadialAction.HOTBAR_6, RadialAction.HOTBAR_7, RadialAction.HOTBAR_8};
        for (int i = 0; i < SLOT_COUNT; i++) { double angle = Math.toRadians(270 + (i * 360.0 / SLOT_COUNT)); slotX[i] = (float) Math.cos(angle) * RADIUS; slotY[i] = (float) Math.sin(angle) * RADIUS; }
    }

    @Override public void tick() { super.tick(); if (animationProgress < 1.0f) animationProgress = Math.min(1.0f, animationProgress + 0.15f); updateSelection(); }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x66000000);
        int cx = this.width / 2, cy = this.height / 2;
        for (int i = 0; i < SLOT_COUNT; i++) drawSlot(graphics, (int)(cx + slotX[i] * animationProgress), (int)(cy + slotY[i] * animationProgress), actions[i], i == selectedSlot);
        int s = (int)(8 * animationProgress); graphics.fill(cx - s, cy - s, cx + s, cy + s, 0xFFFFFFFF);
        if (selectedSlot >= 0 && selectedSlot < actions.length) graphics.drawCenteredString(font, actions[selectedSlot].getDisplayName(), cx, cy + (int)RADIUS + 30, 0xFFFFFFFF);
        graphics.drawCenteredString(font, "Use right stick to select, release to confirm", cx, 20, 0xFFAAAAAA);
    }

    private void drawSlot(GuiGraphics g, int cx, int cy, RadialAction action, boolean sel) {
        int h = (int)(BUTTON_SIZE/2), x = cx-h, y = cy-h, sz = (int)BUTTON_SIZE;
        g.fill(x, y, x+sz, y+sz, sel ? 0xCC2266CC : 0xAA333333);
        int bc = sel ? 0xFFFFFFFF : 0xFF666666;
        g.fill(x-1, y-1, x+sz+1, y, bc); g.fill(x-1, y+sz, x+sz+1, y+sz+1, bc); g.fill(x-1, y, x, y+sz, bc); g.fill(x+sz, y, x+sz+1, y+sz, bc);
        if (sel) { g.fill(x-2, y, x-1, y+sz, 0xFF4488FF); g.fill(x+sz+1, y, x+sz+2, y+sz, 0xFF4488FF); g.fill(x, y-2, x+sz, y-1, 0xFF4488FF); g.fill(x, y+sz+1, x+sz, y+sz+2, 0xFF4488FF); }
        g.drawCenteredString(font, getLabel(action), cx, cy-4, 0xFFFFFFFF);
    }

    private String getLabel(RadialAction a) { if (a == null) return "-"; return switch(a) { case HOTBAR_1->"1"; case HOTBAR_2->"2"; case HOTBAR_3->"3"; case HOTBAR_4->"4"; case HOTBAR_5->"5"; case HOTBAR_6->"6"; case HOTBAR_7->"7"; case HOTBAR_8->"8"; case HOTBAR_9->"9"; case TOGGLE_PERSPECTIVE->"CAM"; case DROP_ITEM->"DROP"; case DROP_STACK->"STACK"; case OPEN_CHAT->"CHAT"; case OPEN_COMMAND->"CMD"; case TOGGLE_HUD->"HUD"; case NONE->"-"; }; }

    private void updateSelection() {
        LabyControllerAddon addon = LabyControllerAddon.getInstance(); if (addon == null) return;
        Optional<ControllerState> opt = addon.getActiveControllerState(); if (opt.isEmpty()) return;
        ControllerState st = opt.get(); float sx = st.getRightStickX(), sy = st.getRightStickY();
        if (Math.sqrt(sx*sx + sy*sy) < DEADZONE) return;
        double deg = Math.toDegrees(Math.atan2(sy, sx)); if (deg < 0) deg += 360; deg = (deg + 90) % 360;
        selectedSlot = Math.max(0, Math.min(SLOT_COUNT - 1, (int)(deg / (360.0 / SLOT_COUNT))));
    }

    public void executeSelection() { if (selectedSlot >= 0 && selectedSlot < actions.length) executeAction(actions[selectedSlot]); onClose(); }

    private void executeAction(RadialAction a) {
        if (a == null || a == RadialAction.NONE) return;
        LabyControllerAddon addon = LabyControllerAddon.getInstance(); if (addon != null) addon.vibratePulse();
        Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return;
        switch (a) {
            case HOTBAR_1 -> mc.player.getInventory().selected = 0; case HOTBAR_2 -> mc.player.getInventory().selected = 1;
            case HOTBAR_3 -> mc.player.getInventory().selected = 2; case HOTBAR_4 -> mc.player.getInventory().selected = 3;
            case HOTBAR_5 -> mc.player.getInventory().selected = 4; case HOTBAR_6 -> mc.player.getInventory().selected = 5;
            case HOTBAR_7 -> mc.player.getInventory().selected = 6; case HOTBAR_8 -> mc.player.getInventory().selected = 7;
            case HOTBAR_9 -> mc.player.getInventory().selected = 8;
            case TOGGLE_PERSPECTIVE -> mc.options.setCameraType(mc.options.getCameraType().cycle());
            case DROP_ITEM -> mc.player.drop(false); case DROP_STACK -> mc.player.drop(true);
            case OPEN_CHAT -> mc.execute(() -> mc.setScreen(new net.minecraft.client.gui.screens.ChatScreen("")));
            case OPEN_COMMAND -> mc.execute(() -> mc.setScreen(new net.minecraft.client.gui.screens.ChatScreen("/")));
            case TOGGLE_HUD -> mc.options.hideGui = !mc.options.hideGui; case NONE -> {}
        }
    }

    @Override public void onClose() { Minecraft.getInstance().setScreen(null); }
    @Override public boolean isPauseScreen() { return false; }
    public int getSelectedSlot() { return selectedSlot; }
}
