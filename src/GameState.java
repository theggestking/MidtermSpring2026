import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GameState {
    ArrayList<String> playerNames = new ArrayList<String>();
    ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
    ArrayList<Card> deck = new ArrayList<Card>();
    ArrayList<Card> discard = new ArrayList<Card>();
    int[] scores = new int[10];
    int currentPlayer = 0;
    int direction = 1;
    Card upCard = Card.from("R0");
    String calledColor = "";

    void nextPlayer() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    String upCardCode() {
        return upCard.code();
    }

    void buildDeck(Random random) {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (int c = 0; c < colors.length; c++) {
            deck.add(Card.from(colors[c] + "0"));
            for (int n = 1; n <= 9; n++) {
                deck.add(Card.from(colors[c] + n));
                deck.add(Card.from(colors[c] + n));
            }
            deck.add(Card.from(colors[c] + "S"));
            deck.add(Card.from(colors[c] + "S"));
            deck.add(Card.from(colors[c] + "R"));
            deck.add(Card.from(colors[c] + "R"));
            deck.add(Card.from(colors[c] + "+2"));
            deck.add(Card.from(colors[c] + "+2"));
        }
        for (int i = 0; i < 4; i++) {
            deck.add(Card.from("W"));
            deck.add(Card.from("W4"));
        }
        Collections.shuffle(deck, random);
    }

    Card draw(Random random) {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) {
            return Card.from("W");
        }
        return deck.remove(0);
    }
}