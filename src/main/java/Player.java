import java.util.ArrayList;

public class Player {
    private final String name;
    private final boolean human;
    private final ArrayList<Card> hand = new ArrayList<Card>();

    Player(String name, boolean human) {
        this.name = name;
        this.human = human;
    }

    String name() {
        return name;
    }

    boolean isHuman() {
        return human;
    }

    ArrayList<Card> handSnapshot() {
        return new ArrayList<Card>(hand);
    }

    int handSize() {
        return hand.size();
    }

    Card cardInHand(int index) {
        return hand.get(index);
    }

    void addCard(Card card) {
        hand.add(card);
    }

    Card removeCard(int index) {
        return hand.remove(index);
    }

    void clearHand() {
        hand.clear();
    }
}
