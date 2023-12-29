package it.twinsbrain.dojos.result;


public sealed interface MoveResult extends Result permits PlayerMoved, PlayerBouncedBack, GameFinished{}

