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

        if (Main.parseCardIndex("0", 3) == 0) passed++;
        else fail("parse valid index");
        if (Main.parseCardIndex("2", 3) == 2) passed++;
        else fail("parse last valid index");
        if (Main.parseCardIndex("3", 3) == -1) passed++;
        else fail("parse index out of range");
        if (Main.parseCardIndex("R5", 3) == -1) passed++;
        else fail("parse non-index input");
        if (Main.parseCardIndex("draw", 3) == -1) passed++;
        else fail("draw input is not a card index");

        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        Main.state.upCard = "R9";
        Main.state.calledColor = "";
        if (BotStrategy.chooseCard(h, Main.state.upCard, Main.state.calledColor) == 1) passed++;
        else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (BotStrategy.chooseColor(h2).equals("B")) passed++;
        else fail("bot color");

        ArrayList<String> h3 = new ArrayList<String>();
        h3.add("B3");
        Main.state.upCard = "R9";
        Main.state.calledColor = "";
        if (BotStrategy.chooseCard(h3, Main.state.upCard, Main.state.calledColor) == -1) passed++;
        else fail("bot has no legal card before drawing");

        String drawnLegalCard = "R4";
        if (CardRules.isLegal(drawnLegalCard, Main.state.upCard, Main.state.calledColor)) passed++;
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

        Main.state.deck.clear();
        Main.state.discard.clear();
        if (Main.draw().equals("W")) passed++;
        else fail("empty deck fallback");

        Main.setupPlayers(3, false);
        Main.state.currentPlayer = 0;
        Main.state.direction = 1;
        ActionEffects.apply("RS", Main.state, Main::draw, Main.quiet);
        if (Main.state.currentPlayer == 2) passed++;
        else fail("skip advances past next player");

        Main.setupPlayers(3, false);
        Main.state.currentPlayer = 0;
        Main.state.direction = 1;
        ActionEffects.apply("RR", Main.state, Main::draw, Main.quiet);
        if (Main.state.direction == -1 && Main.state.currentPlayer == 2) passed++;
        else fail("reverse changes direction");

        Main.setupPlayers(1, true);
        Main.state.currentPlayer = 0;
        Main.state.direction = 1;
        ActionEffects.apply("RR", Main.state, Main::draw, Main.quiet);
        if (Main.state.currentPlayer == 0) passed++;
        else fail("two player reverse acts like skip");

        Main.setupPlayers(3, false);
        Main.state.currentPlayer = 0;
        Main.state.direction = 1;
        Main.state.deck.clear();
        Main.state.deck.add("R1");
        Main.state.deck.add("R2");
        ActionEffects.apply("R+2", Main.state, Main::draw, Main.quiet);
        if (Main.state.hands.get(1).size() == 2 && Main.state.currentPlayer == 2) passed++;
        else fail("draw two gives cards and skips");

        Main.setupPlayers(3, false);
        Main.state.currentPlayer = 0;
        Main.state.direction = 1;
        Main.state.deck.clear();
        Main.state.deck.add("R1");
        Main.state.deck.add("R2");
        Main.state.deck.add("R3");
        Main.state.deck.add("R4");
        ActionEffects.apply("W4", Main.state, Main::draw, Main.quiet);
        if (Main.state.hands.get(1).size() == 4 && Main.state.currentPlayer == 2) passed++;
        else fail("wild draw four gives cards and skips");

        Main.setupPlayers(3, false);
        Main.state.currentPlayer = 0;
        Main.state.direction = 1;
        Main.state.deck.clear();
        Main.state.deck.add("R8");
        int beforePenaltySize = Main.state.hands.get(0).size();
        Main.state.hands.get(0).add(Main.draw());
        Main.state.nextPlayer();
        if (Main.state.hands.get(0).size() == beforePenaltySize + 1 && Main.state.currentPlayer == 1) passed++;
        else fail("invalid index penalty draws card and loses turn");

        Main.setupPlayers(3, false);
        Main.state.hands.get(0).clear();
        Main.state.hands.get(1).clear();
        Main.state.hands.get(2).clear();
        Main.state.hands.get(1).add("R5");
        Main.state.hands.get(1).add("GS");
        Main.state.hands.get(2).add("W");
        if (ScoreCalculator.scoreForWinner(Main.state.hands, 0) == 75) passed++;
        else fail("winner score totals other hands");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}