package net.labymod.addons.labycontroller.v1_21_3.radial;

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
    private static final float RADIUS = 80.0f;
    private static final float BUTTON_SIZE = 40.0f;
    private static final float DEADZONE = 0.3f;

    private final RadialAction[] actions;
    private int selectedSlot = -1;
    private float animationProgress = 0.0f;
    private ControllerType controllerType = ControllerType.GENERIC;

    private final float[] slotX = new float[SLOT_COUNT];
    private final float[] slotY = new float[SLOT_COUNT];

    public RadialMenuScreen() {
        super(Component.literal("Radial Menu"));
        this.actions = getDefaultActions();
        calculateSlotPositions();
    }

    public RadialMenuScreen(RadialAction[] actions) {
        super(Component.literal("Radial Menu"));
        this.actions = actions != null ? actions : getDefaultActions();
        calculateSlotPositions();
    }

    private RadialAction[] getDefaultActions() {
        return new RadialAction[] {
            RadialAction.HOTBAR_1, RadialAction.HOTBAR_2, RadialAction.HOTBAR_3, RadialAction.HOTBAR_4,
            RadialAction.HOTBAR_5, RadialAction.HOTBAR_6, RadialAction.HOTBAR_7, RadialAction.HOTBAR_8
        };
    }

    private void calculateSlotPositions() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            double angle = Math.toRadians(270 + (i * 360.0 / SLOT_COUNT));
            slotX[i] = (float) Math.cos(angle) * RADIUS;
            slotY[i] = (float) Math.sin(angle) * RADIUS;
        }
    }

    @Override
    protected void init() {
        super.init();
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon != null && addon.getSDLControllerManager() != null) {
            addon.getSDLControllerManager().getActiveGamepad().ifPresent(gamepad -> controllerType = gamepad.getType());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (animationProgress < 1.0f) animationProgress = Math.min(1.0f, animationProgress + 0.15f);
        updateSelection();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x66000000);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        for (int i = 0; i < SLOT_COUNT; i++) {
            float scale = animationProgress;
            float x = centerX + slotX[i] * scale;
            float y = centerY + slotY[i] * scale;
            drawSlot(graphics, (int) x, (int) y, actions[i], i == selectedSlot);
        }

        int indicatorSize = (int) (8 * animationProgress);
        graphics.fill(centerX - indicatorSize, centerY - indicatorSize, centerX + indicatorSize, centerY + indicatorSize, 0xFFFFFFFF);

        if (selectedSlot >= 0 && selectedSlot < actions.length) {
            graphics.drawCenteredString(font, actions[selectedSlot].getDisplayName(), centerX, centerY + (int) RADIUS + 30, 0xFFFFFFFF);
        }
        graphics.drawCenteredString(font, "Use right stick to select, release to confirm", centerX, 20, 0xFFAAAAAA);
    }

    private void drawSlot(GuiGraphics graphics, int cx, int cy, RadialAction action, boolean selected) {
        int halfSize = (int) (BUTTON_SIZE / 2);
        int x = cx - halfSize, y = cy - halfSize, size = (int) BUTTON_SIZE;

        graphics.fill(x, y, x + size, y + size, selected ? 0xCC2266CC : 0xAA333333);
        int borderColor = selected ? 0xFFFFFFFF : 0xFF666666;
        graphics.fill(x - 1, y - 1, x + size + 1, y, borderColor);
        graphics.fill(x - 1, y + size, x + size + 1, y + size + 1, borderColor);
        graphics.fill(x - 1, y, x, y + size, borderColor);
        graphics.fill(x + size, y, x + size + 1, y + size, borderColor);

        if (selected) {
            graphics.fill(x - 2, y, x - 1, y + size, 0xFF4488FF);
            graphics.fill(x + size + 1, y, x + size + 2, y + size, 0xFF4488FF);
            graphics.fill(x, y - 2, x + size, y - 1, 0xFF4488FF);
            graphics.fill(x, y + size + 1, x + size, y + size + 2, 0xFF4488FF);
        }
        graphics.drawCenteredString(font, getActionLabel(action), cx, cy - 4, 0xFFFFFFFF);
    }

    private String getActionLabel(RadialAction action) {
        if (action == null) return "-";
        return switch (action) {
            case HOTBAR_1 -> "1"; case HOTBAR_2 -> "2"; case HOTBAR_3 -> "3"; case HOTBAR_4 -> "4";
            case HOTBAR_5 -> "5"; case HOTBAR_6 -> "6"; case HOTBAR_7 -> "7"; case HOTBAR_8 -> "8"; case HOTBAR_9 -> "9";
            case TOGGLE_PERSPECTIVE -> "CAM"; case DROP_ITEM -> "DROP"; case DROP_STACK -> "STACK";
            case OPEN_CHAT -> "CHAT"; case OPEN_COMMAND -> "CMD"; case TOGGLE_HUD -> "HUD"; case NONE -> "-";
        };
    }

    private void updateSelection() {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null) return;
        Optional<ControllerState> stateOpt = addon.getActiveControllerState();
        if (stateOpt.isEmpty()) return;

        ControllerState state = stateOpt.get();
        float stickX = state.getRightStickX(), stickY = state.getRightStickY();
        float magnitude = (float) Math.sqrt(stickX * stickX + stickY * stickY);
        if (magnitude < DEADZONE) return;

        double degrees = Math.toDegrees(Math.atan2(stickY, stickX));
        if (degrees < 0) degrees += 360;
        degrees = (degrees + 90) % 360;
        selectedSlot = Math.max(0, Math.min(SLOT_COUNT - 1, (int) (degrees / (360.0 / SLOT_COUNT))));
    }

    public void executeSelection() {
        if (selectedSlot >= 0 && selectedSlot < actions.length) executeAction(actions[selectedSlot]);
        onClose();
    }

    private void executeAction(RadialAction action) {
        if (action == null || action == RadialAction.NONE) return;
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon != null) addon.vibratePulse();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        switch (action) {
            case HOTBAR_1 -> mc.player.getInventory().selected = 0;
            case HOTBAR_2 -> mc.player.getInventory().selected = 1;
            case HOTBAR_3 -> mc.player.getInventory().selected = 2;
            case HOTBAR_4 -> mc.player.getInventory().selected = 3;
            case HOTBAR_5 -> mc.player.getInventory().selected = 4;
            case HOTBAR_6 -> mc.player.getInventory().selected = 5;
            case HOTBAR_7 -> mc.player.getInventory().selected = 6;
            case HOTBAR_8 -> mc.player.getInventory().selected = 7;
            case HOTBAR_9 -> mc.player.getInventory().selected = 8;
            case TOGGLE_PERSPECTIVE -> mc.options.setCameraType(mc.options.getCameraType().cycle());
            case DROP_ITEM -> mc.player.drop(false);
            case DROP_STACK -> mc.player.drop(true);
            case OPEN_CHAT -> mc.execute(() -> mc.setScreen(new net.minecraft.client.gui.screens.ChatScreen("")));
            case OPEN_COMMAND -> mc.execute(() -> mc.setScreen(new net.minecraft.client.gui.screens.ChatScreen("/")));
            case TOGGLE_HUD -> mc.options.hideGui = !mc.options.hideGui;
            case NONE -> {}
        }
    }

    @Override public void onClose() { Minecraft.getInstance().setScreen(null); }
    @Override public boolean isPauseScreen() { return false; }
    public int getSelectedSlot() { return selectedSlot; }
}
