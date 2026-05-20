import java.util.ArrayList;

public class CharacterizationTests {
    private CharacterizationTests() {
    }

    static void run() {
        int passed = 0;
        Main.quiet = true;

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

        if (InputParser.parseCardIndex("0", 3) == 0) passed++;
        else fail("parse valid index");
        if (InputParser.parseCardIndex("2", 3) == 2) passed++;
        else fail("parse last valid index");
        if (InputParser.parseCardIndex("3", 3) == -1) passed++;
        else fail("parse index out of range");
        if (InputParser.parseCardIndex("R5", 3) == -1) passed++;
        else fail("parse non-index input");
        if (InputParser.parseCardIndex("draw", 3) == -1) passed++;
        else fail("draw input is not a card index");
        if (InputParser.parseCalledColor("r").equals("R")) passed++;
        else fail("parse lowercase called color");
        if (InputParser.parseCalledColor("B").equals("B")) passed++;
        else fail("parse valid called color");
        if (InputParser.parseCalledColor("purple").equals("")) passed++;
        else fail("parse invalid called color");

        if (Card.isValid("R5")) passed++;
        else fail("valid number card accepted");
        if (Card.isValid("W4")) passed++;
        else fail("valid wild draw four accepted");
        if (Card.isValid("R10") && CardRules.points("R10") == 10) passed++;
        else fail("two-digit number helper behavior preserved");
        if (!Card.isValid("Q5")) passed++;
        else fail("invalid color rejected");

        ArrayList<Card> h = new ArrayList<Card>();
        h.add(Card.from("B3"));
        h.add(Card.from("R4"));
        h.add(Card.from("W"));
        Main.state.setUpCard(Card.from("R9"));
        Main.state.clearCalledColor();
        if (BotStrategy.chooseCard(h, Main.state.upCardCode(), Main.state.calledColor()) == 1) passed++;
        else fail("bot normal before wild");

        ArrayList<Card> h2 = new ArrayList<Card>();
        h2.add(Card.from("B1"));
        h2.add(Card.from("B2"));
        h2.add(Card.from("R3"));
        if (BotStrategy.chooseColor(h2).equals("B")) passed++;
        else fail("bot color");

        ArrayList<Card> h3 = new ArrayList<Card>();
        h3.add(Card.from("B3"));
        Main.state.setUpCard(Card.from("R9"));
        Main.state.clearCalledColor();
        if (BotStrategy.chooseCard(h3, Main.state.upCardCode(), Main.state.calledColor()) == -1) passed++;
        else fail("bot has no legal card before drawing");

        String drawnLegalCard = "R4";
        if (CardRules.isLegal(drawnLegalCard, Main.state.upCardCode(), Main.state.calledColor())) passed++;
        else fail("drawn bot card can be legal for auto-play");

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

        TurnController testController = new TurnController(Main.state, Main.random, Main.view, true);

        Main.state.clearDeck();
        Main.state.clearDiscard();
        if (Main.state.draw(Main.random).code().equals("W")) passed++;
        else fail("empty deck fallback");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        ActionEffects.apply(Card.from("RS"), Main.state, () -> Main.state.draw(Main.random));
        if (Main.state.currentPlayerIndex() == 2) passed++;
        else fail("skip advances past next player");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        ActionEffects.apply(Card.from("RR"), Main.state, () -> Main.state.draw(Main.random));
        if (Main.state.direction() == -1 && Main.state.currentPlayerIndex() == 2) passed++;
        else fail("reverse changes direction");

        Main.setupPlayers(1, true);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        ActionEffects.apply(Card.from("RR"), Main.state, () -> Main.state.draw(Main.random));
        if (Main.state.currentPlayerIndex() == 0) passed++;
        else fail("two player reverse acts like skip");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        Main.state.clearDeck();
        Main.state.addToDeck(Card.from("R1"));
        Main.state.addToDeck(Card.from("R2"));
        String drawTwoMessage = ActionEffects.apply(Card.from("R+2"), Main.state, () -> Main.state.draw(Main.random));
        if (Main.state.handSize(1) == 2 && Main.state.currentPlayerIndex() == 2) passed++;
        else fail("draw two gives cards and skips");
        if (drawTwoMessage.equals("Bot2 draws two.")) passed++;
        else fail("draw two effect message");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        Main.state.clearDeck();
        Main.state.addToDeck(Card.from("R1"));
        Main.state.addToDeck(Card.from("R2"));
        Main.state.addToDeck(Card.from("R3"));
        Main.state.addToDeck(Card.from("R4"));
        String wildDrawFourMessage = ActionEffects.apply(Card.from("W4"), Main.state, () -> Main.state.draw(Main.random));
        if (Main.state.handSize(1) == 4 && Main.state.currentPlayerIndex() == 2) passed++;
        else fail("wild draw four gives cards and skips");
        if (wildDrawFourMessage.equals("Bot2 draws four.")) passed++;
        else fail("wild draw four effect message");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        Main.state.clearDeck();
        Main.state.addToDeck(Card.from("R8"));
        int beforePenaltySize = Main.state.handSize(0);
        Main.state.addCardToPlayer(0, Main.state.draw(Main.random));
        Main.state.nextPlayer();
        if (Main.state.handSize(0) == beforePenaltySize + 1 && Main.state.currentPlayerIndex() == 1) passed++;
        else fail("invalid index penalty draws card and loses turn");

        Main.setupPlayers(3, false);
        Main.state.clearHand(0);
        Main.state.clearHand(1);
        Main.state.clearHand(2);
        Main.state.addCardToPlayer(1, Card.from("R5"));
        Main.state.addCardToPlayer(1, Card.from("GS"));
        Main.state.addCardToPlayer(2, Card.from("W"));
        if (ScoreCalculator.scoreForWinner(Main.state.handsSnapshot(), 0) == 75) passed++;
        else fail("winner score totals other hands");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        Main.state.setUpCard(Card.from("R9"));
        Main.state.clearCalledColor();
        Main.state.clearDeck();
        Main.state.addToDeck(Card.from("R4"));
        int autoPlayedDrawnCard = testController.handleDrawIfNeeded(-1, "Bot1");
        if (autoPlayedDrawnCard == 0 && Main.state.currentHandSize() == 1 && Main.state.cardInCurrentHand(0).code().equals("R4")) passed++;
        else fail("bot auto-selects drawn legal card");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        Main.state.setUpCard(Card.from("R9"));
        Main.state.clearCalledColor();
        Main.state.clearDeck();
        Main.state.addToDeck(Card.from("B3"));
        int unplayableDrawChoice = testController.handleDrawIfNeeded(-1, "Bot1");
        if (unplayableDrawChoice == -1 && Main.state.currentHandSize() == 1 && Main.state.cardInCurrentHand(0).code().equals("B3"))
            passed++;
        else fail("bot keeps drawn illegal card without selecting it");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        Main.state.clearDeck();
        Main.state.addToDeck(Card.from("R8"));
        int invalidIndexBeforeSize = Main.state.currentHandSize();
        boolean invalidIndexEndedGame = testController.resolveChosenCard(5, "Bot1");
        if (!invalidIndexEndedGame && Main.state.handSize(0) == invalidIndexBeforeSize + 1 && Main.state.currentPlayerIndex() == 1)
            passed++;
        else fail("resolve invalid index penalty and turn loss");

        Main.setupPlayers(3, false);
        Main.state.setCurrentPlayer(0);
        Main.state.setDirection(1);
        Main.state.setUpCard(Card.from("R9"));
        Main.state.clearCalledColor();
        Main.state.clearDeck();
        Main.state.addToDeck(Card.from("G1"));
        Main.state.addCardToCurrentPlayer(Card.from("B3"));
        int illegalCardBeforeSize = Main.state.currentHandSize();
        boolean illegalCardEndedGame = testController.resolveChosenCard(0, "Bot1");
        if (!illegalCardEndedGame && Main.state.handSize(0) == illegalCardBeforeSize + 1 && Main.state.currentPlayerIndex() == 1)
            passed++;
        else fail("resolve illegal card penalty and turn loss");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}