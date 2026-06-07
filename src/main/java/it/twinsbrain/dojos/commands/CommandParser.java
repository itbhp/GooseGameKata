package it.twinsbrain.dojos.commands;

import it.twinsbrain.dojos.DiceRoller;
import it.twinsbrain.dojos.RandomDiceRoller;

public class CommandParser {

  private final DiceRoller diceRoller;

  public CommandParser() {
    this(new RandomDiceRoller());
  }

  public CommandParser(DiceRoller diceRoller) {
    this.diceRoller = diceRoller;
  }

  public Command parse(String line, int boardSize) {
    var commandParts = line.split(" ");
    var commandName = commandParts[0];
    return switch (commandName) {
      case "add" -> new AddPlayerCommand(commandParts[2]);
      case "move" -> {
        if (commandParts.length == 2) {
          var dice = diceRoller.roll();
          yield new MovePlayerCommand(commandParts[1], dice.first(), dice.second(), boardSize);
        }
        var firstDice = Integer.parseInt(commandParts[2].replace(",", "").trim());
        var secondDice = Integer.parseInt(commandParts[3].trim());
        yield new MovePlayerCommand(commandParts[1], firstDice, secondDice, boardSize);
      }
      default -> throw new UnsupportedOperationException("unknown command " + commandName);
    };
  }
}
