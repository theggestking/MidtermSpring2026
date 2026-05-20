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
        for (int i = 0; i < state.playerCount(); i++) {
            System.out.println(state.playerName(i) + ": " + state.scoreForPlayer(i));
        }
    }

    void showTurn(String upCard, String calledColor, String name, ArrayList<Card> hand) {
        System.out.println("\nUp card: " + upCard + (calledColor.equals("") ? "" : " called " + calledColor));
        System.out.println(name + " hand: " + join(hand));
    }

    String askMoveInput() {
        System.out.print("Choose card index/code or draw: ");
        return scanner.nextLine().trim().toUpperCase();
    }

    void showIllegalSelection() {
        System.out.println("That card is not legal.");
    }

    void showCardNotFound() {
        System.out.println("Card not found.");
    }

    String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String parsedColor = InputParser.parseCalledColor(scanner.nextLine());
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

    private String join(ArrayList<Card> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i).code();
            if (i < cards.size() - 1) {
                out += " ";
            }
        }
        return out;
    }
}