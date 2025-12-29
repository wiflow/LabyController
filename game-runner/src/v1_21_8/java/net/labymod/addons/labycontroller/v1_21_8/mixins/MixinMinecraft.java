package net.labymod.addons.labycontroller.v1_21_8.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.GamepadButton;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.labymod.addons.labycontroller.input.VirtualMouse;
import net.labymod.addons.labycontroller.v1_21_8.input.KeyMappingAccess;
import net.labymod.addons.labycontroller.v1_21_8.radial.RadialMenuOpenerImpl;
import net.labymod.addons.labycontroller.v1_21_8.radial.RadialMenuScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import java.lang.reflect.Field;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public LocalPlayer player;

    @Shadow
    @Final
    public Options options;

    @Shadow
    public abstract DeltaTracker getDeltaTracker();

    @Shadow
    public abstract void pauseGame(boolean pauseOnly);

    @Shadow
    public abstract void setScreen(net.minecraft.client.gui.screens.Screen screen);

    @Shadow
    public abstract void pickBlock();

    // Track previous button states for press/release detection
    @Unique
    private boolean labycontroller$lastAttackState = false;
    @Unique
    private boolean labycontroller$lastUseState = false;
    @Unique
    private boolean labycontroller$lastDropState = false;
    @Unique
    private boolean labycontroller$lastSwapState = false;

    // Hotbar navigation repeat helper
    @Unique
    private int labycontroller$hotbarNextCooldown = 0;
    @Unique
    private int labycontroller$hotbarPrevCooldown = 0;
    @Unique
    private static final int HOTBAR_REPEAT_DELAY = 10;
    @Unique
    private static final int HOTBAR_REPEAT_RATE = 4;

    // Virtual mouse for screen navigation
    @Unique
    private VirtualMouse labycontroller$virtualMouse = new VirtualMouse();
    @Unique
    private boolean labycontroller$lastScreenAPressed = false;
    @Unique
    private boolean labycontroller$lastScreenBPressed = false;
    @Unique
    private boolean labycontroller$lastScreenXPressed = false;
    @Unique
    private Screen labycontroller$lastScreen = null;
    @Unique
    private float labycontroller$scrollAccumulator = 0f;
    @Unique
    private static final float SCROLL_THRESHOLD = 0.3f;
    @Unique
    private static final float SCROLL_SPEED = 3.0f;
    @Unique
    private boolean labycontroller$radialMenuInitialized = false;

        @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;handleAccumulatedMovement()V"))
    private void labycontroller$processPlayerLook(boolean tick, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;

        // Handle virtual mouse interpolation for smooth cursor movement in screens
        if (mc.screen != null) {
            // ALWAYS skip for any chat-related screen - our keyboard mixin handles input
            String screenClassName = mc.screen.getClass().getName().toLowerCase();
            if (mc.screen instanceof ChatScreen || screenClassName.contains("chat")) {
                return;
            }

            LabyControllerAddon addon = LabyControllerAddon.getInstance();
            if (addon != null && addon.isControllerActive()) {
                // Interpolate virtual mouse position every frame using realtime delta
                // This matches Controlify's approach for smooth movement
                float deltaTicks = getDeltaTracker().getRealtimeDeltaTicks();
                if (labycontroller$virtualMouse.interpolate(deltaTicks)) {
                    // Only sync cursor when position actually changed
                    labycontroller$syncCursorPosition(mc);
                }
            }
        }

        // Handle camera look
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null || !addon.isControllerActive()) {
            return;
        }

        if (player == null) {
            return;
        }

        // Don't process look input when in a screen
        if (mc.screen != null) {
            return;
        }

        InputHandler input = addon.getInputHandler();
        if (input == null) {
            return;
        }

        float lookX = input.getLookX();
        float lookY = input.getLookY();

        if (lookX != 0 || lookY != 0) {
            // Get delta time for frame-rate independent movement
            float deltaTime = getDeltaTracker().getGameTimeDeltaTicks();

            // Sensitivity: 10 degrees per tick at 100% sensitivity
            // Divide by 0.15 to convert to the format player.turn() expects
            float sensitivity = 10.0f;

            // Apply aim sensitivity reduction when using bow/crossbow/spyglass
            if (addon.configuration().aimSensitivityReduction().get()) {
                if (labycontroller$isAiming()) {
                    sensitivity *= addon.configuration().aimSensitivityMultiplier().get();
                }
            }

            double velX = (lookX * sensitivity) / 0.15 * deltaTime;
            double velY = (lookY * sensitivity) / 0.15 * deltaTime;

            player.turn(velX, velY);
        }
    }

        @Inject(method = "tick", at = @At("HEAD"))
    private void labycontroller$handleControllerInput(CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null) {
            return;
        }

        // Initialize radial menu opener once
        if (!labycontroller$radialMenuInitialized && addon.getRadialMenuHandler() != null) {
            addon.getRadialMenuHandler().setMenuOpener(new RadialMenuOpenerImpl());
            labycontroller$radialMenuInitialized = true;
        }

        if (!addon.isControllerActive()) {
            return;
        }

        InputHandler input = addon.getInputHandler();
        if (input == null) {
            return;
        }

        Minecraft mc = (Minecraft) (Object) this;

        // Decrease cooldowns
        if (labycontroller$hotbarNextCooldown > 0) labycontroller$hotbarNextCooldown--;
        if (labycontroller$hotbarPrevCooldown > 0) labycontroller$hotbarPrevCooldown--;

        // Handle pause - works even in screens
        if (input.wasActionJustActivated(GameAction.PAUSE)) {
            if (mc.screen == null) {
                pauseGame(false);
            }
        }

        // Handle screen input with virtual mouse
        if (mc.screen != null) {
            labycontroller$handleScreenInput(mc, addon);
            return;
        }

        // Reset screen tracking when no screen
        labycontroller$lastScreen = null;

        if (player == null) {
            return;
        }

        // === Key emulation for attack/use ===
        labycontroller$handleKeyEmulation(input);

        // === Direct action handling (like Controlify's handleKeybinds) ===
        labycontroller$handleKeybinds(mc, input);
    }

        @Unique
    private void labycontroller$handleKeyEmulation(InputHandler input) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();

        // Attack key emulation
        boolean attackPressed = input.isActionActive(GameAction.ATTACK);
        if (attackPressed && !labycontroller$lastAttackState) {
            labycontroller$setKeyPressed(options.keyAttack, true);
            // Haptic feedback on attack press
            if (addon != null && addon.configuration().vibrationEnabled().get()
                    && addon.configuration().attackVibration().get()) {
                float strength = addon.configuration().vibrationStrength().get();
                addon.vibrate(0.3f * strength, 0.2f * strength, 50);
            }
        } else if (!attackPressed && labycontroller$lastAttackState) {
            labycontroller$setKeyPressed(options.keyAttack, false);
        }
        labycontroller$lastAttackState = attackPressed;

        // Use key emulation
        boolean usePressed = input.isActionActive(GameAction.USE);
        if (usePressed && !labycontroller$lastUseState) {
            labycontroller$setKeyPressed(options.keyUse, true);
            // Subtle haptic on use/place
            if (addon != null && addon.configuration().vibrationEnabled().get()
                    && addon.configuration().useVibration().get()) {
                float strength = addon.configuration().vibrationStrength().get();
                addon.vibrate(0.15f * strength, 0.1f * strength, 30);
            }
        } else if (!usePressed && labycontroller$lastUseState) {
            labycontroller$setKeyPressed(options.keyUse, false);
        }
        labycontroller$lastUseState = usePressed;
    }

        @Unique
    private void labycontroller$handleKeybinds(Minecraft mc, InputHandler input) {
        Inventory inventory = player.getInventory();

        // === Hotbar navigation with repeat ===
        if (input.isActionActive(GameAction.HOTBAR_NEXT)) {
            if (labycontroller$hotbarNextCooldown == 0) {
                inventory.setSelectedSlot((inventory.getSelectedSlot() + 1) % Inventory.getSelectionSize());
                // First press has longer delay, then faster repeat
                labycontroller$hotbarNextCooldown = input.wasActionJustActivated(GameAction.HOTBAR_NEXT)
                    ? HOTBAR_REPEAT_DELAY : HOTBAR_REPEAT_RATE;
            }
        } else {
            labycontroller$hotbarNextCooldown = 0;
        }

        if (input.isActionActive(GameAction.HOTBAR_PREV)) {
            if (labycontroller$hotbarPrevCooldown == 0) {
                inventory.setSelectedSlot((inventory.getSelectedSlot() - 1 + Inventory.getSelectionSize()) % Inventory.getSelectionSize());
                labycontroller$hotbarPrevCooldown = input.wasActionJustActivated(GameAction.HOTBAR_PREV)
                    ? HOTBAR_REPEAT_DELAY : HOTBAR_REPEAT_RATE;
            }
        } else {
            labycontroller$hotbarPrevCooldown = 0;
        }

        // === Drop item ===
        boolean dropPressed = input.isActionActive(GameAction.DROP_ITEM);
        if (dropPressed && !labycontroller$lastDropState) {
            if (!player.isSpectator() && player.drop(false)) {
                player.swing(InteractionHand.MAIN_HAND);
            }
        }
        labycontroller$lastDropState = dropPressed;

        // === Swap hands ===
        boolean swapPressed = input.isActionActive(GameAction.SWAP_HANDS);
        if (swapPressed && !labycontroller$lastSwapState) {
            if (!player.isSpectator()) {
                player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                    BlockPos.ZERO,
                    Direction.DOWN
                ));
            }
        }
        labycontroller$lastSwapState = swapPressed;

        // === Pick Block ===
        if (input.wasActionJustActivated(GameAction.PICK_BLOCK)) {
            pickBlock();
        }

        // === Inventory ===
        if (input.wasActionJustActivated(GameAction.INVENTORY)) {
            if (mc.gameMode.isServerControlledInventory()) {
                player.sendOpenInventory();
            } else {
                mc.getTutorial().onOpenInventory();
                setScreen(new InventoryScreen(player));
            }
        }

        // === Chat with On-Screen Keyboard ===
        if (input.wasActionJustActivated(GameAction.CHAT)) {
            openChatWithKeyboard(mc);
        }

        // === Toggle perspective ===
        if (input.wasActionJustActivated(GameAction.TOGGLE_PERSPECTIVE)) {
            CameraType cameraType = options.getCameraType();
            options.setCameraType(cameraType.cycle());
            if (cameraType.isFirstPerson() != options.getCameraType().isFirstPerson()) {
                mc.gameRenderer.checkEntityPostEffect(
                    options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null
                );
            }
            mc.levelRenderer.needsUpdate();
        }

        // === Player list toggle ===
        // Note: Player list is handled differently - it's a toggle that shows while held
        // For now, we just set the key state
        if (input.wasActionJustActivated(GameAction.PLAYERLIST)) {
            labycontroller$setKeyPressed(options.keyPlayerList, true);
        } else if (!input.isActionActive(GameAction.PLAYERLIST)) {
            // Release when not held
            labycontroller$setKeyPressed(options.keyPlayerList, false);
        }
    }

        @Unique
    private void labycontroller$setKeyPressed(KeyMapping keyMapping, boolean pressed) {
        ((KeyMappingAccess) keyMapping).labycontroller$setPressed(pressed);
    }

        @Unique
    private boolean labycontroller$isAiming() {
        if (player == null || !player.isUsingItem()) {
            return false;
        }

        ItemStack useItem = player.getUseItem();
        if (useItem.isEmpty()) {
            return false;
        }

        // Check if using bow, crossbow, or spyglass
        return useItem.getItem() instanceof BowItem ||
               useItem.getItem() instanceof CrossbowItem ||
               useItem.getItem() instanceof SpyglassItem;
    }

        @Unique
    private void openChatWithKeyboard(Minecraft mc) {
        // MixinChatScreen adds the keyboard automatically if enabled in settings
        setScreen(new ChatScreen(""));
    }

        @Unique
    private void labycontroller$handleScreenInput(Minecraft mc, LabyControllerAddon addon) {
        Screen screen = mc.screen;

        // ALWAYS skip for any chat-related screen - MixinChatScreen handles its own input
        String screenClassName = screen.getClass().getName().toLowerCase();
        if (screen instanceof ChatScreen || screenClassName.contains("chat")) {
            return;
        }

        // Skip for RadialMenuScreen - it handles its own input
        if (screen instanceof RadialMenuScreen) {
            return;
        }

        ControllerState state = addon.getActiveControllerState().orElse(null);
        if (state == null) {
            return;
        }

        // Reset virtual mouse position when screen changes
        if (screen != labycontroller$lastScreen) {
            labycontroller$lastScreen = screen;
            // Update screen size
            labycontroller$virtualMouse.setScreenSize(
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight()
            );
            // Center cursor when new screen opens
            labycontroller$virtualMouse.resetPosition();
            // Move actual cursor to center
            labycontroller$syncCursorPosition(mc);
        }

        // Update virtual mouse target from controller state (per-tick)
        // Interpolation and cursor sync is done per-frame in labycontroller$processPlayerLook
        labycontroller$virtualMouse.updateFromState(state);

        double mouseX = labycontroller$virtualMouse.getX();
        double mouseY = labycontroller$virtualMouse.getY();

        // Note: Left Bumper for shift-click is handled by MixinScreen.hasShiftDown()

        // Handle A button (left click) - press and release
        // When LB is held, Screen.hasShiftDown() returns true, enabling shift-click
        boolean aPressed = state.isButtonPressed(GamepadButton.A);
        if (aPressed && !labycontroller$lastScreenAPressed) {
            // Mouse down - shift-click is handled automatically via MixinScreen
            screen.mouseClicked(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        } else if (!aPressed && labycontroller$lastScreenAPressed) {
            // Mouse up - important for inventory item pickup/placement
            screen.mouseReleased(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }
        labycontroller$lastScreenAPressed = aPressed;

        // Handle B button (back/close)
        boolean bPressed = state.isButtonPressed(GamepadButton.B);
        if (bPressed && !labycontroller$lastScreenBPressed) {
            screen.onClose();
        }
        labycontroller$lastScreenBPressed = bPressed;

        // Handle X button (right-click) - press and release
        boolean xPressed = state.isButtonPressed(GamepadButton.X);
        if (xPressed && !labycontroller$lastScreenXPressed) {
            // Mouse down
            screen.mouseClicked(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        } else if (!xPressed && labycontroller$lastScreenXPressed) {
            // Mouse up
            screen.mouseReleased(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }
        labycontroller$lastScreenXPressed = xPressed;

        // Handle right stick scrolling
        float rightStickY = state.getRightStickY();
        if (Math.abs(rightStickY) > SCROLL_THRESHOLD) {
            // Accumulate scroll amount for smooth scrolling
            labycontroller$scrollAccumulator += rightStickY * SCROLL_SPEED;

            // Send scroll events when accumulated enough
            if (Math.abs(labycontroller$scrollAccumulator) >= 1.0f) {
                double scrollAmount = -Math.signum(labycontroller$scrollAccumulator);
                screen.mouseScrolled(mouseX, mouseY, 0, scrollAmount);
                labycontroller$scrollAccumulator = 0f;
            }
        } else {
            labycontroller$scrollAccumulator = 0f;
        }
    }

        @Unique
    private void labycontroller$syncCursorPosition(Minecraft mc) {
        // NEVER move cursor if in any chat-related screen
        if (mc.screen != null) {
            String screenClassName = mc.screen.getClass().getName().toLowerCase();
            if (mc.screen instanceof ChatScreen || screenClassName.contains("chat")) {
                return;
            }
        }

        // Convert GUI-scaled coordinates to window coordinates
        double guiScale = mc.getWindow().getGuiScale();
        double windowX = labycontroller$virtualMouse.getX() * guiScale;
        double windowY = labycontroller$virtualMouse.getY() * guiScale;

        // Move actual cursor
        GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), windowX, windowY);
    }
}
