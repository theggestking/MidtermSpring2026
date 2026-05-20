import java.util.ArrayList;
import java.util.Random;

public class GameState {
    private PlayerTable players = new PlayerTable();
    private CardPiles piles = new CardPiles();
    private ScoreBoard scoreBoard = new ScoreBoard();
    private TurnState turnState = new TurnState();

    void clearPlayers() {
        players.clearPlayers();
    }

    void addPlayer(String name, boolean human) {
        players.addPlayer(name, human);
    }

    int playerCount() {
        return players.playerCount();
    }

    String playerName(int player) {
        return players.playerName(player);
    }

    String currentPlayerName() {
        return players.playerName(turnState.currentPlayerIndex());
    }

    int currentPlayerIndex() {
        return turnState.currentPlayerIndex();
    }

    void setCurrentPlayer(int player) {
        turnState.setCurrentPlayer(player);
    }

    boolean isCurrentPlayerHuman() {
        return players.isHuman(turnState.currentPlayerIndex());
    }

    int direction() {
        return turnState.direction();
    }

    void setDirection(int newDirection) {
        turnState.setDirection(newDirection);
    }

    void resetDirection() {
        turnState.resetDirection();
    }

    void chooseRandomCurrentPlayer(Random random) {
        turnState.chooseRandomCurrentPlayer(random, players.playerCount());
    }

    void nextPlayer() {
        turnState.nextPlayer(players.playerCount());
    }

    ArrayList<Card> currentHandSnapshot() {
        return players.handSnapshot(turnState.currentPlayerIndex());
    }

    ArrayList<Card> handSnapshot(int player) {
        return players.handSnapshot(player);
    }

    ArrayList<ArrayList<Card>> handsSnapshot() {
        return players.handsSnapshot();
    }

    int currentHandSize() {
        return players.handSize(turnState.currentPlayerIndex());
    }

    int handSize(int player) {
        return players.handSize(player);
    }

    Card cardInCurrentHand(int index) {
        return players.cardInHand(turnState.currentPlayerIndex(), index);
    }

    Card cardInHand(int player, int index) {
        return players.cardInHand(player, index);
    }

    void addCardToCurrentPlayer(Card card) {
        players.addCardToPlayer(turnState.currentPlayerIndex(), card);
    }

    void addCardToPlayer(int player, Card card) {
        players.addCardToPlayer(player, card);
    }

    Card removeCardFromCurrentHand(int index) {
        return players.removeCardFromHand(turnState.currentPlayerIndex(), index);
    }

    void clearHand(int player) {
        players.clearHand(player);
    }

    void clearHands() {
        players.clearHands();
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
        return turnState.calledColor();
    }

    void setCalledColor(String color) {
        turnState.setCalledColor(color);
    }

    void clearCalledColor() {
        turnState.clearCalledColor();
    }

    int scoreForPlayer(int player) {
        return scoreBoard.scoreForPlayer(player);
    }

    void addScoreToPlayer(int player, int points) {
        scoreBoard.addScoreToPlayer(player, points);
    }

    void buildDeck(Random random) {
        piles.buildDeck(random);
    }

    Card draw(Random random) {
        return piles.draw(random);
    }
}