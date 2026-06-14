import java.util.Random;

public class TurnState {
    private int currentPlayer = 0;
    private int direction = 1;
    private String calledColor = "";

    int currentPlayerIndex() {
        return currentPlayer;
    }

    void setCurrentPlayer(int player) {
        currentPlayer = player;
    }

    int direction() {
        return direction;
    }

    void setDirection(int newDirection) {
        direction = newDirection;
    }

    void resetDirection() {
        direction = 1;
    }

    void chooseRandomCurrentPlayer(Random random, int playerCount) {
        currentPlayer = random.nextInt(playerCount);
    }

    void nextPlayer(int playerCount) {
        currentPlayer += direction;
        if (currentPlayer >= playerCount) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerCount - 1;
        }
    }

    String calledColor() {
        return calledColor;
    }

    void setCalledColor(String color) {
        calledColor = color;
    }

    void clearCalledColor() {
        calledColor = "";
    }
}
