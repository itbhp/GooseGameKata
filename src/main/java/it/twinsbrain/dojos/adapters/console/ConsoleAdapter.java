package it.twinsbrain.dojos.adapters.console;

import it.twinsbrain.dojos.Board;
import it.twinsbrain.dojos.Game;
import it.twinsbrain.dojos.commands.AddPlayerCommand;
import it.twinsbrain.dojos.commands.Command;
import it.twinsbrain.dojos.commands.MovePlayerCommand;
import it.twinsbrain.dojos.result.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ConsoleAdapter {

  private final BufferedReader input;
  private final PrintWriter output;
  private final Board board = new Board(63);
  private final Game game = new Game(board);
  private final CommandParser commandParser = new CommandParser();
  private final ConsoleMessageFormatter formatter = new ConsoleMessageFormatter(board);

  public ConsoleAdapter(InputStream input, OutputStream output) {
    this.input = new BufferedReader(new InputStreamReader(input));
    this.output = new PrintWriter(output, true);
  }

  public void play() throws IOException {
    String line;
    while (!"quit".equals(line = input.readLine())) {
      try {
        var command = commandParser.parse(line, board);
        if (gameFinishedAfter(command)) break;
      } catch (Exception e) {
        output.println("Unrecognized command, try again!");
      }
    }
    if (!game.isOver()) {
      output.print("See you!");
    }
    output.flush();
  }

  private boolean gameFinishedAfter(Command command) {
    return switch (command) {
      case AddPlayerCommand c -> {
        var result = game.addPlayer(c.playerName());
        output.println(formatter.format(result, game.players()));
        yield false;
      }
      case MovePlayerCommand c -> {
        var result = game.movePlayer(c.playerName(), c.firstDice(), c.secondDice());
        var message = formatter.format(result);
        if (result instanceof GameFinished) output.print(message);
        else output.println(message);
        yield result instanceof GameFinished;
      }
    };
  }
}
