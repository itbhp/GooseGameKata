package it.twinsbrain.dojos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.twinsbrain.dojos.commands.AddPlayerCommand;
import it.twinsbrain.dojos.commands.MovePlayerCommand;
import it.twinsbrain.dojos.result.*;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates that a UI adapter can drive the game purely through Game's API —
 * no text parsing, no streams, no console involvement.
 */
class GameApiTest {

  private final Board board = new Board(63);
  private final Game game = new Game(board);

  @Test
  void uiCanAddAPlayerAndObserveTheResult() {
    var result = game.execute(new AddPlayerCommand("Pippo"));
    assertThat(result, instanceOf(PlayerAdded.class));
    assertThat(game.players(), hasSize(1));
  }

  @Test
  void uiCanAddMultiplePlayersAndSeeAllOfThem() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new AddPlayerCommand("Pluto"));
    var names = game.players().stream().map(Player::name).toList();
    assertThat(names, containsInAnyOrder("Pippo", "Pluto"));
  }

  @Test
  void uiCanMoveAPlayerAndReadItsNewPosition() {
    game.execute(new AddPlayerCommand("Pippo"));
    var result = game.execute(new MovePlayerCommand("Pippo", 4, 2, board)); // Start → Bridge → jumps to 12
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(12));
  }

  @Test
  void uiKnowsTheGameIsNotOverDuringNormalPlay() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new MovePlayerCommand("Pippo", 2, 1, board));
    assertFalse(game.isOver());
  }

  @Test
  void uiKnowsWhenTheGameIsOver() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new MovePlayerCommand("Pippo", 58, 2, board)); // → 60
    game.execute(new MovePlayerCommand("Pippo", 1, 2, board));  // → 63, wins
    assertTrue(game.isOver());
  }

  @Test
  void uiCanInspectPlayerPositionAfterEachMove() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new MovePlayerCommand("Pippo", 2, 1, board)); // → 3
    var pippo = game.players().stream().findFirst().orElseThrow();
    assertThat(pippo.position(), equalTo(3));
  }
}
