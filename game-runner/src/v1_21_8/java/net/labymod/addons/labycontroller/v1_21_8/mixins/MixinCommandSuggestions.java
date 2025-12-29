package net.labymod.addons.labycontroller.v1_21_8.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.labymod.addons.labycontroller.v1_21_8.keyboard.ChatKeyboardInfo;
import net.labymod.addons.labycontroller.v1_21_8.keyboard.SuggestionsController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mixin to provide controller access to command suggestions.
 * Based on Controlify's CommandSuggestionsMixin.
 */
@Mixin(CommandSuggestions.class)
public class MixinCommandSuggestions implements SuggestionsController {

    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;

    /**
     * Modify the height used for rendering suggestions to account for on-screen keyboard.
     */
    @ModifyExpressionValue(
        method = {"renderUsage", "showSuggestions"},
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/Screen;height:I")
    )
    private int labycontroller$modifyHeightForKeyboard(int height) {
        if (minecraft.screen instanceof ChatScreen) {
            float shift = ChatKeyboardInfo.getKeyboardShift(minecraft.screen);
            if (shift > 0) {
                return (int) (height * (1 - shift));
            }
        }
        return height;
    }

    @Override
    public boolean labycontroller$cycle(int amount) {
        if (this.suggestions == null) {
            return false;
        }
        this.suggestions.cycle(amount);
        return true;
    }

    @Override
    public boolean labycontroller$useSuggestion() {
        if (this.suggestions == null) {
            return false;
        }
        this.suggestions.useSuggestion();
        return true;
    }

    @Override
    public boolean labycontroller$hasSuggestions() {
        return this.suggestions != null;
    }
}
