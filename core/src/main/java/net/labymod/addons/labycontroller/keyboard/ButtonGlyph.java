package net.labymod.addons.labycontroller.keyboard;

import net.labymod.addons.labycontroller.controller.ControllerType;
import net.labymod.addons.labycontroller.controller.GamepadButton;
import org.jetbrains.annotations.Nullable;

public final class ButtonGlyph {
    private ButtonGlyph() {}

    public static String getGlyph(@Nullable GamepadButton button, ControllerType type) {
        if (button == null) return "";
        return switch (type) {
            case PLAYSTATION -> getPlayStationGlyph(button);
            case SWITCH -> getNintendoGlyph(button);
            default -> getXboxGlyph(button);
        };
    }

    private static String getXboxGlyph(GamepadButton button) {
        return switch (button) {
            case A -> "A";
            case B -> "B";
            case X -> "X";
            case Y -> "Y";
            case LEFT_BUMPER -> "LB";
            case RIGHT_BUMPER -> "RB";
            case BACK -> "View";
            case START -> "Menu";
            case GUIDE -> "Xbox";
            case LEFT_STICK -> "LS";
            case RIGHT_STICK -> "RS";
            case DPAD_UP -> "\u2191";
            case DPAD_DOWN -> "\u2193";
            case DPAD_LEFT -> "\u2190";
            case DPAD_RIGHT -> "\u2192";
            default -> button.name();
        };
    }

    private static String getPlayStationGlyph(GamepadButton button) {
        return switch (button) {
            case A -> "\u00D7";
            case B -> "\u25CB";
            case X -> "\u25A1";
            case Y -> "\u25B3";
            case LEFT_BUMPER -> "L1";
            case RIGHT_BUMPER -> "R1";
            case BACK -> "Share";
            case START -> "Options";
            case GUIDE -> "PS";
            case LEFT_STICK -> "L3";
            case RIGHT_STICK -> "R3";
            case DPAD_UP -> "\u2191";
            case DPAD_DOWN -> "\u2193";
            case DPAD_LEFT -> "\u2190";
            case DPAD_RIGHT -> "\u2192";
            default -> button.name();
        };
    }

    private static String getNintendoGlyph(GamepadButton button) {
        return switch (button) {
            case A -> "B";
            case B -> "A";
            case X -> "Y";
            case Y -> "X";
            case LEFT_BUMPER -> "L";
            case RIGHT_BUMPER -> "R";
            case BACK -> "-";
            case START -> "+";
            case GUIDE -> "Home";
            case LEFT_STICK -> "LS";
            case RIGHT_STICK -> "RS";
            case DPAD_UP -> "\u2191";
            case DPAD_DOWN -> "\u2193";
            case DPAD_LEFT -> "\u2190";
            case DPAD_RIGHT -> "\u2192";
            default -> button.name();
        };
    }

    public static String getTriggerGlyph(boolean left, ControllerType type) {
        return switch (type) {
            case PLAYSTATION -> left ? "L2" : "R2";
            case SWITCH -> left ? "ZL" : "ZR";
            default -> left ? "LT" : "RT";
        };
    }

    public static String getHint(@Nullable GamepadButton button, String action, ControllerType type) {
        if (button == null) return action;
        return getGlyph(button, type) + " " + action;
    }
}
