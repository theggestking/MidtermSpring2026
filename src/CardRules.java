public class CardRules {
    private CardRules() {
    }

    static boolean isLegal(String card, String up, String call) {
        Card candidate = Card.from(card);
        Card upCard = Card.from(up);

        if (candidate.isWild()) {
            return true;
        }
        if (candidate.colorValue() == upCard.colorValue()) {
            return true;
        }
        if (!call.equals("") && candidate.color().equals(call)) {
            return true;
        }
        if (candidate.rankValue() == upCard.rankValue() && candidate.rankValue() != CardRank.NUMBER) {
            return true;
        }
        if (candidate.rankValue() == CardRank.NUMBER && upCard.rankValue() == CardRank.NUMBER && candidate.number() == upCard.number()) {
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