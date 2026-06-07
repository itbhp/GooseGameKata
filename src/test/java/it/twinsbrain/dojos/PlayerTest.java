package it.twinsbrain.dojos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class PlayerTest {

  @Test
  void shouldMoveByGivenSteps() {
    var player = new Player("Pippo", 0);
    var moved = player.move(5);
    assertThat(moved.position(), equalTo(5));
  }

  @Test
  void shouldWinWhenLandingExactlyOnLastCell() {
    var player = new Player("Pippo", 63);
    assertThat(player.hasWonGiven(63), equalTo(true));
  }

  @Test
  void shouldNotWinWhenPositionIsBelowLastCell() {
    var player = new Player("Pippo", 62);
    assertThat(player.hasWonGiven(63), equalTo(false));
  }

  @Test
  void shouldNotWinWhenPositionIsBeyondLastCell() {
    var player = new Player("Pippo", 64);
    assertThat(player.hasWonGiven(63), equalTo(false));
  }

  @Test
  void shouldBounceBackWhenOvershoots() {
    var player = new Player("Pippo", 65);
    var bounced = player.bounceBack(63);
    assertThat(bounced.position(), equalTo(61));
  }

  @Test
  void shouldCalculateCorrectReturnPositionOnBounce() {
    var player = new Player("Pippo", 67);
    var bounced = player.bounceBack(63);
    assertThat(bounced.position(), equalTo(59));
  }

  @Test
  void shouldPreserveNameOnBounce() {
    var player = new Player("Pluto", 65);
    var bounced = player.bounceBack(63);
    assertThat(bounced.name(), equalTo("Pluto"));
  }

  @Test
  void shouldBeBeyondFinishWhenPositionExceedsBoardSize() {
    var player = new Player("Pippo", 64);
    assertThat(player.isBeyondTheFinish(63), equalTo(true));
  }

  @Test
  void shouldNotBeBeyondFinishWhenPositionEqualsBoardSize() {
    var player = new Player("Pippo", 63);
    assertThat(player.isBeyondTheFinish(63), equalTo(false));
  }

  @Test
  void shouldNotBeBeyondFinishWhenPositionIsBelowBoardSize() {
    var player = new Player("Pippo", 62);
    assertThat(player.isBeyondTheFinish(63), equalTo(false));
  }

  @Test
  void cellGivenShouldReturnStartForPositionZero() {
    var player = new Player("Pippo", 0);
    assertThat(player.cellGiven(63), equalTo("Start"));
  }

  @Test
  void cellGivenShouldReturnTheBridgeForPositionSix() {
    var player = new Player("Pippo", 6);
    assertThat(player.cellGiven(63), equalTo("The Bridge"));
  }

  @Test
  void cellGivenShouldReturnNumberForOtherPositions() {
    var player = new Player("Pippo", 10);
    assertThat(player.cellGiven(63), equalTo("10"));
  }

  @Test
  void cellGivenShouldReturnNumberForGoosePositions() {
    var player = new Player("Pippo", 5);
    assertThat(player.cellGiven(63), equalTo("5"));
  }

  @Test
  void cellGivenShouldClampToBoardSize() {
    var player = new Player("Pippo", 70);
    assertThat(player.cellGiven(63), equalTo("63"));
  }

  @Test
  void moveShouldPreservePlayerName() {
    var player = new Player("Pippo", 0);
    var moved = player.move(5);
    assertThat(moved.name(), equalTo("Pippo"));
  }
}
