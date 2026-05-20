public class Card {
    private final String code;
    private final CardColor color;
    private final CardRank rank;
    private final int number;

    private Card(String code, CardColor color, CardRank rank, int number) {
        this.code = code;
        this.color = color;
        this.rank = rank;
        this.number = number;
    }

    static Card from(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Card code cannot be null");
        }

        if (code.equals("W")) {
            return new Card(code, CardColor.NONE, CardRank.WILD, -1);
        }

        if (code.equals("W4")) {
            return new Card(code, CardColor.NONE, CardRank.WILD_DRAW_FOUR, -1);
        }

        if (code.length() < 2) {
            throw new IllegalArgumentException("Invalid card code: " + code);
        }

        String colorCode = code.substring(0, 1);
        CardColor color = CardColor.fromCode(colorCode);

        String suffix = code.substring(1);

        if (suffix.equals("S")) {
            return new Card(code, color, CardRank.SKIP, -1);
        }

        if (suffix.equals("R")) {
            return new Card(code, color, CardRank.REVERSE, -1);
        }

        if (suffix.equals("+2")) {
            return new Card(code, color, CardRank.DRAW_TWO, -1);
        }

        if (isNumeric(suffix)) {
            return new Card(code, color, CardRank.NUMBER, Integer.parseInt(suffix));
        }

        throw new IllegalArgumentException("Invalid card code: " + code);
    }

    static boolean isValid(String code) {
        try {
            from(code);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    static boolean isNumeric(String value) {
        if (value.length() == 0) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isColor(String color) {
        try {
            CardColor.fromCode(color);
            return !CardColor.fromCode(color).equals(CardColor.NONE);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    boolean isWild() {
        return rank == CardRank.WILD || rank == CardRank.WILD_DRAW_FOUR;
    }

    String color() {
        return color.code();
    }

    CardColor colorValue() {
        return color;
    }

    String rank() {
        return rank.name();
    }

    CardRank rankValue() {
        return rank;
    }

    int number() {
        return number;
    }

    int points() {
        if (rank == CardRank.NUMBER) {
            return number;
        }
        if (rank == CardRank.SKIP || rank == CardRank.REVERSE || rank == CardRank.DRAW_TWO) {
            return 20;
        }
        if (rank == CardRank.WILD || rank == CardRank.WILD_DRAW_FOUR) {
            return 50;
        }
        return 0;
    }

    String code() {
        return code;
    }
}

enum CardColor {
    RED("R"),
    YELLOW("Y"),
    GREEN("G"),
    BLUE("B"),
    NONE("");

    private final String code;

    CardColor(String code) {
        this.code = code;
    }

    String code() {
        return code;
    }

    static CardColor fromCode(String code) {
        if (code.equals("R")) {
            return RED;
        }
        if (code.equals("Y")) {
            return YELLOW;
        }
        if (code.equals("G")) {
            return GREEN;
        }
        if (code.equals("B")) {
            return BLUE;
        }
        if (code.equals("")) {
            return NONE;
        }
        throw new IllegalArgumentException("Invalid card color: " + code);
    }
}

enum CardRank {
    NUMBER,
    SKIP,
    REVERSE,
    DRAW_TWO,
    WILD,
    WILD_DRAW_FOUR
}