package it.twinsbrain.dojos.commands;

public class CommandParser {

    public Command parse(String line, int boardSize) {
        var commandParts = line.split(" ");
        var commandName = commandParts[0];
        return switch (commandName) {
            case "add" -> new AddPlayerCommand(commandParts[2]);
            case "move" -> {
                var firstDice = Integer.parseInt(commandParts[2].replace(",", "").trim());
                var secondDice = Integer.parseInt(commandParts[3].trim());
                yield new MovePlayerCommand(commandParts[1], firstDice, secondDice, boardSize);
            }
            default -> throw new UnsupportedOperationException("unknown command " + commandName);
        };
    }
}
