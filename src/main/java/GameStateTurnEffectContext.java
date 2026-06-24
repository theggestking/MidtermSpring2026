import java.util.function.Supplier;

final class GameStateTurnEffectContext implements TurnEffectContext {
    private final GameState state;
    private final Supplier<Card> drawCard;

    GameStateTurnEffectContext(GameState state, Supplier<Card> drawCard) {
        this.state = state;
        this.drawCard = drawCard;
    }

    public int playerCount() {
        return state.playerCount();
    }

    public int currentPlayerIndex() {
        return state.currentPlayerIndex();
    }

    public String currentPlayerName() {
        return state.currentPlayerName();
    }

    public int direction() {
        return state.direction();
    }

    public void reverseDirection() {
        state.setDirection(state.direction() * -1);
    }

    public void nextPlayer() {
        state.nextPlayer();
    }

    public void drawCardsForCurrentPlayer(int count) {
        for (int card = 0; card < count; card++) {
            state.addCardToCurrentPlayer(drawCard.get());
        }
    }
}
