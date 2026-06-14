import java.util.Objects;

record FinalPlayerScore(String playerName, int seatNumber, int finalScore, boolean winner) {
    FinalPlayerScore {
        Objects.requireNonNull(playerName, "playerName");
        if (playerName.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
        if (seatNumber < 0 || finalScore < 0) {
            throw new IllegalArgumentException("Seat and score cannot be negative");
        }
    }
}
