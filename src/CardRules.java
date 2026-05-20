public class CardRules {
    private CardRules() {
    }

    static boolean isLegal(String card, String up, String call) {
        Card candidate = Card.from(card);
        Card upCard = Card.from(up);

        if (candidate.isWild()) {
            return true;
        }
        if (candidate.color().equals(upCard.color())) {
            return true;
        }
        if (!call.equals("") && candidate.color().equals(call)) {
            return true;
        }
        if (candidate.rank().equals(upCard.rank()) && !candidate.rank().equals("NUMBER")) {
            return true;
        }
        if (candidate.rank().equals("NUMBER") && upCard.rank().equals("NUMBER") && candidate.number() == upCard.number()) {
            return true;
        }
        return false;
    }

    static String color(String card) {
        return Card.from(card).color();
    }

    static String rank(String card) {
        return Card.from(card).rank();
    }

    static int number(String card) {
        return Card.from(card).number();
    }

    static int points(String card) {
        return Card.from(card).points();
    }
}