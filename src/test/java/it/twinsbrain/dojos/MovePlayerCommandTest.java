package it.twinsbrain.dojos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import it.twinsbrain.dojos.commands.MovePlayerCommand;
import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;
import org.junit.jupiter.api.Test;

class MovePlayerCommandTest {

  @Test
  void bridgeRuleShouldMoveToPosition12() {
    var cmd = new MovePlayerCommand("Pippo", 4, 2, 63);
    var result = cmd.move(new Player("Pippo", 0));
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(12));
  }

  @Test
  void bridgeRuleMessageShouldContainJumpsTo12() {
    var cmd = new MovePlayerCommand("Pippo", 4, 2, 63);
    var result = cmd.move(new Player("Pippo", 0));
    assertThat(((PlayerMoved) result).message(), containsString("jumps to 12"));
  }

  @Test
  void bridgeRuleShouldWorkFromAnyPosition() {
    var cmd = new MovePlayerCommand("Pippo", 2, 1, 63);
    var result = cmd.move(new Player("Pippo", 3));
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(12));
  }

  @Test
  void gooseRuleShouldMoveAgainWithSameDice() {
    var cmd = new MovePlayerCommand("Pippo", 1, 1, 63);
    var result = cmd.move(new Player("Pippo", 3));
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(7));
  }

  @Test
  void gooseRuleMessageShouldContainTheGoose() {
    var cmd = new MovePlayerCommand("Pippo", 1, 1, 63);
    var result = cmd.move(new Player("Pippo", 3));
    assertThat(((PlayerMoved) result).message(), containsString("The Goose"));
  }

  @Test
  void gooseRuleShouldChainMultipleJumps() {
    var cmd = new MovePlayerCommand("Pippo", 2, 2, 63);
    var result = cmd.move(new Player("Pippo", 10));
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(22));
  }

  @Test
  void bridgeRuleTakesPrecedenceOverGooseRule() {
    var cmd = new MovePlayerCommand("Pippo", 1, 1, 63);
    var result = cmd.move(new Player("Pippo", 4));
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(12));
  }

  @Test
  void gooseChainShouldStopAtNonGoosePosition() {
    var cmd = new MovePlayerCommand("Pippo", 1, 2, 63);
    var result = cmd.move(new Player("Pippo", 12));
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(15));
  }

  @Test
  void bounceRuleShouldBounceBackFromOvershoot() {
    var cmd = new MovePlayerCommand("Pippo", 2, 3, 63);
    var result = cmd.move(new Player("Pippo", 60));
    assertThat(result, instanceOf(PlayerBouncedBack.class));
    assertThat(((PlayerBouncedBack) result).player().position(), equalTo(61));
  }

  @Test
  void bounceRuleShouldCalculateCorrectReturnPosition() {
    var cmd = new MovePlayerCommand("Pippo", 2, 5, 63);
    var result = cmd.move(new Player("Pippo", 60));
    assertThat(result, instanceOf(PlayerBouncedBack.class));
    assertThat(((PlayerBouncedBack) result).player().position(), equalTo(59));
  }

  @Test
  void bounceRuleMessageShouldContainBounce() {
    var cmd = new MovePlayerCommand("Pippo", 2, 3, 63);
    var result = cmd.move(new Player("Pippo", 60));
    assertThat(((PlayerBouncedBack) result).message(), containsString("bounces"));
  }

  @Test
  void bounceRuleShouldPreservePlayerName() {
    var cmd = new MovePlayerCommand("Pluto", 2, 3, 63);
    var result = cmd.move(new Player("Pluto", 60));
    assertThat(((PlayerBouncedBack) result).player().name(), equalTo("Pluto"));
  }

  @Test
  void winRuleShouldFinishGameOnExactLanding() {
    var cmd = new MovePlayerCommand("Pippo", 1, 2, 63);
    var result = cmd.move(new Player("Pippo", 60));
    assertThat(result, instanceOf(GameFinished.class));
    assertThat(((GameFinished) result).winner().position(), equalTo(63));
  }

  @Test
  void winRuleMessageShouldContainWins() {
    var cmd = new MovePlayerCommand("Pippo", 1, 2, 63);
    var result = cmd.move(new Player("Pippo", 60));
    assertThat(((GameFinished) result).message(), containsString("Wins!!"));
  }

  @Test
  void winRuleShouldNotTriggerOnOvershoot() {
    var cmd = new MovePlayerCommand("Pippo", 3, 2, 63);
    var result = cmd.move(new Player("Pippo", 60));
    assertThat(result, not(instanceOf(GameFinished.class)));
  }

  @Test
  void normalRuleShouldMovePlayerByDiceSum() {
    var cmd = new MovePlayerCommand("Pippo", 2, 3, 63);
    var result = cmd.move(new Player("Pippo", 15));
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(20));
  }

  @Test
  void normalRuleShouldPreservePlayerName() {
    var cmd = new MovePlayerCommand("Pluto", 2, 3, 63);
    var result = cmd.move(new Player("Pluto", 15));
    assertThat(((PlayerMoved) result).player().name(), equalTo("Pluto"));
  }
}
