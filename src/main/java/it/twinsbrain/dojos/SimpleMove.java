package it.twinsbrain.dojos;

public record SimpleMove(
    String playerName, int firstDice, int secondDice, int fromPosition, int landedPosition)
    implements MoveContext {}
