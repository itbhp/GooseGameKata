package it.twinsbrain.dojos;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class GooseGame {
    private final BufferedReader input;
    private final PrintWriter output;

    private final List<Player> players = new ArrayList<>();

    public GooseGame(InputStream input, OutputStream output) {
        this.input = new BufferedReader(new InputStreamReader(input));
        this.output = new PrintWriter(output);
    }

    public void play() throws Exception {
        String line;
        while (!(line = input.readLine()).equals("quit")) {
            var commandParts = line.split(" ");
            players.add(new Player(commandParts[2]));
            output.println("players: " + players.stream().map(Player::name).collect(joining(", ")));
        }
        output.print("See you!");
        output.flush();
    }

    record Player(String name) {}
}
