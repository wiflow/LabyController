package net.labymod.addons.labycontroller.keyboard;

public sealed interface KeyFunction {
    String displayName();
    default KeyFunction createShifted() { return this; }

    record StringFunc(String string, String manualDisplayName) implements KeyFunction {
        public StringFunc(String string) { this(string, null); }
        @Override public String displayName() { return manualDisplayName != null ? manualDisplayName : string; }
        @Override public KeyFunction createShifted() { return new StringFunc(string.toUpperCase(), manualDisplayName); }
    }

    record SpecialFunc(Action action) implements KeyFunction {
        @Override public String displayName() { return action.displayName; }

        public enum Action {
            SHIFT("Shift", "\u21E7"),
            SHIFT_LOCK("Caps", "\u21EA"),
            ENTER("Enter", "\u21B5"),
            BACKSPACE("Del", "\u2190"),
            SPACE("Space", "___"),
            LEFT_ARROW("<", "\u2190"),
            RIGHT_ARROW(">", "\u2192"),
            PASTE("Paste", "Paste"),
            COPY_ALL("Copy", "Copy");

            public final String displayName;
            public final String symbol;
            Action(String displayName, String symbol) {
                this.displayName = displayName;
                this.symbol = symbol;
            }
        }
    }
}
