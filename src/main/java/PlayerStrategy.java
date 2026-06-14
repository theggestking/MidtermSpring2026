import java.util.ArrayList;

public interface PlayerStrategy {
    int chooseCard(ArrayList<Card> hand, String upCard, String calledColor);

    String chooseColor(ArrayList<Card> hand);
}
