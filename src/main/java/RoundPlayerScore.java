import java.util.Objects;

record RoundPlayerScore(String playerName, int scoreBefore, int scoreDelta, int scoreAfter) {
    RoundPlayerScore {
        Objects.requireNonNull(playerName, "playerName");
        if (playerName.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
        if (scoreBefore < 0 || scoreDelta < 0 || scoreAfter < 0) {
            throw new IllegalArgumentException("Scores cannot be negative");
        }
        if (scoreBefore + scoreDelta != scoreAfter) {
            throw new IllegalArgumentException("Score transition is inconsistent");
        }
    }
}
