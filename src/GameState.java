import java.util.ArrayList;
import java.util.Random;

public class GameState {
    private ArrayList<String> playerNames = new ArrayList<String>();
    private ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    private ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
    private CardPiles piles = new CardPiles();
    private int[] scores = new int[10];
    private int currentPlayer = 0;
    private int direction = 1;
    private String calledColor = "";

    void clearPlayers() {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
    }

    void addPlayer(String name, boolean human) {
        playerNames.add(name);
        humanPlayers.add(Boolean.valueOf(human));
        hands.add(new ArrayList<Card>());
    }

    int playerCount() {
        return playerNames.size();
    }

    String playerName(int player) {
        return playerNames.get(player);
    }

    String currentPlayerName() {
        return playerNames.get(currentPlayer);
    }

    int currentPlayerIndex() {
        return currentPlayer;
    }

    void setCurrentPlayer(int player) {
        currentPlayer = player;
    }

    boolean isCurrentPlayerHuman() {
        return humanPlayers.get(currentPlayer).booleanValue();
    }

    int direction() {
        return direction;
    }

    void setDirection(int newDirection) {
        direction = newDirection;
    }

    void resetDirection() {
        direction = 1;
    }

    void chooseRandomCurrentPlayer(Random random) {
        currentPlayer = random.nextInt(playerNames.size());
    }

    void nextPlayer() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    ArrayList<Card> currentHandSnapshot() {
        return new ArrayList<Card>(hands.get(currentPlayer));
    }

    ArrayList<Card> handSnapshot(int player) {
        return new ArrayList<Card>(hands.get(player));
    }

    ArrayList<ArrayList<Card>> handsSnapshot() {
        ArrayList<ArrayList<Card>> copy = new ArrayList<ArrayList<Card>>();
        for (int i = 0; i < hands.size(); i++) {
            copy.add(new ArrayList<Card>(hands.get(i)));
        }
        return copy;
    }

    int currentHandSize() {
        return hands.get(currentPlayer).size();
    }

    int handSize(int player) {
        return hands.get(player).size();
    }

    Card cardInCurrentHand(int index) {
        return hands.get(currentPlayer).get(index);
    }

    Card cardInHand(int player, int index) {
        return hands.get(player).get(index);
    }

    void addCardToCurrentPlayer(Card card) {
        hands.get(currentPlayer).add(card);
    }

    void addCardToPlayer(int player, Card card) {
        hands.get(player).add(card);
    }

    Card removeCardFromCurrentHand(int index) {
        return hands.get(currentPlayer).remove(index);
    }

    void clearHand(int player) {
        hands.get(player).clear();
    }

    void clearHands() {
        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
        }
    }

    void clearDeck() {
        piles.clearDeck();
    }

    void addToDeck(Card card) {
        piles.addToDeck(card);
    }

    void clearDiscard() {
        piles.clearDiscard();
    }

    void discardUpCard() {
        piles.discardUpCard();
    }

    Card upCard() {
        return piles.upCard();
    }

    void setUpCard(Card card) {
        piles.setUpCard(card);
    }

    boolean isUpCardWild() {
        return piles.isUpCardWild();
    }

    String upCardCode() {
        return piles.upCardCode();
    }

    String calledColor() {
        return calledColor;
    }

    void setCalledColor(String color) {
        calledColor = color;
    }

    void clearCalledColor() {
        calledColor = "";
    }

    int scoreForPlayer(int player) {
        return scores[player];
    }

    void addScoreToPlayer(int player, int points) {
        scores[player] += points;
    }

    void buildDeck(Random random) {
        piles.buildDeck(random);
    }

    Card draw(Random random) {
        return piles.draw(random);
    }
}