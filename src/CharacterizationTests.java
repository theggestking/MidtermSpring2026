import java.util.ArrayList;
import java.util.Random;

public class CharacterizationTests {
    private static int passed = 0;
    private static final Random TEST_RANDOM = new Random(123);

    private CharacterizationTests() {
    }

    static void run() {
        passed = 0;
        Main.quiet = true;

        testCardRules();
        testInputParser();
        testCardValueObject();
        testBotStrategy();
        testDeckFallback();
        testActionEffects();
        testPenaltyBehavior();
        testScoring();
        testDrawnCardBehavior();

        System.out.println("Passed " + passed + " characterization checks.");
    }

    private static void testCardRules() {
        check(CardRules.color("R5").equals("R"), "color R5");
        check(CardRules.rank("G+2").equals("DRAW_TWO"), "rank +2");
        check(CardRules.points("W4") == 50, "wild points");
        check(CardRules.isLegal("R2", "R9", ""), "same color");
        check(CardRules.isLegal("G9", "R9", ""), "same number");
        check(CardRules.isLegal("B3", "W", "B"), "called color");
        check(!CardRules.isLegal("B3", "R9", ""), "illegal mismatch");
        check(CardRules.isLegal("BS", "RS", ""), "same action skip");
        check(CardRules.isLegal("B+2", "R+2", ""), "same action draw two");
        check(CardRules.isLegal("BR", "RR", ""), "same action reverse");
        check(CardRules.isLegal("W", "R9", ""), "wild legal");
        check(CardRules.isLegal("W4", "R9", ""), "wild draw four legal");
        check(CardRules.points("R7") == 7, "number points");
        check(CardRules.points("RS") == 20, "skip points");
        check(CardRules.points("R+2") == 20, "draw two points");
        check(CardRules.points("W") == 50, "wild points");
    }

    private static void testInputParser() {
        check(InputParser.parseCardIndex("0", 3) == 0, "parse valid index");
        check(InputParser.parseCardIndex("2", 3) == 2, "parse last valid index");
        check(InputParser.parseCardIndex("3", 3) == -1, "parse index out of range");
        check(InputParser.parseCardIndex("R5", 3) == -1, "parse non-index input");
        check(InputParser.parseCardIndex("draw", 3) == -1, "draw input is not a card index");
        check(InputParser.parseCalledColor("r").equals("R"), "parse lowercase called color");
        check(InputParser.parseCalledColor("B").equals("B"), "parse valid called color");
        check(InputParser.parseCalledColor("purple").equals(""), "parse invalid called color");
    }

    private static void testCardValueObject() {
        check(Card.isValid("R5"), "valid number card accepted");
        check(Card.isValid("W4"), "valid wild draw four accepted");
        check(Card.isValid("R10") && CardRules.points("R10") == 10, "two-digit number helper behavior preserved");
        check(!Card.isValid("Q5"), "invalid color rejected");
        check(Card.from("R5").colorValue() == CardColor.RED, "card color enum");
        check(Card.from("W4").rankValue() == CardRank.WILD_DRAW_FOUR, "card rank enum");
    }

    private static void testBotStrategy() {
        ArrayList<Card> h = new ArrayList<Card>();
        h.add(Card.from("B3"));
        h.add(Card.from("R4"));
        h.add(Card.from("W"));
        check(BotStrategy.chooseCard(h, "R9", "") == 1, "bot normal before wild");

        ArrayList<Card> h2 = new ArrayList<Card>();
        h2.add(Card.from("B1"));
        h2.add(Card.from("B2"));
        h2.add(Card.from("R3"));
        check(BotStrategy.chooseColor(h2).equals("B"), "bot color");

        ArrayList<Card> h3 = new ArrayList<Card>();
        h3.add(Card.from("B3"));
        check(BotStrategy.chooseCard(h3, "R9", "") == -1, "bot has no legal card before drawing");

        check(CardRules.isLegal("R4", "R9", ""), "drawn bot card can be legal for auto-play");
    }

    private static void testDeckFallback() {
        GameState state = new GameState();
        state.clearDeck();
        state.clearDiscard();

        check(state.draw(TEST_RANDOM).code().equals("W"), "empty deck fallback");
    }

    private static void testActionEffects() {
        GameState state = threeBotState();
        ActionEffects.apply(Card.from("RS"), state, new TestDrawSupplier(state));
        check(state.currentPlayerIndex() == 2, "skip advances past next player");

        state = threeBotState();
        ActionEffects.apply(Card.from("RR"), state, new TestDrawSupplier(state));
        check(state.direction() == -1 && state.currentPlayerIndex() == 2, "reverse changes direction");

        state = twoPlayerState();
        ActionEffects.apply(Card.from("RR"), state, new TestDrawSupplier(state));
        check(state.currentPlayerIndex() == 0, "two player reverse acts like skip");

        state = threeBotState();
        state.clearDeck();
        state.addToDeck(Card.from("R1"));
        state.addToDeck(Card.from("R2"));
        String drawTwoMessage = ActionEffects.apply(Card.from("R+2"), state, new TestDrawSupplier(state));
        check(state.handSize(1) == 2 && state.currentPlayerIndex() == 2, "draw two gives cards and skips");
        check(drawTwoMessage.equals("Bot2 draws two."), "draw two effect message");

        state = threeBotState();
        state.clearDeck();
        state.addToDeck(Card.from("R1"));
        state.addToDeck(Card.from("R2"));
        state.addToDeck(Card.from("R3"));
        state.addToDeck(Card.from("R4"));
        String wildDrawFourMessage = ActionEffects.apply(Card.from("W4"), state, new TestDrawSupplier(state));
        check(state.handSize(1) == 4 && state.currentPlayerIndex() == 2, "wild draw four gives cards and skips");
        check(wildDrawFourMessage.equals("Bot2 draws four."), "wild draw four effect message");
    }

