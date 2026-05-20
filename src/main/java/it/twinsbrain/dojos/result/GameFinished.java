package it.twinsbrain.dojos.result;

import it.twinsbrain.dojos.MoveContext;
import it.twinsbrain.dojos.Player;

public record GameFinished(Player winner, MoveContext context) implements MoveResult {}
