package it.twinsbrain.dojos;

import it.twinsbrain.dojos.result.PlayerAdded;
import it.twinsbrain.dojos.result.PlayerAlreadyPresent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

class AddPlayerCommandTest {

  @Test
  void shouldReturnPlayerAlreadyPresentWhenPlayerExists() {
    var cmd = new it.twinsbrain.dojos.commands.AddPlayerCommand("Pippo");
    var result = cmd.createIfNotExists(ignored -> true);
    assertThat(result, instanceOf(PlayerAlreadyPresent.class));
  }

  @Test
  void shouldReturnPlayerAddedWhenPlayerDoesNotExist() {
    var cmd = new it.twinsbrain.dojos.commands.AddPlayerCommand("Pippo");
    var result = cmd.createIfNotExists(ignored -> false);
    assertThat(result, instanceOf(PlayerAdded.class));
  }

  @Test
  void playerAlreadyPresentShouldIncludePlayerNameInMessage() {
    var cmd = new it.twinsbrain.dojos.commands.AddPlayerCommand("Pippo");
    var result = cmd.createIfNotExists(ignored -> true);
    assertThat(((PlayerAlreadyPresent) result).message(), equalTo("Pippo: already existing player"));
  }

  @Test
  void playerAddedShouldCreatePlayerAtStart() {
    var cmd = new it.twinsbrain.dojos.commands.AddPlayerCommand("Pippo");
    var result = cmd.createIfNotExists(ignored -> false);
    assertThat(((PlayerAdded) result).player(), equalTo(new Player("Pippo", 0)));
  }

  @Test
  void playerAddedMessageFnShouldListAllPlayers() {
    var cmd = new it.twinsbrain.dojos.commands.AddPlayerCommand("Pippo");
    var result = (PlayerAdded) cmd.createIfNotExists(ignored -> false);
    var message = result.messageFn().apply(List.of(new Player("Pippo", 0)));
    assertThat(message, equalTo("players: Pippo"));
  }

  @Test
  void playerAddedMessageFnShouldListMultiplePlayers() {
    var cmd = new it.twinsbrain.dojos.commands.AddPlayerCommand("Pluto");
    var result = (PlayerAdded) cmd.createIfNotExists(ignored -> false);
    var message =
        result.messageFn().apply(List.of(new Player("Pippo", 0), new Player("Pluto", 0)));
    assertThat(message, equalTo("players: Pippo, Pluto"));
  }

  @Test
  void playerAddedMessageFnShouldPreserveInsertionOrder() {
    var cmd = new it.twinsbrain.dojos.commands.AddPlayerCommand("Pluto");
    var result = (PlayerAdded) cmd.createIfNotExists(ignored -> false);
    var message =
        result.messageFn().apply(List.of(new Player("Pluto", 0), new Player("Pippo", 0)));
    assertThat(message, equalTo("players: Pluto, Pippo"));
  }
}
