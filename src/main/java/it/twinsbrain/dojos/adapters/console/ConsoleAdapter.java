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

  private static final CommandParser commandParser = new CommandParser();
  private final BufferedReader input;
  private final PrintWriter output;
  private final Board board = new Board(63);
  private final Game game = new Game(board);

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
        display(game.addPlayer(c.playerName()));
        yield false;
      }
      case MovePlayerCommand c -> {
        var result = game.movePlayer(c.playerName(), c.firstDice(), c.secondDice());
        display(result);
        yield result instanceof GameFinished;
      }
    };
  }

  private void display(AddResult result) {
    switch (result) {
      case PlayerAdded playerAdded ->
          output.println(playerAdded.messageFn().apply(game.players()));
      case PlayerAlreadyPresent playerAlreadyPresent ->
          output.println(playerAlreadyPresent.message());
    }
  }

  private void display(MoveResult result) {
    switch (result) {
      case GameFinished gameFinished -> output.print(gameFinished.message());
      case PlayerMoved playerMoved -> output.println(playerMoved.message());
      case PlayerBouncedBack playerBouncedBack -> output.println(playerBouncedBack.message());
    }
  }
}
