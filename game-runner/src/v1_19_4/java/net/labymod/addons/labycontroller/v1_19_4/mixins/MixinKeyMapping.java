package net.labymod.addons.labycontroller.v1_19_4.mixins;

import net.labymod.addons.labycontroller.v1_19_4.input.KeyMappingAccess;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyMapping.class)
public class MixinKeyMapping implements KeyMappingAccess {

    @Shadow
    private int clickCount;

    @Shadow
    private boolean isDown;

    @Override
    public void labycontroller$setPressed(boolean pressed) {
        if (pressed) {
            this.isDown = true;
            this.clickCount++;
        } else {
            this.isDown = false;
        }
    }
}
