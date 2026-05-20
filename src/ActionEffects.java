import java.util.function.Supplier;

public class ActionEffects {
    private ActionEffects() {
    }

    static String apply(Card card, GameState state, Supplier<Card> drawCard) {
        return card.rankValue().applyEffect(state, drawCard);
    }
}