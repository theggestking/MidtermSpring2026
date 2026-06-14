import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

final class HistoryReportView {
    private final PrintStream output;

    HistoryReportView(PrintStream output) {
        this.output = output;
    }

    void showRecentGames(List<RecentGameReport> games) {
        output.println("Recent games:");
        if (games.isEmpty()) {
            output.println("No games found.");
            return;
        }

        for (RecentGameReport game : games) {
            String winners = game.finalScores().stream()
                    .filter(FinalPlayerScore::winner)
                    .map(FinalPlayerScore::playerName)
                    .collect(Collectors.joining(", "));
            if (winners.isEmpty()) {
                winners = "none";
            }
            String scores = game.finalScores().stream()
                    .map(score -> score.playerName() + "=" + score.finalScore())
                    .collect(Collectors.joining(", "));
            output.printf(
                    "Game %d | completed=%s | rounds=%d/%d | winners=%s | scores=%s%n",
                    game.gameId(),
                    game.completedAt(),
                    game.completedRounds(),
                    game.requestedRounds(),
                    winners,
                    scores);
        }
    }

    void showPlayerWins(String playerName, long wins) {
        output.printf("Wins for %s: %d%n", playerName, wins);
    }

    void showHighestScores(List<HighScoreReport> scores) {
        output.println("Highest scores:");
        if (scores.isEmpty()) {
            output.println("No scores found.");
            return;
        }

        for (HighScoreReport score : scores) {
            output.printf(
                    "%s: %d | game=%d | completed=%s | winner=%s%n",
                    score.playerName(),
                    score.finalScore(),
                    score.gameId(),
                    score.completedAt(),
                    score.winner() ? "yes" : "no");
        }
    }
}
