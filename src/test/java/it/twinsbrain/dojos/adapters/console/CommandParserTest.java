package it.twinsbrain.dojos.adapters.console;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.twinsbrain.dojos.Board;
import it.twinsbrain.dojos.DiceRoll;
import it.twinsbrain.dojos.commands.AddPlayerCommand;
import it.twinsbrain.dojos.commands.MovePlayerCommand;
import org.junit.jupiter.api.Test;

class CommandParserTest {

  private final Board board = new Board(63);

  @Test
  void shouldParseAddPlayerCommand() {
    var parser = new CommandParser();
    var command = parser.parse("add player Pippo", board);
    assertThat(command, instanceOf(AddPlayerCommand.class));
    assertThat(((AddPlayerCommand) command).playerName(), equalTo("Pippo"));
  }

  @Test
  void shouldParseMovePlayerCommandWithExplicitDice() {
    var parser = new CommandParser();
    var command = parser.parse("move Pippo 4, 2", board);
    assertThat(command, instanceOf(MovePlayerCommand.class));
    var move = (MovePlayerCommand) command;
    assertThat(move.playerName(), equalTo("Pippo"));
    assertThat(move.firstDice(), equalTo(4));
    assertThat(move.secondDice(), equalTo(2));
  }

  @Test
  void shouldParseMovePlayerCommandWithAutoRoll() {
    var parser = new CommandParser(() -> new DiceRoll(3, 5));
    var command = parser.parse("move Pippo", board);
    assertThat(command, instanceOf(MovePlayerCommand.class));
    var move = (MovePlayerCommand) command;
    assertThat(move.playerName(), equalTo("Pippo"));
    assertThat(move.firstDice(), equalTo(3));
    assertThat(move.secondDice(), equalTo(5));
  }

  @Test
  void shouldThrowOnUnknownCommand() {
    var parser = new CommandParser();
    assertThrows(UnsupportedOperationException.class, () -> parser.parse("unknown", board));
  }

  @Test
  void shouldParseMovePlayerCommandWithSingleDigitDice() {
    var parser = new CommandParser();
    var command = parser.parse("move Pippo 1, 2", board);
    assertThat(command, instanceOf(MovePlayerCommand.class));
    var move = (MovePlayerCommand) command;
    assertThat(move.firstDice(), equalTo(1));
    assertThat(move.secondDice(), equalTo(2));
  }

  @Test
  void shouldParseMovePlayerCommandWithDoubleDigitDice() {
    var parser = new CommandParser();
    var command = parser.parse("move Pippo 10, 5", board);
    assertThat(command, instanceOf(MovePlayerCommand.class));
    var move = (MovePlayerCommand) command;
    assertThat(move.firstDice(), equalTo(10));
    assertThat(move.secondDice(), equalTo(5));
  }
}
