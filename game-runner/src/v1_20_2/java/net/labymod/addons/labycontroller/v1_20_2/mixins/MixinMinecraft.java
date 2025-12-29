package net.labymod.addons.labycontroller.v1_20_2.mixins;

import net.labymod.addons.labycontroller.LabyControllerAddon;
import net.labymod.addons.labycontroller.binding.GameAction;
import net.labymod.addons.labycontroller.controller.ControllerState;
import net.labymod.addons.labycontroller.controller.GamepadButton;
import net.labymod.addons.labycontroller.input.InputHandler;
import net.labymod.addons.labycontroller.input.VirtualMouse;
import net.labymod.addons.labycontroller.v1_20_2.input.KeyMappingAccess;
import net.labymod.addons.labycontroller.v1_20_2.radial.RadialMenuOpenerImpl;
import net.labymod.addons.labycontroller.v1_20_2.radial.RadialMenuScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
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

    @Shadow public LocalPlayer player;
    @Shadow @Final public Options options;
    @Shadow public abstract void pauseGame(boolean pauseOnly);
    @Shadow public abstract void setScreen(Screen screen);
    @Shadow protected abstract boolean startAttack();
    @Shadow protected abstract void startUseItem();

    @Unique private boolean labycontroller$lastAttackState = false;
    @Unique private boolean labycontroller$lastUseState = false;
    @Unique private boolean labycontroller$lastDropState = false;
    @Unique private boolean labycontroller$lastSwapState = false;
    @Unique private int labycontroller$hotbarNextCooldown = 0;
    @Unique private int labycontroller$hotbarPrevCooldown = 0;
    @Unique private static final int HOTBAR_REPEAT_DELAY = 10;
    @Unique private static final int HOTBAR_REPEAT_RATE = 4;
    @Unique private VirtualMouse labycontroller$virtualMouse = new VirtualMouse();
    @Unique private boolean labycontroller$lastScreenAPressed = false;
    @Unique private boolean labycontroller$lastScreenBPressed = false;
    @Unique private boolean labycontroller$lastScreenXPressed = false;
    @Unique private Screen labycontroller$lastScreen = null;
    @Unique private float labycontroller$scrollAccumulator = 0f;
    @Unique private long labycontroller$lastFrameTime = System.nanoTime();
    @Unique private boolean labycontroller$radialMenuInitialized = false;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void labycontroller$processPlayerLook(boolean tick, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        LabyControllerAddon addon = LabyControllerAddon.getInstance();

        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - labycontroller$lastFrameTime) / 1_000_000_000.0f * 20.0f;
        labycontroller$lastFrameTime = currentTime;
        deltaTime = Math.min(deltaTime, 1.0f);

        if (mc.screen != null && addon != null && addon.isControllerActive()) {
            if (!(mc.screen instanceof ChatScreen)) {
                if (labycontroller$virtualMouse.interpolate(deltaTime)) {
                    labycontroller$syncCursorPosition(mc);
                }
            }
        }

        if (addon == null || !addon.isControllerActive() || player == null || mc.screen != null) {
            return;
        }

        InputHandler input = addon.getInputHandler();
        if (input == null) return;

        float lookX = input.getLookX();
        float lookY = input.getLookY();

        if (lookX != 0 || lookY != 0) {
            float sensitivity = 10.0f;
            double velX = (lookX * sensitivity) / 0.15 * deltaTime;
            double velY = (lookY * sensitivity) / 0.15 * deltaTime;
            player.turn(velX, velY);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void labycontroller$handleControllerInput(CallbackInfo ci) {
        LabyControllerAddon addon = LabyControllerAddon.getInstance();
        if (addon == null) return;

        if (!labycontroller$radialMenuInitialized && addon.getRadialMenuHandler() != null) {
            addon.getRadialMenuHandler().setMenuOpener(new RadialMenuOpenerImpl());
            labycontroller$radialMenuInitialized = true;
        }

        if (!addon.isControllerActive()) return;

        InputHandler input = addon.getInputHandler();
        if (input == null) return;

        Minecraft mc = (Minecraft) (Object) this;

        if (labycontroller$hotbarNextCooldown > 0) labycontroller$hotbarNextCooldown--;
        if (labycontroller$hotbarPrevCooldown > 0) labycontroller$hotbarPrevCooldown--;

        if (input.wasActionJustActivated(GameAction.PAUSE) && mc.screen == null) {
            pauseGame(false);
        }

        if (mc.screen != null) {
            labycontroller$handleScreenInput(mc, addon);
            return;
        }

        labycontroller$lastScreen = null;
        if (player == null) return;

        labycontroller$handleCombatInput(input, addon);
        labycontroller$handleKeybinds(mc, input, addon);
    }

    @Unique
    private void labycontroller$handleCombatInput(InputHandler input, LabyControllerAddon addon) {
        boolean attackPressed = input.isActionActive(GameAction.ATTACK);
        if (attackPressed && !labycontroller$lastAttackState) {
            startAttack();
            if (addon.configuration().vibrationEnabled().get() && addon.configuration().attackVibration().get()) {
                float strength = addon.configuration().vibrationStrength().get();
                addon.vibrate(0.3f * strength, 0.2f * strength, 50);
            }
        }
        labycontroller$lastAttackState = attackPressed;

        boolean usePressed = input.isActionActive(GameAction.USE);
        if (usePressed && !labycontroller$lastUseState) {
            startUseItem();
            if (addon.configuration().vibrationEnabled().get() && addon.configuration().useVibration().get()) {
                float strength = addon.configuration().vibrationStrength().get();
                addon.vibrate(0.15f * strength, 0.1f * strength, 30);
            }
        }
        labycontroller$lastUseState = usePressed;
    }

    @Unique
    private void labycontroller$handleKeybinds(Minecraft mc, InputHandler input, LabyControllerAddon addon) {
        Inventory inventory = player.getInventory();

        if (input.isActionActive(GameAction.HOTBAR_NEXT)) {
            if (labycontroller$hotbarNextCooldown == 0) {
                inventory.selected = (inventory.selected + 1) % 9;
                labycontroller$hotbarNextCooldown = input.wasActionJustActivated(GameAction.HOTBAR_NEXT) ? HOTBAR_REPEAT_DELAY : HOTBAR_REPEAT_RATE;
            }
        } else {
            labycontroller$hotbarNextCooldown = 0;
        }

        if (input.isActionActive(GameAction.HOTBAR_PREV)) {
            if (labycontroller$hotbarPrevCooldown == 0) {
                inventory.selected = (inventory.selected - 1 + 9) % 9;
                labycontroller$hotbarPrevCooldown = input.wasActionJustActivated(GameAction.HOTBAR_PREV) ? HOTBAR_REPEAT_DELAY : HOTBAR_REPEAT_RATE;
            }
        } else {
            labycontroller$hotbarPrevCooldown = 0;
        }

        boolean dropPressed = input.isActionActive(GameAction.DROP_ITEM);
        if (dropPressed && !labycontroller$lastDropState) {
            if (!player.isSpectator() && player.drop(false)) {
                player.swing(InteractionHand.MAIN_HAND);
            }
        }
        labycontroller$lastDropState = dropPressed;

        boolean swapPressed = input.isActionActive(GameAction.SWAP_HANDS);
        if (swapPressed && !labycontroller$lastSwapState) {
            if (!player.isSpectator()) {
                player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            }
        }
        labycontroller$lastSwapState = swapPressed;

        if (input.wasActionJustActivated(GameAction.INVENTORY)) {
            setScreen(new InventoryScreen(player));
        }

        if (input.wasActionJustActivated(GameAction.CHAT)) {
            setScreen(new ChatScreen(""));
        }

        if (input.wasActionJustActivated(GameAction.TOGGLE_PERSPECTIVE)) {
            CameraType cameraType = options.getCameraType();
            options.setCameraType(cameraType.cycle());
            mc.levelRenderer.needsUpdate();
        }

        // Radial menu is handled via update() in RadialMenuHandler

        // Player list (hold to show)
        if (input.wasActionJustActivated(GameAction.PLAYERLIST)) {
            labycontroller$setKeyPressed(options.keyPlayerList, true);
        } else if (!input.isActionActive(GameAction.PLAYERLIST)) {
            labycontroller$setKeyPressed(options.keyPlayerList, false);
        }
    }

    @Unique
    private void labycontroller$setKeyPressed(KeyMapping keyMapping, boolean pressed) {
        ((KeyMappingAccess) keyMapping).labycontroller$setPressed(pressed);
    }

    @Unique
    private void labycontroller$handleScreenInput(Minecraft mc, LabyControllerAddon addon) {
        Screen screen = mc.screen;
        if (screen instanceof ChatScreen) return;
        if (screen instanceof RadialMenuScreen) return;

        ControllerState state = addon.getActiveControllerState().orElse(null);
        if (state == null) return;

        if (screen != labycontroller$lastScreen) {
            labycontroller$lastScreen = screen;
            labycontroller$virtualMouse.setScreenSize(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            labycontroller$virtualMouse.resetPosition();
            labycontroller$syncCursorPosition(mc);
        }

        labycontroller$virtualMouse.updateFromState(state);
        double mouseX = labycontroller$virtualMouse.getX();
        double mouseY = labycontroller$virtualMouse.getY();

        boolean aPressed = state.isButtonPressed(GamepadButton.A);
        if (aPressed && !labycontroller$lastScreenAPressed) {
            screen.mouseClicked(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        } else if (!aPressed && labycontroller$lastScreenAPressed) {
            screen.mouseReleased(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }
        labycontroller$lastScreenAPressed = aPressed;

        boolean bPressed = state.isButtonPressed(GamepadButton.B);
        if (bPressed && !labycontroller$lastScreenBPressed) {
            screen.onClose();
        }
        labycontroller$lastScreenBPressed = bPressed;

        boolean xPressed = state.isButtonPressed(GamepadButton.X);
        if (xPressed && !labycontroller$lastScreenXPressed) {
            screen.mouseClicked(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        } else if (!xPressed && labycontroller$lastScreenXPressed) {
            screen.mouseReleased(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }
        labycontroller$lastScreenXPressed = xPressed;

        float rightStickY = state.getRightStickY();
        if (Math.abs(rightStickY) > 0.3f) {
            labycontroller$scrollAccumulator += rightStickY * 3.0f;
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
        if (mc.screen instanceof ChatScreen) return;
        double guiScale = mc.getWindow().getGuiScale();
        GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), labycontroller$virtualMouse.getX() * guiScale, labycontroller$virtualMouse.getY() * guiScale);
    }
}
