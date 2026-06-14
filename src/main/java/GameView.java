import java.util.List;

public interface GameView {
    void showGameHeader(int gameNumber);

    void showFinalScores(GameState state);

    void showTurn(String upCard, String calledColor, String name, List<Card> hand);

    String askMoveInput();

    void showIllegalSelection();

    void showCardNotFound();

    String askColor();

    boolean askPlayDrawnCard(String drawn);

    void showDraw(String name, String drawn);

    void showInvalidIndexPenalty(String name);

    void showIllegalCardPenalty(String name, String card);

    void showPlayedCard(String name, String card);

    void showCalledColor(String name, String calledColor);

    void showUno(String name);

    void showWin(String name, int points);

    void showEffectMessage(String message);

    void showSafetyLimit();
}
