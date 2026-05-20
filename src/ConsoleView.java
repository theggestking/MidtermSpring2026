import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleView {
    private final Scanner scanner;

    ConsoleView(Scanner scanner) {
        this.scanner = scanner;
    }

    void showGameHeader(int gameNumber) {
        System.out.println("\n=== Game " + gameNumber + " ===");
    }

    void showFinalScores(GameState state) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < state.playerNames.size(); i++) {
            System.out.println(state.playerNames.get(i) + ": " + state.scores[i]);
        }
    }

    void showTurn(String upCard, String calledColor, String name, ArrayList<String> hand) {
        System.out.println("\nUp card: " + upCard + (calledColor.equals("") ? "" : " called " + calledColor));
        System.out.println(name + " hand: " + join(hand));
    }

    int askHuman(ArrayList<String> hand, String upCard, String calledColor) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) {
                return -1;
            }

            int index = Main.parseCardIndex(input, hand.size());
            if (index != -1) {
                return index;
            }

            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (CardRules.isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }

            System.out.println("Card not found.");
        }
    }

    String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String parsedColor = Main.parseCalledColor(scanner.nextLine());
            if (!parsedColor.equals("")) {
                return parsedColor;
            }
            System.out.println("Bad color.");
        }
    }

    boolean askPlayDrawnCard(String drawn) {
        System.out.print("Play drawn card " + drawn + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    void showDraw(String name, String drawn) {
        System.out.println(name + " draws " + drawn);
    }

    void showInvalidIndexPenalty(String name) {
        System.out.println(name + " selected an invalid index and draws a penalty card.");
    }

    void showIllegalCardPenalty(String name, String card) {
        System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
    }

    void showPlayedCard(String name, String card) {
        System.out.println(name + " plays " + card);
    }

    void showCalledColor(String name, String calledColor) {
        System.out.println(name + " calls " + calledColor);
    }

    void showUno(String name) {
        System.out.println(name + " says UNO!");
    }

    void showWin(String name, int points) {
        System.out.println(name + " wins and scores " + points);
    }

    void showEffectMessage(String message) {
        System.out.println(message);
    }

    void showSafetyLimit() {
        System.out.println("Game stopped at safety limit.");
    }

    private String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) {
                out += " ";
            }
        }
        return out;
    }
}