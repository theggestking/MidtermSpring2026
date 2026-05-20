public class Card {
    private final String code;
    private final String color;
    private final String rank;
    private final int number;

    private Card(String code, String color, String rank, int number) {
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
            return new Card(code, "", "WILD", -1);
        }

        if (code.equals("W4")) {
            return new Card(code, "", "WILD_DRAW_FOUR", -1);
        }

        if (code.length() < 2) {
            throw new IllegalArgumentException("Invalid card code: " + code);
        }

        String color = code.substring(0, 1);
        if (!isColor(color)) {
            throw new IllegalArgumentException("Invalid card color: " + code);
        }

        String suffix = code.substring(1);

        if (suffix.equals("S")) {
            return new Card(code, color, "SKIP", -1);
        }

        if (suffix.equals("R")) {
            return new Card(code, color, "REVERSE", -1);
        }

        if (suffix.equals("+2")) {
            return new Card(code, color, "DRAW_TWO", -1);
        }

        if (suffix.length() == 1 && Character.isDigit(suffix.charAt(0))) {
            return new Card(code, color, "NUMBER", Integer.parseInt(suffix));
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

    static boolean isColor(String color) {
        return color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B");
    }

    boolean isWild() {
        return rank.equals("WILD") || rank.equals("WILD_DRAW_FOUR");
    }

    String color() {
        return color;
    }

    String rank() {
        return rank;
    }

    int number() {
        return number;
    }

    int points() {
        if (rank.equals("NUMBER")) {
            return number;
        }
        if (rank.equals("SKIP") || rank.equals("REVERSE") || rank.equals("DRAW_TWO")) {
            return 20;
        }
        if (rank.equals("WILD") || rank.equals("WILD_DRAW_FOUR")) {
            return 50;
        }
        return 0;
    }

    String code() {
        return code;
    }
}