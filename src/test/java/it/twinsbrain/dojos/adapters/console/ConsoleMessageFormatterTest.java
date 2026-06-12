package it.twinsbrain.dojos.adapters.console;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import it.twinsbrain.dojos.Board;
import it.twinsbrain.dojos.GooseChain;
import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.SimpleMove;
import it.twinsbrain.dojos.result.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConsoleMessageFormatterTest {

  private final Board board = new Board(63);
  private final ConsoleMessageFormatter formatter = new ConsoleMessageFormatter(board);

  @Test
  void playerAlreadyPresentFormatted() {
    assertThat(
        formatter.format(new PlayerAlreadyPresent("Pippo"), List.of()),
        equalTo("Pippo: already existing player"));
  }

  @Test
  void playerAddedFormattedWithAllCurrentPlayers() {
    var players = List.of(new Player("Pippo", 0), new Player("Pluto", 0));
    assertThat(
        formatter.format(new PlayerAdded(new Player("Pluto", 0)), players),
        equalTo("players: Pippo, Pluto"));
  }

  @Test
  void moveFromStartFormattedCorrectly() {
    var ctx = new SimpleMove("Pippo", 2, 1, 0, 3);
    assertThat(
        formatter.format(new PlayerMoved(new Player("Pippo", 3), ctx)),
        equalTo("Pippo rolls 2, 1. Pippo moves from Start to 3"));
  }

  @Test
  void normalMoveFormattedCorrectly() {
    var ctx = new SimpleMove("Pippo", 2, 1, 10, 13);
    assertThat(
        formatter.format(new PlayerMoved(new Player("Pippo", 13), ctx)),
        equalTo("Pippo rolls 2, 1. Pippo moves from 10 to 13"));
  }

  @Test
  void bridgeMoveFormattedCorrectly() {
    var ctx = new SimpleMove("Pippo", 1, 1, 4, 6);
    assertThat(
        formatter.format(new PlayerMoved(new Player("Pippo", 12), ctx)),
        equalTo("Pippo rolls 1, 1. Pippo moves from 4 to The Bridge. Pippo jumps to 12"));
  }

  @Test
  void singleGooseJumpFormattedCorrectly() {
    var ctx = new GooseChain("Pippo", 1, 1, 3, List.of(5, 7));
    assertThat(
        formatter.format(new PlayerMoved(new Player("Pippo", 7), ctx)),
        equalTo("Pippo rolls 1, 1. Pippo moves from 3 to 5, The Goose. Pippo moves again and goes to 7"));
  }

  @Test
  void multipleGooseJumpsFormattedCorrectly() {
    var ctx = new GooseChain("Pippo", 2, 2, 10, List.of(14, 18, 22));
    assertThat(
        formatter.format(new PlayerMoved(new Player("Pippo", 22), ctx)),
        equalTo("Pippo rolls 2, 2. Pippo moves from 10 to 14, The Goose. Pippo moves again and goes to 18, The Goose. Pippo moves again and goes to 22"));
  }

  @Test
  void bounceFormattedCorrectly() {
    var ctx = new SimpleMove("Pippo", 2, 3, 60, 65);
    assertThat(
        formatter.format(new PlayerBouncedBack(new Player("Pippo", 61), ctx)),
        equalTo("Pippo rolls 2, 3. Pippo moves from 60 to 63. Pippo bounces! Pippo returns to 61"));
  }

  @Test
  void normalMoveWithPrankFormattedCorrectly() {
    var ctx = new SimpleMove("Bob", 2, 2, 0, 4);
    var prank = new Prank("Pippo", 4, 0);
    assertThat(
        formatter.format(new PlayerMoved(new Player("Bob", 4), ctx, prank)),
        equalTo("Bob rolls 2, 2. Bob moves from Start to 4. On 4 there is Pippo, who returns to Start"));
  }

  @Test
  void winFormattedCorrectly() {
    var ctx = new SimpleMove("Pippo", 1, 2, 60, 63);
    assertThat(
        formatter.format(new GameFinished(new Player("Pippo", 63), ctx)),
        equalTo("Pippo rolls 1, 2. Pippo moves from 60 to 63. Pippo Wins!!"));
  }
}
