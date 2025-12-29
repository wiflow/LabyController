package net.labymod.addons.labycontroller.v1_21_8.input;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {

    @Invoker("createArrowEvent")
    FocusNavigationEvent.ArrowNavigation invokeCreateArrowEvent(ScreenDirection direction);

    @Invoker("changeFocus")
    void invokeChangeFocus(ComponentPath path);

    @Invoker("clearFocus")
    void invokeClearFocus();
}
