package it.twinsbrain.dojos.commands;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import it.twinsbrain.dojos.Board;
import it.twinsbrain.dojos.GooseChain;
import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.SimpleMove;
import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;
import java.util.List;
import org.junit.jupiter.api.Test;

class MovePlayerCommandTest {

  private final Board board = new Board(63);

  @Test
  void normalMoveAdvancesPlayerByDiceSum() {
    var result = move("Pippo", 10, 2, 1);
    assertThat(result, instanceOf(PlayerMoved.class));
    var moved = (PlayerMoved) result;
    assertThat(moved.player().position(), equalTo(13));
    var ctx = (SimpleMove) moved.context();
    assertThat(ctx.fromPosition(), equalTo(10));
    assertThat(ctx.landedPosition(), equalTo(13));
  }

  @Test
  void bridgeRuleJumpsToTwelve() {
    var result = move("Pippo", 4, 1, 1);
    assertThat(result, instanceOf(PlayerMoved.class));
    var moved = (PlayerMoved) result;
    assertThat(moved.player().position(), equalTo(12));
    var ctx = (SimpleMove) moved.context();
    assertThat(ctx.fromPosition(), equalTo(4));
    assertThat(ctx.landedPosition(), equalTo(6));
  }

  @Test
  void gooseRuleChainsASingleJump() {
    var result = move("Pippo", 3, 1, 1);
    assertThat(result, instanceOf(PlayerMoved.class));
    var moved = (PlayerMoved) result;
    assertThat(moved.player().position(), equalTo(7));
    var ctx = (GooseChain) moved.context();
    assertThat(ctx.fromPosition(), equalTo(3));
    assertThat(ctx.visitedPositions(), equalTo(List.of(5, 7)));
  }

  @Test
  void gooseRuleChainsMultipleJumps() {
    var result = move("Pippo", 10, 2, 2);
    assertThat(result, instanceOf(PlayerMoved.class));
    var moved = (PlayerMoved) result;
    assertThat(moved.player().position(), equalTo(22));
    var ctx = (GooseChain) moved.context();
    assertThat(ctx.fromPosition(), equalTo(10));
    assertThat(ctx.visitedPositions(), equalTo(List.of(14, 18, 22)));
  }

  @Test
  void bounceRuleReflectsPlayerBackFromFinish() {
    var result = move("Pippo", 60, 2, 3);
    assertThat(result, instanceOf(PlayerBouncedBack.class));
    var bounced = (PlayerBouncedBack) result;
    assertThat(bounced.player().position(), equalTo(61));
    var ctx = (SimpleMove) bounced.context();
    assertThat(ctx.fromPosition(), equalTo(60));
    assertThat(ctx.landedPosition(), equalTo(65));
  }

  @Test
  void winRuleFinishesGameWhenPlayerLandsOnLastCell() {
    var result = move("Paolo", 60, 1, 2);
    assertThat(result, instanceOf(GameFinished.class));
    var finished = (GameFinished) result;
    assertThat(finished.winner().position(), equalTo(63));
    var ctx = (SimpleMove) finished.context();
    assertThat(ctx.fromPosition(), equalTo(60));
    assertThat(ctx.landedPosition(), equalTo(63));
  }

  private it.twinsbrain.dojos.result.MoveResult move(String name, int startPosition, int d1, int d2) {
    return new MovePlayerCommand(name, d1, d2, board).move(new Player(name, startPosition));
  }
}
