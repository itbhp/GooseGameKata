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
}