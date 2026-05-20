package it.twinsbrain.dojos.adapters.console;

import it.twinsbrain.dojos.Board;
import it.twinsbrain.dojos.commands.AddPlayerCommand;
import it.twinsbrain.dojos.commands.Command;
import it.twinsbrain.dojos.commands.MovePlayerCommand;

public class CommandParser {

  public Command parse(String line, Board board) {
    var commandParts = line.split(" ");
    var commandName = commandParts[0];
    return switch (commandName) {
      case "add" -> new AddPlayerCommand(commandParts[2]);
      case "move" -> {
        var firstDice = Integer.parseInt(commandParts[2].replace(",", "").trim());
        var secondDice = Integer.parseInt(commandParts[3].trim());
        yield new MovePlayerCommand(commandParts[1], firstDice, secondDice, board);
      }
      default -> throw new UnsupportedOperationException("unknown command " + commandName);
    };
  }
}
