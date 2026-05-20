package it.twinsbrain.dojos.result;

import it.twinsbrain.dojos.MoveContext;
import it.twinsbrain.dojos.Player;

public record PlayerBouncedBack(Player player, MoveContext context) implements MoveResult {}
