package it.twinsbrain.dojos.commands;

import static java.util.stream.Collectors.joining;

import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.result.AddResult;
import it.twinsbrain.dojos.result.PlayerAdded;
import it.twinsbrain.dojos.result.PlayerAlreadyPresent;
import java.util.function.Predicate;

public record AddPlayerCommand(String playerName) implements Command {
  public AddResult execute(Predicate<String> isExistingPlayer) {
    if (isExistingPlayer.test(playerName)) {
      return new PlayerAlreadyPresent(playerName + ": already existing player");
    } else {
      return new PlayerAdded(
          new Player(playerName, 0),
          players ->
              "players: " + players.stream().map(Player::name).collect(joining(", ")));
    }
  }
}
