package it.twinsbrain.dojos;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GooseGameTest {

    @Test
    void should_allow_to_quit_game() throws Exception {
        var commands = Stream.of("quit").collect(Collectors.joining("\n"));
        var input = new ByteArrayInputStream(commands.getBytes());
        var output = new ByteArrayOutputStream();
        new GooseGame(input, output).play();

        assertThat(output.toString(), equalTo("See you!"));
    }

    @Test
    void should_allow_to_add_a_player() throws Exception {
        var commands = String.join("\n", "add player Pippo", "quit");
        var input = new ByteArrayInputStream(commands.getBytes());
        var output = new ByteArrayOutputStream();
        new GooseGame(input, output).play();
        assertThat(output.toString(),
                equalTo(
                        "players: Pippo" + "\n" +
                                "See you!"
                )
        );
    }

    @Test
    void should_allow_to_add_more_than_one_player() throws Exception {
        var commands = String.join("\n", "add player Pippo", "add player Pluto", "quit");
        var input = new ByteArrayInputStream(commands.getBytes());
        var output = new ByteArrayOutputStream();
        new GooseGame(input, output).play();
        assertThat(output.toString(),
                equalTo(
                        """
                                players: Pippo
                                players: Pippo, Pluto
                                See you!"""
                )
        );
    }

    @Test
    void should_not_allow_to_add_more_than_once_the_same_player() throws Exception {
        var commands = String.join("\n", "add player Pippo", "add player Pippo", "quit");
        var input = new ByteArrayInputStream(commands.getBytes());
        var output = new ByteArrayOutputStream();
        new GooseGame(input, output).play();
        assertThat(output.toString(),
                equalTo(
                        """
                                players: Pippo
                                Pippo: already existing player
                                See you!"""
                )
        );
    }
}