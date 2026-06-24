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
        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (IllegalArgumentException exception) {
            System.err.println("Argument error: " + exception.getMessage());
            System.err.println(CliOptions.usage());
            return 2;
        }
        if (options.mode() == CliMode.HELP) {
            System.out.println(CliOptions.usage());
            return 0;
        }

        PersistenceBootstrap bootstrap;
        try {
            bootstrap = PersistenceBootstrap.open(DatabaseConfig.fromEnvironment());
        } catch (RuntimeException exception) {
            System.err.println("Unable to initialize game history: " + conciseMessage(exception));
            LOGGER.error("event=session_end status=persistence_bootstrap_failed");
            return 1;
        }

        try (bootstrap) {
            GameHistoryRepository repository = new GameHistoryRepository(bootstrap.entityManagerFactory());
            if (options.mode() != CliMode.GAMEPLAY) {
                new GameHistoryReportController(repository, new HistoryReportView(System.out)).show(options);
                return 0;
            }
            return playAndPersist(options, repository);
        } catch (RuntimeException exception) {
            System.err.println("Unable to use game history: " + conciseMessage(exception));
            LOGGER.error("event=session_end status=persistence_operation_failed");
            return 1;
        }
    }

    private static int playAndPersist(CliOptions options, GameHistoryRepository repository) {
        state = new GameState();
        quiet = options.quiet();
        random = new Random(options.seed());
        setupPlayers(options.bots(), options.human());

        if (state.playerCount() < 2 || state.playerCount() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            LOGGER.info("event=session_end status=invalid_player_count players={}", state.playerCount());
            return 0;
        }

        Clock clock = Clock.systemUTC();
        TurnController controller = new TurnController(
                state, random, view, quiet, new BotStrategy(), clock);
        GameSessionController sessionController = new GameSessionController(
                state, controller, view, quiet, clock);
        CompletedGame completedGame = options.usesTargetScore()
                ? sessionController.playToTargetScore(options.targetScore())
                : sessionController.play(options.games());
        repository.save(completedGame);
        LOGGER.info("event=session_end status=completed games={} players={}",
                completedGame.completedRounds(), state.playerCount());
        return 0;
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
