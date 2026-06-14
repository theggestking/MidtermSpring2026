final class GameHistoryReportController {
    private final GameHistoryRepository repository;
    private final HistoryReportView view;

    GameHistoryReportController(GameHistoryRepository repository, HistoryReportView view) {
        this.repository = repository;
        this.view = view;
    }

    void show(CliOptions options) {
        switch (options.mode()) {
            case RECENT_GAMES -> view.showRecentGames(repository.recentGames(options.reportLimit()));
            case PLAYER_WINS -> view.showPlayerWins(
                    options.playerName(), repository.playerWinCount(options.playerName()));
            case HIGHEST_SCORES -> view.showHighestScores(
                    repository.highestScores(options.reportLimit()));
            default -> throw new IllegalArgumentException("Not a report mode: " + options.mode());
        }
    }
}
