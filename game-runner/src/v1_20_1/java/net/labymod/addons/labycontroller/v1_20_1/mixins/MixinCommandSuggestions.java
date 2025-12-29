package net.labymod.addons.labycontroller.v1_20_1.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.labymod.addons.labycontroller.v1_20_1.keyboard.ChatKeyboardInfo;
import net.labymod.addons.labycontroller.v1_20_1.keyboard.SuggestionsController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSuggestions.class)
public class MixinCommandSuggestions implements SuggestionsController {
    @Shadow @Final Minecraft minecraft;
    @Shadow @Nullable private CommandSuggestions.SuggestionsList suggestions;

    @ModifyExpressionValue(method={"renderUsage","showSuggestions"},at=@At(value="FIELD",target="Lnet/minecraft/client/gui/screens/Screen;height:I"))
    private int labycontroller$modifyHeightForKeyboard(int h){if(minecraft.screen instanceof ChatScreen){float s=ChatKeyboardInfo.getKeyboardShift(minecraft.screen);if(s>0)return(int)(h*(1-s));}return h;}

    @Override public boolean labycontroller$cycle(int a){if(suggestions==null)return false;suggestions.cycle(a);return true;}
    @Override public boolean labycontroller$useSuggestion(){if(suggestions==null)return false;suggestions.useSuggestion();return true;}
    @Override public boolean labycontroller$hasSuggestions(){return suggestions!=null;}
}