    private static void testPenaltyBehavior() {
        GameState state = threeBotState();
        state.clearDeck();
        state.addToDeck(Card.from("R8"));
        int beforePenaltySize = state.handSize(0);
        state.addCardToPlayer(0, state.draw(TEST_RANDOM));
        state.nextPlayer();
        check(state.handSize(0) == beforePenaltySize + 1 && state.currentPlayerIndex() == 1,
                "invalid index penalty draws card and loses turn");

        state = threeBotState();
        TurnController controller = new TurnController(state, TEST_RANDOM, new TestGameView(), true);
        state.clearDeck();
        state.addToDeck(Card.from("R8"));
        int invalidIndexBeforeSize = state.currentHandSize();
        boolean invalidIndexEndedGame = controller.resolveChosenCard(5, "Bot1");
        check(!invalidIndexEndedGame && state.handSize(0) == invalidIndexBeforeSize + 1 && state.currentPlayerIndex() == 1,
                "resolve invalid index penalty and turn loss");

        state = threeBotState();
        controller = new TurnController(state, TEST_RANDOM, new TestGameView(), true);
        state.setUpCard(Card.from("R9"));
        state.clearCalledColor();
        state.clearDeck();
        state.addToDeck(Card.from("G1"));
        state.addCardToCurrentPlayer(Card.from("B3"));
        int illegalCardBeforeSize = state.currentHandSize();
        boolean illegalCardEndedGame = controller.resolveChosenCard(0, "Bot1");
        check(!illegalCardEndedGame && state.handSize(0) == illegalCardBeforeSize + 1 && state.currentPlayerIndex() == 1,
                "resolve illegal card penalty and turn loss");
    }

    private static void testScoring() {
        GameState state = threeBotState();
        state.clearHand(0);
        state.clearHand(1);
        state.clearHand(2);
        state.addCardToPlayer(1, Card.from("R5"));
        state.addCardToPlayer(1, Card.from("GS"));
        state.addCardToPlayer(2, Card.from("W"));

        check(ScoreCalculator.scoreForWinner(state.handsSnapshot(), 0) == 75,
                "winner score totals other hands");
    }

    private static void testDrawnCardBehavior() {
        GameState state = threeBotState();
        TurnController controller = new TurnController(state, TEST_RANDOM, new TestGameView(), true);
        state.setUpCard(Card.from("R9"));
        state.clearCalledColor();
        state.clearDeck();
        state.addToDeck(Card.from("R4"));

        int autoPlayedDrawnCard = controller.handleDrawIfNeeded(-1, "Bot1");
        check(autoPlayedDrawnCard == 0 && state.currentHandSize() == 1 && state.cardInCurrentHand(0).code().equals("R4"),
                "bot auto-selects drawn legal card");

        state = threeBotState();
        controller = new TurnController(state, TEST_RANDOM, new TestGameView(), true);
        state.setUpCard(Card.from("R9"));
        state.clearCalledColor();
        state.clearDeck();
        state.addToDeck(Card.from("B3"));

        int unplayableDrawChoice = controller.handleDrawIfNeeded(-1, "Bot1");
        check(unplayableDrawChoice == -1 && state.currentHandSize() == 1 && state.cardInCurrentHand(0).code().equals("B3"),
                "bot keeps drawn illegal card without selecting it");
    }

    private static GameState threeBotState() {
        GameState state = new GameState();
        state.addPlayer("Bot1", false);
        state.addPlayer("Bot2", false);
        state.addPlayer("Bot3", false);
        state.setCurrentPlayer(0);
        state.setDirection(1);
        return state;
    }

    private static GameState twoPlayerState() {
        GameState state = new GameState();
        state.addPlayer("You", true);
        state.addPlayer("Bot1", false);
        state.setCurrentPlayer(0);
        state.setDirection(1);
        return state;
    }

    private static class TestGameView implements GameView {
        public void showGameHeader(int gameNumber) {
        }

        public void showFinalScores(GameState state) {
        }

        public void showTurn(String upCard, String calledColor, String name, java.util.List<Card> hand) {
        }

        public String askMoveInput() {
            return "DRAW";
        }

        public void showIllegalSelection() {
        }

        public void showCardNotFound() {
        }

        public String askColor() {
            return "R";
        }

        public boolean askPlayDrawnCard(String drawn) {
            return false;
        }

        public void showDraw(String name, String drawn) {
        }

        public void showInvalidIndexPenalty(String name) {
        }

        public void showIllegalCardPenalty(String name, String card) {
        }

        public void showPlayedCard(String name, String card) {
        }

        public void showCalledColor(String name, String calledColor) {
        }

        public void showUno(String name) {
        }

        public void showWin(String name, int points) {
        }

        public void showEffectMessage(String message) {
        }

        public void showSafetyLimit() {
        }
    }

    private static class TestDrawSupplier implements java.util.function.Supplier<Card> {
        private final GameState state;

        TestDrawSupplier(GameState state) {
            this.state = state;
        }

        public Card get() {
            return state.draw(TEST_RANDOM);
        }
    }

    private static void check(boolean condition, String name) {
        if (condition) {
            passed++;
        } else {
            fail(name);
        }
    }

    private static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}