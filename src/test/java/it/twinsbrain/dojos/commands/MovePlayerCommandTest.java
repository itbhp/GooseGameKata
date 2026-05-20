package it.twinsbrain.dojos.commands;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import it.twinsbrain.dojos.Board;
import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;
import org.junit.jupiter.api.Test;

class MovePlayerCommandTest {

  private final Board board = new Board(63);

  @Test
  void normalMoveAdvancesPlayerByDiceSum() {
    var result = move("Pippo", 10, 2, 1);
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(13));
    assertThat(((PlayerMoved) result).message(), equalTo("Pippo rolls 2, 1. Pippo moves from 10 to 13"));
  }

  @Test
  void bridgeRuleJumpsToTwelve() {
    var result = move("Pippo", 4, 1, 1);
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(12));
    assertThat(((PlayerMoved) result).message(),
        equalTo("Pippo rolls 1, 1. Pippo moves from 4 to The Bridge. Pippo jumps to 12"));
  }

  @Test
  void gooseRuleChainsASingleJump() {
    var result = move("Pippo", 3, 1, 1);
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(7));
    assertThat(((PlayerMoved) result).message(),
        equalTo("Pippo rolls 1, 1. Pippo moves from 3 to 5, The Goose. Pippo moves again and goes to 7"));
  }

  @Test
  void gooseRuleChainsMultipleJumps() {
    var result = move("Pippo", 10, 2, 2);
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(22));
    assertThat(((PlayerMoved) result).message(), equalTo(
        "Pippo rolls 2, 2. Pippo moves from 10 to 14, The Goose. Pippo moves again and goes to 18, The Goose. Pippo moves again and goes to 22"));
  }

  @Test
  void bounceRuleReflectsPlayerBackFromFinish() {
    var result = move("Pippo", 60, 2, 3);
    assertThat(result, instanceOf(PlayerBouncedBack.class));
    assertThat(((PlayerBouncedBack) result).player().position(), equalTo(61));
    assertThat(((PlayerBouncedBack) result).message(),
        equalTo("Pippo rolls 2, 3. Pippo moves from 60 to 63. Pippo bounces! Pippo returns to 61"));
  }

  @Test
  void winRuleFinishesGameWhenPlayerLandsOnLastCell() {
    var result = move("Paolo", 60, 1, 2);
    assertThat(result, instanceOf(GameFinished.class));
    assertThat(((GameFinished) result).winner().position(), equalTo(63));
    assertThat(((GameFinished) result).message(),
        equalTo("Paolo rolls 1, 2. Paolo moves from 60 to 63. Paolo Wins!!"));
  }

  private it.twinsbrain.dojos.result.MoveResult move(String name, int startPosition, int d1, int d2) {
    return new MovePlayerCommand(name, d1, d2, board).move(new Player(name, startPosition));
  }
}
