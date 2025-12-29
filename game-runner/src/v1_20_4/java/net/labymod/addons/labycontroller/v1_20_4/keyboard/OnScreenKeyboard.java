package net.labymod.addons.labycontroller.v1_20_4.keyboard;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.controller.ControllerType;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class OnScreenKeyboard extends Screen {

    private static final String[][] KEYBOARD_LAYOUT = {
        {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "Shift Lock"},
        {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "Del|X"},
        {"/", "a", "s", "d", "f", "g", "h", "j", "k", "l", "Enter|M"},
        {"Shift|LB", "z", "x", "c", "v", "b", "n", "m", ",", ".", "Space|Y"},
    };

    private final Screen parentScreen;
    private EditBox targetEditBox;
    private CommandSuggestions commandSuggestions;
    private ControllerType controllerType = ControllerType.GENERIC;

    private final List<List<KeyData>> keyData = new ArrayList<>();
    private int focusedRow = 1;
    private int focusedCol = 1;

    private boolean shifted = false;
    private boolean shiftLocked = false;

    private int navCooldown = 0;
    private static final int NAV_COOLDOWN_TICKS = 4;

    private int suggestionNavCooldown = 0;

    private boolean lastAPressed = false;
    private boolean lastBPressed = false;
    private boolean lastXPressed = false;
    private boolean lastYPressed = false;
    private boolean lastStartPressed = false;
    private boolean lastLBPressed = false;

    private int keyboardHeight = 0;
    private int keyboardY = 0;

    public OnScreenKeyboard(Screen parentScreen, EditBox targetEditBox) {
        super(Component.literal("On-Screen Keyboard"));
        this.parentScreen = parentScreen;
        this.targetEditBox = targetEditBox;
    }

    @Override
    protected void init() {
        super.init();

        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon != null && addon.getSDLControllerManager() != null) {
            addon.getSDLControllerManager().getActiveGamepad().ifPresent(gamepad -> {
                controllerType = gamepad.getType();
            });
        }

        int keyHeight = 36;
        int padding = 2;
        keyboardHeight = (KEYBOARD_LAYOUT.length * (keyHeight + padding)) + padding + 4;
        keyboardY = this.height - keyboardHeight;

        if (parentScreen != null) {
            parentScreen.init(minecraft, width, height);

            if (parentScreen instanceof ChatScreen chatScreen) {
                try {
                    for (Field field : ChatScreen.class.getDeclaredFields()) {
                        field.setAccessible(true);
                        if (EditBox.class.isAssignableFrom(field.getType())) {
                            targetEditBox = (EditBox) field.get(chatScreen);
                        } else if (CommandSuggestions.class.isAssignableFrom(field.getType())) {
                            commandSuggestions = (CommandSuggestions) field.get(chatScreen);
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (targetEditBox != null) {
                targetEditBox.setFocused(true);
            }
        }

        buildKeyData();
    }

    private void buildKeyData() {
        keyData.clear();

        int keyHeight = 36;
        int padding = 2;
        int startY = keyboardY + 4;

        for (int row = 0; row < KEYBOARD_LAYOUT.length; row++) {
            List<KeyData> rowData = new ArrayList<>();
            String[] keys = KEYBOARD_LAYOUT[row];

            int numKeys = keys.length;
            int totalPadding = (numKeys + 1) * padding;
            int availableWidth = this.width - totalPadding;
            int baseKeyWidth = availableWidth / numKeys;

            int x = padding;
            int y = startY + row * (keyHeight + padding);

            for (String keyStr : keys) {
                String key;
                String shortcut = null;
                if (keyStr.contains("|")) {
                    String[] parts = keyStr.split("\\|");
                    key = parts[0];
                    shortcut = parts[1];
                } else {
                    key = keyStr;
                }

                int keyWidth = baseKeyWidth;
                if (key.equals("Shift Lock") || key.equals("Shift") ||
                    key.equals("Del") || key.equals("Enter") || key.equals("Space")) {
                    keyWidth = (int)(baseKeyWidth * 1.3);
                }

                rowData.add(new KeyData(key, shortcut, x, y, keyWidth, keyHeight));
                x += keyWidth + padding;
            }
            keyData.add(rowData);
        }

        focusedRow = Math.max(0, Math.min(focusedRow, keyData.size() - 1));
        if (!keyData.isEmpty() && focusedRow < keyData.size()) {
            focusedCol = Math.max(0, Math.min(focusedCol, keyData.get(focusedRow).size() - 1));
        }
    }

    private void onKeyPressed(String key) {
        if (targetEditBox == null) return;

        switch (key) {
            case "Del" -> doBackspace();
            case "Shift" -> {
                shifted = !shifted;
                if (!shifted) shiftLocked = false;
            }
            case "Shift Lock" -> {
                shiftLocked = !shiftLocked;
                shifted = shiftLocked;
            }
            case "Enter" -> doEnter();
            case "Space" -> insertChar(" ");
            default -> {
                if (key.length() == 1) {
                    String c = (shifted || shiftLocked) ? key.toUpperCase() : key.toLowerCase();
                    insertChar(c);
                    if (shifted && !shiftLocked) shifted = false;
                }
            }
        }
    }

    private void insertChar(String str) {
        if (targetEditBox == null) return;

        targetEditBox.insertText(str);

        if (commandSuggestions != null) {
            try {
                Method setAllowSuggestions = CommandSuggestions.class.getDeclaredMethod("setAllowSuggestions", boolean.class);
                setAllowSuggestions.setAccessible(true);
                setAllowSuggestions.invoke(commandSuggestions, true);
            } catch (Exception ignored) {}

            commandSuggestions.updateCommandInfo();
        }
    }

    private void doBackspace() {
        if (targetEditBox == null) return;
        targetEditBox.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0);

        if (commandSuggestions != null) {
            commandSuggestions.updateCommandInfo();
        }
    }

    private void doEnter() {
        if (parentScreen instanceof ChatScreen && targetEditBox != null) {
            String message = targetEditBox.getValue().trim();
            if (!message.isEmpty()) {
                try {
                    Method handleChat = ChatScreen.class.getDeclaredMethod("handleChatInput", String.class, boolean.class);
                    handleChat.setAccessible(true);
                    handleChat.invoke(parentScreen, message, true);
                } catch (Exception e) {
                    if (minecraft != null && minecraft.player != null) {
                        if (message.startsWith("/")) {
                            minecraft.player.connection.sendCommand(message.substring(1));
                        } else {
                            minecraft.player.connection.sendChat(message);
                        }
                    }
                }
            }
        }
        this.onClose();
    }

    @Override
    public void tick() {
        super.tick();

        if (parentScreen != null) {
            parentScreen.tick();
        }

        if (commandSuggestions != null && targetEditBox != null) {
            String text = targetEditBox.getValue();
            if (text.startsWith("/")) {
                commandSuggestions.updateCommandInfo();
            }
        }

        if (navCooldown > 0) navCooldown--;
        if (suggestionNavCooldown > 0) suggestionNavCooldown--;

        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) return;

        InputHandler input = addon.getInputHandler();
        if (input == null) return;

        float moveX = input.getMoveStrafe();
        float moveY = input.getMoveForward();

        if (navCooldown == 0) {
            boolean moved = handleNavigation(moveX, moveY);
            if (moved) {
                clampFocusPosition();
                navCooldown = NAV_COOLDOWN_TICKS;
            }
        }

        handleButtonInputs(input);
    }

    private boolean handleNavigation(float moveX, float moveY) {
        boolean moved = false;

        if (moveX > 0.5f) {
            focusedCol++;
            moved = true;
        } else if (moveX < -0.5f) {
            focusedCol--;
            moved = true;
        }

        if (moveY < -0.5f) {
            focusedRow++;
            moved = true;
        } else if (moveY > 0.5f) {
            focusedRow--;
            moved = true;
        }

        return moved;
    }

    private void clampFocusPosition() {
        focusedRow = Math.max(0, Math.min(focusedRow, keyData.size() - 1));
        if (!keyData.isEmpty()) {
            focusedCol = Math.max(0, Math.min(focusedCol, keyData.get(focusedRow).size() - 1));
        }
    }

    private void handleButtonInputs(InputHandler input) {
        boolean aPressed = input.isActionActive(GameAction.JUMP);
        if (aPressed && !lastAPressed) {
            pressSelectedKey();
        }
        lastAPressed = aPressed;

        boolean bPressed = input.isActionActive(GameAction.SNEAK);
        if (bPressed && !lastBPressed) {
            onClose();
        }
        lastBPressed = bPressed;

        boolean xPressed = input.isActionActive(GameAction.SWAP_HANDS);
        if (xPressed && !lastXPressed) {
            doBackspace();
        }
        lastXPressed = xPressed;

        boolean yPressed = input.isActionActive(GameAction.INVENTORY);
        if (yPressed && !lastYPressed) {
            insertChar(" ");
        }
        lastYPressed = yPressed;

        boolean startPressed = input.isActionActive(GameAction.PAUSE);
        if (startPressed && !lastStartPressed) {
            doEnter();
        }
        lastStartPressed = startPressed;

        boolean lbPressed = input.isActionActive(GameAction.HOTBAR_PREV);
        if (lbPressed && !lastLBPressed) {
            shifted = !shifted;
            if (!shifted) {
                shiftLocked = false;
            }
        }
        lastLBPressed = lbPressed;
    }

    private void pressSelectedKey() {
        if (focusedRow >= 0 && focusedRow < keyData.size()) {
            List<KeyData> row = keyData.get(focusedRow);
            if (focusedCol >= 0 && focusedCol < row.size()) {
                onKeyPressed(row.get(focusedCol).key);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        int inputY = keyboardY - 30;
        int inputHeight = 24;

        graphics.fill(4, inputY - 2, this.width - 4, inputY + inputHeight, 0xF0222222);
        graphics.fill(4, inputY - 2, this.width - 4, inputY - 1, 0xFF444444);
        graphics.fill(4, inputY + inputHeight - 1, this.width - 4, inputY + inputHeight, 0xFF111111);

        if (targetEditBox != null) {
            String text = targetEditBox.getValue();
            String prefix = text.startsWith("/") ? "" : "> ";

            String displayText = text.isEmpty() ? "Type a message..." : prefix + text;
            int textColor = text.isEmpty() ? 0xFF888888 : 0xFFFFFFFF;

            graphics.drawString(font, displayText, 10, inputY + 6, textColor);

            if (!text.isEmpty() && (System.currentTimeMillis() / 500) % 2 == 0) {
                int cursorX = 10 + font.width(prefix + text);
                graphics.fill(cursorX, inputY + 4, cursorX + 1, inputY + 16, 0xFFFFFFFF);
            }
        }

        if (commandSuggestions != null && targetEditBox != null && targetEditBox.getValue().startsWith("/")) {
            commandSuggestions.render(graphics, mouseX, mouseY);
        }

        graphics.fill(0, keyboardY, this.width, this.height, 0xF0111111);
        graphics.fill(0, keyboardY, this.width, keyboardY + 1, 0xFF333333);

        for (int row = 0; row < keyData.size(); row++) {
            List<KeyData> rowKeys = keyData.get(row);
            for (int col = 0; col < rowKeys.size(); col++) {
                KeyData key = rowKeys.get(col);
                boolean selected = (row == focusedRow && col == focusedCol);
                renderKey(graphics, key, selected);
            }
        }

        renderButtonLegend(graphics);

        String hint = "Use left stick to navigate, press buttons for shortcuts";
        graphics.drawCenteredString(font, hint, this.width / 2, 22, 0xFF888888);
    }

    private void renderButtonLegend(GuiGraphics graphics) {
        int y = 6;
        int x = 10;

        String btnA = getButtonLabel("A");
        renderButtonIcon(graphics, btnA, getButtonColor("A"), x, y);
        graphics.drawString(font, "Select", x + 18, y + 3, 0xFFCCCCCC);
        x += 60;

        String btnX = getButtonLabel("X");
        renderButtonIcon(graphics, btnX, getButtonColor("X"), x, y);
        graphics.drawString(font, "Delete", x + 18, y + 3, 0xFFCCCCCC);
        x += 60;

        String btnY = getButtonLabel("Y");
        renderButtonIcon(graphics, btnY, getButtonColor("Y"), x, y);
        graphics.drawString(font, "Space", x + 18, y + 3, 0xFFCCCCCC);
        x += 55;

        String btnLB = getButtonLabel("LB");
        renderButtonIcon(graphics, btnLB, getButtonColor("LB"), x, y);
        graphics.drawString(font, "Shift", x + 22, y + 3, 0xFFCCCCCC);
        x += 55;

        renderButtonIcon(graphics, "≡", 0xFF666666, x, y);
        graphics.drawString(font, "Enter", x + 18, y + 3, 0xFFCCCCCC);
        x += 55;

        String btnB = getButtonLabel("B");
        renderButtonIcon(graphics, btnB, getButtonColor("B"), x, y);
        graphics.drawString(font, "Close", x + 18, y + 3, 0xFFCCCCCC);
    }

    private String getButtonLabel(String xboxButton) {
        if (controllerType == ControllerType.PLAYSTATION) {
            return switch (xboxButton) {
                case "A" -> "×";
                case "B" -> "○";
                case "X" -> "□";
                case "Y" -> "△";
                case "LB" -> "L1";
                case "RB" -> "R1";
                default -> xboxButton;
            };
        } else if (controllerType == ControllerType.SWITCH) {
            return switch (xboxButton) {
                case "A" -> "B";
                case "B" -> "A";
                case "X" -> "Y";
                case "Y" -> "X";
                default -> xboxButton;
            };
        }
        return xboxButton;
    }

    private int getButtonColor(String xboxButton) {
        if (controllerType == ControllerType.PLAYSTATION) {
            return switch (xboxButton) {
                case "A" -> 0xFF6888C8;
                case "B" -> 0xFFE86A6A;
                case "X" -> 0xFFD88AD8;
                case "Y" -> 0xFF68C8A8;
                case "LB", "RB" -> 0xFF555555;
                default -> 0xFF555555;
            };
        }
        return switch (xboxButton) {
            case "A" -> 0xFF22AA22;
            case "B" -> 0xFFCC3333;
            case "X" -> 0xFF2266CC;
            case "Y" -> 0xFFCC9900;
            case "LB", "RB" -> 0xFF555555;
            default -> 0xFF555555;
        };
    }

    private void renderKey(GuiGraphics graphics, KeyData key, boolean selected) {
        int x = key.x;
        int y = key.y;
        int w = key.width;
        int h = key.height;

        int bgColor = selected ? 0xFF1E5BA8 : 0xFF2A2A2A;
        int highlightColor = selected ? 0xFF2E6BC8 : 0xFF3A3A3A;
        int shadowColor = selected ? 0xFF0E4B98 : 0xFF1A1A1A;

        graphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, bgColor);

        graphics.fill(x + 2, y, x + w - 2, y + 2, highlightColor);
        graphics.fill(x + 2, y + h - 2, x + w - 2, y + h, shadowColor);
        graphics.fill(x, y + 2, x + 2, y + h - 2, highlightColor);
        graphics.fill(x + w - 2, y + 2, x + w, y + h - 2, shadowColor);

        if (selected) {
            graphics.fill(x - 1, y + 2, x, y + h - 2, 0xFF4488DD);
            graphics.fill(x + w, y + 2, x + w + 1, y + h - 2, 0xFF4488DD);
            graphics.fill(x + 2, y - 1, x + w - 2, y, 0xFF4488DD);
            graphics.fill(x + 2, y + h, x + w - 2, y + h + 1, 0xFF4488DD);
        }

        String displayText = key.key;
        if (displayText.length() == 1 && Character.isLetter(displayText.charAt(0))) {
            displayText = (shifted || shiftLocked) ? displayText.toUpperCase() : displayText.toLowerCase();
        }

        int textColor = selected ? 0xFFFFFFFF : 0xFFCCCCCC;

        if (key.shortcut != null) {
            int iconX = x + 8;
            int iconY = y + (h - 14) / 2;

            String buttonLabel = getButtonLabel(key.shortcut);
            int buttonColor = getButtonColor(key.shortcut);

            renderButtonIcon(graphics, buttonLabel, buttonColor, iconX, iconY);

            int textX = iconX + (buttonLabel.length() > 1 ? 24 : 20);
            graphics.drawString(font, displayText, textX, y + (h - 8) / 2, textColor);
        } else {
            graphics.drawCenteredString(font, displayText, x + w / 2, y + (h - 8) / 2, textColor);
        }
    }

    private void renderButtonIcon(GuiGraphics graphics, String label, int color, int x, int y) {
        int size = 14;

        if (label.equals("LB") || label.equals("RB")) {
            int w = 20;
            graphics.fill(x + 1, y, x + w - 1, y + size, color);
            graphics.fill(x, y + 1, x + w, y + size - 1, color);
            graphics.fill(x + 1, y + 1, x + w - 1, y + 2, lightenColor(color, 40));
            graphics.drawCenteredString(font, label, x + w / 2, y + 3, 0xFFFFFFFF);
        } else if (label.equals("≡")) {
            graphics.fill(x + 2, y + 2, x + size - 2, y + size - 2, color);
            graphics.fill(x + 1, y + 3, x + size - 1, y + size - 3, color);
            graphics.fill(x + 3, y + 1, x + size - 3, y + size - 1, color);
            graphics.fill(x + 4, y + 4, x + size - 4, y + 5, 0xFFFFFFFF);
            graphics.fill(x + 4, y + 6, x + size - 4, y + 7, 0xFFFFFFFF);
            graphics.fill(x + 4, y + 8, x + size - 4, y + 9, 0xFFFFFFFF);
        } else {
            graphics.fill(x + 3, y, x + size - 3, y + size, color);
            graphics.fill(x + 2, y + 1, x + size - 2, y + size - 1, color);
            graphics.fill(x + 1, y + 2, x + size - 1, y + size - 2, color);
            graphics.fill(x, y + 3, x + size, y + size - 3, color);

            graphics.fill(x + 3, y + 1, x + size - 3, y + 2, lightenColor(color, 50));
            graphics.fill(x + 4, y + 2, x + size - 4, y + 3, lightenColor(color, 30));

            graphics.fill(x + 3, y + size - 2, x + size - 3, y + size - 1, darkenColor(color, 40));

            graphics.drawCenteredString(font, label, x + size / 2, y + 3, 0xFFFFFFFF);
        }
    }

    private int lightenColor(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int darkenColor(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, ((color >> 16) & 0xFF) - amount);
        int g = Math.max(0, ((color >> 8) & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (parentScreen != null && (keyCode == GLFW.GLFW_KEY_TAB ||
            keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN)) {
            return parentScreen.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            doEnter();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            doBackspace();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32) {
            insertChar(String.valueOf(chr));
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record KeyData(String key, String shortcut, int x, int y, int width, int height) {}
}
