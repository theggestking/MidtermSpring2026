import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

class FinalUnoRuleTests {
    @Test
    void deckCompositionMatchesClassicUnoDeck() {
        GameState state = new GameState();
        state.buildDeck(new Random(123));

        Map<String, Integer> counts = new HashMap<>();
        for (int card = 0; card < 108; card++) {
            counts.merge(state.draw(new Random(123)).code(), 1, Integer::sum);
        }

        for (String color : new String[]{"R", "Y", "G", "B"}) {
            assertEquals(1, counts.get(color + "0"));
            for (int number = 1; number <= 9; number++) {
                assertEquals(2, counts.get(color + number));
            }
            assertEquals(2, counts.get(color + "S"));
            assertEquals(2, counts.get(color + "R"));
            assertEquals(2, counts.get(color + "+2"));
        }
        assertEquals(4, counts.get("W"));
        assertEquals(4, counts.get("W4"));
        assertEquals(108, counts.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void legalPlayValidationCoversColorNumberActionWildAndMismatch() {
        assertTrue(CardRules.isLegal("R2", "R9", ""));
        assertTrue(CardRules.isLegal("G9", "R9", ""));
        assertTrue(CardRules.isLegal("BS", "RS", ""));
        assertTrue(CardRules.isLegal("BR", "RR", ""));
        assertTrue(CardRules.isLegal("B+2", "R+2", ""));
        assertTrue(CardRules.isLegal("W", "R9", ""));
        assertTrue(CardRules.isLegal("W4", "R9", ""));
        assertFalse(CardRules.isLegal("B3", "R9", ""));
    }

    @Test
    void selectedWildColorControlsFutureLegalPlays() {
        assertTrue(CardRules.isLegal("B3", "W", "B"));
        assertFalse(CardRules.isLegal("B3", "W", "R"));
    }

    @Test
    void skipReverseDrawTwoAndWildDrawFourEffectsUseTurnFlowRules() {
        GameState state = threeBotState();
        ActionEffects.apply(Card.from("RS"), state, state::upCard);
        assertEquals(2, state.currentPlayerIndex());

        state = threeBotState();
        ActionEffects.apply(Card.from("RR"), state, state::upCard);
        assertEquals(-1, state.direction());
        assertEquals(2, state.currentPlayerIndex());

        state = twoBotState();
        ActionEffects.apply(Card.from("RR"), state, state::upCard);
        assertEquals(0, state.currentPlayerIndex());

        state = threeBotState();
        state.clearDeck();
        state.addToDeck(Card.from("R1"));
        state.addToDeck(Card.from("R2"));
        GameState drawTwoState = state;
        String drawTwoMessage = ActionEffects.apply(Card.from("R+2"), state, () -> drawTwoState.draw(new Random(1)));
        assertEquals(2, state.handSize(1));
        assertEquals(2, state.currentPlayerIndex());
        assertEquals("Bot2 draws two.", drawTwoMessage);

        state = threeBotState();
        state.clearDeck();
        state.addToDeck(Card.from("R1"));
        state.addToDeck(Card.from("R2"));
        state.addToDeck(Card.from("R3"));
        state.addToDeck(Card.from("R4"));
        GameState drawFourState = state;
        String drawFourMessage = ActionEffects.apply(Card.from("W4"), state, () -> drawFourState.draw(new Random(1)));
        assertEquals(4, state.handSize(1));
        assertEquals(2, state.currentPlayerIndex());
        assertEquals("Bot2 draws four.", drawFourMessage);
    }

    @Test
    void drawPassFlowAllowsImmediatePlayOnlyWhenDrawnCardIsLegal() {
        GameState state = threeBotState();
        state.setUpCard(Card.from("R9"));
        state.clearCalledColor();
        state.clearDeck();
        state.addToDeck(Card.from("R4"));
        MoveSelector selector = new MoveSelector(state, new Random(1), new NoopView(), true, new BotStrategy());

        assertEquals(0, selector.handleDrawIfNeeded(-1, "Bot1"));
        assertEquals("R4", state.cardInCurrentHand(0).code());

        state = threeBotState();
        state.setUpCard(Card.from("R9"));
        state.clearCalledColor();
        state.clearDeck();
        state.addToDeck(Card.from("B3"));
        selector = new MoveSelector(state, new Random(1), new NoopView(), true, new BotStrategy());

        assertEquals(-1, selector.handleDrawIfNeeded(-1, "Bot1"));
        assertEquals("B3", state.cardInCurrentHand(0).code());
    }

    @Test
    void scoreCalculatorUsesUnoCardValues() {
        GameState state = threeBotState();
        state.addCardToPlayer(1, Card.from("R5"));
        state.addCardToPlayer(1, Card.from("GS"));
        state.addCardToPlayer(1, Card.from("BR"));
        state.addCardToPlayer(2, Card.from("Y+2"));
        state.addCardToPlayer(2, Card.from("W"));
        state.addCardToPlayer(2, Card.from("W4"));

        assertEquals(165, ScoreCalculator.scoreForWinner(state.handsSnapshot(), 0));
    }

    @Test
    void startingDiscardIsANumberCard() {
        GameState state = threeBotState();
        TurnController controller = new TurnController(
                state, new Random(2), new NoopView(), true, new BotStrategy());

        controller.startNewGame();

        assertEquals(CardRank.NUMBER, state.upCard().rankValue());
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

    private static GameState twoBotState() {
        GameState state = new GameState();
        state.addPlayer("Bot1", false);
        state.addPlayer("Bot2", false);
        state.setCurrentPlayer(0);
        state.setDirection(1);
        return state;
    }

    private static final class NoopView implements GameView {
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
}
