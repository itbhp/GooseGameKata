package it.twinsbrain.dojos;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class GooseGame {
    private final BufferedReader input;
    private final PrintWriter output;
    private final Map<String, Player> players = new HashMap<>();

    public GooseGame(InputStream input, OutputStream output) {
        this.input = new BufferedReader(new InputStreamReader(input));
        this.output = new PrintWriter(output);
    }

    public void play() throws Exception {
        String line;
        while (!(line = input.readLine()).equals("quit")) {
            AddPlayerCommand addCommand = parseCommand(line);
            executeAddCommand(addCommand);
        }
        output.print("See you!");
        output.flush();
    }

    private void executeAddCommand(AddPlayerCommand addCommand) {
        if (players.containsKey(addCommand.playerName)) {
            output.println(addCommand.playerName + ": already existing player");
        } else {
            players.put(addCommand.playerName, new Player(addCommand.playerName));
            output.println("players: " + players.values().stream().map(Player::name).collect(joining(", ")));
        }
    }

    private static AddPlayerCommand parseCommand(String line) {
        var commandParts = line.split(" ");
        String playerName = commandParts[2];
        var addCommand = new AddPlayerCommand(playerName);
        return addCommand;
    }

    record Player(String name) {
    }

    record AddPlayerCommand(String playerName) {
    }
}
