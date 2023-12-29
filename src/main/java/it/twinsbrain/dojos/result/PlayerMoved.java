package it.twinsbrain.dojos.result;

import it.twinsbrain.dojos.Player;

public record PlayerMoved(Player player, String message) implements MoveResult {}
