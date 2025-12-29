package net.labymod.addons.labycontroller.v1_21_3.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.ControllerType;
import net.labymod.addons.labycontroller.controller.GamepadButton;
import net.labymod.addons.labycontroller.keyboard.*;
import net.labymod.addons.labycontroller.util.HoldRepeatHelper;
import net.labymod.addons.labycontroller.v1_21_3.keyboard.SuggestionsController;
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
public abstract class MixinChatScreen extends Screen implements InputTarget {

    @Shadow protected EditBox input;
    @Shadow CommandSuggestions commandSuggestions;

    // Keyboard state
    @Unique private boolean labycontroller$keyboardEnabled = false;
    @Unique private int labycontroller$keyboardHeight = 0;
    @Unique private int labycontroller$keyboardY = 0;
    @Unique private ControllerType labycontroller$controllerType = ControllerType.GENERIC;

    // Keyboard layout data
    @Unique private KeyboardLayout labycontroller$layout = KeyboardLayout.FULL;
    @Unique private final List<List<KeyRenderData>> labycontroller$keyRenderData = new ArrayList<>();

    // Navigation state
    @Unique private int labycontroller$focusedRow = 1;
    @Unique private int labycontroller$focusedCol = 1;
    @Unique private boolean labycontroller$shifted = false;
    @Unique private boolean labycontroller$shiftLocked = false;

    // Hold repeat helpers
    @Unique private final HoldRepeatHelper labycontroller$navRightHelper = new HoldRepeatHelper(12, 4);
    @Unique private final HoldRepeatHelper labycontroller$navLeftHelper = new HoldRepeatHelper(12, 4);
    @Unique private final HoldRepeatHelper labycontroller$navUpHelper = new HoldRepeatHelper(12, 4);
    @Unique private final HoldRepeatHelper labycontroller$navDownHelper = new HoldRepeatHelper(12, 4);
    @Unique private final HoldRepeatHelper labycontroller$keyPressHelper = new HoldRepeatHelper(15, 5);
    @Unique private final HoldRepeatHelper labycontroller$suggestionUpHelper = new HoldRepeatHelper(12, 4);
    @Unique private final HoldRepeatHelper labycontroller$suggestionDownHelper = new HoldRepeatHelper(12, 4);
    @Unique private final HoldRepeatHelper labycontroller$cursorLeftHelper = new HoldRepeatHelper(12, 3);
    @Unique private final HoldRepeatHelper labycontroller$cursorRightHelper = new HoldRepeatHelper(12, 3);

    // Button state tracking
    @Unique private boolean labycontroller$lastAPressed = false;
    @Unique private boolean labycontroller$lastBPressed = false;
    @Unique private boolean labycontroller$lastXPressed = false;
    @Unique private boolean labycontroller$lastYPressed = false;
    @Unique private boolean labycontroller$lastLBPressed = false;
    @Unique private boolean labycontroller$lastRBPressed = false;
    @Unique private boolean labycontroller$lastStartPressed = false;
    @Unique private boolean labycontroller$lastDpadUp = false;
    @Unique private boolean labycontroller$lastDpadDown = false;
    @Unique private boolean labycontroller$lastDpadLeft = false;
    @Unique private boolean labycontroller$lastDpadRight = false;
    @Unique private boolean labycontroller$lastStickUp = false;
    @Unique private boolean labycontroller$lastStickDown = false;
    @Unique private boolean labycontroller$lastStickLeft = false;
    @Unique private boolean labycontroller$lastStickRight = false;

    // Visual state
    @Unique private boolean labycontroller$keyVisuallyPressed = false;

    protected MixinChatScreen(Component title) {
        super(title);
    }

    // InputTarget implementation
    @Override
    public boolean supportsCharInput() { return true; }

