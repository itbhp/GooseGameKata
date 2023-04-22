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
                case AddPlayerCommand addPlayerCommand -> executeAddCommand(addPlayerCommand);
                case MovePlayerCommand movePlayerCommand -> executeMoveCommand(movePlayerCommand);
            }
        }
        output.print("See you!");
        output.flush();
    }

    private void executeMoveCommand(MovePlayerCommand command) {
        if (players.containsKey(command.playerName)) {
            var player = players.get(command.playerName);
            var newPosition = player.position() + command.firstDraw + command.secondDraw;
            var updatedPlayer = new Player(player.name, newPosition);
            players.put(player.name, updatedPlayer);
            output.println(format(
                            "%s rolls %d, %d. %s moves from %s to %s",
                            player.name,
                            command.firstDraw,
                            command.secondDraw,
                            player.name,
                            player.cell(),
                            updatedPlayer.cell()
                    )
            );
        }
    }

    private void executeAddCommand(AddPlayerCommand addCommand) {
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
        var playerName = commandName.equals("add") ? commandParts[2] : commandParts[1];
        return switch (commandName) {
            case "add" -> new AddPlayerCommand(playerName);
            case "move" -> {
                var firstDraw = Integer.parseInt(commandParts[2].replace(",", "").trim());
                var secondDraw = Integer.parseInt(commandParts[3].trim());
                yield new MovePlayerCommand(playerName, firstDraw, secondDraw);
            }
            default -> throw new UnsupportedOperationException("unknown command");
        };
    }

    record Player(String name, int position) {
        public String cell() {
            return position == 0 ? "Start" : String.valueOf(position);
        }
    }

    sealed interface Command {
    }

    record AddPlayerCommand(String playerName) implements Command {
    }

    record MovePlayerCommand(String playerName, int firstDraw, int secondDraw) implements Command {
    }
}
