package it.twinsbrain.dojos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class PlayerTest {

  @Test
  void playerHasANameAndAPosition() {
    Player player = new Player("Pippo", 3);
    assertThat(player.name(), equalTo("Pippo"));
    assertThat(player.position(), equalTo(3));
  }

  @Test
  void moveReturnsANewPlayerAtTheNewPosition() {
    Player player = new Player("Pippo", 3);
    assertThat(player.move(4).position(), equalTo(7));
  }

  @Test
  void moveDoesNotMutateTheOriginalPlayer() {
    Player player = new Player("Pippo", 3);
    player.move(4);
    assertThat(player.position(), equalTo(3));
  }
}
