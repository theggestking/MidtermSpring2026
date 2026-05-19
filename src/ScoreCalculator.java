import java.util.ArrayList;

public class ScoreCalculator {
    private ScoreCalculator() {
    }

    static int scoreForWinner(ArrayList<ArrayList<String>> hands, int winner) {
        int total = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i != winner) {
                for (int j = 0; j < hands.get(i).size(); j++) {
                    total += CardRules.points(hands.get(i).get(j));
                }
            }
        }
        return total;
    }
}