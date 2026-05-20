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
