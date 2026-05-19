import java.util.ArrayList;

public class GameState {
    ArrayList<String> playerNames = new ArrayList<String>();
    ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
    ArrayList<String> deck = new ArrayList<String>();
    ArrayList<String> discard = new ArrayList<String>();
    int[] scores = new int[10];
    int currentPlayer = 0;
    int direction = 1;
    String upCard = "";
    String calledColor = "";
}