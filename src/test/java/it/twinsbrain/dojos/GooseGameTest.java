package it.twinsbrain.dojos;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static it.twinsbrain.dojos.GooseGameTest.GameTester.givenTheseCommands;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GooseGameTest {

    @Test
    void should_allow_to_quit_game() throws Exception {
        givenTheseCommands("quit")
                .whenGameIsPlayed()
                .thenOutputShouldBe("See you!");
    }

    @Test
    void should_allow_to_add_a_player() throws Exception {
        givenTheseCommands("add player Pippo", "quit")
                .whenGameIsPlayed()
                .thenOutputShouldBe("""
                        players: Pippo
                        See you!""");
    }

    @Test
    void should_allow_to_add_more_than_one_player() throws Exception {
        givenTheseCommands("add player Pippo", "add player Pluto", "quit")
                .whenGameIsPlayed()
                .thenOutputShouldBe("""
                        players: Pippo
                        players: Pippo, Pluto
                        See you!""");
    }

    @Test
    void should_not_allow_to_add_more_than_once_the_same_player() throws Exception {
        givenTheseCommands("add player Pippo", "add player Pippo", "quit")
                .whenGameIsPlayed()
                .thenOutputShouldBe("""
                        players: Pippo
                        Pippo: already existing player
                        See you!""");
    }

    static class GameTester {
        private final CharSequence[] commandList;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        GameTester(CharSequence[] commandList) {
            this.commandList = commandList;
        }

        static GameTester givenTheseCommands(CharSequence... commandList) {
            return new GameTester(commandList);
        }

        GameTester whenGameIsPlayed() throws Exception {
            var commands = String.join("\n", commandList);
            var input = new ByteArrayInputStream(commands.getBytes());
            new GooseGame(input, output).play();
            return this;
        }

        void thenOutputShouldBe(String expectedOutput) {
            assertThat(output.toString(), equalTo(expectedOutput));
        }
    }
}