package it.twinsbrain.dojos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.twinsbrain.dojos.result.*;
import org.junit.jupiter.api.Test;

class GameTest {

  private final Game game = new Game(new Board(63));

  @Test
  void addingANewPlayerReturnsPlayerAdded() {
    var result = game.addPlayer("Pippo");
    assertThat(result, instanceOf(PlayerAdded.class));
  }

  @Test
  void addingAnExistingPlayerReturnsPlayerAlreadyPresent() {
    game.addPlayer("Pippo");
    var result = game.addPlayer("Pippo");
    assertThat(result, instanceOf(PlayerAlreadyPresent.class));
  }

  @Test
  void playersReflectsAllAddedPlayers() {
    game.addPlayer("Pippo");
    game.addPlayer("Pluto");
    assertThat(game.players(), hasSize(2));
  }

  @Test
  void movingAPlayerReturnsPlayerMoved() {
    game.addPlayer("Pippo");
    var result = game.movePlayer("Pippo", 2, 1);
    assertThat(result, instanceOf(PlayerMoved.class));
  }

  @Test
  void movingAPlayerUpdatesTheirPosition() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 2, 1);
    var pippo = game.players().stream().findFirst().orElseThrow();
    assertThat(pippo.position(), equalTo(3));
  }

  @Test
  void movingAPlayerBeyondTheFinishReturnsPlayerBouncedBack() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 58, 2); // → 60
    var result = game.movePlayer("Pippo", 2, 3); // → 65, bounces to 61
    assertThat(result, instanceOf(PlayerBouncedBack.class));
  }

  @Test
  void movingAPlayerToTheWinCellReturnsGameFinished() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 58, 2); // → 60
    var result = game.movePlayer("Pippo", 1, 2); // → 63, wins
    assertThat(result, instanceOf(GameFinished.class));
  }

  @Test
  void gameIsNotOverBeforeAnyoneWins() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 2, 1);
    assertFalse(game.isOver());
  }

  @Test
  void gameIsOverAfterAPlayerWins() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 58, 2); // → 60
    game.movePlayer("Pippo", 1, 2);  // → 63, wins
    assertTrue(game.isOver());
  }
}
