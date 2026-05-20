import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CardPiles {
    private ArrayList<Card> deck = new ArrayList<Card>();
    private ArrayList<Card> discard = new ArrayList<Card>();
    private Card upCard = Card.from("R0");

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

    void clearDeck() {
        deck.clear();
    }

    void addToDeck(Card card) {
        deck.add(card);
    }

    void clearDiscard() {
        discard.clear();
    }

    void discardUpCard() {
        discard.add(upCard);
    }

    Card upCard() {
        return upCard;
    }

    void setUpCard(Card card) {
        upCard = card;
    }

    boolean isUpCardWild() {
        return upCard.isWild();
    }

    String upCardCode() {
        return upCard.code();
    }
}