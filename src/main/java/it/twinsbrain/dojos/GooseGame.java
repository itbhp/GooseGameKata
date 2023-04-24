package it.twinsbrain.dojos;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

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
        loop:
        while (!(line = input.readLine()).equals("quit")) {
            Command command = parseCommand(line);
            switch (command) {
                case AddPlayerCommand addPlayerCommand -> {
                    switch (execute(addPlayerCommand, players::containsKey)) {
                        case PlayerAdded playerAdded -> {
                            players.put(addPlayerCommand.playerName, playerAdded.player);
                            output.println(playerAdded.messageFn.apply(players));
                        }
                        case PlayerAlreadyPresent playerAlreadyPresent -> output.println(playerAlreadyPresent.message);
                    }
                }
                case MovePlayerCommand movePlayerCommand -> {
                    switch (execute(movePlayerCommand, players::get)) {
                        case GameFinished gameFinished -> {
                            players.put(movePlayerCommand.playerName, gameFinished.winner);
                            output.print(gameFinished.message);
                            break loop;
                        }
                        case PlayerMoved playerMoved -> {
                            players.put(movePlayerCommand.playerName, playerMoved.player);
                            output.println(playerMoved.message);
                        }
                    }
                }
            }
        }
        if (players.values().stream().noneMatch(Player::hasWon)) {
            output.print("See you!");
        }
        output.flush();
    }

    private MoveResult execute(MovePlayerCommand command, Function<String, Player> retrievePlayer) {
        var player = retrievePlayer.apply(command.playerName);
        var updatedPlayer = player.move(command.steps());
        if (updatedPlayer.hasWon()) {
            return new GameFinished(
                    updatedPlayer,
                    moveMessage(command, player, updatedPlayer) + ". " + player.name + " Wins!!"
            );
        } else {
            return new PlayerMoved(updatedPlayer, moveMessage(command, player, updatedPlayer));
        }
    }

    private static String moveMessage(MovePlayerCommand command, Player player, Player updatedPlayer) {
        return format(
                "%s rolls %d, %d. %s moves from %s to %s",
                player.name,
                command.firstDice,
                command.secondDice,
                player.name,
                player.cell(),
                updatedPlayer.cell()
        );
    }

    private AddResult execute(AddPlayerCommand addCommand, Predicate<String> isExistingPlayer) {
        if (isExistingPlayer.test(addCommand.playerName)) {
            return new PlayerAlreadyPresent(addCommand.playerName + ": already existing player");
        } else {
            return new PlayerAdded(
                    new Player(addCommand.playerName, 0, false),
                    players -> "players: " + players.values().stream().map(Player::name).collect(joining(", "))
            );
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

    sealed interface Result {
    }

    sealed interface AddResult extends Result {
    }

    record PlayerAlreadyPresent(String message) implements AddResult {
    }

    record PlayerAdded(Player player, Function<Map<String, Player>, String> messageFn) implements AddResult {
    }

    sealed interface MoveResult extends Result {
    }

    record PlayerMoved(Player player, String message) implements MoveResult {
    }

    record GameFinished(Player winner, String message) implements MoveResult {
    }

    record Player(String name, int position, boolean hasWon) {
        private static final int BOARD_SIZE = 63;

        public String cell() {
            return position == 0
                    ? (hasWon ? String.valueOf(BOARD_SIZE) : "Start")
                    : String.valueOf(position);
        }

        public Player move(int steps) {
            return new Player(name, (position + steps) % BOARD_SIZE, position + steps == BOARD_SIZE);
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
