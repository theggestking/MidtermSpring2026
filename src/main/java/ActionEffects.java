import java.util.function.Supplier;

public class ActionEffects {
    private ActionEffects() {
    }

    static String apply(Card card, TurnEffectContext context) {
        return card.rankValue().applyEffect(context);
    }

    static String apply(Card card, GameState state, Supplier<Card> drawCard) {
        return apply(card, new GameStateTurnEffectContext(state, drawCard));
    }
}
