import java.util.ArrayList;

public class PlayerTable {
    private ArrayList<String> playerNames = new ArrayList<String>();
    private ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    private ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();

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

    boolean isHuman(int player) {
        return humanPlayers.get(player).booleanValue();
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

    int handSize(int player) {
        return hands.get(player).size();
    }

    Card cardInHand(int player, int index) {
        return hands.get(player).get(index);
    }

    void addCardToPlayer(int player, Card card) {
        hands.get(player).add(card);
    }

    Card removeCardFromHand(int player, int index) {
        return hands.get(player).remove(index);
    }

    void clearHand(int player) {
        hands.get(player).clear();
    }

    void clearHands() {
        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
        }
    }
}