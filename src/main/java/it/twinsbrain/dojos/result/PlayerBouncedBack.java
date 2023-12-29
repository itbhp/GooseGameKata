package it.twinsbrain.dojos.result;

import it.twinsbrain.dojos.Player;

public record PlayerBouncedBack(Player player, String message) implements MoveResult {}
