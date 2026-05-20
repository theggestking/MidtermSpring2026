import java.util.ArrayList;

public class BotStrategy implements PlayerStrategy {
    public int chooseCard(ArrayList<Card> hand, String upCard, String calledColor) {
        int drawTwo = findPlayableCardByRank(hand, CardRank.DRAW_TWO, upCard, calledColor);
        if (drawTwo != -1) {
            return drawTwo;
        }

        int skip = findPlayableCardByRank(hand, CardRank.SKIP, upCard, calledColor);
        if (skip != -1) {
            return skip;
        }

        int number = findPlayableCardByRank(hand, CardRank.NUMBER, upCard, calledColor);
        if (number != -1) {
            return number;
        }

        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isWild()) {
                return i;
            }
        }

        return -1;
    }

    int findPlayableCardByRank(ArrayList<Card> hand, CardRank preferredRank, String upCard, String calledColor) {
        Card up = Card.from(upCard);
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.rankValue() == preferredRank && CardRules.isLegal(card, up, calledColor)) {
                return i;
            }
        }
        return -1;
    }

    public String chooseColor(ArrayList<Card> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;

        for (int i = 0; i < hand.size(); i++) {
            String color = hand.get(i).color();
            if (color.equals("R")) {
                r++;
            } else if (color.equals("Y")) {
                y++;
            } else if (color.equals("G")) {
                g++;
            } else if (color.equals("B")) {
                b++;
            }
        }

        if (r >= y && r >= g && r >= b) {
            return "R";
        }
        if (y >= r && y >= g && y >= b) {
            return "Y";
        }
        if (g >= r && g >= y && g >= b) {
            return "G";
        }
        return "B";
    }
}