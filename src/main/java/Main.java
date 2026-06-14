import java.time.Clock;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static GameState state = new GameState();
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);
    static GameView view = new ConsoleView(scanner);

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args) {
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
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: java -jar target/uno-cli.jar [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return 0;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (state.playerCount() < 2 || state.playerCount() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            LOGGER.info("event=session_end status=invalid_player_count players={}", state.playerCount());
            return 0;
        }

        Clock clock = Clock.systemUTC();
        PersistenceBootstrap bootstrap;
        try {
            bootstrap = PersistenceBootstrap.open(DatabaseConfig.fromEnvironment());
        } catch (RuntimeException exception) {
            System.err.println("Unable to initialize game history: " + conciseMessage(exception));
            LOGGER.error("event=session_end status=persistence_bootstrap_failed");
            return 1;
        }

        try (bootstrap) {
            TurnController controller = new TurnController(
                    state, random, view, quiet, new BotStrategy(), clock);
            GameSessionController sessionController = new GameSessionController(
                    state, controller, view, quiet, clock);
            CompletedGame completedGame = sessionController.play(games);
            new GameHistoryRepository(bootstrap.entityManagerFactory()).save(completedGame);
            LOGGER.info("event=session_end status=completed games={} players={}", games, state.playerCount());
            return 0;
        } catch (RuntimeException exception) {
            System.err.println("Unable to save game history: " + conciseMessage(exception));
            LOGGER.error("event=session_end status=persistence_save_failed");
            return 1;
        }
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

    private static String conciseMessage(RuntimeException exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null || message.isBlank()
                ? cause.getClass().getSimpleName()
                : message;
    }
}
