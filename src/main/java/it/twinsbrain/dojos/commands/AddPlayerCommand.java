package it.twinsbrain.dojos.commands;

import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.result.AddResult;
import it.twinsbrain.dojos.result.PlayerAdded;
import it.twinsbrain.dojos.result.PlayerAlreadyPresent;
import java.util.function.Predicate;

public record AddPlayerCommand(String playerName) implements Command {
  public AddResult createIfNotExists(Predicate<String> isExistingPlayer) {
    if (isExistingPlayer.test(playerName)) {
      return new PlayerAlreadyPresent(playerName);
    } else {
      return new PlayerAdded(new Player(playerName, 0));
    }
  }
}
