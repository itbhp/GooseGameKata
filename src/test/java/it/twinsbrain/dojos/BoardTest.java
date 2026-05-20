package it.twinsbrain.dojos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class BoardTest {

  private final Board board = new Board(63);

  @Test
  void boardHasASize() {
    assertThat(board.size(), equalTo(63));
  }

  @Test
  void positionZeroIsNamedStart() {
    assertThat(board.nameOf(0), equalTo("Start"));
  }

  @Test
  void positionSixIsNamedTheBridge() {
    assertThat(board.nameOf(6), equalTo("The Bridge"));
  }

  @Test
  void otherPositionsAreNamedByTheirNumber() {
    assertThat(board.nameOf(7), equalTo("7"));
    assertThat(board.nameOf(63), equalTo("63"));
  }

  @Test
  void positionSixIsTheBridge() {
    assertThat(board.isBridge(6), is(true));
    assertThat(board.isBridge(7), is(false));
  }

  @Test
  void bridgeDestinationIsTwelve() {
    assertThat(board.bridgeDestination(), equalTo(12));
  }

  @Test
  void goosePositionsAreRecognised() {
    assertThat(board.isGoose(5), is(true));
    assertThat(board.isGoose(9), is(true));
    assertThat(board.isGoose(14), is(true));
    assertThat(board.isGoose(18), is(true));
    assertThat(board.isGoose(23), is(true));
    assertThat(board.isGoose(27), is(true));
    assertThat(board.isGoose(6), is(false));
    assertThat(board.isGoose(63), is(false));
  }

  @Test
  void winningPositionIsAtBoardSize() {
    assertThat(board.isWin(63), is(true));
    assertThat(board.isWin(62), is(false));
  }

  @Test
  void positionBeyondBoardSizeIsBeyondFinish() {
    assertThat(board.isBeyondFinish(64), is(true));
    assertThat(board.isBeyondFinish(63), is(false));
  }

  @Test
  void bouncePositionReflectsExcessStepsBackFromFinish() {
    assertThat(board.bouncePositionFor(65), equalTo(61)); // 63 - (65 - 63)
    assertThat(board.bouncePositionFor(64), equalTo(62)); // 63 - (64 - 63)
  }
}
