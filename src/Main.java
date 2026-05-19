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
                selfTest();
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
            String name = state.playerNames.get(state.currentPlayer);
            ArrayList<String> hand = state.hands.get(state.currentPlayer);

            if (!quiet) {
                System.out.println("\nUp card: " + state.upCard + (state.calledColor.equals("") ? "" : " called " + state.calledColor));
                System.out.println(name + " hand: " + join(hand));
            }

            int chosen = -1;
            if (state.humanPlayers.get(state.currentPlayer).booleanValue()) {
                chosen = askHuman(hand);
            } else {
                chosen = BotStrategy.chooseCard(hand, state.upCard, state.calledColor);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                if (!quiet) {
                    System.out.println(name + " draws " + drawn);
                }
                if (CardRules.isLegal(drawn, state.upCard, state.calledColor)) {
                    if (!state.humanPlayers.get(state.currentPlayer).booleanValue()) {
                        chosen = hand.size() - 1;
                    } else {
                        System.out.print("Play drawn card " + drawn + "? y/n: ");
                        String answer = scanner.nextLine();
                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    if (!quiet) {
                        System.out.println(name + " selected an invalid index and draws a penalty card.");
                    }
                    hand.add(draw());
                    state.nextPlayer();
                    continue;
                }

                String card = hand.get(chosen);
                boolean ok = CardRules.isLegal(card, state.upCard, state.calledColor);

                if (!ok) {
                    if (!quiet) {
                        System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
                    }
                    hand.add(draw());
                    state.nextPlayer();
                    continue;
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
                    return;
                }

                ActionEffects.apply(card, state, Main::draw, quiet);
            } else {
                state.nextPlayer();
            }
        }
        if (!quiet) {
            System.out.println("Game stopped at safety limit.");
        }
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
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R")) {
                return "R";
            }
            if (input.equals("Y")) {
                return "Y";
            }
            if (input.equals("G")) {
                return "G";
            }
            if (input.equals("B")) {
                return "B";
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

    static void selfTest() {
        int passed = 0;
        quiet = true;
        if (CardRules.color("R5").equals("R")) passed++;
        else fail("color R5");
        if (CardRules.rank("G+2").equals("DRAW_TWO")) passed++;
        else fail("rank +2");
        if (CardRules.points("W4") == 50) passed++;
        else fail("wild points");
        if (CardRules.isLegal("R2", "R9", "")) passed++;
        else fail("same color");
        if (CardRules.isLegal("G9", "R9", "")) passed++;
        else fail("same number");
        if (CardRules.isLegal("B3", "W", "B")) passed++;
        else fail("called color");
        if (!CardRules.isLegal("B3", "R9", "")) passed++;
        else fail("illegal mismatch");
        if (parseCardIndex("0", 3) == 0) passed++;
        else fail("parse valid index");
        if (parseCardIndex("2", 3) == 2) passed++;
        else fail("parse last valid index");
        if (parseCardIndex("3", 3) == -1) passed++;
        else fail("parse index out of range");
        if (parseCardIndex("R5", 3) == -1) passed++;
        else fail("parse non-index input");

        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        state.upCard = "R9";
        state.calledColor = "";
        if (BotStrategy.chooseCard(h, state.upCard, state.calledColor) == 1) passed++;
        else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (BotStrategy.chooseColor(h2).equals("B")) passed++;
        else fail("bot color");

        if (CardRules.isLegal("BS", "RS", "")) passed++;
        else fail("same action skip");
        if (CardRules.isLegal("B+2", "R+2", "")) passed++;
        else fail("same action draw two");
        if (CardRules.isLegal("BR", "RR", "")) passed++;
        else fail("same action reverse");
        if (CardRules.isLegal("W", "R9", "")) passed++;
        else fail("wild legal");
        if (CardRules.isLegal("W4", "R9", "")) passed++;
        else fail("wild draw four legal");
        if (CardRules.points("R7") == 7) passed++;
        else fail("number points");
        if (CardRules.points("RS") == 20) passed++;
        else fail("skip points");
        if (CardRules.points("R+2") == 20) passed++;
        else fail("draw two points");
        if (CardRules.points("W") == 50) passed++;
        else fail("wild points");
        state.deck.clear();
        state.discard.clear();
        if (draw().equals("W")) passed++;
        else fail("empty deck fallback");

        setupPlayers(3, false);
        state.currentPlayer = 0;
        state.direction = 1;
        ActionEffects.apply("RS", state, Main::draw, quiet);
        if (state.currentPlayer == 2) passed++;
        else fail("skip advances past next player");

        setupPlayers(3, false);
        state.currentPlayer = 0;
        state.direction = 1;
        ActionEffects.apply("RR", state, Main::draw, quiet);
        if (state.direction == -1 && state.currentPlayer == 2) passed++;
        else fail("reverse changes direction");

        setupPlayers(1, true);
        state.currentPlayer = 0;
        state.direction = 1;
        ActionEffects.apply("RR", state, Main::draw, quiet);
        if (state.currentPlayer == 0) passed++;
        else fail("two player reverse acts like skip");

        setupPlayers(3, false);
        state.currentPlayer = 0;
        state.direction = 1;
        state.deck.clear();
        state.deck.add("R1");
        state.deck.add("R2");
        ActionEffects.apply("R+2", state, Main::draw, quiet);
        if (state.hands.get(1).size() == 2 && state.currentPlayer == 2) passed++;
        else fail("draw two gives cards and skips");

        setupPlayers(3, false);
        state.currentPlayer = 0;
        state.direction = 1;
        state.deck.clear();
        state.deck.add("R1");
        state.deck.add("R2");
        state.deck.add("R3");
        state.deck.add("R4");
        ActionEffects.apply("W4", state, Main::draw, quiet);
        if (state.hands.get(1).size() == 4 && state.currentPlayer == 2) passed++;
        else fail("wild draw four gives cards and skips");

        setupPlayers(3, false);
        state.hands.get(0).clear();
        state.hands.get(1).clear();
        state.hands.get(2).clear();
        state.hands.get(1).add("R5");
        state.hands.get(1).add("GS");
        state.hands.get(2).add("W");
        if (ScoreCalculator.scoreForWinner(state.hands, 0) == 75) passed++;
        else fail("winner score totals other hands");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}