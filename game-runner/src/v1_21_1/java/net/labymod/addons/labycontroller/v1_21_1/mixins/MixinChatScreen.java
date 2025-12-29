package net.labymod.addons.labycontroller.v1_21_1.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.ControllerType;
import net.labymod.addons.labycontroller.controller.GamepadButton;
import net.labymod.addons.labycontroller.util.HoldRepeatHelper;
import net.labymod.addons.labycontroller.v1_21_1.keyboard.ChatKeyboardInfo;
import net.labymod.addons.labycontroller.v1_21_1.keyboard.KeyData;
import net.labymod.addons.labycontroller.v1_21_1.keyboard.SuggestionsController;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen implements ChatKeyboardInfo {

    @Shadow
    protected EditBox input;

    @Shadow
    CommandSuggestions commandSuggestions;

    @Unique
    private boolean labycontroller$keyboardEnabled = false;
    @Unique
    private int labycontroller$keyboardHeight = 0;
    @Unique
    private int labycontroller$keyboardY = 0;
    @Unique
    private ControllerType labycontroller$controllerType = ControllerType.GENERIC;

    @Unique
    private static final String[][] KEYBOARD_LAYOUT = {
        {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-"},
        {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "DEL"},
        {"a", "s", "d", "f", "g", "h", "j", "k", "l", "/", "ENTER"},
        {"SHIFT", "z", "x", "c", "v", "b", "n", "m", ",", ".", "SPACE"},
    };

    @Unique
    private final List<List<KeyData>> labycontroller$keyData = new ArrayList<>();
    @Unique
    private int labycontroller$focusedRow = 1;
    @Unique
    private int labycontroller$focusedCol = 1;
    @Unique
    private boolean labycontroller$shifted = false;

    @Unique
    private final HoldRepeatHelper labycontroller$navRightHelper = new HoldRepeatHelper(30, 10);
    @Unique
    private final HoldRepeatHelper labycontroller$navLeftHelper = new HoldRepeatHelper(30, 10);
    @Unique
    private final HoldRepeatHelper labycontroller$navUpHelper = new HoldRepeatHelper(30, 10);
    @Unique
    private final HoldRepeatHelper labycontroller$navDownHelper = new HoldRepeatHelper(30, 10);
    @Unique
    private final HoldRepeatHelper labycontroller$suggestionUpHelper = new HoldRepeatHelper(25, 10);
    @Unique
    private final HoldRepeatHelper labycontroller$suggestionDownHelper = new HoldRepeatHelper(25, 10);

    @Unique
    private int labycontroller$keyPressCooldown = 0;
    @Unique
    private static final int KEY_PRESS_COOLDOWN_TICKS = 5;

    @Unique
    private boolean labycontroller$lastAPressed = false;
    @Unique
    private boolean labycontroller$lastBPressed = false;
    @Unique
    private boolean labycontroller$lastXPressed = false;
    @Unique
    private boolean labycontroller$lastYPressed = false;
    @Unique
    private boolean labycontroller$lastLBPressed = false;
    @Unique
    private boolean labycontroller$lastRBPressed = false;
    @Unique
    private boolean labycontroller$lastStartPressed = false;
    @Unique
    private boolean labycontroller$lastDpadUpPressed = false;
    @Unique
    private boolean labycontroller$lastDpadDownPressed = false;
    @Unique
    private boolean labycontroller$lastDpadLeftPressed = false;
    @Unique
    private boolean labycontroller$lastDpadRightPressed = false;
    @Unique
    private boolean labycontroller$lastLeftStickUp = false;
    @Unique
    private boolean labycontroller$lastLeftStickDown = false;
    @Unique
    private boolean labycontroller$lastLeftStickLeft = false;
    @Unique
    private boolean labycontroller$lastLeftStickRight = false;

    protected MixinChatScreen(Component title) {
        super(title);
    }

    @Override
    public float labycontroller$getKeyboardShift() {
        return 0f;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void labycontroller$onInit(CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) {
            labycontroller$keyboardEnabled = false;
            return;
        }

        if (addon.configuration() != null && addon.configuration().showOnScreenKeyboard().get()) {
            labycontroller$keyboardEnabled = true;
            addon.setOnScreenKeyboardActive(true);

            if (addon.getSDLControllerManager() != null) {
                addon.getSDLControllerManager().getActiveGamepad().ifPresent(gamepad -> {
                    labycontroller$controllerType = gamepad.getType();
                });
            }

            int chatAreaHeight = 120;
            labycontroller$keyboardHeight = (int) (this.height * 0.35f);
            labycontroller$keyboardY = this.height - chatAreaHeight - labycontroller$keyboardHeight;

            labycontroller$buildKeyData();

            if (minecraft != null && minecraft.mouseHandler != null) {
                GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR,
                    GLFW.GLFW_CURSOR_DISABLED);
            }
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void labycontroller$onRemoved(CallbackInfo ci) {
        if (labycontroller$keyboardEnabled) {
            LabyControllerAddon addon = LabyControllerAddon.getInstance();
            if (addon != null) {
                addon.setOnScreenKeyboardActive(false);
            }
            if (minecraft != null) {
                GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR,
                    GLFW.GLFW_CURSOR_NORMAL);
            }
        }
    }

    @Unique
    private void labycontroller$buildKeyData() {
        labycontroller$keyData.clear();
        int keyHeight = (labycontroller$keyboardHeight - 20) / KEYBOARD_LAYOUT.length;
        int padding = 2;
        int startY = labycontroller$keyboardY + 10;

        for (int row = 0; row < KEYBOARD_LAYOUT.length; row++) {
            List<KeyData> rowData = new ArrayList<>();
            String[] keys = KEYBOARD_LAYOUT[row];
            int numKeys = keys.length;
            int totalPadding = (numKeys + 1) * padding;
            int availableWidth = this.width - totalPadding - 8;
            int baseKeyWidth = availableWidth / numKeys;
            int x = padding + 4;
            int y = startY + row * (keyHeight + padding);

            for (String key : keys) {
                int keyWidth = baseKeyWidth;
                if (key.equals("SHIFT") || key.equals("SPACE") || key.equals("DEL") || key.equals("ENTER")) {
                    keyWidth = (int) (baseKeyWidth * 1.2);
                }
                rowData.add(new KeyData(key, x, y, keyWidth, keyHeight));
                x += keyWidth + padding;
            }
            labycontroller$keyData.add(rowData);
        }
    }

    @Unique
    private void labycontroller$handleControllerInput() {
        if (!labycontroller$keyboardEnabled) return;
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) return;
        ControllerState state = addon.getActiveControllerState().orElse(null);
        if (state == null) return;
        labycontroller$handleNavigation(state);
        labycontroller$handleButtonInputs(state);
    }

    @Unique
    private void labycontroller$handleNavigation(ControllerState state) {
        boolean dpadUp = state.isButtonPressed(GamepadButton.DPAD_UP);
        boolean dpadDown = state.isButtonPressed(GamepadButton.DPAD_DOWN);
        boolean dpadLeft = state.isButtonPressed(GamepadButton.DPAD_LEFT);
        boolean dpadRight = state.isButtonPressed(GamepadButton.DPAD_RIGHT);
        float stickX = state.getLeftStickX();
        float stickY = state.getLeftStickY();
        boolean stickUp = stickY < -0.5f;
        boolean stickDown = stickY > 0.5f;
        boolean stickLeft = stickX < -0.5f;
        boolean stickRight = stickX > 0.5f;
        boolean navUp = dpadUp || stickUp;
        boolean navDown = dpadDown || stickDown;
        boolean navLeft = dpadLeft || stickLeft;
        boolean navRight = dpadRight || stickRight;
        boolean wasUp = labycontroller$lastDpadUpPressed || labycontroller$lastLeftStickUp;
        boolean wasDown = labycontroller$lastDpadDownPressed || labycontroller$lastLeftStickDown;
        boolean wasLeft = labycontroller$lastDpadLeftPressed || labycontroller$lastLeftStickLeft;
        boolean wasRight = labycontroller$lastDpadRightPressed || labycontroller$lastLeftStickRight;
        boolean moved = false;

        if (labycontroller$navRightHelper.shouldAction(navRight, wasRight)) {
            labycontroller$focusedCol++;
            labycontroller$navRightHelper.onNavigate();
            moved = true;
        }
        if (labycontroller$navLeftHelper.shouldAction(navLeft, wasLeft)) {
            labycontroller$focusedCol--;
            labycontroller$navLeftHelper.onNavigate();
            moved = true;
        }
        if (labycontroller$navDownHelper.shouldAction(navDown, wasDown)) {
            labycontroller$focusedRow++;
            labycontroller$navDownHelper.onNavigate();
            moved = true;
        }
        if (labycontroller$navUpHelper.shouldAction(navUp, wasUp)) {
            labycontroller$focusedRow--;
            labycontroller$navUpHelper.onNavigate();
            moved = true;
        }

        labycontroller$lastDpadUpPressed = dpadUp;
        labycontroller$lastDpadDownPressed = dpadDown;
        labycontroller$lastDpadLeftPressed = dpadLeft;
        labycontroller$lastDpadRightPressed = dpadRight;
        labycontroller$lastLeftStickUp = stickUp;
        labycontroller$lastLeftStickDown = stickDown;
        labycontroller$lastLeftStickLeft = stickLeft;
        labycontroller$lastLeftStickRight = stickRight;

        if (moved) {
            labycontroller$focusedRow = Math.max(0, Math.min(labycontroller$focusedRow, labycontroller$keyData.size() - 1));
            if (!labycontroller$keyData.isEmpty()) {
                labycontroller$focusedCol = Math.max(0, Math.min(labycontroller$focusedCol, labycontroller$keyData.get(labycontroller$focusedRow).size() - 1));
            }
        }
    }

    @Unique
    private void labycontroller$handleButtonInputs(ControllerState state) {
        if (labycontroller$keyPressCooldown > 0) labycontroller$keyPressCooldown--;

        boolean aPressed = state.isButtonPressed(GamepadButton.A);
        if (aPressed && !labycontroller$lastAPressed && labycontroller$keyPressCooldown == 0) {
            labycontroller$pressSelectedKey();
            labycontroller$keyPressCooldown = KEY_PRESS_COOLDOWN_TICKS;
        }
        labycontroller$lastAPressed = aPressed;

        boolean xPressed = state.isButtonPressed(GamepadButton.X);
        if (xPressed && !labycontroller$lastXPressed) labycontroller$doBackspace();
        labycontroller$lastXPressed = xPressed;

        boolean yPressed = state.isButtonPressed(GamepadButton.Y);
        if (yPressed && !labycontroller$lastYPressed) labycontroller$insertChar(" ");
        labycontroller$lastYPressed = yPressed;

        boolean lbPressed = state.isButtonPressed(GamepadButton.LEFT_BUMPER);
        boolean rbPressed = state.isButtonPressed(GamepadButton.RIGHT_BUMPER);
        SuggestionsController suggestions = labycontroller$getSuggestionsController();
        if (suggestions != null && suggestions.labycontroller$hasSuggestions()) {
            if (labycontroller$suggestionUpHelper.shouldAction(lbPressed, labycontroller$lastLBPressed)) {
                suggestions.labycontroller$cycle(-1);
                labycontroller$suggestionUpHelper.onNavigate();
            }
            if (labycontroller$suggestionDownHelper.shouldAction(rbPressed, labycontroller$lastRBPressed)) {
                suggestions.labycontroller$cycle(1);
                labycontroller$suggestionDownHelper.onNavigate();
            }
        }
        labycontroller$lastLBPressed = lbPressed;
        labycontroller$lastRBPressed = rbPressed;

        labycontroller$shifted = state.getLeftTrigger() > 0.5f;

        boolean startPressed = state.isButtonPressed(GamepadButton.START);
        if (startPressed && !labycontroller$lastStartPressed) labycontroller$doEnter();
        labycontroller$lastStartPressed = startPressed;

        boolean bPressed = state.isButtonPressed(GamepadButton.B);
        if (bPressed && !labycontroller$lastBPressed) this.onClose();
        labycontroller$lastBPressed = bPressed;
    }

    @Unique
    private SuggestionsController labycontroller$getSuggestionsController() {
        if (commandSuggestions instanceof SuggestionsController controller) return controller;
        return null;
    }

    @Unique
    private void labycontroller$pressSelectedKey() {
        if (labycontroller$focusedRow >= 0 && labycontroller$focusedRow < labycontroller$keyData.size()) {
            List<KeyData> row = labycontroller$keyData.get(labycontroller$focusedRow);
            if (labycontroller$focusedCol >= 0 && labycontroller$focusedCol < row.size()) {
                labycontroller$onKeyPressed(row.get(labycontroller$focusedCol).key());
            }
        }
    }

    @Unique
    private void labycontroller$onKeyPressed(String key) {
        switch (key) {
            case "DEL" -> labycontroller$doBackspace();
            case "SHIFT" -> labycontroller$shifted = !labycontroller$shifted;
            case "ENTER" -> {
                SuggestionsController suggestions = labycontroller$getSuggestionsController();
                if (suggestions != null && suggestions.labycontroller$hasSuggestions()) {
                    suggestions.labycontroller$useSuggestion();
                } else {
                    labycontroller$doEnter();
                }
            }
            case "SPACE" -> labycontroller$insertChar(" ");
            default -> {
                if (key.length() == 1) {
                    labycontroller$insertChar(labycontroller$shifted ? key.toUpperCase() : key.toLowerCase());
                    labycontroller$shifted = false;
                }
            }
        }
    }

    @Unique
    private void labycontroller$insertChar(String str) {
        if (input == null) return;
        input.insertText(str);
        if (commandSuggestions != null) commandSuggestions.updateCommandInfo();
    }

    @Unique
    private void labycontroller$doBackspace() {
        if (input == null) return;
        input.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0);
        if (commandSuggestions != null) commandSuggestions.updateCommandInfo();
    }

    @Unique
    private void labycontroller$doEnter() {
        if (input == null) return;
        String message = input.getValue().trim();
        if (!message.isEmpty() && minecraft != null && minecraft.player != null) {
            if (message.startsWith("/")) {
                minecraft.player.connection.sendCommand(message.substring(1));
            } else {
                minecraft.player.connection.sendChat(message);
            }
        }
        this.onClose();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void labycontroller$onRenderHead(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        labycontroller$handleControllerInput();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void labycontroller$onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!labycontroller$keyboardEnabled) return;

        int keyboardBottom = labycontroller$keyboardY + labycontroller$keyboardHeight;
        graphics.fill(0, labycontroller$keyboardY, this.width, keyboardBottom, 0xF0101010);
        graphics.fill(0, labycontroller$keyboardY, this.width, labycontroller$keyboardY + 1, 0xFF333333);
        graphics.fill(0, keyboardBottom - 1, this.width, keyboardBottom, 0xFF333333);

        for (int row = 0; row < labycontroller$keyData.size(); row++) {
            List<KeyData> rowKeys = labycontroller$keyData.get(row);
            for (int col = 0; col < rowKeys.size(); col++) {
                labycontroller$renderKey(graphics, rowKeys.get(col), row == labycontroller$focusedRow && col == labycontroller$focusedCol);
            }
        }
        labycontroller$renderHints(graphics, keyboardBottom);
    }

    @Unique
    private void labycontroller$renderKey(GuiGraphics graphics, KeyData key, boolean selected) {
        int x = key.x(), y = key.y(), w = key.width(), h = key.height();
        graphics.fill(x, y, x + w, y + h, selected ? 0xFF1E5BA8 : 0xFF2A2A2A);
        if (selected) {
            graphics.fill(x - 1, y, x, y + h, 0xFF4488FF);
            graphics.fill(x + w, y, x + w + 1, y + h, 0xFF4488FF);
            graphics.fill(x, y - 1, x + w, y, 0xFF4488FF);
            graphics.fill(x, y + h, x + w, y + h + 1, 0xFF4488FF);
        }
        String displayText = key.key();
        if (displayText.length() == 1 && Character.isLetter(displayText.charAt(0))) {
            displayText = labycontroller$shifted ? displayText.toUpperCase() : displayText.toLowerCase();
        }
        String label = switch (displayText) {
            case "SHIFT" -> labycontroller$shifted ? "SHIFT*" : "SHIFT";
            case "SPACE" -> "___";
            case "DEL" -> "\u2190";
            case "ENTER" -> "\u21B5";
            default -> displayText;
        };
        graphics.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, selected ? 0xFFFFFFFF : 0xFFCCCCCC);
    }

    @Unique
    private void labycontroller$renderHints(GuiGraphics graphics, int keyboardBottom) {
        int y = keyboardBottom + 2, x = 8, spacing = 70;
        String btnA = labycontroller$controllerType == ControllerType.PLAYSTATION ? "\u00D7" : "A";
        String btnX = labycontroller$controllerType == ControllerType.PLAYSTATION ? "\u25A1" : "X";
        String btnY = labycontroller$controllerType == ControllerType.PLAYSTATION ? "\u25B3" : "Y";
        String btnB = labycontroller$controllerType == ControllerType.PLAYSTATION ? "\u25CB" : "B";
        graphics.drawString(font, btnA + " Select", x, y, 0xFF888888);
        x += spacing;
        graphics.drawString(font, btnX + " Delete", x, y, 0xFF888888);
        x += spacing;
        graphics.drawString(font, btnY + " Space", x, y, 0xFF888888);
        x += spacing;
        graphics.drawString(font, "LB/RB Suggest", x, y, 0xFF888888);
        x += spacing + 10;
        graphics.drawString(font, btnB + " Close", x, y, 0xFF888888);
    }
}
