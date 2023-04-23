package it.twinsbrain.dojos;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
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
            Command command = parseCommand(line);
            switch (command) {
                case AddPlayerCommand addPlayerCommand -> execute(addPlayerCommand);
                case MovePlayerCommand movePlayerCommand -> execute(movePlayerCommand);
            }
        }
        output.print("See you!");
        output.flush();
    }

    private void execute(MovePlayerCommand command) {
        if (players.containsKey(command.playerName)) {
            var player = players.get(command.playerName);
            var updatedPlayer = player.move(command.steps());
            players.put(player.name, updatedPlayer);
            output.println(format(
                            "%s rolls %d, %d. %s moves from %s to %s",
                            player.name,
                            command.firstDice,
                            command.secondDice,
                            player.name,
                            player.cell(),
                            updatedPlayer.cell()
                    )
            );
        }
    }

    private void execute(AddPlayerCommand addCommand) {
        if (players.containsKey(addCommand.playerName)) {
            output.println(addCommand.playerName + ": already existing player");
        } else {
            players.put(addCommand.playerName, new Player(addCommand.playerName, 0));
            output.println("players: " + players.values().stream().map(Player::name).collect(joining(", ")));
        }
    }

    private static Command parseCommand(String line) {
        var commandParts = line.split(" ");
        var commandName = commandParts[0];
        return switch (commandName) {
            case "add" -> new AddPlayerCommand(commandParts[2]);
            case "move" -> {
                var firstDice = Integer.parseInt(commandParts[2].replace(",", "").trim());
                var secondDice = Integer.parseInt(commandParts[3].trim());
                yield new MovePlayerCommand(commandParts[1], firstDice, secondDice);
            }
            default -> throw new UnsupportedOperationException("unknown command");
        };
    }

    record Player(String name, int position) {
        public String cell() {
            return position == 0 ? "Start" : String.valueOf(position);
        }

        public Player move(int steps) {
            return new Player(name, position + steps);
        }
    }

    sealed interface Command {
    }

    record AddPlayerCommand(String playerName) implements Command {
    }

    record MovePlayerCommand(String playerName, int firstDice, int secondDice) implements Command {
        private int steps() {
            return firstDice + secondDice;
        }
    }
}
