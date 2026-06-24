interface TurnEffectContext {
    int playerCount();

    int currentPlayerIndex();

    String currentPlayerName();

    int direction();

    void reverseDirection();

    void nextPlayer();

    void drawCardsForCurrentPlayer(int count);
}
