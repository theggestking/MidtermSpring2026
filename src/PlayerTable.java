import java.util.ArrayList;

public class PlayerTable {
    private ArrayList<Player> players = new ArrayList<Player>();

    void clearPlayers() {
        players.clear();
    }

    void addPlayer(String name, boolean human) {
        players.add(new Player(name, human));
    }

    int playerCount() {
        return players.size();
    }

    String playerName(int player) {
        return players.get(player).name();
    }

    boolean isHuman(int player) {
        return players.get(player).isHuman();
    }

    ArrayList<Card> handSnapshot(int player) {
        return players.get(player).handSnapshot();
    }

    ArrayList<ArrayList<Card>> handsSnapshot() {
        ArrayList<ArrayList<Card>> copy = new ArrayList<ArrayList<Card>>();
        for (int i = 0; i < players.size(); i++) {
            copy.add(players.get(i).handSnapshot());
        }
        return copy;
    }

    int handSize(int player) {
        return players.get(player).handSize();
    }

    Card cardInHand(int player, int index) {
        return players.get(player).cardInHand(index);
    }

    void addCardToPlayer(int player, Card card) {
        players.get(player).addCard(card);
    }

    Card removeCardFromHand(int player, int index) {
        return players.get(player).removeCard(index);
    }

    void clearHand(int player) {
        players.get(player).clearHand();
    }

    void clearHands() {
        for (int i = 0; i < players.size(); i++) {
            players.get(i).clearHand();
        }
    }
}