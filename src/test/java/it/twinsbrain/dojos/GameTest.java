package it.twinsbrain.dojos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.twinsbrain.dojos.commands.AddPlayerCommand;
import it.twinsbrain.dojos.commands.MovePlayerCommand;
import it.twinsbrain.dojos.result.*;
import org.junit.jupiter.api.Test;

class GameTest {

  private final Board board = new Board(63);
  private final Game game = new Game(board);

  @Test
  void addingANewPlayerReturnsPlayerAdded() {
    var result = game.execute(new AddPlayerCommand("Pippo"));
    assertThat(result, instanceOf(PlayerAdded.class));
  }

  @Test
  void addingAnExistingPlayerReturnsPlayerAlreadyPresent() {
    game.execute(new AddPlayerCommand("Pippo"));
    var result = game.execute(new AddPlayerCommand("Pippo"));
    assertThat(result, instanceOf(PlayerAlreadyPresent.class));
  }

  @Test
  void playersReflectsAllAddedPlayers() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new AddPlayerCommand("Pluto"));
    assertThat(game.players(), hasSize(2));
  }

  @Test
  void movingAPlayerReturnsPlayerMoved() {
    game.execute(new AddPlayerCommand("Pippo"));
    var result = game.execute(new MovePlayerCommand("Pippo", 2, 1, board));
    assertThat(result, instanceOf(PlayerMoved.class));
  }

  @Test
  void movingAPlayerUpdatesTheirPosition() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new MovePlayerCommand("Pippo", 2, 1, board));
    var pippo = game.players().stream().findFirst().orElseThrow();
    assertThat(pippo.position(), equalTo(3));
  }

  @Test
  void movingAPlayerBeyondTheFinishReturnsPlayerBouncedBack() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new MovePlayerCommand("Pippo", 58, 2, board)); // → 60
    var result = game.execute(new MovePlayerCommand("Pippo", 2, 3, board)); // → 65, bounces to 61
    assertThat(result, instanceOf(PlayerBouncedBack.class));
  }

  @Test
  void movingAPlayerToTheWinCellReturnsGameFinished() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new MovePlayerCommand("Pippo", 58, 2, board)); // → 60
    var result = game.execute(new MovePlayerCommand("Pippo", 1, 2, board)); // → 63, wins
    assertThat(result, instanceOf(GameFinished.class));
  }

  @Test
  void gameIsNotOverBeforeAnyoneWins() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new MovePlayerCommand("Pippo", 2, 1, board));
    assertFalse(game.isOver());
  }

  @Test
  void gameIsOverAfterAPlayerWins() {
    game.execute(new AddPlayerCommand("Pippo"));
    game.execute(new MovePlayerCommand("Pippo", 58, 2, board)); // → 60
    game.execute(new MovePlayerCommand("Pippo", 1, 2, board));  // → 63, wins
    assertTrue(game.isOver());
  }
}
