package net.labymod.addons.labycontroller.v1_20_6.keyboard;

/**
 * Interface for controlling command suggestions from a controller.
 * Implemented by MixinCommandSuggestions.
 */
public interface SuggestionsController {

    /**
     * Cycle through suggestions.
     * @param amount Positive to go down, negative to go up
     * @return true if suggestions were cycled
     */
    boolean labycontroller$cycle(int amount);

    /**
     * Use the currently selected suggestion.
     * @return true if a suggestion was used
     */
    boolean labycontroller$useSuggestion();

    /**
     * Check if suggestions are currently available.
     * @return true if suggestions are showing
     */
    boolean labycontroller$hasSuggestions();
}
