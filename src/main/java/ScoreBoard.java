import java.util.ArrayList;
import java.util.List;

public class ScoreBoard {
    private int[] scores = new int[10];

    int scoreForPlayer(int player) {
        return scores[player];
    }

    void addScoreToPlayer(int player, int points) {
        scores[player] += points;
    }

    List<Integer> scoresSnapshot(int playerCount) {
        List<Integer> snapshot = new ArrayList<>(playerCount);
        for (int player = 0; player < playerCount; player++) {
            snapshot.add(scores[player]);
        }
        return List.copyOf(snapshot);
    }
}
