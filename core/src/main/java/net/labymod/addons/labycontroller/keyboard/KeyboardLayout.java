package net.labymod.addons.labycontroller.keyboard;

import net.labymod.addons.labycontroller.controller.GamepadButton;
import java.util.List;

public record KeyboardLayout(float width, List<List<KeyboardKey>> rows) {
    public static final KeyboardLayout FULL = createFullLayout();

    private static KeyboardLayout createFullLayout() {
        return new KeyboardLayout(12f, List.of(
            List.of(
                KeyboardKey.character("1", "!"), KeyboardKey.character("2", "@"), KeyboardKey.character("3", "#"),
                KeyboardKey.character("4", "$"), KeyboardKey.character("5", "%"), KeyboardKey.character("6", "^"),
                KeyboardKey.character("7", "&"), KeyboardKey.character("8", "*"), KeyboardKey.character("9", "("),
                KeyboardKey.character("0", ")"), KeyboardKey.special(KeyFunction.SpecialFunc.Action.SHIFT_LOCK, 2.0f, null)
            ),
            List.of(
                KeyboardKey.character("q"), KeyboardKey.character("w"), KeyboardKey.character("e"),
                KeyboardKey.character("r"), KeyboardKey.character("t"), KeyboardKey.character("y"),
                KeyboardKey.character("u"), KeyboardKey.character("i"), KeyboardKey.character("o"),
                KeyboardKey.character("p"), KeyboardKey.special(KeyFunction.SpecialFunc.Action.BACKSPACE, 2.0f, GamepadButton.X)
            ),
            List.of(
                KeyboardKey.character("/", "|"), KeyboardKey.character("a"), KeyboardKey.character("s"),
                KeyboardKey.character("d"), KeyboardKey.character("f"), KeyboardKey.character("g"),
                KeyboardKey.character("h"), KeyboardKey.character("j"), KeyboardKey.character("k"),
                KeyboardKey.character("l"), KeyboardKey.special(KeyFunction.SpecialFunc.Action.ENTER, 2.0f, GamepadButton.START)
            ),
            List.of(
                KeyboardKey.special(KeyFunction.SpecialFunc.Action.SHIFT, 2.0f, null),
                KeyboardKey.character("z"), KeyboardKey.character("x"), KeyboardKey.character("c"),
                KeyboardKey.character("v"), KeyboardKey.character("b"), KeyboardKey.character("n"),
                KeyboardKey.character("m"), KeyboardKey.character(",", "."),
                KeyboardKey.special(KeyFunction.SpecialFunc.Action.SPACE, 2.0f, GamepadButton.Y)
            )
        ));
    }
}
