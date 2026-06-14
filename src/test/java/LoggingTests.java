import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class LoggingTests {
    private Logger rootLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void captureLogs() {
        rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
    }

    @AfterEach
    void stopCapturingLogs() {
        rootLogger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void logsGameStartAndPlayerTurn() {
        GameState state = twoBotState();
        TurnController controller = new TurnController(state, new Random(123), new StubView(), true);

        controller.startNewGame();
        controller.takeTurn();

        assertLogged("event=game_start");
        assertLogged("event=player_turn");
    }

    @Test
    void logsCardDrawAndInvalidInput() {
        GameState state = twoBotState();
        state.clearHand(0);
        state.addCardToCurrentPlayer(Card.from("R4"));
        state.setUpCard(Card.from("R9"));
        state.clearDeck();
        state.addToDeck(Card.from("B3"));

        MoveSelector selector = new MoveSelector(
                state, new Random(123), new InvalidThenDrawView(), true, new BotStrategy());
        selector.chooseMove("You");

        assertLogged("event=invalid_input");
        assertLogged("event=card_drawn");
    }

    @Test
    void logsCardPlayedAndGameEnd() {
        GameState state = twoBotState();
        state.clearHand(0);
        state.clearHand(1);
        state.addCardToCurrentPlayer(Card.from("R4"));
        state.addCardToPlayer(1, Card.from("G5"));
        state.setUpCard(Card.from("R9"));

        TurnResolver resolver = new TurnResolver(
                state, new Random(123), new StubView(), true, new BotStrategy());
        resolver.resolveChosenCard(0, "You");

        assertLogged("event=card_played");
        assertLogged("event=game_end");
    }

    private GameState twoBotState() {
        GameState state = new GameState();
        state.addPlayer("You", true);
        state.addPlayer("Bot1", false);
        state.setCurrentPlayer(0);
        return state;
    }

    private void assertLogged(String event) {
        assertTrue(appender.list.stream().map(ILoggingEvent::getFormattedMessage).anyMatch(message -> message.contains(event)),
                "Expected log event " + event);
    }

    private static class InvalidThenDrawView extends StubView {
        private final List<String> inputs = new ArrayList<>(List.of("NOPE", "DRAW"));

        @Override
        public String askMoveInput() {
            return inputs.remove(0);
        }
    }

    private static class StubView implements GameView {
        public void showGameHeader(int gameNumber) {
        }

        public void showFinalScores(GameState state) {
        }

        public void showTurn(String upCard, String calledColor, String name, List<Card> hand) {
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
