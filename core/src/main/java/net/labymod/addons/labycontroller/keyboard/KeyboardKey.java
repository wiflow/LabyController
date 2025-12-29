package net.labymod.addons.labycontroller.keyboard;

import net.labymod.addons.labycontroller.controller.GamepadButton;
import org.jetbrains.annotations.Nullable;

public record KeyboardKey(KeyFunction regular, KeyFunction shifted, float width, @Nullable GamepadButton shortcut) {
    public KeyboardKey(KeyFunction function) { this(function, function, 1.0f, null); }
    public KeyboardKey(KeyFunction function, float width) { this(function, function.createShifted(), width, null); }
    public KeyboardKey(KeyFunction function, float width, @Nullable GamepadButton shortcut) { this(function, function.createShifted(), width, shortcut); }

    public static KeyboardKey character(String c) { return new KeyboardKey(new KeyFunction.StringFunc(c)); }
    public static KeyboardKey character(String regular, String shifted) {
        return new KeyboardKey(new KeyFunction.StringFunc(regular), new KeyFunction.StringFunc(shifted), 1.0f, null);
    }
    public static KeyboardKey special(KeyFunction.SpecialFunc.Action action, float width, @Nullable GamepadButton shortcut) {
        KeyFunction func = new KeyFunction.SpecialFunc(action);
        return new KeyboardKey(func, func, width, shortcut);
    }

    public KeyFunction getFunction(boolean shifted) { return shifted ? this.shifted : this.regular; }
}