    @Override
    public boolean acceptChar(char ch, int modifiers) {
        if (input != null) {
            input.charTyped(ch, modifiers);
            updateSuggestions();
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsKeyCodeInput() { return true; }

    @Override
    public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
        if (input != null) {
            input.keyPressed(keycode, scancode, modifiers);
            updateSuggestions();
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsCopying() { return true; }

    @Override
    public boolean copy() {
        if (minecraft != null && input != null) {
            minecraft.keyboardHandler.setClipboard(input.getValue());
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsCursorMovement() { return true; }

    @Override
    public boolean moveCursor(int amount) {
        if (input != null) {
            input.moveCursor(amount, false);
            return true;
        }
        return false;
    }

    @Unique
    private void updateSuggestions() {
        if (commandSuggestions != null) {
            commandSuggestions.updateCommandInfo();
        }
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

            // Get controller type
            if (addon.getSDLControllerManager() != null) {
                addon.getSDLControllerManager().getActiveGamepad().ifPresent(gamepad -> {
                    labycontroller$controllerType = gamepad.getType();
                });
            }

            // Calculate keyboard dimensions - 45% of screen height
            int chatAreaHeight = 120;
            labycontroller$keyboardHeight = (int) (this.height * 0.45f);
            labycontroller$keyboardY = this.height - chatAreaHeight - labycontroller$keyboardHeight;

            // Build key render data
            labycontroller$buildKeyRenderData();

            // Hide cursor
            if (minecraft != null) {
                GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
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
                GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            }
        }
    }

    @Unique
    private void labycontroller$buildKeyRenderData() {
        labycontroller$keyRenderData.clear();

        List<List<KeyboardKey>> rows = labycontroller$layout.rows();
        int numRows = rows.size();
        int padding = 3;
        int keyHeight = (labycontroller$keyboardHeight - padding * (numRows + 1)) / numRows;
        int startY = labycontroller$keyboardY + padding;

        for (int rowIdx = 0; rowIdx < numRows; rowIdx++) {
            List<KeyboardKey> row = rows.get(rowIdx);
            List<KeyRenderData> rowRenderData = new ArrayList<>();

            // Calculate total width units for this row
            float totalUnits = 0;
            for (KeyboardKey key : row) {
                totalUnits += key.width();
            }

            // Calculate available width and unit width
            int availableWidth = this.width - padding * (row.size() + 1);
            float unitWidth = availableWidth / totalUnits;

            int x = padding;
            int y = startY + rowIdx * (keyHeight + padding);

            for (KeyboardKey key : row) {
                int keyWidth = (int) (key.width() * unitWidth);
                rowRenderData.add(new KeyRenderData(key, x, y, keyWidth, keyHeight));
                x += keyWidth + padding;
            }

            labycontroller$keyRenderData.add(rowRenderData);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void labycontroller$onRenderHead(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        labycontroller$handleControllerInput();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void labycontroller$onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!labycontroller$keyboardEnabled) return;

        // Draw keyboard background
        int keyboardBottom = labycontroller$keyboardY + labycontroller$keyboardHeight;
        graphics.fill(0, labycontroller$keyboardY - 2, this.width, keyboardBottom + 2, 0xE8101010);
        graphics.fill(0, labycontroller$keyboardY - 2, this.width, labycontroller$keyboardY - 1, 0xFF3A3A3A);
        graphics.fill(0, keyboardBottom + 1, this.width, keyboardBottom + 2, 0xFF3A3A3A);

        // Draw all keys
        for (int rowIdx = 0; rowIdx < labycontroller$keyRenderData.size(); rowIdx++) {
            List<KeyRenderData> row = labycontroller$keyRenderData.get(rowIdx);
            for (int colIdx = 0; colIdx < row.size(); colIdx++) {
                KeyRenderData keyData = row.get(colIdx);
                boolean isFocused = (rowIdx == labycontroller$focusedRow && colIdx == labycontroller$focusedCol);
                labycontroller$renderKey(graphics, keyData, isFocused);
            }
        }

        // Draw button hints at bottom
        labycontroller$renderHints(graphics, keyboardBottom + 6);
    }

    @Unique
    private void labycontroller$renderKey(GuiGraphics graphics, KeyRenderData keyData, boolean focused) {
        int x = keyData.x();
        int y = keyData.y();
        int w = keyData.width();
        int h = keyData.height();
        KeyboardKey key = keyData.key();

        boolean isShifted = labycontroller$shifted || labycontroller$shiftLocked;
        KeyFunction function = key.getFunction(isShifted);
        boolean isPressed = focused && labycontroller$keyVisuallyPressed;

        // Check if this is a shift key that should show as active
        boolean isShiftActive = false;
        if (function instanceof KeyFunction.SpecialFunc special) {
            if (special.action() == KeyFunction.SpecialFunc.Action.SHIFT && labycontroller$shifted && !labycontroller$shiftLocked) {
                isShiftActive = true;
            }
            if (special.action() == KeyFunction.SpecialFunc.Action.SHIFT_LOCK && labycontroller$shiftLocked) {
                isShiftActive = true;
            }
        }

        // Key background colors
        int bgColor;
        if (isPressed || isShiftActive) {
            bgColor = 0xFF1565C0; // Blue when pressed
        } else if (focused) {
            bgColor = 0xFF2A5298; // Lighter blue when focused
        } else {
            bgColor = 0xFF252525; // Dark gray default
        }

        // Draw key background with slight 3D effect
        if (isPressed) {
            // Pressed - flat, shifted down slightly
            graphics.fill(x, y + 2, x + w, y + h, bgColor);
        } else {
            // Normal - with shadow/depth
            graphics.fill(x, y + 3, x + w, y + h, 0xFF1A1A1A); // Shadow
            graphics.fill(x, y, x + w, y + h - 2, bgColor);
        }

        // Draw border for focused key
        if (focused && !isPressed) {
            int borderColor = 0xFF64B5F6;
            graphics.fill(x - 1, y - 1, x + w + 1, y, borderColor); // Top
            graphics.fill(x - 1, y + h - 2, x + w + 1, y + h - 1, borderColor); // Bottom
            graphics.fill(x - 1, y, x, y + h - 2, borderColor); // Left
            graphics.fill(x + w, y, x + w + 1, y + h - 2, borderColor); // Right
        }

        // Get display text
        String displayText = function.displayName();
        if (function instanceof KeyFunction.SpecialFunc special) {
            displayText = special.action().symbol;
        }

        // Adjust text position if pressed
        int textY = y + (h - 8) / 2 - 1;
        if (isPressed) {
            textY += 2;
        }

        // Draw shortcut button glyph if present
        GamepadButton shortcut = key.shortcut();
        if (shortcut != null) {
            String glyph = ButtonGlyph.getGlyph(shortcut, labycontroller$controllerType);
            int glyphWidth = font.width(glyph);
            int textWidth = font.width(displayText);
            int totalWidth = glyphWidth + 2 + textWidth;
            int startX = x + (w - totalWidth) / 2;

            // Draw glyph in accent color
            graphics.drawString(font, glyph, startX, textY, 0xFF90CAF9, false);
            // Draw main text
            graphics.drawString(font, displayText, startX + glyphWidth + 2, textY, 0xFFFFFFFF, false);
        } else {
            // Center text
            int textColor = focused ? 0xFFFFFFFF : 0xFFCCCCCC;
            graphics.drawCenteredString(font, displayText, x + w / 2, textY, textColor);
        }
    }

    @Unique
    private void labycontroller$renderHints(GuiGraphics graphics, int y) {
        int x = 8;
        int spacing = 90;
        int hintColor = 0xFF888888;
        int glyphColor = 0xFFAAAAAA;

        String btnA = ButtonGlyph.getGlyph(GamepadButton.A, labycontroller$controllerType);
        String btnX = ButtonGlyph.getGlyph(GamepadButton.X, labycontroller$controllerType);
        String btnY = ButtonGlyph.getGlyph(GamepadButton.Y, labycontroller$controllerType);
        String btnB = ButtonGlyph.getGlyph(GamepadButton.B, labycontroller$controllerType);
        String btnLB = ButtonGlyph.getGlyph(GamepadButton.LEFT_BUMPER, labycontroller$controllerType);
        String btnRB = ButtonGlyph.getGlyph(GamepadButton.RIGHT_BUMPER, labycontroller$controllerType);
        String btnStart = ButtonGlyph.getGlyph(GamepadButton.START, labycontroller$controllerType);

        // Draw hints
        x = labycontroller$drawHint(graphics, x, y, btnA, "Select", glyphColor, hintColor);
        x = labycontroller$drawHint(graphics, x + spacing, y, btnX, "Delete", glyphColor, hintColor);
        x = labycontroller$drawHint(graphics, x + spacing, y, btnY, "Space", glyphColor, hintColor);
        x = labycontroller$drawHint(graphics, x + spacing, y, btnLB + "/" + btnRB, "Cursor", glyphColor, hintColor);
        x = labycontroller$drawHint(graphics, x + spacing, y, btnStart, "Send", glyphColor, hintColor);
        labycontroller$drawHint(graphics, x + spacing, y, btnB, "Close", glyphColor, hintColor);
    }

    @Unique
    private int labycontroller$drawHint(GuiGraphics graphics, int x, int y, String glyph, String text, int glyphColor, int textColor) {
        graphics.drawString(font, glyph, x, y, glyphColor, false);
        int glyphWidth = font.width(glyph);
        graphics.drawString(font, " " + text, x + glyphWidth, y, textColor, false);
        return x + glyphWidth + font.width(" " + text);
    }

    @Unique
    private void labycontroller$handleControllerInput() {
        if (!labycontroller$keyboardEnabled) return;

        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) return;

        ControllerState state = addon.getActiveControllerState().orElse(null);
        if (state == null) return;

        labycontroller$handleNavigation(state);
        labycontroller$handleButtons(state);
        labycontroller$handleCursorMovement(state);
    }

    @Unique
    private void labycontroller$handleNavigation(ControllerState state) {
        // D-pad
        boolean dpadUp = state.isButtonPressed(GamepadButton.DPAD_UP);
        boolean dpadDown = state.isButtonPressed(GamepadButton.DPAD_DOWN);
        boolean dpadLeft = state.isButtonPressed(GamepadButton.DPAD_LEFT);
        boolean dpadRight = state.isButtonPressed(GamepadButton.DPAD_RIGHT);

        // Left stick
        float stickX = state.getLeftStickX();
        float stickY = state.getLeftStickY();
        boolean stickUp = stickY < -0.5f;
        boolean stickDown = stickY > 0.5f;
        boolean stickLeft = stickX < -0.5f;
        boolean stickRight = stickX > 0.5f;

        // Combined
        boolean navUp = dpadUp || stickUp;
        boolean navDown = dpadDown || stickDown;
        boolean navLeft = dpadLeft || stickLeft;
        boolean navRight = dpadRight || stickRight;

        // Previous states
        boolean wasUp = labycontroller$lastDpadUp || labycontroller$lastStickUp;
        boolean wasDown = labycontroller$lastDpadDown || labycontroller$lastStickDown;
        boolean wasLeft = labycontroller$lastDpadLeft || labycontroller$lastStickLeft;
        boolean wasRight = labycontroller$lastDpadRight || labycontroller$lastStickRight;

        // Apply navigation with hold-repeat
        if (labycontroller$navUpHelper.shouldAction(navUp, wasUp)) {
            labycontroller$focusedRow = Math.max(0, labycontroller$focusedRow - 1);
            labycontroller$clampColumn();
            labycontroller$navUpHelper.onNavigate();
        }
        if (labycontroller$navDownHelper.shouldAction(navDown, wasDown)) {
            labycontroller$focusedRow = Math.min(labycontroller$keyRenderData.size() - 1, labycontroller$focusedRow + 1);
            labycontroller$clampColumn();
            labycontroller$navDownHelper.onNavigate();
        }
        if (labycontroller$navLeftHelper.shouldAction(navLeft, wasLeft)) {
            labycontroller$focusedCol = Math.max(0, labycontroller$focusedCol - 1);
            labycontroller$navLeftHelper.onNavigate();
        }
        if (labycontroller$navRightHelper.shouldAction(navRight, wasRight)) {
            List<KeyRenderData> row = labycontroller$keyRenderData.get(labycontroller$focusedRow);
            labycontroller$focusedCol = Math.min(row.size() - 1, labycontroller$focusedCol + 1);
            labycontroller$navRightHelper.onNavigate();
        }

        // Update previous states
        labycontroller$lastDpadUp = dpadUp;
        labycontroller$lastDpadDown = dpadDown;
        labycontroller$lastDpadLeft = dpadLeft;
        labycontroller$lastDpadRight = dpadRight;
        labycontroller$lastStickUp = stickUp;
        labycontroller$lastStickDown = stickDown;
        labycontroller$lastStickLeft = stickLeft;
        labycontroller$lastStickRight = stickRight;
    }

    @Unique
    private void labycontroller$clampColumn() {
        if (!labycontroller$keyRenderData.isEmpty()) {
            List<KeyRenderData> row = labycontroller$keyRenderData.get(labycontroller$focusedRow);
            labycontroller$focusedCol = Math.min(labycontroller$focusedCol, row.size() - 1);
        }
    }

    @Unique
    private void labycontroller$handleButtons(ControllerState state) {
        // A button - press key with hold-repeat
        boolean aPressed = state.isButtonPressed(GamepadButton.A);
        labycontroller$keyVisuallyPressed = aPressed;

        if (labycontroller$keyPressHelper.shouldAction(aPressed, labycontroller$lastAPressed)) {
            labycontroller$pressSelectedKey();
            labycontroller$keyPressHelper.onNavigate();
        }
        labycontroller$lastAPressed = aPressed;

        // X button - backspace
        boolean xPressed = state.isButtonPressed(GamepadButton.X);
        if (xPressed && !labycontroller$lastXPressed) {
            acceptKeyCode(GLFW.GLFW_KEY_BACKSPACE, 0, 0);
        }
        labycontroller$lastXPressed = xPressed;

        // Y button - space
        boolean yPressed = state.isButtonPressed(GamepadButton.Y);
        if (yPressed && !labycontroller$lastYPressed) {
            acceptChar(' ', 0);
        }
        labycontroller$lastYPressed = yPressed;

        // B button - close
        boolean bPressed = state.isButtonPressed(GamepadButton.B);
        if (bPressed && !labycontroller$lastBPressed) {
            this.onClose();
        }
        labycontroller$lastBPressed = bPressed;

        // Start button - send
        boolean startPressed = state.isButtonPressed(GamepadButton.START);
        if (startPressed && !labycontroller$lastStartPressed) {
            labycontroller$sendMessage();
        }
        labycontroller$lastStartPressed = startPressed;

        // Left trigger - shift (hold)
        float leftTrigger = state.getLeftTrigger();
        labycontroller$shifted = leftTrigger > 0.5f;

        // Bumpers - cycle suggestions
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
    }

    @Unique
    private void labycontroller$handleCursorMovement(ControllerState state) {
        // LB/RB for cursor movement when no suggestions
        boolean lbPressed = state.isButtonPressed(GamepadButton.LEFT_BUMPER);
        boolean rbPressed = state.isButtonPressed(GamepadButton.RIGHT_BUMPER);

        SuggestionsController suggestions = labycontroller$getSuggestionsController();
        if (suggestions == null || !suggestions.labycontroller$hasSuggestions()) {
            if (labycontroller$cursorLeftHelper.shouldAction(lbPressed, labycontroller$lastLBPressed)) {
                moveCursor(-1);
                labycontroller$cursorLeftHelper.onNavigate();
            }
            if (labycontroller$cursorRightHelper.shouldAction(rbPressed, labycontroller$lastRBPressed)) {
                moveCursor(1);
                labycontroller$cursorRightHelper.onNavigate();
            }
        }
    }

    @Unique
    private void labycontroller$pressSelectedKey() {
        if (labycontroller$focusedRow < 0 || labycontroller$focusedRow >= labycontroller$keyRenderData.size()) return;

        List<KeyRenderData> row = labycontroller$keyRenderData.get(labycontroller$focusedRow);
        if (labycontroller$focusedCol < 0 || labycontroller$focusedCol >= row.size()) return;

        KeyboardKey key = row.get(labycontroller$focusedCol).key();
        boolean isShifted = labycontroller$shifted || labycontroller$shiftLocked;
        KeyFunction function = key.getFunction(isShifted);

        boolean wasShiftAction = false;

        if (function instanceof KeyFunction.StringFunc stringFunc) {
            String str = stringFunc.string();
            for (char c : str.toCharArray()) {
                acceptChar(c, 0);
            }
        } else if (function instanceof KeyFunction.SpecialFunc special) {
            switch (special.action()) {
                case SHIFT -> {
                    if (!labycontroller$shiftLocked) {
                        labycontroller$shifted = !labycontroller$shifted;
                    } else {
                        labycontroller$shifted = false;
                        labycontroller$shiftLocked = false;
                    }
                    wasShiftAction = true;
                }
                case SHIFT_LOCK -> {
                    labycontroller$shiftLocked = !labycontroller$shiftLocked;
                    labycontroller$shifted = labycontroller$shiftLocked;
                    wasShiftAction = true;
                }
                case ENTER -> {
                    SuggestionsController suggestions = labycontroller$getSuggestionsController();
                    if (suggestions != null && suggestions.labycontroller$hasSuggestions()) {
                        suggestions.labycontroller$useSuggestion();
                    } else {
                        labycontroller$sendMessage();
                    }
                }
                case BACKSPACE -> acceptKeyCode(GLFW.GLFW_KEY_BACKSPACE, 0, 0);
                case SPACE -> acceptChar(' ', 0);
                case LEFT_ARROW -> moveCursor(-1);
                case RIGHT_ARROW -> moveCursor(1);
                case PASTE -> {
                    if (minecraft != null) {
                        String clipboard = minecraft.keyboardHandler.getClipboard();
                        for (char c : clipboard.toCharArray()) {
                            acceptChar(c, 0);
                        }
                    }
                }
                case COPY_ALL -> copy();
            }
        }

        // Auto-unshift after typing (not for shift keys)
        if (!wasShiftAction && labycontroller$shifted && !labycontroller$shiftLocked) {
            labycontroller$shifted = false;
        }
    }

    @Unique
    private void labycontroller$sendMessage() {
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

    @Unique
    private SuggestionsController labycontroller$getSuggestionsController() {
        if (commandSuggestions instanceof SuggestionsController controller) {
            return controller;
        }
        return null;
    }

    // Simple record for key render data
    @Unique
    private record KeyRenderData(KeyboardKey key, int x, int y, int width, int height) {}
}
