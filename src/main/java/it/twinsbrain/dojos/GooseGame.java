package it.twinsbrain.dojos;

import it.twinsbrain.dojos.commands.AddPlayerCommand;
import it.twinsbrain.dojos.commands.Command;
import it.twinsbrain.dojos.commands.MovePlayerCommand;
import it.twinsbrain.dojos.result.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class GooseGame {
  private final BufferedReader input;
  private final PrintWriter output;
  private final Map<String, Player> playersMap = new HashMap<>();

  public GooseGame(InputStream input, OutputStream output) {
    this.input = new BufferedReader(new InputStreamReader(input));
    this.output = new PrintWriter(output, true);
  }

  public void play() throws Exception {
    String line;
    while (!"quit".equals(line = input.readLine())) {
      try {
        var command = parseCommand(line);
        if (gameFinishedAfter(command)) break;
      } catch (Exception e) {
        output.println("Unrecognized command, try again!");
      }
    }
    if (noPlayersWon()) {
      output.print("See you!");
    }
    output.flush();
  }

  private boolean noPlayersWon() {
    return playersMap.values().stream().noneMatch(Player::hasWon);
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
      default -> throw new UnsupportedOperationException("unknown command " + commandName);
    };
  }

  private boolean gameFinishedAfter(Command command) {
    return switch (command) {
      case AddPlayerCommand addPlayerCommand -> {
        execute(addPlayerCommand);
        yield false;
      }
      case MovePlayerCommand movePlayerCommand -> switch (execute(movePlayerCommand)) {
        case GameFinished ignored -> true;
        case PlayerBouncedBack ignored -> false;
        case PlayerMoved ignored -> false;
      };
    };
  }

  private void execute(AddPlayerCommand addPlayerCommand) {
    switch (addPlayerCommand.execute(playersMap::containsKey)) {
      case PlayerAdded playerAdded -> {
        playersMap.put(addPlayerCommand.playerName(), playerAdded.player());
        output.println(playerAdded.messageFn().apply(playersMap.values()));
      }
      case PlayerAlreadyPresent playerAlreadyPresent -> output.println(playerAlreadyPresent.message());
    }
  }

  private MoveResult execute(MovePlayerCommand movePlayerCommand) {
    return switch (movePlayerCommand.execute(playersMap::get)) {
      case GameFinished gameFinished -> {
        playersMap.put(movePlayerCommand.playerName(), gameFinished.winner());
        output.print(gameFinished.message());
        yield gameFinished;
      }
      case PlayerMoved playerMoved -> {
        playersMap.put(movePlayerCommand.playerName(), playerMoved.player());
        output.println(playerMoved.message());
        yield playerMoved;
      }
      case PlayerBouncedBack playerBouncedBack -> {
        playersMap.put(movePlayerCommand.playerName(), playerBouncedBack.player());
        output.println(playerBouncedBack.message());
        yield playerBouncedBack;
      }
    };
  }

}
