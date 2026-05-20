import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static GameState state = new GameState();
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);
    static ConsoleView view = new ConsoleView(scanner);

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
                view.showGameHeader(g);
            }
            playGame();
        }

        view.showFinalScores(state);
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
            view.showSafetyLimit();
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
            view.showTurn(state.upCard, state.calledColor, name, hand);
        }
    }

    static int chooseMove(ArrayList<String> hand) {
        if (state.humanPlayers.get(state.currentPlayer).booleanValue()) {
            return view.askHuman(hand, state.upCard, state.calledColor);
        }
        return BotStrategy.chooseCard(hand, state.upCard, state.calledColor);
    }

    static int handleDrawIfNeeded(int chosen, ArrayList<String> hand, String name) {
        if (chosen != -1) {
            return chosen;
        }

        String drawn = state.draw(random);
        hand.add(drawn);

        if (!quiet) {
            view.showDraw(name, drawn);
        }

        if (CardRules.isLegal(drawn, state.upCard, state.calledColor)) {
            if (!state.humanPlayers.get(state.currentPlayer).booleanValue()) {
                return hand.size() - 1;
            }

            if (view.askPlayDrawnCard(drawn)) {
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
                view.showInvalidIndexPenalty(name);
            }
            hand.add(state.draw(random));
            state.nextPlayer();
            return false;
        }

        String card = hand.get(chosen);
        boolean ok = CardRules.isLegal(card, state.upCard, state.calledColor);

        if (!ok) {
            if (!quiet) {
                view.showIllegalCardPenalty(name, card);
            }
            hand.add(state.draw(random));
            state.nextPlayer();
            return false;
        }

        hand.remove(chosen);
        state.discard.add(state.upCard);
        state.upCard = card;
        state.calledColor = "";

        if (!quiet) {
            view.showPlayedCard(name, card);
        }

        if (card.equals("W") || card.equals("W4")) {
            if (state.humanPlayers.get(state.currentPlayer).booleanValue()) {
                state.calledColor = view.askColor();
            } else {
                state.calledColor = BotStrategy.chooseColor(hand);
            }
            if (!quiet) {
                view.showCalledColor(name, state.calledColor);
            }
        }

        if (hand.size() == 1 && !quiet) {
            view.showUno(name);
        }

        if (hand.size() == 0) {
            int points = ScoreCalculator.scoreForWinner(state.hands, state.currentPlayer);
            state.scores[state.currentPlayer] += points;
            if (!quiet) {
                view.showWin(name, points);
            }
            return true;
        }

        String effectMessage = ActionEffects.apply(card, state, () -> state.draw(random));
        if (!quiet && !effectMessage.equals("")) {
            view.showEffectMessage(effectMessage);
        }

        return false;
    }

    static void startNewGame() {
        state.buildDeck(random);
        state.discard.clear();
        for (int i = 0; i < state.hands.size(); i++) {
            state.hands.get(i).clear();
        }
        for (int i = 0; i < state.playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                state.hands.get(i).add(state.draw(random));
            }
        }
        state.upCard = state.draw(random);
        while (state.upCard.startsWith("W")) {
            state.discard.add(state.upCard);
            state.upCard = state.draw(random);
        }
        state.calledColor = "";
        state.direction = 1;
        state.currentPlayer = random.nextInt(state.playerNames.size());
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
}