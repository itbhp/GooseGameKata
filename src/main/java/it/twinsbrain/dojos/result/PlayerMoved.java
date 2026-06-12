package it.twinsbrain.dojos.result;

import it.twinsbrain.dojos.MoveContext;
import it.twinsbrain.dojos.Player;

public record PlayerMoved(Player player, MoveContext context, Prank prank) implements MoveResult {

  public PlayerMoved(Player player, MoveContext context) {
    this(player, context, null);
  }
}
