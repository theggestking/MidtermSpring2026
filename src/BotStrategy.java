import java.util.ArrayList;

public class BotStrategy {
    private BotStrategy() {
    }

    static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {
        int drawTwo = findPlayableCardByRank(hand, "DRAW_TWO", upCard, calledColor);
        if (drawTwo != -1) {
            return drawTwo;
        }

        int skip = findPlayableCardByRank(hand, "SKIP", upCard, calledColor);
        if (skip != -1) {
            return skip;
        }

        int number = findPlayableCardByRank(hand, "NUMBER", upCard, calledColor);
        if (number != -1) {
            return number;
        }

        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
        }
        return -1;
    }

    static int findPlayableCardByRank(ArrayList<String> hand, String preferredRank, String upCard, String calledColor) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.rank(card).equals(preferredRank) && CardRules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        return -1;
    }

    static String chooseColor(ArrayList<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < hand.size(); i++) {
            String c = CardRules.color(hand.get(i));
            if (c.equals("R")) {
                r++;
            } else if (c.equals("Y")) {
                y++;
            } else if (c.equals("G")) {
                g++;
            } else if (c.equals("B")) {
                b++;
            }
        }
        if (r >= y && r >= g && r >= b) {
            return "R";
        } else if (y >= r && y >= g && y >= b) {
            return "Y";
        } else if (g >= r && g >= y && g >= b) {
            return "G";
        } else {
            return "B";
        }
    }
}