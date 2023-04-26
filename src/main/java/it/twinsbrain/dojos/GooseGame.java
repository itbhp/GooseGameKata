package it.twinsbrain.dojos;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class GooseGame {
  private final BufferedReader input;
  private final PrintWriter output;
  private final Map<String, Player> players = new HashMap<>();

  public GooseGame(InputStream input, OutputStream output) {
    this.input = new BufferedReader(new InputStreamReader(input));
    this.output = new PrintWriter(output, true);
  }

  public void play() throws Exception {
    String line;
    loop:
    while (!(line = input.readLine()).equals("quit")) {
      Command command = parseCommand(line);
      switch (command) {
        case AddPlayerCommand addPlayerCommand -> {
          switch (addPlayerCommand.execute(players::containsKey)) {
            case PlayerAdded playerAdded -> {
              players.put(addPlayerCommand.playerName, playerAdded.player);
              output.println(playerAdded.messageFn.apply(players));
            }
            case PlayerAlreadyPresent playerAlreadyPresent -> output.println(
                playerAlreadyPresent.message);
          }
        }
        case MovePlayerCommand movePlayerCommand -> {
          switch (movePlayerCommand.execute(players::get)) {
            case GameFinished gameFinished -> {
              players.put(movePlayerCommand.playerName, gameFinished.winner);
              output.print(gameFinished.message);
              break loop;
            }
            case PlayerMoved playerMoved -> {
              players.put(movePlayerCommand.playerName, playerMoved.player);
              output.println(playerMoved.message);
            }
            case PlayerBouncedBack playerBouncedBack -> {
              players.put(movePlayerCommand.playerName, playerBouncedBack.player);
              output.println(playerBouncedBack.message);
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

  sealed interface Result {}

  sealed interface AddResult extends Result {}

  record PlayerAlreadyPresent(String message) implements AddResult {}

  record PlayerAdded(Player player, Function<Map<String, Player>, String> messageFn)
      implements AddResult {}

  sealed interface MoveResult extends Result {}

  record PlayerMoved(Player player, String message) implements MoveResult {}

  record PlayerBouncedBack(Player player, String message) implements MoveResult {}

  record GameFinished(Player winner, String message) implements MoveResult {}

  record Player(String name, int position) {

    private static final int BOARD_SIZE = 63;

    public Player move(int steps) {
      return new Player(name, position + steps);
    }

    public boolean hasWon() {
      return position == BOARD_SIZE;
    }

    public Player bounceBack() {
      return new Player(name, BOARD_SIZE - exceedingSteps());
    }

    public boolean isBeyondTheFinish() {
      return exceedingSteps() > 0;
    }

    public String cell() {
      return effectivePosition() == 0 ? "Start" : String.valueOf(effectivePosition());
    }

    private int effectivePosition() {
      return Math.min(position, BOARD_SIZE);
    }

    private int exceedingSteps() {
      return position - BOARD_SIZE;
    }
  }

  sealed interface Command {}

  record AddPlayerCommand(String playerName) implements Command {
    private AddResult execute(Predicate<String> isExistingPlayer) {
      if (isExistingPlayer.test(playerName)) {
        return new PlayerAlreadyPresent(playerName + ": already existing player");
      } else {
        return new PlayerAdded(
            new Player(playerName, 0),
            players ->
                "players: " + players.values().stream().map(Player::name).collect(joining(", ")));
      }
    }
  }

  record MovePlayerCommand(String playerName, int firstDice, int secondDice) implements Command {
    private int steps() {
      return firstDice + secondDice;
    }

    private MoveResult execute(Function<String, Player> retrievePlayer) {
      var player = retrievePlayer.apply(playerName);
      var movedPlayer = player.move(steps());
      if (movedPlayer.isBeyondTheFinish()) {
        var bounced = movedPlayer.bounceBack();
        return new PlayerBouncedBack(
            bounced,
            moveMessage(player.cell(), movedPlayer.cell())
                + String.format(
                    ". %s bounces! %s returns to %d",
                    player.name(), player.name(), bounced.position()));
      }
      if (movedPlayer.hasWon()) {
        return new GameFinished(
            movedPlayer,
            moveMessage(player.cell(), movedPlayer.cell()) + ". " + player.name() + " Wins!!");
      }
      return new PlayerMoved(movedPlayer, moveMessage(player.cell(), movedPlayer.cell()));
    }

    private String moveMessage(String startCell, String finishCell) {
      return format(
          "%s rolls %d, %d. %s moves from %s to %s",
          playerName, firstDice, secondDice, playerName, startCell, finishCell);
    }
  }
}
