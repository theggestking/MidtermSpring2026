import java.util.List;
import java.util.Scanner;

public record ConsoleView(Scanner scanner) implements GameView {

    @Override
    public void showGameHeader(int gameNumber) {
        System.out.println("\n=== Game " + gameNumber + " ===");
    }

    @Override
    public void showFinalScores(GameState state) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < state.playerCount(); i++) {
            System.out.println(state.playerName(i) + ": " + state.scoreForPlayer(i));
        }
    }

    @Override
    public void showTurn(String upCard, String calledColor, String name, List<Card> hand) {
        System.out.println("\nUp card: " + upCard + (calledColor.equals("") ? "" : " called " + calledColor));
        System.out.println(name + " hand: " + join(hand));
    }

    @Override
    public String askMoveInput() {
        System.out.print("Choose card index/code or draw: ");
        return scanner.nextLine().trim().toUpperCase();
    }

    @Override
    public void showIllegalSelection() {
        System.out.println("That card is not legal.");
    }

    @Override
    public void showCardNotFound() {
        System.out.println("Card not found.");
    }

    @Override
    public String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String parsedColor = InputParser.parseCalledColor(scanner.nextLine());
            if (!parsedColor.equals("")) {
                return parsedColor;
            }
            System.out.println("Bad color.");
        }
    }

    @Override
    public boolean askPlayDrawnCard(String drawn) {
        System.out.print("Play drawn card " + drawn + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    @Override
    public void showDraw(String name, String drawn) {
        System.out.println(name + " draws " + drawn);
    }

    @Override
    public void showInvalidIndexPenalty(String name) {
        System.out.println(name + " selected an invalid index and draws a penalty card.");
    }

    @Override
    public void showIllegalCardPenalty(String name, String card) {
        System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
    }

    @Override
    public void showPlayedCard(String name, String card) {
        System.out.println(name + " plays " + card);
    }

    @Override
    public void showCalledColor(String name, String calledColor) {
        System.out.println(name + " calls " + calledColor);
    }

    @Override
    public boolean askCallUno() {
        System.out.print("Call UNO? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    @Override
    public void showUno(String name) {
        System.out.println(name + " says UNO!");
    }

    @Override
    public void showMissedUnoPenalty(String name) {
        System.out.println(name + " missed UNO and draws two penalty cards.");
    }

    @Override
    public void showWin(String name, int points) {
        System.out.println(name + " wins and scores " + points);
    }

    @Override
    public void showEffectMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void showSafetyLimit() {
        System.out.println("Game stopped at safety limit.");
    }

    private String join(List<Card> cards) {
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
