package it.twinsbrain.dojos.result;

import it.twinsbrain.dojos.MoveContext;
import it.twinsbrain.dojos.Player;

public record PlayerBouncedBack(Player player, MoveContext context, Prank prank) implements MoveResult {

  public PlayerBouncedBack(Player player, MoveContext context) {
    this(player, context, null);
  }
}
