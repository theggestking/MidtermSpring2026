import java.time.Instant;

record HighScoreReport(
        long gameId,
        String playerName,
        int finalScore,
        Instant completedAt,
        boolean winner) {
}
