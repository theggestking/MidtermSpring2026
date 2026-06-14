public class InputParser {
    private InputParser() {
    }

    static int parseCardIndex(String input, int handSize) {
        try {
            int index = Integer.parseInt(input);
            if (index >= 0 && index < handSize) {
                return index;
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    static String parseCalledColor(String input) {
        String color = input.trim().toUpperCase();
        if (color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B")) {
            return color;
        }
        return "";
    }
}
