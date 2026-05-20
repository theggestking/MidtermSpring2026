import java.util.Random;
import java.util.Scanner;

public class Main {
    static GameState state = new GameState();
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);
    static ConsoleView view = new ConsoleView(scanner);

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--self-test")) {
                CharacterizationTests.run();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (state.playerCount() < 2 || state.playerCount() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        TurnController controller = new TurnController(state, random, view, quiet);

        for (int g = 1; g <= games; g++) {
            if (!quiet) {
                view.showGameHeader(g);
            }
            controller.playGame();
        }

        view.showFinalScores(state);
    }

    static void setupPlayers(int bots, boolean human) {
        state.clearPlayers();
        if (human) {
            state.addPlayer("You", true);
        }
        for (int i = 1; i <= bots; i++) {
            state.addPlayer("Bot" + i, false);
        }
    }
}