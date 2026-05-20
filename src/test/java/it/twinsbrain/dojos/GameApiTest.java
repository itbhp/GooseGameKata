package it.twinsbrain.dojos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.twinsbrain.dojos.result.*;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates that a UI adapter can drive the game purely through Game's API —
 * no text parsing, no streams, no console involvement.
 */
class GameApiTest {

  private final Game game = new Game(new Board(63));

  @Test
  void uiCanAddAPlayerAndObserveTheResult() {
    var result = game.addPlayer("Pippo");
    assertThat(result, instanceOf(PlayerAdded.class));
    assertThat(game.players(), hasSize(1));
  }

  @Test
  void uiCanAddMultiplePlayersAndSeeAllOfThem() {
    game.addPlayer("Pippo");
    game.addPlayer("Pluto");
    var names = game.players().stream().map(Player::name).toList();
    assertThat(names, containsInAnyOrder("Pippo", "Pluto"));
  }

  @Test
  void uiCanMoveAPlayerAndReadItsNewPosition() {
    game.addPlayer("Pippo");
    var result = game.movePlayer("Pippo", 4, 2); // Start → Bridge → jumps to 12
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(12));
  }

  @Test
  void uiKnowsTheGameIsNotOverDuringNormalPlay() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 2, 1);
    assertFalse(game.isOver());
  }

  @Test
  void uiKnowsWhenTheGameIsOver() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 58, 2); // → 60
    game.movePlayer("Pippo", 1, 2);  // → 63, wins
    assertTrue(game.isOver());
  }

  @Test
  void uiCanInspectPlayerPositionAfterEachMove() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 2, 1); // → 3
    var pippo = game.players().stream().findFirst().orElseThrow();
    assertThat(pippo.position(), equalTo(3));
  }
}
