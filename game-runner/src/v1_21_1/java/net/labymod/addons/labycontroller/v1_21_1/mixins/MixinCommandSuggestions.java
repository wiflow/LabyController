package net.labymod.addons.labycontroller.v1_21_1.mixins;

import net.labymod.addons.labycontroller.v1_21_1.keyboard.SuggestionsController;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CommandSuggestions.class)
public class MixinCommandSuggestions implements SuggestionsController {

    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;

    @Override
    public boolean labycontroller$cycle(int amount) {
        if (this.suggestions == null) return false;
        this.suggestions.cycle(amount);
        return true;
    }

    @Override
    public boolean labycontroller$useSuggestion() {
        if (this.suggestions == null) return false;
        this.suggestions.useSuggestion();
        return true;
    }

    @Override
    public boolean labycontroller$hasSuggestions() {
        return this.suggestions != null;
    }
}
