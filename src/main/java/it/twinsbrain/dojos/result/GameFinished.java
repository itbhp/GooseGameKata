package it.twinsbrain.dojos.result;

import it.twinsbrain.dojos.Player;

public record GameFinished(Player winner, String message) implements MoveResult {}
