package it.twinsbrain.dojos.commands;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.result.PlayerAdded;
import it.twinsbrain.dojos.result.PlayerAlreadyPresent;
import org.junit.jupiter.api.Test;

class AddPlayerCommandTest {

  @Test
  void shouldReturnPlayerAlreadyPresentWhenPlayerExists() {
    var cmd = new AddPlayerCommand("Pippo");
    var result = cmd.createIfNotExists(ignored -> true);
    assertThat(result, instanceOf(PlayerAlreadyPresent.class));
  }

  @Test
  void shouldReturnPlayerAddedWhenPlayerDoesNotExist() {
    var cmd = new AddPlayerCommand("Pippo");
    var result = cmd.createIfNotExists(ignored -> false);
    assertThat(result, instanceOf(PlayerAdded.class));
  }

  @Test
  void playerAlreadyPresentShouldCarryPlayerName() {
    var cmd = new AddPlayerCommand("Pippo");
    var result = (PlayerAlreadyPresent) cmd.createIfNotExists(ignored -> true);
    assertThat(result.playerName(), equalTo("Pippo"));
  }

  @Test
  void playerAddedShouldCreatePlayerAtStart() {
    var cmd = new AddPlayerCommand("Pippo");
    var result = (PlayerAdded) cmd.createIfNotExists(ignored -> false);
    assertThat(result.player(), equalTo(new Player("Pippo", 0)));
  }
}
