record CliOptions(
        CliMode mode,
        int bots,
        int games,
        boolean human,
        boolean quiet,
        long seed,
        int targetScore,
        int reportLimit,
        String playerName) {

    private static final int DEFAULT_REPORT_LIMIT = 10;

    static CliOptions parse(String[] args) {
        return parse(args, System.currentTimeMillis());
    }

    static CliOptions parse(String[] args, long defaultSeed) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        boolean quiet = false;
        long seed = defaultSeed;
        int targetScore = 0;
        CliMode mode = CliMode.GAMEPLAY;
        int reportLimit = DEFAULT_REPORT_LIMIT;
        String playerName = null;
        boolean gamesArgumentSeen = false;
        boolean targetScoreArgumentSeen = false;
        boolean gameplayArgumentSeen = false;
        boolean reportArgumentSeen = false;

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            switch (argument) {
                case "--bots" -> {
                    bots = parseInteger(requiredValue(args, ++index, argument), argument);
                    gameplayArgumentSeen = true;
                }
                case "--games" -> {
                    games = parseInteger(requiredValue(args, ++index, argument), argument);
                    gamesArgumentSeen = true;
                    gameplayArgumentSeen = true;
                }
                case "--human" -> {
                    human = true;
                    gameplayArgumentSeen = true;
                }
                case "--quiet" -> {
                    quiet = true;
                    gameplayArgumentSeen = true;
                }
                case "--seed" -> {
                    seed = parseLong(requiredValue(args, ++index, argument), argument);
                    gameplayArgumentSeen = true;
                }
                case "--target-score" -> {
                    targetScore = parseInteger(requiredValue(args, ++index, argument), argument);
                    targetScoreArgumentSeen = true;
                    gameplayArgumentSeen = true;
                }
                case "--recent-games" -> {
                    ensureSingleReportMode(reportArgumentSeen);
                    mode = CliMode.RECENT_GAMES;
                    reportArgumentSeen = true;
                    if (hasOptionalValue(args, index + 1)) {
                        reportLimit = parseInteger(args[++index], argument);
                    }
                }
                case "--player-wins" -> {
                    ensureSingleReportMode(reportArgumentSeen);
                    mode = CliMode.PLAYER_WINS;
                    reportArgumentSeen = true;
                    playerName = requiredValue(args, ++index, argument);
                }
                case "--highest-scores" -> {
                    ensureSingleReportMode(reportArgumentSeen);
                    mode = CliMode.HIGHEST_SCORES;
                    reportArgumentSeen = true;
                    if (hasOptionalValue(args, index + 1)) {
                        reportLimit = parseInteger(args[++index], argument);
                    }
                }
                case "--help" -> {
                    if (args.length != 1) {
                        throw new IllegalArgumentException("--help cannot be combined with other arguments");
                    }
                    mode = CliMode.HELP;
                }
                default -> throw new IllegalArgumentException("Unknown argument: " + argument);
            }
        }

        if (reportArgumentSeen && gameplayArgumentSeen) {
            throw new IllegalArgumentException("Report modes cannot be combined with gameplay arguments");
        }
        if (mode == CliMode.GAMEPLAY && games < 1) {
            throw new IllegalArgumentException("--games must be at least 1");
        }
        if (targetScoreArgumentSeen && targetScore < 1) {
            throw new IllegalArgumentException("--target-score must be at least 1");
        }
        if (targetScore > 0 && gamesArgumentSeen) {
            throw new IllegalArgumentException("--target-score cannot be combined with --games");
        }
        if (mode == CliMode.RECENT_GAMES || mode == CliMode.HIGHEST_SCORES) {
            validateReportLimit(reportLimit);
        }

        return new CliOptions(
                mode, bots, games, human, quiet, seed, targetScore, reportLimit, playerName);
    }

    static String usage() {
        return """
                Usage:
                  java -jar target/uno-cli.jar [--bots N] [--games N | --target-score N] [--human] [--quiet] [--seed N]
                  java -jar target/uno-cli.jar --recent-games [1-100]
                  java -jar target/uno-cli.jar --player-wins NAME
                  java -jar target/uno-cli.jar --highest-scores [1-100]
                """.stripTrailing();
    }

    boolean usesTargetScore() {
        return targetScore > 0;
    }

    private static String requiredValue(String[] args, int index, String argument) {
        if (index >= args.length || args[index].startsWith("--")) {
            throw new IllegalArgumentException("Missing value for " + argument);
        }
        return args[index];
    }

    private static boolean hasOptionalValue(String[] args, int index) {
        return index < args.length && !args[index].startsWith("--");
    }

    private static int parseInteger(String value, String argument) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer for " + argument + ": " + value);
        }
    }

    private static long parseLong(String value, String argument) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid number for " + argument + ": " + value);
        }
    }

    private static void ensureSingleReportMode(boolean reportArgumentSeen) {
        if (reportArgumentSeen) {
            throw new IllegalArgumentException("Only one report mode may be used at a time");
        }
    }

    private static void validateReportLimit(int reportLimit) {
        if (reportLimit < 1 || reportLimit > 100) {
            throw new IllegalArgumentException("Report limit must be between 1 and 100");
        }
    }
}
