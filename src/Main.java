import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static GameState state = new GameState();
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--self-test")) {
                CharacterizationTests.run();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (state.playerNames.size() < 2 || state.playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        for (int g = 1; g <= games; g++) {
            if (!quiet) {
                System.out.println("\n=== Game " + g + " ===");
            }
            playGame();
        }

        System.out.println("\nFinal scores:");
        for (int i = 0; i < state.playerNames.size(); i++) {
            System.out.println(state.playerNames.get(i) + ": " + state.scores[i]);
        }
    }

    static void setupPlayers(int bots, boolean human) {
        state.playerNames.clear();
        state.humanPlayers.clear();
        state.hands.clear();
        if (human) {
            state.playerNames.add("You");
            state.humanPlayers.add(Boolean.TRUE);
            state.hands.add(new ArrayList<String>());
        }
        for (int i = 1; i <= bots; i++) {
            state.playerNames.add("Bot" + i);
            state.humanPlayers.add(Boolean.FALSE);
            state.hands.add(new ArrayList<String>());
        }
    }

    static void playGame() {
        startNewGame();

        int guard = 0;
        while (guard < 3000) {
            guard++;
            if (takeTurn()) {
                return;
            }
        }

        if (!quiet) {
            System.out.println("Game stopped at safety limit.");
        }
    }

    static boolean takeTurn() {
        String name = state.playerNames.get(state.currentPlayer);
        ArrayList<String> hand = state.hands.get(state.currentPlayer);

        renderTurn(name, hand);
        int chosen = chooseMove(hand);
        chosen = handleDrawIfNeeded(chosen, hand, name);
        return resolveChosenCard(chosen, hand, name);
    }

    static void renderTurn(String name, ArrayList<String> hand) {
        if (!quiet) {
            System.out.println("\nUp card: " + state.upCard + (state.calledColor.equals("") ? "" : " called " + state.calledColor));
            System.out.println(name + " hand: " + join(hand));
        }
    }

    static int chooseMove(ArrayList<String> hand) {
        if (state.humanPlayers.get(state.currentPlayer).booleanValue()) {
            return askHuman(hand);
        }
        return BotStrategy.chooseCard(hand, state.upCard, state.calledColor);
    }

    static int handleDrawIfNeeded(int chosen, ArrayList<String> hand, String name) {
        if (chosen != -1) {
            return chosen;
        }

        String drawn = draw();
        hand.add(drawn);

        if (!quiet) {
            System.out.println(name + " draws " + drawn);
        }

        if (CardRules.isLegal(drawn, state.upCard, state.calledColor)) {
            if (!state.humanPlayers.get(state.currentPlayer).booleanValue()) {
                return hand.size() - 1;
            }

            System.out.print("Play drawn card " + drawn + "? y/n: ");
            String answer = scanner.nextLine();
            if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                return hand.size() - 1;
            }
        }

        return chosen;
    }

    static boolean resolveChosenCard(int chosen, ArrayList<String> hand, String name) {
        if (chosen < 0) {
            state.nextPlayer();
            return false;
        }

        if (chosen >= hand.size()) {
            if (!quiet) {
                System.out.println(name + " selected an invalid index and draws a penalty card.");
            }
            hand.add(draw());
            state.nextPlayer();
            return false;
        }

        String card = hand.get(chosen);
        boolean ok = CardRules.isLegal(card, state.upCard, state.calledColor);

        if (!ok) {
            if (!quiet) {
                System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
            }
            hand.add(draw());
            state.nextPlayer();
            return false;
        }

        hand.remove(chosen);
        state.discard.add(state.upCard);
        state.upCard = card;
        state.calledColor = "";

        if (!quiet) {
            System.out.println(name + " plays " + card);
        }

        if (card.equals("W") || card.equals("W4")) {
            if (state.humanPlayers.get(state.currentPlayer).booleanValue()) {
                state.calledColor = askColor();
            } else {
                state.calledColor = BotStrategy.chooseColor(hand);
            }
            if (!quiet) {
                System.out.println(name + " calls " + state.calledColor);
            }
        }

        if (hand.size() == 1 && !quiet) {
            System.out.println(name + " says UNO!");
        }

        if (hand.size() == 0) {
            int points = ScoreCalculator.scoreForWinner(state.hands, state.currentPlayer);
            state.scores[state.currentPlayer] += points;
            if (!quiet) {
                System.out.println(name + " wins and scores " + points);
            }
            return true;
        }

        String effectMessage = ActionEffects.apply(card, state, Main::draw);
        if (!quiet && !effectMessage.equals("")) {
            System.out.println(effectMessage);
        }

        return false;
    }

    static void startNewGame() {
        buildDeck();
        state.discard.clear();
        for (int i = 0; i < state.hands.size(); i++) {
            state.hands.get(i).clear();
        }
        for (int i = 0; i < state.playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                state.hands.get(i).add(draw());
            }
        }
        state.upCard = draw();
        while (state.upCard.startsWith("W")) {
            state.discard.add(state.upCard);
            state.upCard = draw();
        }
        state.calledColor = "";
        state.direction = 1;
        state.currentPlayer = random.nextInt(state.playerNames.size());
    }

    static void buildDeck() {
        state.deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (int c = 0; c < colors.length; c++) {
            state.deck.add(colors[c] + "0");
            for (int n = 1; n <= 9; n++) {
                state.deck.add(colors[c] + n);
                state.deck.add(colors[c] + n);
            }
            state.deck.add(colors[c] + "S");
            state.deck.add(colors[c] + "S");
            state.deck.add(colors[c] + "R");
            state.deck.add(colors[c] + "R");
            state.deck.add(colors[c] + "+2");
            state.deck.add(colors[c] + "+2");
        }
        for (int i = 0; i < 4; i++) {
            state.deck.add("W");
            state.deck.add("W4");
        }
        Collections.shuffle(state.deck, random);
    }

    static String draw() {
        if (state.deck.size() == 0) {
            state.deck.addAll(state.discard);
            state.discard.clear();
            Collections.shuffle(state.deck, random);
        }
        if (state.deck.size() == 0) {
            return "W";
        }
        return state.deck.remove(0);
    }

    static int parseCardIndex(String input, int handSize) {
        try {
            int index = Integer.parseInt(input);
            if (index >= 0 && index < handSize) {
                return index;
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    static String parseCalledColor(String input) {
        String color = input.trim().toUpperCase();
        if (color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B")) {
            return color;
        }
        return "";
    }

    static int askHuman(ArrayList<String> hand) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) {
                return -1;
            }
            int index = parseCardIndex(input, hand.size());
            if (index != -1) {
                return index;
            }
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (CardRules.isLegal(hand.get(i), state.upCard, state.calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }
            System.out.println("Card not found.");
        }
    }

    static String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String parsedColor = parseCalledColor(scanner.nextLine());
            if (!parsedColor.equals("")) {
                return parsedColor;
            }
            System.out.println("Bad color.");
        }
    }

    static String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) {
                out += " ";
            }
        }
        return out;
    }
}