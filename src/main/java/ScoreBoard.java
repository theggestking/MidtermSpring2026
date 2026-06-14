public class ScoreBoard {
    private int[] scores = new int[10];

    int scoreForPlayer(int player) {
        return scores[player];
    }

    void addScoreToPlayer(int player, int points) {
        scores[player] += points;
    }
}
